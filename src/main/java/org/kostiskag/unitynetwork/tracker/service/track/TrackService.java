package org.kostiskag.unitynetwork.tracker.service.track;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.sql.SQLException;
import java.util.concurrent.locks.Lock;

import javax.crypto.SecretKey;

import org.kostiskag.unitynetwork.tracker.AppLogger;
import org.kostiskag.unitynetwork.tracker.App;
import org.kostiskag.unitynetwork.tracker.rundata.serviceoperations.BlueNodeToTracker;
import org.kostiskag.unitynetwork.tracker.rundata.serviceoperations.RedNodeToTracker;
import org.kostiskag.unitynetwork.tracker.rundata.utilities.CryptoUtilities;
import org.kostiskag.unitynetwork.tracker.rundata.utilities.SocketUtilities;
import org.kostiskag.unitynetwork.tracker.rundata.table.BlueNodeTable;
import org.kostiskag.unitynetwork.tracker.service.BlueNodeGlobalFunctions;

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
public class TrackService extends Thread {

	private final String pre = "^TrackService ";
	private final String prebn = "Bluenode ";
	private final String prern = "Rednode ";
	private Socket socket;
	private DataInputStream reader;
	private DataOutputStream writer;
	private SecretKey sessionKey;

	TrackService(Socket connectionSocket) {
		socket = connectionSocket;
	}

	@Override
	public void run() {
		System.out.println("@Started auth service at " + Thread.currentThread().getName());
		try {
			reader = SocketUtilities.makeDataReader(socket);
			writer = SocketUtilities.makeDataWriter(socket);

			byte[] received = SocketUtilities.receiveData(reader);
			String receivedStr = new String(received, StandardCharsets.UTF_8);
			String[] args = receivedStr.split("\\s+");

			if (args[0].equals("GETPUB")) {
				// plain data transfer no encryption
				AppLogger.getLogger().consolePrint(pre+"GETPUB"+" from "+socket.getInetAddress().getHostAddress());
				SocketUtilities.sendPlainStringData(
						CryptoUtilities.objectToBase64StringRepresentation(App.TRACKER_APP.trackerKeys.getPublic()), writer);
			} else {
				//client uses server's public key to send a session key
				String decrypted = CryptoUtilities.decryptWithPrivate(received, App.TRACKER_APP.trackerKeys.getPrivate());
				sessionKey = (SecretKey) CryptoUtilities.base64StringRepresentationToObject(decrypted);
				args = SocketUtilities.sendReceiveAESEncryptedStringData("UnityTracker", reader, writer, sessionKey);

				if (args.length == 2 && args[0].equals("BLUENODE")) {
					BlueNodeService(args[1]);
				} else if (args.length == 2 && args[0].equals("REDNODE")) {
					RedNodeService(args[1]);
				} else {
					SocketUtilities.sendAESEncryptedStringData("WRONG_COMMAND", writer, sessionKey);
				}
			}
		} catch (IllegalAccessException | GeneralSecurityException | IOException | SQLException e) {
			AppLogger.getLogger().consolePrint(pre+e.getMessage());
		} finally {
			try {
				SocketUtilities.connectionClose(socket);
			} catch (IOException e) {
				AppLogger.getLogger().consolePrint(pre+e.getMessage());
			}
		}
	}

