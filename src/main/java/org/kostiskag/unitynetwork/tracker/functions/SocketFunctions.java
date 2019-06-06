package org.kostiskag.unitynetwork.tracker.functions;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import org.kostiskag.unitynetwork.tracker.AppLogger;


/**
 * low-level socket methods here
 *
 * @author Konstantinos Kagiampakis
 */
public class SocketFunctions {

	public static final String pre = "^SOCKET METHODS ";       
    
    public static InetAddress getAddress(String PhAddress) throws UnknownHostException {
        InetAddress IPaddress = null;
        try {
            IPaddress = InetAddress.getByName(PhAddress);
        } catch (UnknownHostException ex) {            
        	 AppLogger.getLogger().consolePrint(pre + "UNKNOWN HOST "+PhAddress);
        	 throw ex;
        }
        return IPaddress;
    }
    
    public static Socket absoluteConnect(InetAddress IPaddress, int authPort) throws IOException {
        return absoluteConnect(IPaddress,authPort,8000);
    }

    public static Socket absoluteConnect(InetAddress IPaddress, int authPort, int timeout) throws IOException {
        Socket socket = null;
        try {
            socket = new Socket(IPaddress, authPort);
            socket.setSoTimeout(timeout);
        } catch (IOException ex) {
            AppLogger.getLogger().consolePrint(pre + "CONNECTION ERROR FOR "+IPaddress.getHostAddress()+" "+authPort);
            throw ex;
        }
        return socket;
    }

    public static DataInputStream makeDataReader(Socket socket) throws IOException {
    	//BufferedInputStream bin = new BufferedInputStream();
		DataInputStream dataStream = new DataInputStream(socket.getInputStream());
		return dataStream;
    }

    public static DataOutputStream makeDataWriter(Socket socket) throws IOException {
        DataOutputStream dataStream = new DataOutputStream(socket.getOutputStream());
        return dataStream;
    }
    
    public static void sendData(byte[] toSend, DataOutputStream writer) throws IOException {
    	writer.write(toSend);
    }
    
    public static byte[] receiveData(DataInputStream reader) throws IOException {
    	byte[] byteT = new byte[]{0x00};
    	byte[] bytes = new byte[2048];
    	for (int i=0; i<2; i++) {
	    	int read = reader.read(bytes);
			if (read > 0) {
		    	byteT = new byte[read];
				System.arraycopy(bytes, 0, byteT, 0, read);
			
				if (byteT[0] == (int)0) {
					AppLogger.getLogger().consolePrint(pre + "RECEIVED a zero char");
			    } else if (byteT[0] == (int)13) {
					AppLogger.getLogger().consolePrint(pre + "RECEIVED a new line char");
				} else if (byteT[0] == (int)10) {
					AppLogger.getLogger().consolePrint(pre+ "received a return char");
				}
				return byteT;
			} else if (read == 0){
				AppLogger.getLogger().consolePrint(pre + "RECEIVED zero");
			} else {
				AppLogger.getLogger().consolePrint(pre + "RECEIVED "+read);
			}
    	}
    	return byteT; 		
    }
    
    public static byte[] sendReceiveData(byte[] toSend, DataInputStream reader, DataOutputStream writer) throws Exception  {
    	sendData(toSend, writer);
    	byte[] received = receiveData(reader);
    	return received;
    }

    public static void sendPlainStringData(String message, DataOutputStream writer) throws IOException {
    	if (message == null) {
        	AppLogger.getLogger().consolePrint(pre + "NO DATA TO SEND");
            throw new IOException(pre+"NO DATA TO SEND");
        } else if (message.isEmpty()) {
        	//line feed
        	message = "\n";
        }        
    	//include a line feed and a return char
    	//message += "\n\r";
    	byte[] toSend = message.getBytes();        
        sendData(toSend, writer);
    }
    
    public static String[] receivePlainStringData(DataInputStream reader) throws IOException {
    	byte[] received = receiveData(reader);
    	String receivedMessage = new String(received, "utf-8");
        String[] args = receivedMessage.split("\\s+");
        return args;
    }
    
    public static String[] sendReceivePlainStringData(String data, DataInputStream reader, DataOutputStream writer) throws Exception  {
    	sendPlainStringData(data, writer);
    	String[] args = receivePlainStringData(reader);
    	return args;
    }
    
    public static void sendAESEncryptedStringData(String message, DataOutputStream writer, SecretKey sessionKey) throws IOException {
    	if (message == null) {
        	AppLogger.getLogger().consolePrint(pre + "NO DATA TO SEND");
            throw new IOException(pre+"NO DATA TO SEND");
        } else if (message.isEmpty()) {
        	//line feed
        	message = "\n";
        }        
    	//include a line feed and a return char
    	//message += "\n\r";
    	byte[] chiphered = CryptoMethods.aesEncrypt(message, sessionKey);
        sendData(chiphered, writer);
    }
    
