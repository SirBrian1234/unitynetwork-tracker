package kostiskag.unitynetwork.tracker.service.track;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import kostiskag.unitynetwork.tracker.App;
import kostiskag.unitynetwork.tracker.functions.CryptoMethods;
import kostiskag.unitynetwork.tracker.functions.SocketFunctions;
import kostiskag.unitynetwork.tracker.service.BlueNodeGlobalFunctions;

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

	private Socket socket;
	private DataInputStream reader;
	private DataOutputStream writer;
	private SecretKey sessionKey;

	TrackService(Socket connectionSocket) {
		socket = connectionSocket;
	}

	private void close() {
		try {
			SocketFunctions.connectionClose(socket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		System.out.println("@Started auth service at " + Thread.currentThread().getName());
		try {
			reader = SocketFunctions.makeDataReader(socket);
			writer = SocketFunctions.makeDataWriter(socket);

			byte[] received = SocketFunctions.receiveData(reader);
			String receivedStr = new String(received, "utf-8");
			String[] args = receivedStr.split("\\s+");

			if (args[0].equals("GETPUB")) {
				// plain data transfer no encryption
				SocketFunctions.sendPlainStringData(
						CryptoMethods.objectToBase64StringRepresentation(App.trackerKeys.getPublic()), writer);
			} else {
				//client uses server's public key to send a session key
				String decrypted = CryptoMethods.decryptWithPrivate(received, App.trackerKeys.getPrivate());
				sessionKey = (SecretKey) CryptoMethods.base64StringRepresentationToObject(decrypted);
				args = SocketFunctions.sendReceiveAESEncryptedStringData("UnityTracker", reader, writer, sessionKey);

				if (args.length == 2 && args[0].equals("BLUENODE")) {
					BlueNodeService(args[1]);
				} else if (args.length == 2 && args[0].equals("REDNODE")) {
					RedNodeService(args[1]);
				} else {
					SocketFunctions.sendAESEncryptedStringData("WRONG_COMMAND", writer, sessionKey);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		close();
	}

	public void BlueNodeService(String BlueNodeHostname) throws Exception {
		PublicKey pub = BlueNodeGlobalFunctions.fetchPubKey(BlueNodeHostname);
		if (pub == null) {
			/*
			 * the bn is a member however he has not set a public key this
			 * allows only to set key based on session key
			 */

			// this is a pub key offer
			String[] args = SocketFunctions.sendReceiveAESEncryptedStringData("PUBLIC_NOT_SET", reader, writer,
					sessionKey);
			if (args[0].equals("EXIT")) {
				return;
			} else if (args.length == 3 && args[0].equals("OFFERPUB")) {
				// bluenode offers its pub based on a ticket
				BlueNodeFunctions.offerPublicKey(BlueNodeHostname, args[1], args[2], writer, sessionKey);
			} else {
				SocketFunctions.sendAESEncryptedStringData("NOT_ALLOWED", writer, sessionKey);
			}

		} else {
			/*
			 * The Bn has a public key set authentication will follow based on
			 * its public key
			 */

			// generate a random question
			String question = CryptoMethods.generateQuestion();

			// encrypt question with target bluenode's public
			byte[] questionb = CryptoMethods.encryptWithPublic(question, pub);

			// encode it to base 64
			String encq = CryptoMethods.bytesToBase64String(questionb);

			// send it, wait for response
			String args[] = SocketFunctions.sendReceiveAESEncryptedStringData(encq, reader, writer, sessionKey);

			System.out.println("received " + args[0]);
			if (args[0].equals("EXIT")) {
				return;
			} else if (args[0].equals(question)) {
				// now this is a proper RSA authentication
				SocketFunctions.sendAESEncryptedStringData("OK", writer, sessionKey);
			} else {
				SocketFunctions.sendAESEncryptedStringData("NOT_ALLOWED", writer, sessionKey);
				throw new Exception("RSA auth for BlueNode " + BlueNodeHostname + " failed.");
			}

			args = SocketFunctions.receiveAESEncryptedStringData(reader, sessionKey);
			// OPTIONS
			if (args.length == 2 && args[0].equals("LEASE")) {
				BlueNodeFunctions.BlueLease(BlueNodeHostname, pub, socket, args[1], writer, sessionKey);
			} else if (args.length == 4 && args[0].equals("LEASE_RN")) {
				BlueNodeFunctions.RedLease(BlueNodeHostname, args[1], args[2], args[3], writer, sessionKey);
			} else if (args.length == 1 && args[0].equals("RELEASE")) {
				BlueNodeFunctions.BlueRel(BlueNodeHostname, writer, sessionKey);
			} else if (args.length == 2 && args[0].equals("RELEASE_RN")) {
				BlueNodeFunctions.RedRel(BlueNodeHostname, args[1], writer, sessionKey);
			} else if (args.length == 2 && args[0].equals("GETPH")) {
				BlueNodeFunctions.GetPh(args[1], writer, sessionKey);
			} else if (args.length == 2 && args[0].equals("CHECK_RN")) {
				BlueNodeFunctions.CheckRn(args[1], writer, sessionKey);
			} else if (args.length == 2 && args[0].equals("CHECK_RNA")) {
				BlueNodeFunctions.CheckRnAddr(args[1], writer, sessionKey);
			} else if (args.length == 2 && args[0].equals("LOOKUP_H")) {
				BlueNodeFunctions.LookupByHn(args[1], writer, sessionKey);
			} else if (args.length == 2 && args[0].equals("LOOKUP_V")) {
				BlueNodeFunctions.LookupByAddr(args[1], writer, sessionKey);
			} else if (args.length == 2 && args[0].equals("GETBNPUB")) {
				CommonFunctions.getBlueNodesPublic(args[1], writer, sessionKey);
			} else if (args.length == 2 && args[0].equals("GETRNPUB")) {
				CommonFunctions.getRedNodesPublic(args[1], writer, sessionKey);
			} else if (args.length == 1 && args[0].equals("REVOKEPUB")) {
				// bluenode may be compromised and decides to revoke its public
				BlueNodeFunctions.revokePublicKey(BlueNodeHostname, writer, sessionKey);
			} else {
				SocketFunctions.sendAESEncryptedStringData("WRONG_COMMAND", writer, sessionKey);
			}
		}
	}

	private void RedNodeService(String hostname) throws Exception {
		PublicKey pub = RedNodeFunctions.fetchPubKey(hostname);
		if (pub == null) {
			/*
			 * the rn is a member however he has not set a public key this
			 * allows only to set key based on session key
			 */

			// this is a pub key offer
			String[] args = SocketFunctions.sendReceiveAESEncryptedStringData("PUBLIC_NOT_SET", reader, writer, sessionKey);
			if (args[0].equals("EXIT")) {
				return;
			} else if (args.length == 3 && args[0].equals("OFFERPUB")) {
				// bluenode offers its pub based on a ticket
				RedNodeFunctions.offerPublicKey(hostname, args[1], args[2], writer, sessionKey);
			} else {
				SocketFunctions.sendAESEncryptedStringData("NOT_ALLOWED", writer, sessionKey);
			}

		} else {
			/*
			 * The RN has a public key set authentication will follow based on
			 * its public key
			 */

			// generate a random question
			String question = CryptoMethods.generateQuestion();

			// encrypt question with target rednodes's public
			byte[] questionb = CryptoMethods.encryptWithPublic(question, pub);

			// encode it to base 64
			String encq = CryptoMethods.bytesToBase64String(questionb);

			// send it, wait for response
			String args[] = SocketFunctions.sendReceiveAESEncryptedStringData(encq, reader, writer, sessionKey);

			if (args[0].equals("EXIT")) {
				return;
			} else if (args[0].equals(question)) {
				// now this is a proper RSA authentication
				SocketFunctions.sendAESEncryptedStringData("OK", writer, sessionKey);
			} else {
				SocketFunctions.sendAESEncryptedStringData("NOT_ALLOWED", writer, sessionKey);
				throw new Exception("RSA auth for RedNode " + hostname + " failed.");
			}

			args = SocketFunctions.receiveAESEncryptedStringData(reader, sessionKey);
			
			// OPTIONS
			if (args.length == 1 && args[0].equals("GETBNS")) {
				//rn collects a list of all the availlable bns
				RedNodeFunctions.getAllConnectedBlueNodes(writer, sessionKey);
			} else if (args.length == 1 && args[0].equals("GETRBN")) {
				//rn collects a recommended bn based on the lowest load
				RedNodeFunctions.getRecomendedBlueNode(writer, sessionKey);
			} else if (args.length == 2 && args[0].equals("GETBNPUB")) {
				//collects a network bns public
				CommonFunctions.getBlueNodesPublic(args[1], writer, sessionKey);
			} else if (args.length == 2 && args[0].equals("GETRNPUB")) {
				//collects a network rns public
				CommonFunctions.getRedNodesPublic(args[1], writer, sessionKey);
			} else if (args.length == 1 && args[0].equals("REVOKEPUB")) {
				//rn may be compromised and decides to revoke its public
				RedNodeFunctions.revokePublicKey(hostname, writer, sessionKey);
			} else {
				SocketFunctions.sendAESEncryptedStringData("WRONG_COMMAND", writer, sessionKey);
			}
		}
	}
}
