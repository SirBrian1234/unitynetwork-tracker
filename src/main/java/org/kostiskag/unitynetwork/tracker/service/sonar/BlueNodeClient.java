package org.kostiskag.unitynetwork.tracker.service.sonar;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.SecretKey;

import org.kostiskag.unitynetwork.tracker.App;
import org.kostiskag.unitynetwork.tracker.AppLogger;
import org.kostiskag.unitynetwork.tracker.rundata.utilities.CryptoUtilities;
import org.kostiskag.unitynetwork.tracker.rundata.utilities.SocketUtilities;
import org.kostiskag.unitynetwork.tracker.rundata.entry.BlueNodeEntry;
import org.kostiskag.unitynetwork.tracker.rundata.entry.RedNodeEntry;
import org.kostiskag.unitynetwork.tracker.rundata.serviceoperations.TrackerToBlueNode;


/**
 * These utilities are being used from the sonar service - the tracker client
 * 
 *  CHECK
 *  KILLSIG
 *  GETREDNODES
 *  
 *  Exception handle policy: the task is to provide resilience and protect the network socket,
 *  keep it alive and block the internal system exceptions such as database
 *  therefore net exceptions should be thrown, system exception should be cached and a
 *  proper response has to be compiled.
 *
 *  @author Konstantinos Kagiampakis
 */
public class BlueNodeClient {

    public static final String PRE = "^BlueNodeClient ";
    public static final int TIMEOUT = 3000;

    private final KeyPair trackerKeyPair;
    private final BlueNodeEntry bn;
    private final Socket socket;
    private final DataInputStream socketReader;
    private final DataOutputStream socketWriter;
    private final SecretKey sessionKey;

