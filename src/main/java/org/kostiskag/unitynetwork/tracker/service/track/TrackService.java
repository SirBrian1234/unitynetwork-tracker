package org.kostiskag.unitynetwork.tracker.service.track;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.sql.SQLException;
import java.util.concurrent.locks.Lock;

import javax.crypto.SecretKey;

import org.kostiskag.unitynetwork.common.serviceoperations.RedNodeToTracker;
import org.kostiskag.unitynetwork.common.serviceoperations.SomeoneToTracker;
import org.kostiskag.unitynetwork.common.utilities.CryptoUtilities;
import org.kostiskag.unitynetwork.common.utilities.SocketUtilities;
import org.kostiskag.unitynetwork.common.serviceoperations.BlueNodeToTracker;

import org.kostiskag.unitynetwork.tracker.AppLogger;

import org.kostiskag.unitynetwork.tracker.rundata.table.BlueNodeTable;

/**
 * CENTRAL TRACK SERVICE
 * 
 * first it determines if it is a rednode or a bluenode
 * 
 * for an (unregistered) rednode a. it indicates the closest bluenode to connect
 * b. or a list of the available bluenodes and their respective load
 * 
 * for a bluenode it can a. lease the BN itself b. auth/lease an RN connected to
 * him c. resolve some queries
 * 
 * Bluenode queries:
 *
 * LEASE BN LEASE RN [HOSTNAME] [USERNAME] [PASSWORD] RELEASE BN RELEASE RN
 * [HOSTNAME] GETPH [BLUENODE_NAME] -> IP_ADDRESS PORT CHECKRN [HOSTNAME] ->
 * BLUENODE_NAME IP_ADDRESS PORT CHECKRNA [VADDRESS] -> ONLINE/OFFLINE
 * BLUENODE_NAME IP_ADDRESS PORT
 * 
 * (unregistered) Rednode queries:
 *
 * GETBNS -> RETURNS A LIST OF BNS GETRBN -> RETURNS BN WITH THE LOWEST LOAD
 *
 * @author Konstantinos Kagiampakis
 */
final class TrackService extends Thread {

	private final String pre = "^TrackService ";
	private final Socket socket;
	private final KeyPair trackerKeyPair;
	
	private enum Type {
		BLUENODE,
		REDNODE
	}

	TrackService(Socket socket, KeyPair trackerKeyPair) {
		this.socket = socket;
		this.trackerKeyPair = trackerKeyPair;
	}

	@Override
	public void run() {
		System.out.println(pre + "Started auth service at " + Thread.currentThread().getName());
		try {
			DataInputStream reader = SocketUtilities.makeDataReader(socket);
			DataOutputStream writer = SocketUtilities.makeDataWriter(socket);

			byte[] received = SocketUtilities.receiveData(reader);
			String receivedStr = new String(received, StandardCharsets.UTF_8);
			String[] args = receivedStr.split("\\s+");

			if (args[0].equals(SomeoneToTracker.GETPUB)) {
				// plain data transfer no encryption
				AppLogger.getLogger().consolePrint(pre+ SomeoneToTracker.GETPUB+" from "+socket.getInetAddress().getHostAddress());
				SocketUtilities.sendPlainStringData(
						CryptoUtilities.objectToBase64StringRepresentation(trackerKeyPair.getPublic()), writer);
			} else {
				//client uses server's public key to send a session key
				String decrypted = CryptoUtilities.decryptWithPrivate(received, trackerKeyPair.getPrivate());
				SecretKey sessionKey = (SecretKey) CryptoUtilities.base64StringRepresentationToObject(decrypted);
				args = SocketUtilities.sendReceiveAESEncryptedStringData(SomeoneToTracker.TRACKER_GREET_TO_OUTER_HANDSHAKE.value(), reader, writer, sessionKey);

				if (args.length == 2 && args[0].equals(BlueNodeToTracker.GREET)) {
					BlueNodeService(args[1], sessionKey, reader, writer);
				} else if (args.length == 2 && args[0].equals(RedNodeToTracker.GREET)) {
					RedNodeService(args[1], sessionKey, reader, writer);
				} else {
					SocketUtilities.sendAESEncryptedStringData(SomeoneToTracker.TRACKER_RESPONCE_TO_IMPROPER_GREETING.value(), writer, sessionKey);
				}
			}
		} catch (InterruptedException | IllegalAccessException | GeneralSecurityException | IOException | SQLException e) {
			AppLogger.getLogger().consolePrint(pre+e.getMessage());
		} finally {
			try {
				SocketUtilities.connectionClose(socket);
			} catch (IOException e) {
				AppLogger.getLogger().consolePrint(pre+e.getMessage());
			}
		}
	}

