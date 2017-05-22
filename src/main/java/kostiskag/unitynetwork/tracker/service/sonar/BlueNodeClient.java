package kostiskag.unitynetwork.tracker.service.sonar;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.LinkedList;

import javax.crypto.SecretKey;

import kostiskag.unitynetwork.tracker.App;
import kostiskag.unitynetwork.tracker.functions.CryptoMethods;
import kostiskag.unitynetwork.tracker.functions.SocketFunctions;
import kostiskag.unitynetwork.tracker.runData.BlueNodeEntry;
import kostiskag.unitynetwork.tracker.runData.RedNodeEntry;

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

    public final String pre = "^BlueNodeClient ";
    public final int timeout = 3000;
    public final BlueNodeEntry bn;
    public boolean connected = false;
    private Socket socket;
    private DataInputStream socketReader;
    private DataOutputStream socketWriter;
    private SecretKey sessionKey;
    
    public BlueNodeClient(BlueNodeEntry bn) {
    	this.bn = bn;
    	
    	try {
    		InetAddress address = SocketFunctions.getAddress(bn.getPhaddress());
			socket = SocketFunctions.absoluteConnect(address, bn.getPort());
			//socket.setSoTimeout(timeout);
			
			socketReader = SocketFunctions.makeDataReader(socket);
			socketWriter = SocketFunctions.makeDataWriter(socket);
			
			sessionKey = CryptoMethods.generateAESSessionkey();
			if (sessionKey == null) {
				throw new Exception();
			}

			String keyStr = CryptoMethods.objectToBase64StringRepresentation(sessionKey);
			SocketFunctions.sendRSAEncryptedStringData(keyStr, socketWriter, bn.getPub());
			
			String[] args = SocketFunctions.receiveAESEncryptedStringData(socketReader, sessionKey);
			System.out.println(args[0]);
			
			if(!args[0].equals("BLUENODE") || !args[1].equals(bn.getName())) {
				throw new Exception("Wrong header message");
			}
			
			//tracker is to be authenticated by the bn
			args = SocketFunctions.sendReceiveAESEncryptedStringData("TRACKER", socketReader, socketWriter, sessionKey);
			
			//decode question
			byte[] question = CryptoMethods.base64StringTobytes(args[0]);
			
			//decrypt with private
			String answer = CryptoMethods.decryptWithPrivate(question, App.trackerKeys.getPrivate());
			
			//send back plain answer
			args = SocketFunctions.sendReceiveAESEncryptedStringData(answer, socketReader, socketWriter, sessionKey);
			
			if (args[0].equals("OK")) {
				connected = true;
			} else {
				throw new Exception("Tracker authentication was not allowed from target bluenode.");
			}
		} catch (Exception e) {
			App.ConsolePrint(pre+" dropped for "+bn.getName()+" at "+socket.getInetAddress().getHostAddress());
			e.printStackTrace();
		}        
	}
    
    public boolean isConnected() {
    	return connected;
	}
    
    public boolean checkBnOnline() throws Exception {
    	if (connected) {
    		App.ConsolePrint(pre+"CHECK"+" towards "+bn.getName()+" at "+socket.getInetAddress().getHostAddress());
	    	String[] args = SocketFunctions.sendReceiveAESEncryptedStringData("CHECK",  socketReader, socketWriter, sessionKey);    
	        SocketFunctions.connectionClose(socket);
	        if (args[0].equals("OK")) {
	        	return true;
	        }         
    	}
        return false;
	}
    
    public void sendkillsig() throws Exception  {
    	if (connected) {   
    		App.ConsolePrint(pre+"KILLSIG"+" towards "+bn.getName()+" at "+socket.getInetAddress().getHostAddress());
	    	SocketFunctions.sendAESEncryptedStringData("KILLSIG", socketWriter, sessionKey); 
	    	SocketFunctions.connectionClose(socket);
	    }
    }
    
    public LinkedList<RedNodeEntry> getRedNodes() throws Exception  {  
    	LinkedList<RedNodeEntry> list = new LinkedList<>();
    	if (connected) {    	
    		App.ConsolePrint(pre+"GETREDNODES"+" towards "+bn.getName()+" at "+socket.getInetAddress().getHostAddress());
	    	SocketFunctions.sendAESEncryptedStringData("GETREDNODES", socketWriter, sessionKey);
	    	String received = SocketFunctions.receiveAESEncryptedString(socketReader, sessionKey);
	    	SocketFunctions.connectionClose(socket);	 
	    	
	    	System.out.println("received:"+received);
			String[] lines = received.split("\n+"); //split into sentences
			String[] args = lines[0].split("\\s+"); //the first sentence contains the number
			int count = Integer.parseInt(args[1]);  //for the given number read the rest sentences
	        for (int i = 1; i < count+1; i++) {        	
				args = lines[i].split("\\s+");
	            try {
	            	System.out.println("getrns "+args[0]+" "+args[1]);
	            	RedNodeEntry r =  new RedNodeEntry(bn, args[0], args[1]);                    
	 	            list.add(r); 
				} catch (Exception e) {
					e.printStackTrace();
				}				
	        }	    	      
    	}
    	return list;
	}    
}
