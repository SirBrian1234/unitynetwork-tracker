/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kostiskag.unitynetwork.tracker.functions;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;

/**
 *
 * @author kostis
 */
public class CryptoMethods {

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
	public static String generateAESSessionkeyInHex() {
		try {
			KeyGenerator AES_keygen = KeyGenerator.getInstance("AES");
			AES_keygen.init(256, new SecureRandom());
			SecretKey Session_Key = AES_keygen.generateKey();
			return HashFunctions.bytesToHexStr(Session_Key.getEncoded());
		} catch (NoSuchAlgorithmException ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Generates a 2048 RSA key pair
	 * 
	 * @return keypair
	 */
	public static KeyPair generateRSAkeyPair() {
		KeyPairGenerator kpg = null;
		try {
			kpg = KeyPairGenerator.getInstance("RSA");
			kpg.initialize(2048);
			return kpg.genKeyPair();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}	
		return null;
	}
	
	public static String publicToString(PublicKey pub) {
		return HashFunctions.bytesToHexStr(pub.getEncoded());
	}
	
	public static String privateToString(PrivateKey priv) {
		return HashFunctions.bytesToHexStr(priv.getEncoded());
	}
	
	public static PublicKey hexStrToRSAPublic(String hex) {
		try {
			// make your public key usable
			byte[] encoded = HashFunctions.hexStrToBytes(hex);

			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			return kf.generatePublic(keySpec);

		} catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public static PrivateKey hexStrToRSAPrivate(String hex) {
		try {
			byte[] encoded = HashFunctions.hexStrToBytes(hex);

			PKCS8EncodedKeySpec PrkeySpec = new PKCS8EncodedKeySpec(encoded);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			return kf.generatePrivate(PrkeySpec);

		} catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static byte[] RSAAuthenticateChallenge(String question, PublicKey publickey) {
		try {
			// encrypt the question &send it to the other side
			Cipher c = Cipher.getInstance("RSA/None/PKCS1Padding");
			c.init(Cipher.ENCRYPT_MODE, publickey);
			return c.doFinal(question.getBytes());

		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
				| BadPaddingException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static byte[] RSAAuthenticateResponce(byte[] chiperedQuestion, PrivateKey privatekey) {
		try {
			// encrypt the question &send it to the other side
			Cipher c = Cipher.getInstance("RSA/None/PKCS1Padding");
			c.init(Cipher.DECRYPT_MODE, privatekey);

			System.out.println("cipher is!!!!!! " + Hex.toHexString(chiperedQuestion));
			System.out.println("priv key is!!!!!! " + Hex.toHexString(privatekey.getEncoded()));
			byte[] plainQuestion = c.doFinal(chiperedQuestion);
			return plainQuestion;

		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
				| BadPaddingException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	
}