	public void BlueNodeService(String BlueNodeHostname, SecretKey sessionKey, DataInputStream reader, DataOutputStream writer) throws IOException, GeneralSecurityException, IllegalAccessException, SQLException {
		PublicKey pub = BlueNodeActions.fetchPubKey(BlueNodeHostname);
		if (pub == null) {
			/*
			 * the bn is a member however he has not set a public key
			 * it is allowed to set a public key based on a special ticket key
			 */
			offerPublicKey(Type.BLUENODE, BlueNodeHostname, pub, sessionKey,reader, writer);
			//this session ends here
		} else {
			
			if (!innerAuthentication(pub,sessionKey,reader,writer)) {
				throw new IllegalAccessException("RSA auth for BlueNode " + BlueNodeHostname + " failed.");
			}

			// OPTIONS
			String[] args = SocketUtilities.receiveAESEncryptedStringData(reader, sessionKey);
			// Now is the proper time to ask for the BNtable Lock!!!
			try {
				Lock bnTableLock = BlueNodeTable.getInstance().aquireLock();
				if (BlueNodeTable.getInstance().getOptionalNodeEntry(bnTableLock, BlueNodeHostname).isPresent()) {
					//in other words in order to execute extensive queries you have to be logged in
					if (args.length == 4 && args[0].equals(BlueNodeToTracker.LEASE_RN.value())) {
						AppLogger.getLogger().consolePrint(pre + Type.BLUENODE + BlueNodeToTracker.LEASE_RN.value() + " from " + socket.getInetAddress().getHostAddress());
						BlueNodeActions.RedLease(bnTableLock, BlueNodeHostname, args[1], args[2], args[3], writer, sessionKey);
					} else if (args.length == 1 && args[0].equals(BlueNodeToTracker.RELEASE.value())) {
						AppLogger.getLogger().consolePrint(pre + Type.BLUENODE + BlueNodeToTracker.RELEASE.value() + " from " + socket.getInetAddress().getHostAddress());
						BlueNodeActions.BlueRel(bnTableLock, BlueNodeHostname, writer, sessionKey);
					} else if (args.length == 2 && args[0].equals(BlueNodeToTracker.RELEASE_RN.value())) {
						AppLogger.getLogger().consolePrint(pre + Type.BLUENODE + BlueNodeToTracker.RELEASE_RN.value() + " from " + socket.getInetAddress().getHostAddress());
						BlueNodeActions.RedRel(bnTableLock, BlueNodeHostname, args[1], writer, sessionKey);
					} else if (args.length == 2 && args[0].equals(BlueNodeToTracker.GETPH.value())) {
						AppLogger.getLogger().consolePrint(pre + Type.BLUENODE + BlueNodeToTracker.GETPH.value() + " from " + socket.getInetAddress().getHostAddress());
						BlueNodeActions.GetPh(bnTableLock, args[1], writer, sessionKey);
					} else if (args.length == 2 && args[0].equals(BlueNodeToTracker.CHECK_RN.value())) {
						AppLogger.getLogger().consolePrint(pre + Type.BLUENODE + BlueNodeToTracker.CHECK_RN.value() + " from " + socket.getInetAddress().getHostAddress());
						BlueNodeActions.CheckRn(bnTableLock, args[1], writer, sessionKey);
					} else if (args.length == 2 && args[0].equals(BlueNodeToTracker.CHECK_RNA.value())) {
						AppLogger.getLogger().consolePrint(pre + Type.BLUENODE + BlueNodeToTracker.CHECK_RNA.value() + " from " + socket.getInetAddress().getHostAddress());
						BlueNodeActions.CheckRnAddr(bnTableLock, args[1], writer, sessionKey);
					} else if (args.length == 2 && args[0].equals(BlueNodeToTracker.LOOKUP_H.value())) {
						AppLogger.getLogger().consolePrint(pre + Type.BLUENODE + BlueNodeToTracker.LOOKUP_H.value() + " from " + socket.getInetAddress().getHostAddress());
						BlueNodeActions.LookupByHn(args[1], writer, sessionKey);
					} else if (args.length == 2 && args[0].equals(BlueNodeToTracker.LOOKUP_V.value())) {
						AppLogger.getLogger().consolePrint(pre + Type.BLUENODE + BlueNodeToTracker.LOOKUP_V.value() + " from " + socket.getInetAddress().getHostAddress());
						BlueNodeActions.LookupByAddr(args[1], writer, sessionKey);
					} else if (args.length == 2 && args[0].equals(BlueNodeToTracker.GETBNPUB.value())) {
						AppLogger.getLogger().consolePrint(pre + Type.BLUENODE + BlueNodeToTracker.GETBNPUB.value() + " from " + socket.getInetAddress().getHostAddress());
						CommonActions.getBlueNodesPublic(bnTableLock, args[1], writer, sessionKey);
					} else if (args.length == 2 && args[0].equals(BlueNodeToTracker.GETRNPUB.value())) {
						AppLogger.getLogger().consolePrint(pre + Type.BLUENODE + BlueNodeToTracker.GETRNPUB.value() + " from " + socket.getInetAddress().getHostAddress());
						CommonActions.getRedNodesPublic(args[1], writer, sessionKey);
					} else if (args.length == 1 && args[0].equals(BlueNodeToTracker.REVOKEPUB.value())) {
						// bluenode may be compromised and decides to revoke its public
						AppLogger.getLogger().consolePrint(pre + Type.BLUENODE + BlueNodeToTracker.REVOKEPUB.value() + " from " + socket.getInetAddress().getHostAddress());
						BlueNodeActions.revokePublicKey(bnTableLock, BlueNodeHostname, writer, sessionKey);
					} else {
						AppLogger.getLogger().consolePrint(pre + Type.BLUENODE + BlueNodeToTracker.TRACKER_RESPONCE_TO_AUTHENTICATED_WRONG_OPTION.value() + args[0] + " for leased bn " + BlueNodeHostname + " from " + socket.getInetAddress().getHostAddress());
						SocketUtilities.sendAESEncryptedStringData(BlueNodeToTracker.TRACKER_RESPONCE_TO_AUTHENTICATED_WRONG_OPTION.value(), writer, sessionKey);
					}
				} else if (args.length == 2 && args[0].equals(BlueNodeToTracker.LEASE.value())) {
					//you can lease only if you are NOT logged in!
					AppLogger.getLogger().consolePrint(pre + Type.BLUENODE + BlueNodeToTracker.LEASE.value() + " from " + socket.getInetAddress().getHostAddress());
					BlueNodeActions.BlueLease(bnTableLock, BlueNodeHostname, pub, socket, args[1], writer, sessionKey);
				} else {
					AppLogger.getLogger().consolePrint(pre + Type.BLUENODE + BlueNodeToTracker.TRACKER_RESPONCE_TO_ALREADY_AUTHENTICATED_TRYING_TO_REAUTH.value() + args[0] + " from " + socket.getInetAddress().getHostAddress());
					SocketUtilities.sendAESEncryptedStringData(BlueNodeToTracker.TRACKER_RESPONCE_TO_ALREADY_AUTHENTICATED_TRYING_TO_REAUTH.value(), writer, sessionKey);
				}
			} catch (InterruptedException e) {
				AppLogger.getLogger().consolePrint(e.getMessage());
			} catch (SQLException | GeneralSecurityException | IOException | IllegalAccessException e1) {
				//the rest should go up!
				throw e1;
			} finally {
				BlueNodeTable.getInstance().releaseLock();
			}
		}
	}

