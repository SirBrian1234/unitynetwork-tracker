/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kostiskag.unitynetwork.tracker.utilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

/**
 *
 * @author Konstantinos Kagiampakis
 */
public class CryptoUtilities {

	/**
	 * Generates a random question
	 * 
	 * @return
	 */
	public static String generateQuestion() {
		SecureRandom random = new SecureRandom();
		return new BigInteger(1024, random).toString(32);
	}

	/**
	 * Generates an AES 256 session key
	 * 
	 * @return
	 */
	public static SecretKey generateAESSessionkey() throws GeneralSecurityException {
		KeyGenerator AES_keygen = null;
		try {
			AES_keygen = KeyGenerator.getInstance("AES");
			AES_keygen.init(128, new SecureRandom());
			return AES_keygen.generateKey();
		} catch (NoSuchAlgorithmException e) {
			throw new GeneralSecurityException(e);
		}
	}
	
	public static byte[] aesEncrypt(String message, SecretKey key) throws GeneralSecurityException {
		Cipher AesCipher;
		try {
			AesCipher = Cipher.getInstance("AES");
			AesCipher.init(Cipher.ENCRYPT_MODE, key);
	        return AesCipher.doFinal(message.getBytes());
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			throw new GeneralSecurityException(e);
		}
	}

	public static String aesDecrypt(byte[] chiphered, SecretKey key) throws GeneralSecurityException {
		Cipher AesCipher;
		try {
			AesCipher = Cipher.getInstance("AES");
			AesCipher.init(Cipher.DECRYPT_MODE, key);
	        return new String(AesCipher.doFinal(chiphered), StandardCharsets.ISO_8859_1);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			throw new GeneralSecurityException(e);
		}
	}
	
	/**
	 * Generates a 2048 RSA key pair
	 * 
	 * @return keypair
	 */
	public static KeyPair generateRSAkeyPair() throws GeneralSecurityException {
		KeyPairGenerator kpg = null;
		try {
			kpg = KeyPairGenerator.getInstance("RSA");
			kpg.initialize(2048, new SecureRandom());
			return kpg.genKeyPair();
		} catch (NoSuchAlgorithmException e) {
			throw new GeneralSecurityException(e);
		}
	}

	public static byte[] encryptWithPublic(String text, PublicKey key) throws GeneralSecurityException {
		byte[] cipherText = null;
		try {
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			return cipher.doFinal(text.getBytes());
		} catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
			throw new GeneralSecurityException(e);
		}
	}

	public static String decryptWithPrivate(byte[] text, PrivateKey key) throws GeneralSecurityException {
		byte[] dectyptedText = null;
		try {
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, key);
			return new String(cipher.doFinal(text));
		} catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
			throw new GeneralSecurityException(e);
		}
	}
	
	public static <A> byte[] objectToBytes(A obj) throws IOException {
		try (ByteArrayOutputStream b = new ByteArrayOutputStream();
			 ObjectOutputStream out = new ObjectOutputStream(b)) {
			out.writeObject(obj);
			return b.toByteArray();
		} catch (IOException e) {
			throw e;
		}
	}

	/**
	 * remember to define a type argument to the object of your preference ex. PrivateKey
	 *
	 * @param bytes
	 * @return
	 */
	public static <A>  A bytesToObject(byte[] bytes) throws IOException, GeneralSecurityException {
		try (ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
			 ObjectInputStream in = new ObjectInputStream(bin)) {
			return (A) in.readObject(); //It seems i cant avoid casting...
		} catch (IOException e) {
			throw e;
		} catch (ClassNotFoundException ex) {
			throw new GeneralSecurityException(ex);
		}
	}
	
	public static <A> String objectToBase64StringRepresentation(A obj) throws IOException {
		return new String(Base64.getEncoder().encode(objectToBytes(obj)));
	}
	
	public static <A> A base64StringRepresentationToObject(String base64Str) throws IOException, GeneralSecurityException {
		return bytesToObject(Base64.getDecoder().decode(base64Str.getBytes()));
	}
	
	public static String bytesToBase64String(byte[] b) {
		return new String(Base64.getEncoder().encode(b));
	}
	
	public static byte[] base64StringTobytes(String base64Str) {
		return Base64.getDecoder().decode(base64Str.getBytes());
	}

	public static <A> void objectToFile(A obj, File file) throws IOException {
		try (FileOutputStream fileOut = new FileOutputStream(file);
			 ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
			out.writeObject(obj);
		} catch (IOException e) {
			throw e;
		}
	}

	public static <A> A fileToObject(File file) throws GeneralSecurityException, IOException {
		try (FileInputStream fileIn = new FileInputStream(file);
			 ObjectInputStream in = new ObjectInputStream(fileIn)) {
			return (A) in.readObject(); //It seems i cant avoid casting...
		} catch (ClassNotFoundException e) {
			throw new GeneralSecurityException(e);
		} catch (IOException e) {
			throw e;
		}
	}
}