	/**
	 * The drill is simple if a caller creates a new BN Client instance and does not
	 * receive any throwables it means that the Object is VALID and there is a connection!
	 * in any other case the object's methods defend themselves with internal try catch
	 *
	 * @param bn
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public BlueNodeClient(BlueNodeEntry bn, KeyPair trackerKeyPair) throws GeneralSecurityException, IOException {
    	this.bn = bn;
    	this.trackerKeyPair = trackerKeyPair;

    	socket = SocketUtilities.absoluteConnect(bn.getAddress().asInet(), bn.getPort());
		socket.setSoTimeout(BlueNodeClient.TIMEOUT);

		socketReader = SocketUtilities.makeDataReader(socket);
		socketWriter = SocketUtilities.makeDataWriter(socket);

		sessionKey = CryptoUtilities.generateAESSessionkey();

		String keyStr = CryptoUtilities.objectToBase64StringRepresentation(sessionKey);
		SocketUtilities.sendRSAEncryptedStringData(keyStr, socketWriter, bn.getPub());

		String[] args = SocketUtilities.receiveAESEncryptedStringData(socketReader, sessionKey);
		System.out.println(args[0]);

		if(!args[0].equals("BLUENODE") || !args[1].equals(bn.getHostname())) {
			throw new IOException("Wrong header message");
		}

		//tracker is to be authenticated by the bn
		args = SocketUtilities.sendReceiveAESEncryptedStringData("TRACKER", socketReader, socketWriter, sessionKey);

		//decode question
		byte[] question = CryptoUtilities.base64StringTobytes(args[0]);

		//decrypt with private
		String answer = CryptoUtilities.decryptWithPrivate(question, trackerKeyPair.getPrivate());

		//send back plain answer
		args = SocketUtilities.sendReceiveAESEncryptedStringData(answer, socketReader, socketWriter, sessionKey);

		if (!args[0].equals("OK")) {
			throw new IOException("Tracker authentication was not allowed from target bluenode.");
		}

		this.bn.setClient(this);
	}

    public boolean testBnOnline()  {
    	try {
    		AppLogger.getLogger().consolePrint(PRE +TrackerToBlueNode.CHECK_IF_ALIVE.value()+" towards "+bn.getHostname()+" at "+socket.getInetAddress().getHostAddress());
	    	String[] args = SocketUtilities.sendReceiveAESEncryptedStringData(TrackerToBlueNode.CHECK_IF_ALIVE.value(),  socketReader, socketWriter, sessionKey);
	        SocketUtilities.connectionClose(socket);
	        if (args[0].equals("OK")) {
	        	return true;
	        } else {
	        	throw new IOException();
			}
    	} catch (GeneralSecurityException | IOException e) {
			AppLogger.getLogger().consolePrint(PRE +TrackerToBlueNode.CHECK_IF_ALIVE.value()+" BN OFFLINE "+bn.getHostname()+" at "+socket.getInetAddress().getHostAddress());
			return false;
		} finally {
			try {
				SocketUtilities.connectionClose(socket);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
    
    public void sendkillsig() {
    	try {
			AppLogger.getLogger().consolePrint(PRE + TrackerToBlueNode.KILLING_SIGNAL.value() + " towards " + bn.getHostname() + " at " + socket.getInetAddress().getHostAddress());
			SocketUtilities.sendAESEncryptedStringData(TrackerToBlueNode.KILLING_SIGNAL.value(), socketWriter, sessionKey);
			SocketUtilities.connectionClose(socket);
		} catch (GeneralSecurityException | IOException e) {
			AppLogger.getLogger().consolePrint(PRE + TrackerToBlueNode.KILLING_SIGNAL.value()+" EXCEPTION" + " towards " + bn.getHostname() + " at " + socket.getInetAddress().getHostAddress());
		} finally {
			try {
				SocketUtilities.connectionClose(socket);
			} catch (IOException e) {
				AppLogger.getLogger().consolePrint(PRE + TrackerToBlueNode.KILLING_SIGNAL.value()+" SOCKET CLOSE EXCEPTION" + " towards " + bn.getHostname() + " at " + socket.getInetAddress().getHostAddress());
			}
		}
    }
    
    public List<RedNodeEntry> getRedNodes() throws GeneralSecurityException, IOException {
    	List<RedNodeEntry> list = new ArrayList<>();
		try {
			AppLogger.getLogger().consolePrint(PRE + TrackerToBlueNode.GET_ALL_LEASED_REDNODES.value() + " towards " + bn.getHostname() + " at " + socket.getInetAddress().getHostAddress());
			SocketUtilities.sendAESEncryptedStringData(TrackerToBlueNode.GET_ALL_LEASED_REDNODES.value(), socketWriter, sessionKey);
			String received = SocketUtilities.receiveAESEncryptedString(socketReader, sessionKey);
			SocketUtilities.connectionClose(socket);

			String[] lines = received.split("\n+"); //split into sentences
			String[] args = lines[0].split("\\s+"); //the first sentence contains the number
			int count = Integer.parseInt(args[1]);  //for the given number read the rest sentences
			for (int i = 1; i < count + 1; i++) {
				args = lines[i].split("\\s+");
				try {
					//after this the address is valid
					RedNodeEntry r = new RedNodeEntry(bn, args[0], args[1]);
					//a bn should not feed with duplicates
					if (list.contains(r)) {
						throw new IllegalAccessException("BN "+bn+" attempted to feed a duplicate RN entry "+r);
					}
					//if entry reached here it is validated
					list.add(r);
				} catch (UnknownHostException e) {
					//not to throw, the element just wont be included on the list
					AppLogger.getLogger().consolePrint(PRE + "A RedNode ON BLUENODE'S LIST found with unvalid IP address" + bn.getHostname() + " at " + bn.getAddress().asString() + ": "+ e.getMessage());
				} catch (IllegalAccessException ex) {
					//not to throw
					AppLogger.getLogger().consolePrint(PRE + "A BN was feeding duplicate rn entries" + bn.getHostname() + " at " + bn.getAddress().asString() + ": "+ ex.getMessage());
				}
			}
		} catch (GeneralSecurityException | IOException e) {
			//this should be thrown!
			AppLogger.getLogger().consolePrint(PRE + "BN GET RedNode LIST EXCEPTION for" + bn.getHostname() + " at " + socket.getInetAddress().getHostAddress());
			throw e;
		} finally {
			try {
				SocketUtilities.connectionClose(socket);
			} catch (IOException e) {
				//this should not be thrown
				AppLogger.getLogger().consolePrint(PRE + "GET RedNode SOCKET CLOSE EXCEPTION" + " towards " + bn.getHostname() + " at " + socket.getInetAddress().getHostAddress());
			}
		}

    	return list;
	}    
}