	private void RedNodeService(String hostname, SecretKey sessionKey, DataInputStream reader, DataOutputStream writer) throws InterruptedException, GeneralSecurityException, IOException, IllegalAccessException, SQLException {
		PublicKey pub = RedNodeActions.fetchPubKey(hostname);
		if (pub == null) {
			/*
			 * null indicates that
			 * the rn is a member however he has not set a public key
			 * it is allowed to set a public key based on a special ticket key
			 */
			offerPublicKey(Type.REDNODE, hostname, pub, sessionKey,reader, writer);
			//this session ends here
		} else {
			//public key exists
			if (!innerAuthentication(pub,sessionKey,reader,writer)){
				throw new IllegalAccessException("RSA auth for RedNode " + hostname + " failed.");
			}

			// OPTIONS
			String[] args = SocketUtilities.receiveAESEncryptedStringData(reader, sessionKey);
			//Now it's a proper time to ask for a lock!
			try {
				Lock bnTableLock = BlueNodeTable.getInstance().aquireLock();
				if (args.length == 1 && args[0].equals(RedNodeToTracker.GET_RECOMENDED_BLUENODE.value())) {
					//rn collects a recommended bn based on the lowest load
					AppLogger.getLogger().consolePrint(pre+Type.REDNODE+RedNodeToTracker.GET_RECOMENDED_BLUENODE.value()+" from "+socket.getInetAddress().getHostAddress());
					RedNodeActions.getRecomendedBlueNode(bnTableLock, writer, sessionKey);
				} else if (args.length == 1 && args[0].equals(RedNodeToTracker.GET_ALL_BLUENODES.value())) {
					//rn collects a list of all the available bns
					AppLogger.getLogger().consolePrint(pre+Type.REDNODE+RedNodeToTracker.GET_ALL_BLUENODES.value()+" from "+socket.getInetAddress().getHostAddress());
					RedNodeActions.getAllConnectedBlueNodes(bnTableLock, writer, sessionKey);
				} else if (args.length == 2 && args[0].equals(RedNodeToTracker.GET_BLUENODE_PUBLIC_KEY.value())) {
					//collects a network bns public
					AppLogger.getLogger().consolePrint(pre+Type.REDNODE+RedNodeToTracker.GET_BLUENODE_PUBLIC_KEY.value()+" from "+socket.getInetAddress().getHostAddress());
					CommonActions.getBlueNodesPublic(bnTableLock, args[1], writer, sessionKey);
				} else if (args.length == 1 && args[0].equals(RedNodeToTracker.REVOKE_PUBLIC_KEY.value())) {
					//rn may be compromised and decides to revoke its public
					AppLogger.getLogger().consolePrint(pre+Type.REDNODE+RedNodeToTracker.REVOKE_PUBLIC_KEY.value()+" from "+socket.getInetAddress().getHostAddress());
					RedNodeActions.revokePublicKey(hostname, writer, sessionKey);
				} else if (BlueNodeTable.getInstance().isOnlineRnByHostname(bnTableLock, hostname)) {
					//Only if a RN is leased inside the network it may colect another's public key
					if (args.length == 2 && args[0].equals(RedNodeToTracker.GET_REDNODE_PUBLIC_KEY.value())) {
						//collects a network rns public
						AppLogger.getLogger().consolePrint(pre+Type.REDNODE+RedNodeToTracker.GET_REDNODE_PUBLIC_KEY.value()+" from "+socket.getInetAddress().getHostAddress());
						CommonActions.getRedNodesPublic(args[1], writer, sessionKey);
					} else {
						AppLogger.getLogger().consolePrint(pre+Type.REDNODE+RedNodeToTracker.TRACKER_RESPONCE_TO_FAILED_GET_ANOTHER_RNS_PUB_KEY.value()+args[0]+" for leased rn "+hostname+" from "+socket.getInetAddress().getHostAddress());
						SocketUtilities.sendAESEncryptedStringData(RedNodeToTracker.TRACKER_RESPONCE_TO_FAILED_GET_ANOTHER_RNS_PUB_KEY.value(), writer, sessionKey);
					}
				} else {
					AppLogger.getLogger().consolePrint(pre+Type.REDNODE+RedNodeToTracker.TRACKER_RESPONCE_TO_AUTHENTICATED_WRONG_OPTION.value()+args[0]+" from "+socket.getInetAddress().getHostAddress());
					SocketUtilities.sendAESEncryptedStringData(RedNodeToTracker.TRACKER_RESPONCE_TO_AUTHENTICATED_WRONG_OPTION.value(), writer, sessionKey);
				}
			} catch (InterruptedException e) {
				//Interrupted is for the lock!
				AppLogger.getLogger().consolePrint(e.getMessage());
			} catch(GeneralSecurityException | IOException  e1) {
				//the rest es should go up!
				throw e1;
			} finally {
				//lock is handled here!
				BlueNodeTable.getInstance().releaseLock();
			}

		}
	}
	