	public void BlueNodeService(String BlueNodeHostname) throws IOException, GeneralSecurityException, IllegalAccessException, SQLException {
		PublicKey pub = BlueNodeGlobalFunctions.fetchPubKey(BlueNodeHostname);
		if (pub == null) {
			/*
			 * the bn is a member however he has not set a public key this
			 * allows only to set key based on session key
			 */

			// this is a pub key offer
			String[] args = SocketUtilities.sendReceiveAESEncryptedStringData("PUBLIC_NOT_SET", reader, writer,
					sessionKey);
			if (args[0].equals("EXIT")) {
				return;
			} else if (args.length == 3 && args[0].equals("OFFERPUB")) {
				// bluenode offers its pub based on a ticket
				AppLogger.getLogger().consolePrint(pre+prebn+"OFFERPUB"+" from "+socket.getInetAddress().getHostAddress());
				BlueNodeActions.offerPublicKey(BlueNodeHostname, args[1], args[2], writer, sessionKey);
			} else {
				SocketUtilities.sendAESEncryptedStringData("NOT_ALLOWED", writer, sessionKey);
			}

		} else {
			/*
			 * The Bn has a public key set authentication will follow based on
			 * its public key
			 */

			// generate a random question
			String question = CryptoUtilities.generateQuestion();

			// encrypt question with target bluenode's public
			byte[] questionb = CryptoUtilities.encryptWithPublic(question, pub);

			// encode it to base 64
			String encq = CryptoUtilities.bytesToBase64String(questionb);

			// send it, wait for response
			String args[] = SocketUtilities.sendReceiveAESEncryptedStringData(encq, reader, writer, sessionKey);

			if (args[0].equals("EXIT")) {
				return;
			} else if (args[0].equals(question)) {
				// now this is a proper RSA authentication
				SocketUtilities.sendAESEncryptedStringData("OK", writer, sessionKey);
			} else {
				SocketUtilities.sendAESEncryptedStringData("NOT_ALLOWED", writer, sessionKey);
				throw new IllegalAccessException("RSA auth for BlueNode " + BlueNodeHostname + " failed.");
			}

			args = SocketUtilities.receiveAESEncryptedStringData(reader, sessionKey);
			// OPTIONS
			// Now is the proper time to ask for the BNtable Lock!!!
			try {
				Lock bnTableLock = BlueNodeTable.getInstance().aquireLock();
				if (BlueNodeTable.getInstance().getOptionalNodeEntry(bnTableLock, BlueNodeHostname).isPresent()) {
					//in other words in order to execute extensive queries you have to be logged in
					if (args.length == 4 && args[0].equals(BlueNodeToTracker.LEASE_RN.value())) {
						AppLogger.getLogger().consolePrint(pre + prebn + BlueNodeToTracker.LEASE_RN.value() + " from " + socket.getInetAddress().getHostAddress());
						BlueNodeActions.RedLease(bnTableLock, BlueNodeHostname, args[1], args[2], args[3], writer, sessionKey);
					} else if (args.length == 1 && args[0].equals(BlueNodeToTracker.RELEASE.value())) {
						AppLogger.getLogger().consolePrint(pre + prebn + BlueNodeToTracker.RELEASE.value() + " from " + socket.getInetAddress().getHostAddress());
						BlueNodeActions.BlueRel(bnTableLock, BlueNodeHostname, writer, sessionKey);
					} else if (args.length == 2 && args[0].equals(BlueNodeToTracker.RELEASE_RN.value())) {
						AppLogger.getLogger().consolePrint(pre + prebn + BlueNodeToTracker.RELEASE_RN.value() + " from " + socket.getInetAddress().getHostAddress());
						BlueNodeActions.RedRel(bnTableLock, BlueNodeHostname, args[1], writer, sessionKey);
					} else if (args.length == 2 && args[0].equals(BlueNodeToTracker.GETPH.value())) {
						AppLogger.getLogger().consolePrint(pre + prebn + BlueNodeToTracker.GETPH.value() + " from " + socket.getInetAddress().getHostAddress());
						BlueNodeActions.GetPh(bnTableLock, args[1], writer, sessionKey);
					} else if (args.length == 2 && args[0].equals(BlueNodeToTracker.CHECK_RN.value())) {
						AppLogger.getLogger().consolePrint(pre + prebn + BlueNodeToTracker.CHECK_RN.value() + " from " + socket.getInetAddress().getHostAddress());
						BlueNodeActions.CheckRn(bnTableLock, args[1], writer, sessionKey);
					} else if (args.length == 2 && args[0].equals(BlueNodeToTracker.CHECK_RNA.value())) {
						AppLogger.getLogger().consolePrint(pre + prebn + BlueNodeToTracker.CHECK_RNA.value() + " from " + socket.getInetAddress().getHostAddress());
						BlueNodeActions.CheckRnAddr(bnTableLock, args[1], writer, sessionKey);
					} else if (args.length == 2 && args[0].equals(BlueNodeToTracker.LOOKUP_H.value())) {
						AppLogger.getLogger().consolePrint(pre + prebn + BlueNodeToTracker.LOOKUP_H.value() + " from " + socket.getInetAddress().getHostAddress());
						BlueNodeActions.LookupByHn(args[1], writer, sessionKey);
					} else if (args.length == 2 && args[0].equals(BlueNodeToTracker.LOOKUP_V.value())) {
						AppLogger.getLogger().consolePrint(pre + prebn + BlueNodeToTracker.LOOKUP_V.value() + " from " + socket.getInetAddress().getHostAddress());
						BlueNodeActions.LookupByAddr(args[1], writer, sessionKey);
					} else if (args.length == 2 && args[0].equals(BlueNodeToTracker.GETBNPUB.value())) {
						AppLogger.getLogger().consolePrint(pre + prebn + BlueNodeToTracker.GETBNPUB.value() + " from " + socket.getInetAddress().getHostAddress());
						CommonActions.getBlueNodesPublic(bnTableLock, args[1], writer, sessionKey);
					} else if (args.length == 2 && args[0].equals(BlueNodeToTracker.GETRNPUB.value())) {
						AppLogger.getLogger().consolePrint(pre + prebn + BlueNodeToTracker.GETRNPUB.value() + " from " + socket.getInetAddress().getHostAddress());
						CommonActions.getRedNodesPublic(args[1], writer, sessionKey);
					} else if (args.length == 1 && args[0].equals(BlueNodeToTracker.REVOKEPUB.value())) {
						// bluenode may be compromised and decides to revoke its public
						AppLogger.getLogger().consolePrint(pre + prebn + BlueNodeToTracker.REVOKEPUB.value() + " from " + socket.getInetAddress().getHostAddress());
						BlueNodeActions.revokePublicKey(bnTableLock, BlueNodeHostname, writer, sessionKey);
					} else {
						AppLogger.getLogger().consolePrint(pre + prebn + "WRONG_COMMAND: " + args[0] + " for leased bn " + BlueNodeHostname + " from " + socket.getInetAddress().getHostAddress());
						SocketUtilities.sendAESEncryptedStringData("WRONG_COMMAND", writer, sessionKey);
					}
				} else if (args.length == 2 && args[0].equals(BlueNodeToTracker.LEASE.value())) {
					//you can lease only if you are NOT logged in!
					AppLogger.getLogger().consolePrint(pre + prebn + BlueNodeToTracker.LEASE.value() + " from " + socket.getInetAddress().getHostAddress());
					BlueNodeActions.BlueLease(bnTableLock, BlueNodeHostname, pub, socket, args[1], writer, sessionKey);
				} else {
					AppLogger.getLogger().consolePrint(pre + prebn + "WRONG_COMMAND: " + args[0] + " from " + socket.getInetAddress().getHostAddress());
					SocketUtilities.sendAESEncryptedStringData("WRONG_COMMAND", writer, sessionKey);
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

	private void RedNodeService(String hostname) throws GeneralSecurityException, IOException, IllegalAccessException, SQLException {
		PublicKey pub = RedNodeActions.fetchPubKey(hostname);
		if (pub == null) {
			/*
			 * null indicates that
			 * the rn is a member however he has not set a public key this
			 * allows only to set key based on session key
			 */

			// this is a pub key offer
			String[] args = SocketUtilities.sendReceiveAESEncryptedStringData("PUBLIC_NOT_SET", reader, writer, sessionKey);
			if (args[0].equals("EXIT")) {
				return;
			} else if (args.length == 3 && args[0].equals("OFFERPUB")) {
				// bluenode offers its pub based on a ticket
				AppLogger.getLogger().consolePrint(pre+prern+"OFFERPUB"+" from "+socket.getInetAddress().getHostAddress());
				RedNodeActions.offerPublicKey(hostname, args[1], args[2], writer, sessionKey);
			} else {
				SocketUtilities.sendAESEncryptedStringData("NOT_ALLOWED", writer, sessionKey);
			}

		} else {
			/*
			 * The RN has a public key set authentication will follow based on
			 * its public key
			 */

			// generate a random question
			String question = CryptoUtilities.generateQuestion();

			// encrypt question with target rednodes's public
			byte[] questionb = CryptoUtilities.encryptWithPublic(question, pub);

			// encode it to base 64
			String encq = CryptoUtilities.bytesToBase64String(questionb);

			// send it, wait for response
			String args[] = SocketUtilities.sendReceiveAESEncryptedStringData(encq, reader, writer, sessionKey);

			if (args[0].equals("EXIT")) {
				return;
			} else if (args[0].equals(question)) {
				// now this is a proper RSA authentication
				SocketUtilities.sendAESEncryptedStringData("OK", writer, sessionKey);
			} else {
				SocketUtilities.sendAESEncryptedStringData("NOT_ALLOWED", writer, sessionKey);
				throw new IllegalAccessException("RSA auth for RedNode " + hostname + " failed.");
			}

			args = SocketUtilities.receiveAESEncryptedStringData(reader, sessionKey);
			
			// OPTIONS
			//Now it's a proper time to ask for a lock!
			try {
				Lock bnTableLock = BlueNodeTable.getInstance().aquireLock();
				if (args.length == 1 && args[0].equals(RedNodeToTracker.GET_RECOMENDED_BLUENODE.value())) {
					//rn collects a recommended bn based on the lowest load
					AppLogger.getLogger().consolePrint(pre+prern+RedNodeToTracker.GET_RECOMENDED_BLUENODE.value()+" from "+socket.getInetAddress().getHostAddress());
					RedNodeActions.getRecomendedBlueNode(bnTableLock, writer, sessionKey);
				} else if (args.length == 1 && args[0].equals(RedNodeToTracker.GET_ALL_BLUENODES.value())) {
					//rn collects a list of all the availlable bns
					AppLogger.getLogger().consolePrint(pre+prern+RedNodeToTracker.GET_ALL_BLUENODES.value()+" from "+socket.getInetAddress().getHostAddress());
					RedNodeActions.getAllConnectedBlueNodes(bnTableLock, writer, sessionKey);
				} else if (args.length == 2 && args[0].equals(RedNodeToTracker.GET_BLUENODE_PUBLIC_KEY.value())) {
					//collects a network bns public
					AppLogger.getLogger().consolePrint(pre+prern+RedNodeToTracker.GET_BLUENODE_PUBLIC_KEY.value()+" from "+socket.getInetAddress().getHostAddress());
					CommonActions.getBlueNodesPublic(bnTableLock, args[1], writer, sessionKey);
				} else if (args.length == 1 && args[0].equals(RedNodeToTracker.REVOKE_PUBLIC_KEY.value())) {
					//rn may be compromised and decides to revoke its public
					AppLogger.getLogger().consolePrint(pre+prern+RedNodeToTracker.REVOKE_PUBLIC_KEY.value()+" from "+socket.getInetAddress().getHostAddress());
					RedNodeActions.revokePublicKey(hostname, writer, sessionKey);
				} else if (BlueNodeTable.getInstance().isOnlineRnByHostname(bnTableLock, hostname)) {
					if (args.length == 2 && args[0].equals(RedNodeToTracker.GET_REDNODE_PUBLIC_KEY.value())) {
						//collects a network rns public
						AppLogger.getLogger().consolePrint(pre+prern+RedNodeToTracker.GET_REDNODE_PUBLIC_KEY.value()+" from "+socket.getInetAddress().getHostAddress());
						CommonActions.getRedNodesPublic(args[1], writer, sessionKey);
					} else {
						AppLogger.getLogger().consolePrint(pre+prern+"WRONG_COMMAND: "+args[0]+" for leased rn "+hostname+" from "+socket.getInetAddress().getHostAddress());
						SocketUtilities.sendAESEncryptedStringData("WRONG_COMMAND", writer, sessionKey);
					}
				} else {
					AppLogger.getLogger().consolePrint(pre+prern+"WRONG_COMMAND "+args[0]+" from "+socket.getInetAddress().getHostAddress());
					SocketUtilities.sendAESEncryptedStringData("WRONG_COMMAND", writer, sessionKey);
				}
			} catch (InterruptedException e) {
				//Interupted is for the lock!
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
}
