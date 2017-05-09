package kostiskag.unitynetwork.tracker.functions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import org.junit.Ignore;
import org.junit.Test;

public class CryptoMethodsTest {

	@Test
	public void generateQuestionTest() {
		for (int k=0; k<10; k++) {
			String question = CryptoMethods.generateQuestion();
			System.out.println(question);
			//assertEquals(1024, question.length());
			for (int i=0; i<500; i++) {				
				assertNotEquals(question, CryptoMethods.generateQuestion());
			}
		}
	}
	
	@Ignore
	public void generateAESkey() {		
		SecretKey keyA = CryptoMethods.generateAESSessionkey();
		SecretKey keyB = CryptoMethods.generateAESSessionkey();
		assertNotEquals(keyA, keyB);
		System.out.println(keyA.getEncoded() +" "+keyA.getEncoded().length);
		System.out.println(keyB.getEncoded() +" "+keyB.getEncoded().length);
		
	}
	
	@Test
	public void aesEncryptDecryptTest() {
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
        
        //encrypt the question
        byte[] enc = CryptoMethods.encryptWithPublic(question, pubkey);
        
        //decrypt the question            
        String dec_q = CryptoMethods.decryptWithPrivate(enc, privkey);

        //THE MOMENT WE ALL WAITED FOR
        System.out.println(dec_q);
        System.out.println(question);
        if (dec_q.equals(question)) {
            System.out.println("Match!!!!!!!!!");           
        }
        assertEquals(question, dec_q);	
	}

	@Test
	public void testSerializeObject () {
		KeyPair kp = CryptoMethods.generateRSAkeyPair();
		PrivateKey privkey = kp.getPrivate();
		byte[] serial = CryptoMethods.objectToBytes(privkey);
		System.out.println(HashFunctions.bytesToHexStr(serial));
		PrivateKey npriv = (PrivateKey) CryptoMethods.bytesToObject(serial);		
		assertEquals(privkey, npriv);		
	}
	
	@Test
	public void testBase64Object () {
		KeyPair kp = CryptoMethods.generateRSAkeyPair();
		PrivateKey privkey = kp.getPrivate();
		String repr = CryptoMethods.objectToBase64StringRepresentation(privkey);
		System.out.println(repr);
		PrivateKey priv = (PrivateKey) CryptoMethods.base64StringRepresentationToObject(repr);
		assertEquals(privkey, priv);		
	}
	
	@Test
	public void testObjectToFile () {
		KeyPair kp = CryptoMethods.generateRSAkeyPair();
		PrivateKey privkey = kp.getPrivate();
		CryptoMethods.objectToFile(privkey, new File("pakis"));
		PrivateKey priv = (PrivateKey) CryptoMethods.fileToObject(new File("pakis"));
		assertEquals(privkey, priv);	
	}
	
	@Test
	public void sessionTest() {
		/* a tcp auth session goes like this...
		 * client has server's public	
		 * client generates aes key
		 * client encrypts aes key with server public
		 * sends  welcome message plus encrypted aes key to server
		 * server decrypts with private
		 * server has AES key
		 * switch to AES
		 * server generates secret question
		 * server encrypts secret q with client's public
		 * server builds welcome message plus encrypted secret question
		 * server encrypts build message with AES and sends to client
		 * client decrypts chiphered with AES key
		 * client decrypts secret question with his private rsa key
		 * client encrypts with AES and sends back secret question
		 * server compares generated and received questions
		 * if the same, server allows client to give command
		 */

		//a tcp auth session goes like this...
		//client has server's public	
		//client generates aes key
		//client encrypts aes key with server public
		//sends  welcome message plus encrypted aes key to server
		//server decrypts with private
		//server has AES key
		//switch to AES
		//server generates secret question
		//server encrypts secret q with client's public
		//server builds welcome message plus encrypted secret question
		//server encrypts build message and sends to client
		//client decrypts chiphered with AES key
		//client decrypts secret question with his private
		//client encrypts with AES and sends back secret question
		//server compares generated and received questions
		//server allows commands
		
	}
}