	private void offerPublicKey(Type type, String hostname, PublicKey pub, SecretKey sessionKey, DataInputStream reader, DataOutputStream writer) throws GeneralSecurityException, IOException, SQLException {
		// this is a pub key offer from either a rn or a bn
		String[] args = SocketUtilities.sendReceiveAESEncryptedStringData(SomeoneToTracker.TRACKER_RESPONCE_TO_PUBLIC_NOT_SET.value(), reader, writer,
				sessionKey);
		if (args[0].equals(SomeoneToTracker.EXIT_ON_PUBLIC_KEY_NOT_SET.value())) {
			return;
		} else if (args.length == 3 && args[0].equals(SomeoneToTracker.OFFERPUB.value())) {
			// client offers its pub based on a ticket
			AppLogger.getLogger().consolePrint(pre+Type.BLUENODE+SomeoneToTracker.OFFERPUB.value() + " from " + socket.getInetAddress().getHostAddress());
			if (type == Type.BLUENODE) {
				BlueNodeActions.offerPublicKey(hostname, args[1], args[2], writer, sessionKey);
			} else {
				RedNodeActions.offerPublicKey(hostname, args[1], args[2], writer, sessionKey);
			}
		} else {
			SocketUtilities.sendAESEncryptedStringData(SomeoneToTracker.TRACKER_RESPONCE_TO_PUBLIC_FAILED_SUBMISSION_AUTHENTICATION.value(), writer, sessionKey);
		}
	}
	