    public static String[] receiveAESEncryptedStringData(DataInputStream reader, SecretKey sessionKey) throws IOException {
    	byte[] received = receiveData(reader);
    	String decrypted = CryptoMethods.aesDecrypt(received, sessionKey);
    	String[] args = decrypted.split("\\s+");
        return args;
    }
    
    public static String receiveAESEncryptedString(DataInputStream reader, SecretKey sessionKey) throws IOException {
    	byte[] received = receiveData(reader);
    	String decrypted = CryptoMethods.aesDecrypt(received, sessionKey);
    	return decrypted;
    }
    
    public static String[] sendReceiveAESEncryptedStringData(String message, DataInputStream reader, DataOutputStream writer, SecretKey sessionKey) throws IOException  {
    	sendAESEncryptedStringData(message, writer, sessionKey);
    	return receiveAESEncryptedStringData(reader, sessionKey);
    }
    
    public static void sendRSAEncryptedStringData(String message, DataOutputStream writer, PublicKey key) throws IOException {
    	if (message == null) {
        	AppLogger.getLogger().consolePrint(pre + "NO DATA TO SEND");
            throw new IOException(pre+" NO DATA TO SEND");
        } else if (message.isEmpty()) {
        	//line feed
        	message = "\n";
        }        
    	//include a line feed and a return char
    	//message += "\n\r";
    	byte[] chiphered = CryptoMethods.encryptWithPublic(message, key);
        sendData(chiphered, writer);
    }
    
    public static String[] receiveRSAEncryptedStringData(DataInputStream reader, PrivateKey priv) throws IOException {
    	byte[] received = receiveData(reader);
    	String decrypted = CryptoMethods.decryptWithPrivate(received, priv);
    	return decrypted.split("\\s+");
    }
    
    public static void connectionClose(Socket socket) throws IOException {
        if (!socket.isClosed()) {
            socket.close();
        }
    }
    
    /*
     * Deprecated
     */
    

    public static BufferedReader makeReadWriter(Socket socket) throws IOException {
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        return inputReader;
    }

    public static PrintWriter makeWriteWriter(Socket socket) throws IOException {
        PrintWriter outputWriter = new PrintWriter(socket.getOutputStream(), true);
        return outputWriter;
    }

    public static String[] sendData(String data,PrintWriter outputWriter,BufferedReader inputReader) throws Exception  {
        if (outputWriter==null) {
        	AppLogger.getLogger().consolePrint(pre + "SEND DATA FAILED, NO CONNECTION");
        	throw new Exception(pre + "SEND DATA FAILED, NO CONNECTION");
        } else if (inputReader==null){ 
        	AppLogger.getLogger().consolePrint(pre + "SEND DATA FAILED, NO CONNECTION");
        	throw new Exception(pre + "SEND DATA FAILED, NO CONNECTION");
        } else if (data == null) {
        	AppLogger.getLogger().consolePrint(pre + "NO DATA TO SEND");
            throw new Exception(pre+"NO DATA TO SEND");
        } else if (data.isEmpty()) {
        	AppLogger.getLogger().consolePrint(pre + "NO DATA TO SEND");
            throw new Exception(pre+"NO DATA TO SEND");
        }
        
        outputWriter.println(data);
        String receivedMessage = null;
        String[] args = null;
        
        try {
			receivedMessage = inputReader.readLine();
		} catch (IOException e) {
			throw e;
		}
        
        AppLogger.getLogger().consolePrint(pre + receivedMessage);
        args = receivedMessage.split("\\s+");
        return args;
    }

    public static void sendFinalData(String data,PrintWriter outputWriter) throws Exception {
        if (data == null) {
        	AppLogger.getLogger().consolePrint(pre + "NO DATA TO SEND");
            throw new Exception(pre + "NO DATA TO SEND");
        }
        outputWriter.println(data);
    }

    public static String[] readData(BufferedReader inputReader) throws Exception  {
    	if (inputReader == null){
    		AppLogger.getLogger().consolePrint(pre + "READ DATA FAILED, NO CONNECTION");
            throw new Exception(pre + "READ DATA FAILED, NO CONNECTION");
    	}
        
        String receivedMessage = null;
        String[] args = null;
        
        try {
			receivedMessage = inputReader.readLine();
		} catch (IOException e) {
			throw e;
		}
               
        System.out.println(pre + receivedMessage);
        args = receivedMessage.split("\\s+");
        return args;
    }
}