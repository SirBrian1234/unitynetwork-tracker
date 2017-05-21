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
				App.ConsolePrint(pre+"GETPUB"+" from "+socket.getInetAddress().getHostAddress());
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
				App.ConsolePrint(pre+prebn+"OFFERPUB"+" from "+socket.getInetAddress().getHostAddress());
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
				App.ConsolePrint(pre+prebn+"LEASE"+" from "+socket.getInetAddress().getHostAddress());
				BlueNodeFunctions.BlueLease(BlueNodeHostname, pub, socket, args[1], writer, sessionKey);
			} else if (App.BNtable.checkOnlineByName(BlueNodeHostname)) {
				//in other words in order to execute extensive queries you have to be logged in
				if (args.length == 4 && args[0].equals("LEASE_RN")) {
					App.ConsolePrint(pre+prebn+"LEASE_RN"+" from "+socket.getInetAddress().getHostAddress());
					BlueNodeFunctions.RedLease(BlueNodeHostname, args[1], args[2], args[3], writer, sessionKey);
				} else if (args.length == 1 && args[0].equals("RELEASE")) {
					App.ConsolePrint(pre+prebn+"RELEASE"+" from "+socket.getInetAddress().getHostAddress());
					BlueNodeFunctions.BlueRel(BlueNodeHostname, writer, sessionKey);
				} else if (args.length == 2 && args[0].equals("RELEASE_RN")) {
					App.ConsolePrint(pre+prebn+"RELEASE_RN"+" from "+socket.getInetAddress().getHostAddress());
					BlueNodeFunctions.RedRel(BlueNodeHostname, args[1], writer, sessionKey);
				} else if (args.length == 2 && args[0].equals("GETPH")) {
					App.ConsolePrint(pre+prebn+"GETPH"+" from "+socket.getInetAddress().getHostAddress());
					BlueNodeFunctions.GetPh(args[1], writer, sessionKey);
				} else if (args.length == 2 && args[0].equals("CHECK_RN")) {
					App.ConsolePrint(pre+prebn+"CHECK_RN"+" from "+socket.getInetAddress().getHostAddress());
					BlueNodeFunctions.CheckRn(args[1], writer, sessionKey);
				} else if (args.length == 2 && args[0].equals("CHECK_RNA")) {
					App.ConsolePrint(pre+prebn+"CHECK_RNA"+" from "+socket.getInetAddress().getHostAddress());
					BlueNodeFunctions.CheckRnAddr(args[1], writer, sessionKey);
				} else if (args.length == 2 && args[0].equals("LOOKUP_H")) {
					App.ConsolePrint(pre+prebn+"LOOKUP_H"+" from "+socket.getInetAddress().getHostAddress());
					BlueNodeFunctions.LookupByHn(args[1], writer, sessionKey);
				} else if (args.length == 2 && args[0].equals("LOOKUP_V")) {
					App.ConsolePrint(pre+prebn+"LOOKUP_V"+" from "+socket.getInetAddress().getHostAddress());
					BlueNodeFunctions.LookupByAddr(args[1], writer, sessionKey);
				} else if (args.length == 2 && args[0].equals("GETBNPUB")) {
					App.ConsolePrint(pre+prebn+"GETBNPUB"+" from "+socket.getInetAddress().getHostAddress());
					CommonFunctions.getBlueNodesPublic(args[1], writer, sessionKey);
				} else if (args.length == 2 && args[0].equals("GETRNPUB")) {
					App.ConsolePrint(pre+prebn+"GETRNPUB"+" from "+socket.getInetAddress().getHostAddress());
					CommonFunctions.getRedNodesPublic(args[1], writer, sessionKey);
				} else if (args.length == 1 && args[0].equals("REVOKEPUB")) {
					// bluenode may be compromised and decides to revoke its public
					App.ConsolePrint(pre+prebn+"REVOKEPUB"+" from "+socket.getInetAddress().getHostAddress());
					BlueNodeFunctions.revokePublicKey(BlueNodeHostname, writer, sessionKey);
				} else {
					App.ConsolePrint(pre+prebn+"WRONG_COMMAND: "+args[0]+" for leased bn "+BlueNodeHostname+" from "+socket.getInetAddress().getHostAddress());
					SocketFunctions.sendAESEncryptedStringData("WRONG_COMMAND", writer, sessionKey);
				}
			} else {
				App.ConsolePrint(pre+prebn+"WRONG_COMMAND: "+args[0]+" from "+socket.getInetAddress().getHostAddress());
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
				App.ConsolePrint(pre+prern+"OFFERPUB"+" from "+socket.getInetAddress().getHostAddress());
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
			if (args.length == 1 && args[0].equals("GETRBN")) {
				//rn collects a recommended bn based on the lowest load
				App.ConsolePrint(pre+prern+"GETRBN"+" from "+socket.getInetAddress().getHostAddress());
				RedNodeFunctions.getRecomendedBlueNode(writer, sessionKey);
			} else if (args.length == 1 && args[0].equals("GETBNS")) {
				//rn collects a list of all the availlable bns
				App.ConsolePrint(pre+prern+"GETBNS"+" from "+socket.getInetAddress().getHostAddress());
				RedNodeFunctions.getAllConnectedBlueNodes(writer, sessionKey);
			} else if (args.length == 2 && args[0].equals("GETBNPUB")) {
				//collects a network bns public
				App.ConsolePrint(pre+prern+"GETBNPUB"+" from "+socket.getInetAddress().getHostAddress());
				CommonFunctions.getBlueNodesPublic(args[1], writer, sessionKey);
			} else if (args.length == 1 && args[0].equals("REVOKEPUB")) {
				//rn may be compromised and decides to revoke its public
				App.ConsolePrint(pre+prern+"REVOKEPUB"+" from "+socket.getInetAddress().getHostAddress());
				RedNodeFunctions.revokePublicKey(hostname, writer, sessionKey);
			} else if (App.BNtable.checkOnlineRnByHn(hostname)) {
				if (args.length == 2 && args[0].equals("GETRNPUB")) {
					//collects a network rns public
					App.ConsolePrint(pre+prern+"GETRNPUB"+" from "+socket.getInetAddress().getHostAddress());
					CommonFunctions.getRedNodesPublic(args[1], writer, sessionKey);
				} else {
					App.ConsolePrint(pre+prern+"WRONG_COMMAND: "+args[0]+" for leased rn "+hostname+" from "+socket.getInetAddress().getHostAddress());
					SocketFunctions.sendAESEncryptedStringData("WRONG_COMMAND", writer, sessionKey);
				}
			} else {
				App.ConsolePrint(pre+prern+"WRONG_COMMAND "+args[0]+" from "+socket.getInetAddress().getHostAddress());
				SocketFunctions.sendAESEncryptedStringData("WRONG_COMMAND", writer, sessionKey);
			}
		}
	}
}