	private boolean innerAuthentication(PublicKey pub, SecretKey sessionKey, DataInputStream reader, DataOutputStream writer) throws GeneralSecurityException, IOException {
		/*
		 * The client has a public key set authentication will follow based on
		 * its public key
		 */

		// generate a random question
		String question = CryptoUtilities.generateQuestion();

		// encrypt question with target rednodes's public
		byte[] questionb = CryptoUtilities.encryptWithPublic(question, pub);

		// encode it to base 64
		String encq = CryptoUtilities.bytesToBase64String(questionb);

		// challenge the client with a question encrypted with his publickey and wait for a response
		String args[] = SocketUtilities.sendReceiveAESEncryptedStringData(encq, reader, writer, sessionKey);

		if (args[0].equals(question)) {
			// client successfully passed the test!
			SocketUtilities.sendAESEncryptedStringData(SomeoneToTracker.TRACKER_PERMITS_AUTHENTICATED_OPTIONS_AFTER_HANDSHAKE.value(), writer, sessionKey);
			return true;
		} else {
			SocketUtilities.sendAESEncryptedStringData(SomeoneToTracker.TRACKER_DENIES_AUTHENTICATED_OPTIONS_AFTER_FAILED_HANDSHAKE.value(), writer, sessionKey);
			return false;
		}
	}
}
