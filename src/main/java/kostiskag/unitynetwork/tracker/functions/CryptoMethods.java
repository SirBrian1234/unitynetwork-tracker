/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kostiskag.unitynetwork.tracker.functions;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
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

	public static String generateQuestion() {
		SecureRandom random = new SecureRandom();
		return new BigInteger(1024, random).toString(32);
	}

	public static PublicKey getPublicKeyFromString(String publicKey) {
		try {
			// make your public key usable
			publicKey = publicKey.replace("-----BEGIN PUBLIC KEY-----", "");
			publicKey = publicKey.replace("-----END PUBLIC KEY-----", "");

			// System.out.println("my key"+publicKey);
			byte[] encoded = Base64.decode(publicKey);

			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			return kf.generatePublic(keySpec);

		} catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static PrivateKey getPrivateKeyFromString(String privateKey) {
		try {
			privateKey = privateKey.replace("-----BEGIN RSA PRIVATE KEY-----", "");
			privateKey = privateKey.replace("-----END RSA PRIVATE KEY-----", "");

			byte[] encoded = Base64.decode(privateKey);

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

	/**
	 * Generates an AES 256 session key
	 * 
	 * @return
	 */
	public static byte[] generateRSASessionkey() {
		try {
			KeyGenerator AES_keygen = KeyGenerator.getInstance("AES");
			AES_keygen.init(256, new SecureRandom());
			SecretKey Session_Key = AES_keygen.generateKey();
			return Session_Key.getEncoded();
		} catch (NoSuchAlgorithmException ex) {
			ex.printStackTrace();
		}
		return null;
	}
}
