package org.kostiskag.unitynetwork.tracker.service.sonar;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.crypto.SecretKey;

import org.kostiskag.unitynetwork.tracker.App;
import org.kostiskag.unitynetwork.tracker.AppLogger;
import org.kostiskag.unitynetwork.tracker.functions.CryptoMethods;
import org.kostiskag.unitynetwork.tracker.functions.SocketFunctions;
import org.kostiskag.unitynetwork.tracker.rundata.BlueNodeEntry;
import org.kostiskag.unitynetwork.tracker.rundata.RedNodeEntry;

/**
 * These functions are being used from the sonar service - the tracker client
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
	public BlueNodeClient(BlueNodeEntry bn) throws NoSuchAlgorithmException, IOException {
    	this.bn = bn;

    	socket = SocketFunctions.absoluteConnect(bn.getPhAddress().asInet(), bn.getPort());
		socket.setSoTimeout(BlueNodeClient.TIMEOUT);

		socketReader = SocketFunctions.makeDataReader(socket);
		socketWriter = SocketFunctions.makeDataWriter(socket);

		sessionKey = CryptoMethods.generateAESSessionkey();

		String keyStr = CryptoMethods.objectToBase64StringRepresentation(sessionKey);
		SocketFunctions.sendRSAEncryptedStringData(keyStr, socketWriter, bn.getPub());

		String[] args = SocketFunctions.receiveAESEncryptedStringData(socketReader, sessionKey);
		System.out.println(args[0]);

		if(!args[0].equals("BLUENODE") || !args[1].equals(bn.getName())) {
			throw new IOException("Wrong header message");
		}

		//tracker is to be authenticated by the bn
		args = SocketFunctions.sendReceiveAESEncryptedStringData("TRACKER", socketReader, socketWriter, sessionKey);

		//decode question
		byte[] question = CryptoMethods.base64StringTobytes(args[0]);

		//decrypt with private
		String answer = CryptoMethods.decryptWithPrivate(question, App.TRACKER_APP.trackerKeys.getPrivate());

		//send back plain answer
		args = SocketFunctions.sendReceiveAESEncryptedStringData(answer, socketReader, socketWriter, sessionKey);

		if (!args[0].equals("OK")) {
			throw new IOException("Tracker authentication was not allowed from target bluenode.");
		}

		this.bn.setClient(this);
	}

    public boolean testBnOnline()  {
    	try {
    		AppLogger.getLogger().consolePrint(PRE +"CHECK"+" towards "+bn.getName()+" at "+socket.getInetAddress().getHostAddress());
	    	String[] args = SocketFunctions.sendReceiveAESEncryptedStringData("CHECK",  socketReader, socketWriter, sessionKey);
	        SocketFunctions.connectionClose(socket);
	        if (args[0].equals("OK")) {
	        	return true;
	        } else {
	        	throw new IOException();
			}
    	} catch (IOException e) {
			AppLogger.getLogger().consolePrint(PRE +"CHECK"+" BN OFFLINE "+bn.getName()+" at "+socket.getInetAddress().getHostAddress());
			return false;
		} finally {
			try {
				SocketFunctions.connectionClose(socket);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
    
    public void sendkillsig() {
    	try {
			AppLogger.getLogger().consolePrint(PRE + "KILLSIG" + " towards " + bn.getName() + " at " + socket.getInetAddress().getHostAddress());
			SocketFunctions.sendAESEncryptedStringData("KILLSIG", socketWriter, sessionKey);
			SocketFunctions.connectionClose(socket);
		} catch (IOException e) {
			AppLogger.getLogger().consolePrint(PRE + "KILLSIG EXCEPTION" + " towards " + bn.getName() + " at " + socket.getInetAddress().getHostAddress());
		} finally {
			try {
				SocketFunctions.connectionClose(socket);
			} catch (IOException e) {
				AppLogger.getLogger().consolePrint(PRE + "KILLSIG SOCKET CLOSE EXCEPTION" + " towards " + bn.getName() + " at " + socket.getInetAddress().getHostAddress());
			}
		}
    }
    
    public List<RedNodeEntry> getRedNodes() throws IOException {
    	List<RedNodeEntry> list = new ArrayList<>();
		try {
			AppLogger.getLogger().consolePrint(PRE + "GETREDNODES" + " towards " + bn.getName() + " at " + socket.getInetAddress().getHostAddress());
			SocketFunctions.sendAESEncryptedStringData("GETREDNODES", socketWriter, sessionKey);
			String received = SocketFunctions.receiveAESEncryptedString(socketReader, sessionKey);
			SocketFunctions.connectionClose(socket);

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
					AppLogger.getLogger().consolePrint(PRE + "A RedNode ON BLUENODE'S LIST found with unvalid IP address" + bn.getName() + " at " + bn.getPhAddress().asString() + ": "+ e.getMessage());
				} catch (IllegalAccessException ex) {
					//not to throw
					AppLogger.getLogger().consolePrint(PRE + "A BN was feeding duplicate rn entries" + bn.getName() + " at " + bn.getPhAddress().asString() + ": "+ ex.getMessage());
				}
			}
		} catch (IOException e) {
			//this should be thrown!
			AppLogger.getLogger().consolePrint(PRE + "BN GET RedNode LIST EXCEPTION for" + bn.getName() + " at " + socket.getInetAddress().getHostAddress());
			throw e;
		} finally {
			try {
				SocketFunctions.connectionClose(socket);
			} catch (IOException e) {
				//this should not be thrown
				AppLogger.getLogger().consolePrint(PRE + "GET RedNode SOCKET CLOSE EXCEPTION" + " towards " + bn.getName() + " at " + socket.getInetAddress().getHostAddress());
			}
		}

    	return list;
	}    
}