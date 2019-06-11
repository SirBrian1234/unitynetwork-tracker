package org.kostiskag.unitynetwork.tracker.utilities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import org.junit.Ignore;
import org.junit.Test;

public class CryptoMethodsTest {

	@Test
	public void generateQuestionTest() {
		for (int k = 0; k < 10; k++) {
			String question = CryptoUtilities.generateQuestion();
			System.out.println(question);
			// assertEquals(1024, question.length());
			for (int i = 0; i < 500; i++) {
				assertNotEquals(question, CryptoUtilities.generateQuestion());
			}
		}
	}

	@Ignore
	public void generateAESkey() throws NoSuchAlgorithmException {
		SecretKey keyA = CryptoUtilities.generateAESSessionkey();
		SecretKey keyB = CryptoUtilities.generateAESSessionkey();
		assertNotEquals(keyA, keyB);
		System.out.println(keyA.getEncoded() + " " + keyA.getEncoded().length);
		System.out.println(keyB.getEncoded() + " " + keyB.getEncoded().length);

	}

	@Test
	public void aesEncryptDecryptTest() throws NoSuchAlgorithmException {
		String plainMessage = "My name is Wapaf!!!";
		SecretKey key = CryptoUtilities.generateAESSessionkey();
		byte[] chipher = CryptoUtilities.aesEncrypt(plainMessage, key);
		System.out.println(new String(chipher));
		String dec = CryptoUtilities.aesDecrypt(chipher, key);
		System.out.println(dec);
		assertEquals(plainMessage, dec);
	}

	@Test
	public void generateKeyPairTest() {
		KeyPair kp = CryptoUtilities.generateRSAkeyPair();
		KeyPair kp2 = CryptoUtilities.generateRSAkeyPair();
		assertNotEquals(kp.getPublic(), kp2.getPublic());
		assertNotEquals(kp.getPrivate(), kp2.getPrivate());
	}

	@Test
	public void rsaEncryptDecryptTest() {
		String question = CryptoUtilities.generateQuestion();
		System.out.println(question);

		KeyPair kp = CryptoUtilities.generateRSAkeyPair();
		PrivateKey privkey = kp.getPrivate();
		PublicKey pubkey = kp.getPublic();

		// encrypt the question
		byte[] enc = CryptoUtilities.encryptWithPublic(question, pubkey);

		// decrypt the question
		String dec_q = CryptoUtilities.decryptWithPrivate(enc, privkey);

		// THE MOMENT WE ALL WAITED FOR
		System.out.println(dec_q);
		System.out.println(question);
		if (dec_q.equals(question)) {
			System.out.println("Match!!!!!!!!!");
		}
		assertEquals(question, dec_q);
	}

	@Test
	public void testSerializeObject() {
		KeyPair kp = CryptoUtilities.generateRSAkeyPair();
		PrivateKey privkey = kp.getPrivate();
		byte[] serial = CryptoUtilities.objectToBytes(privkey);
		System.out.println(HashUtilities.bytesToHexStr(serial));
		PrivateKey npriv = (PrivateKey) CryptoUtilities.bytesToObject(serial);
		assertEquals(privkey, npriv);
	}

	@Test
	public void testBase64Object() {
		KeyPair kp = CryptoUtilities.generateRSAkeyPair();
		PrivateKey privkey = kp.getPrivate();
		String repr = CryptoUtilities.objectToBase64StringRepresentation(privkey);
		System.out.println(repr);
		PrivateKey priv = (PrivateKey) CryptoUtilities.base64StringRepresentationToObject(repr);
		assertEquals(privkey, priv);
	}

	@Test
	public void testObjectToFile() {
		KeyPair kp = CryptoUtilities.generateRSAkeyPair();
		PrivateKey privkey = kp.getPrivate();
		CryptoUtilities.objectToFile(privkey, new File("pakis"));
		PrivateKey priv = (PrivateKey) CryptoUtilities.fileToObject(new File("pakis"));
		assertEquals(privkey, priv);
	}

	@Test
	public void sessionTest() throws NoSuchAlgorithmException {

		KeyPair server = CryptoUtilities.generateRSAkeyPair();
		KeyPair client = CryptoUtilities.generateRSAkeyPair();

		// a tcp auth session goes like this...
		// client has in hold server's public
		// server has in hold client's public

		// client
		// client generates aes key
		SecretKey key = CryptoUtilities.generateAESSessionkey();
		String strkey = CryptoUtilities.objectToBase64StringRepresentation(key);
		System.out.println(strkey);

		// builds welcome message plus aes key
		String message = "BLUENODE " + "Pakis" + " " + strkey;
		System.out.println(message);

		// ecrypts build message with server's rsa public
		byte[] chiph = CryptoUtilities.encryptWithPublic(message, server.getPublic());

		// toot toot toot sending...
		// server's side

		// server decrypts with private
		String received = CryptoUtilities.decryptWithPrivate(chiph, server.getPrivate());
		assertEquals(received, message);
		String[] args = received.split("\\s+");
		System.out.println("received " + args[0] + " " + args[1] + " " + args[2] + " ");

		// server has AES key in hold
		SecretKey key_s = (SecretKey) CryptoUtilities.base64StringRepresentationToObject(args[2]);
		assertEquals(key, key_s);
		// comm switch to AES

		// server generates secret question in order to verify client
		String question = CryptoUtilities.generateQuestion();
		System.out.println("QUESTION: " + question);

		// server encrypts secret q with client's public
		byte[] questionb = CryptoUtilities.encryptWithPublic(question, client.getPublic());
		String encq = CryptoUtilities.bytesToBase64String(questionb);

		// server builds welcome message plus encrypted secret question
		message = "TRACKER " + encq;
		System.out.println(message);

		// server encrypts build message and sends to client
		byte[] tosend = CryptoUtilities.aesEncrypt(message, key_s);

		// send!
		// beep beep boop... client's side!
		// client

		// client decrypts chiphered with AES key
		received = CryptoUtilities.aesDecrypt(tosend, key);
		assertEquals(message, received);
		args = received.split("\\s+");

		assertEquals("TRACKER", args[0]);
		assertEquals(encq, args[1]);

		// client decodes secret q from base64 to bytes
		byte[] cquestionb = CryptoUtilities.base64StringTobytes(args[1]);

		// client decrypts secret question with his private
		String dec_q = CryptoUtilities.decryptWithPrivate(cquestionb, client.getPrivate());
		assertEquals(question, dec_q);

		// client builds command and includes secret question
		message = "CHECKRNA " + dec_q;

		// client encrypts and sends message with aes session
		tosend = CryptoUtilities.aesEncrypt(message, key);

		// server compares generated and received questions
		received = CryptoUtilities.aesDecrypt(tosend, key_s);
		args = received.split("\\s+");
		assertEquals(question, args[1]);

		// server allows the attached command to pass if compared and generated
		// are the same
		System.out.println("Allowed " + args[0]);
	}
}
