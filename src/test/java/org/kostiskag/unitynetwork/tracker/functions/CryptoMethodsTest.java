package org.kostiskag.unitynetwork.tracker.functions;

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
			String question = CryptoMethods.generateQuestion();
			System.out.println(question);
			// assertEquals(1024, question.length());
			for (int i = 0; i < 500; i++) {
				assertNotEquals(question, CryptoMethods.generateQuestion());
			}
		}
	}

	@Ignore
	public void generateAESkey() throws NoSuchAlgorithmException {
		SecretKey keyA = CryptoMethods.generateAESSessionkey();
		SecretKey keyB = CryptoMethods.generateAESSessionkey();
		assertNotEquals(keyA, keyB);
		System.out.println(keyA.getEncoded() + " " + keyA.getEncoded().length);
		System.out.println(keyB.getEncoded() + " " + keyB.getEncoded().length);

	}

	@Test
	public void aesEncryptDecryptTest() throws NoSuchAlgorithmException {
		String plainMessage = "My name is Wapaf!!!";
		SecretKey key = CryptoMethods.generateAESSessionkey();
		byte[] chipher = CryptoMethods.aesEncrypt(plainMessage, key);
		System.out.println(new String(chipher));
		String dec = CryptoMethods.aesDecrypt(chipher, key);
		System.out.println(dec);
		assertEquals(plainMessage, dec);
	}

	@Test
	public void generateKeyPairTest() {
		KeyPair kp = CryptoMethods.generateRSAkeyPair();
		KeyPair kp2 = CryptoMethods.generateRSAkeyPair();
		assertNotEquals(kp.getPublic(), kp2.getPublic());
		assertNotEquals(kp.getPrivate(), kp2.getPrivate());
	}

	@Test
	public void rsaEncryptDecryptTest() {
		String question = CryptoMethods.generateQuestion();
		System.out.println(question);

		KeyPair kp = CryptoMethods.generateRSAkeyPair();
		PrivateKey privkey = kp.getPrivate();
		PublicKey pubkey = kp.getPublic();

		// encrypt the question
		byte[] enc = CryptoMethods.encryptWithPublic(question, pubkey);

		// decrypt the question
		String dec_q = CryptoMethods.decryptWithPrivate(enc, privkey);

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
		KeyPair kp = CryptoMethods.generateRSAkeyPair();
		PrivateKey privkey = kp.getPrivate();
		byte[] serial = CryptoMethods.objectToBytes(privkey);
		System.out.println(HashFunctions.bytesToHexStr(serial));
		PrivateKey npriv = (PrivateKey) CryptoMethods.bytesToObject(serial);
		assertEquals(privkey, npriv);
	}

	@Test
	public void testBase64Object() {
		KeyPair kp = CryptoMethods.generateRSAkeyPair();
		PrivateKey privkey = kp.getPrivate();
		String repr = CryptoMethods.objectToBase64StringRepresentation(privkey);
		System.out.println(repr);
		PrivateKey priv = (PrivateKey) CryptoMethods.base64StringRepresentationToObject(repr);
		assertEquals(privkey, priv);
	}

	@Test
	public void testObjectToFile() {
		KeyPair kp = CryptoMethods.generateRSAkeyPair();
		PrivateKey privkey = kp.getPrivate();
		CryptoMethods.objectToFile(privkey, new File("pakis"));
		PrivateKey priv = (PrivateKey) CryptoMethods.fileToObject(new File("pakis"));
		assertEquals(privkey, priv);
	}

	@Test
	public void sessionTest() throws NoSuchAlgorithmException {

		KeyPair server = CryptoMethods.generateRSAkeyPair();
		KeyPair client = CryptoMethods.generateRSAkeyPair();

		// a tcp auth session goes like this...
		// client has in hold server's public
		// server has in hold client's public

		// client
		// client generates aes key
		SecretKey key = CryptoMethods.generateAESSessionkey();
		String strkey = CryptoMethods.objectToBase64StringRepresentation(key);
		System.out.println(strkey);

		// builds welcome message plus aes key
		String message = "BLUENODE " + "Pakis" + " " + strkey;
		System.out.println(message);

		// ecrypts build message with server's rsa public
		byte[] chiph = CryptoMethods.encryptWithPublic(message, server.getPublic());

		// toot toot toot sending...
		// server's side

		// server decrypts with private
		String received = CryptoMethods.decryptWithPrivate(chiph, server.getPrivate());
		assertEquals(received, message);
		String[] args = received.split("\\s+");
		System.out.println("received " + args[0] + " " + args[1] + " " + args[2] + " ");

		// server has AES key in hold
		SecretKey key_s = (SecretKey) CryptoMethods.base64StringRepresentationToObject(args[2]);
		assertEquals(key, key_s);
		// comm switch to AES

		// server generates secret question in order to verify client
		String question = CryptoMethods.generateQuestion();
		System.out.println("QUESTION: " + question);

		// server encrypts secret q with client's public
		byte[] questionb = CryptoMethods.encryptWithPublic(question, client.getPublic());
		String encq = CryptoMethods.bytesToBase64String(questionb);

		// server builds welcome message plus encrypted secret question
		message = "TRACKER " + encq;
		System.out.println(message);

		// server encrypts build message and sends to client
		byte[] tosend = CryptoMethods.aesEncrypt(message, key_s);

		// send!
		// beep beep boop... client's side!
		// client

		// client decrypts chiphered with AES key
		received = CryptoMethods.aesDecrypt(tosend, key);
		assertEquals(message, received);
		args = received.split("\\s+");

		assertEquals("TRACKER", args[0]);
		assertEquals(encq, args[1]);

		// client decodes secret q from base64 to bytes
		byte[] cquestionb = CryptoMethods.base64StringTobytes(args[1]);

		// client decrypts secret question with his private
		String dec_q = CryptoMethods.decryptWithPrivate(cquestionb, client.getPrivate());
		assertEquals(question, dec_q);

		// client builds command and includes secret question
		message = "CHECKRNA " + dec_q;

		// client encrypts and sends message with aes session
		tosend = CryptoMethods.aesEncrypt(message, key);

		// server compares generated and received questions
		received = CryptoMethods.aesDecrypt(tosend, key_s);
		args = received.split("\\s+");
		assertEquals(question, args[1]);

		// server allows the attached command to pass if compared and generated
		// are the same
		System.out.println("Allowed " + args[0]);
	}
}
