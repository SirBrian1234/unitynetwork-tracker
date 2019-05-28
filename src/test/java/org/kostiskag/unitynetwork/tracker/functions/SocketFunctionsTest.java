package org.kostiskag.unitynetwork.tracker.functions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import org.junit.Test;

public class SocketFunctionsTest {

	@Test
	public void encryptedTest() {		
		SecretKey key = CryptoMethods.generateAESSessionkey();
		
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(bout);
		
		String message = "hello";
		try {
			SocketFunctions.sendAESEncryptedStringData(message, dout, key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		byte[] trimmed;
		trimmed = bout.toByteArray();
		
		ByteArrayInputStream bin = new ByteArrayInputStream(trimmed);
		DataInputStream din = new DataInputStream(bin);
		
		try {
			String[] args = SocketFunctions.receiveAESEncryptedStringData(din, key);
			System.out.println(args[0]);
			assertEquals(args[0], message);
		} catch (IOException e) {
			e.printStackTrace();
			assertTrue(false);
		}		
	}
	
	@Test
	public void testGetPub() {
		try {
			InetAddress IPaddress = null;
			try {
				IPaddress = InetAddress.getByName("127.0.0.1");
			} catch (UnknownHostException ex) {
				ex.printStackTrace();
			}

			Socket socket = new Socket(IPaddress, 8000);
			socket.setSoTimeout(8000);
			
            DataInputStream dIn = SocketFunctions.makeDataReader(socket);
			DataOutputStream dOut = SocketFunctions.makeDataWriter(socket);
			
	    	try {
				SocketFunctions.sendPlainStringData("GETPUB", dOut);
			} catch (Exception e) {
				e.printStackTrace();
			}
	    	String[] args = SocketFunctions.receivePlainStringData(dIn);
			System.out.println("bytes read\n"+args[0]);
			
			socket.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testAESsession() {
		try {
			InetAddress IPaddress = null;
			try {
				IPaddress = InetAddress.getByName("127.0.0.1");
			} catch (UnknownHostException ex) {
				ex.printStackTrace();
			}

			Socket socket = new Socket(IPaddress, 8000);
			socket.setSoTimeout(8000);
			
            DataInputStream dIn = SocketFunctions.makeDataReader(socket);
			DataOutputStream dOut = SocketFunctions.makeDataWriter(socket);
			
			SecretKey key = CryptoMethods.generateAESSessionkey();
			String keyStr = CryptoMethods.objectToBase64StringRepresentation(key);
			
			System.out.println(keyStr);
			
			PublicKey trackerPub = (PublicKey) CryptoMethods.base64StringRepresentationToObject("rO0ABXNyABRqYXZhLnNlY3VyaXR5LktleVJlcL35T7OImqVDAgAETAAJYWxnb3JpdGhtdAASTGphdmEvbGFuZy9TdHJpbmc7WwAHZW5jb2RlZHQAAltCTAAGZm9ybWF0cQB+AAFMAAR0eXBldAAbTGphdmEvc2VjdXJpdHkvS2V5UmVwJFR5cGU7eHB0AANSU0F1cgACW0Ks8xf4BghU4AIAAHhwAAABJjCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAJSDYlnhWQBtDJh5MqWFpg75MLpMy+ZLWt7lxUs8dAgplbTpZiIqRlAIlEy42BcAJ9lIyJtqdO8Gow7jHJ3yQBvvarZHclmblNILbWGsX9tHOe4l1gabIawGkBzsnmi9ViK7oeo4Vj5oD8O000Tr61WkfpQB2M7KMxhMMTs0m0V0BgPT/vyRJAsFqj1WLiyj+KYw1Ncx/upQOn03x0u+r94hri29pmugW7txS/TwmoBzUj7Qv0qV2gh878CR6uyjMTIYRW6XOuOHqk3rLXfHiYBhMSHLDIaQfi4PufK2MlNXTqtMrrsan0n75qraAcH90H+GW9PDP2M/8tC3GcBgZPUCAwEAAXQABVguNTA5fnIAGWphdmEuc2VjdXJpdHkuS2V5UmVwJFR5cGUAAAAAAAAAABIAAHhyAA5qYXZhLmxhbmcuRW51bQAAAAAAAAAAEgAAeHB0AAZQVUJMSUM=");
			
			try {
				SocketFunctions.sendRSAEncryptedStringData(keyStr, dOut, trackerPub);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			String[] args = SocketFunctions.receiveAESEncryptedStringData(dIn, key);
			System.out.println(args[0]);
			assertEquals(args[0],"UnityTracker");
			
			try {
				//intentionaly giving ouiou to receive wrong command and stop
				args = SocketFunctions.sendReceiveAESEncryptedStringData("ouiou", dIn, dOut, key);
				System.out.println(args[0]);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			assertEquals(args[0],"WRONG_COMMAND");
			socket.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testBNoffer() {
		try {
			InetAddress IPaddress = null;
			try {
				IPaddress = InetAddress.getByName("127.0.0.1");
			} catch (UnknownHostException ex) {
				ex.printStackTrace();
			}

			Socket socket = new Socket(IPaddress, 8000);
			socket.setSoTimeout(8000);
			
            DataInputStream dIn = SocketFunctions.makeDataReader(socket);
			DataOutputStream dOut = SocketFunctions.makeDataWriter(socket);
			
			SecretKey key = CryptoMethods.generateAESSessionkey();
			String keyStr = CryptoMethods.objectToBase64StringRepresentation(key);
			
			System.out.println(keyStr);
			
			PublicKey trackerPub = (PublicKey) CryptoMethods.base64StringRepresentationToObject("rO0ABXNyABRqYXZhLnNlY3VyaXR5LktleVJlcL35T7OImqVDAgAETAAJYWxnb3JpdGhtdAASTGphdmEvbGFuZy9TdHJpbmc7WwAHZW5jb2RlZHQAAltCTAAGZm9ybWF0cQB+AAFMAAR0eXBldAAbTGphdmEvc2VjdXJpdHkvS2V5UmVwJFR5cGU7eHB0AANSU0F1cgACW0Ks8xf4BghU4AIAAHhwAAABJjCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAJSDYlnhWQBtDJh5MqWFpg75MLpMy+ZLWt7lxUs8dAgplbTpZiIqRlAIlEy42BcAJ9lIyJtqdO8Gow7jHJ3yQBvvarZHclmblNILbWGsX9tHOe4l1gabIawGkBzsnmi9ViK7oeo4Vj5oD8O000Tr61WkfpQB2M7KMxhMMTs0m0V0BgPT/vyRJAsFqj1WLiyj+KYw1Ncx/upQOn03x0u+r94hri29pmugW7txS/TwmoBzUj7Qv0qV2gh878CR6uyjMTIYRW6XOuOHqk3rLXfHiYBhMSHLDIaQfi4PufK2MlNXTqtMrrsan0n75qraAcH90H+GW9PDP2M/8tC3GcBgZPUCAwEAAXQABVguNTA5fnIAGWphdmEuc2VjdXJpdHkuS2V5UmVwJFR5cGUAAAAAAAAAABIAAHhyAA5qYXZhLmxhbmcuRW51bQAAAAAAAAAAEgAAeHB0AAZQVUJMSUM=");
			
			try {
				SocketFunctions.sendRSAEncryptedStringData(keyStr, dOut, trackerPub);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			String[] args = SocketFunctions.receiveAESEncryptedStringData(dIn, key);
			System.out.println(args[0]);
			assertEquals(args[0],"UnityTracker");
			
			try {
				//intentionaly giving ouiou to receive wrong command and stop
				args = SocketFunctions.sendReceiveAESEncryptedStringData("BLUENODE pakis", dIn, dOut, key);
				System.out.println(args[0]);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			assertEquals(args[0],"OK");
			String ticket = "67q4b41si1frp82u75649vs1s00qd9glr3n5vsb8pqlsb2rnikpg38b011kr81gn76eoi5q9rspqjut2ho5vqmrf791ehbqd51gchgv71857m1uq7cgg65ftr1ac8fkjkph1v55fa7a6mabrtjn5pf99fqbl2mic1e4dl24pjmljojamtdtd9ko2gf921agot3sbutdcbe4er";
			String pubKey = "ouiou";
			String message = "OFFERPUB "+ticket+" "+pubKey;
			try {
				SocketFunctions.sendAESEncryptedStringData(message, dOut, key);
				System.out.println(message);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			socket.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
