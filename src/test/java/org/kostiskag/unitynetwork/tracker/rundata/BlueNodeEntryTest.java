package org.kostiskag.unitynetwork.tracker.rundata;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import java.security.PublicKey;
import java.sql.Time;

import org.kostiskag.unitynetwork.tracker.AppLogger;
import org.kostiskag.unitynetwork.tracker.functions.CryptoMethods;

public class BlueNodeEntryTest {

	static String hostname = "ouiou";
	static String address = "10.0.0.1";
	static int port = 4440;
	static PublicKey pub = CryptoMethods.generateRSAkeyPair().getPublic();

	@BeforeClass
	public static void before() {
		AppLogger.newInstance(null,null);
	}

	@Test
	public void testConstructorB() {
		BlueNodeEntry bn = new BlueNodeEntry(hostname, pub, address, port);
		assertEquals(bn.getName(),hostname);
		assertEquals(bn.getPhaddress(),address);
		assertEquals(bn.getPort(),port);
		assertEquals(bn.getPub(),pub);
		assertNotEquals(null, bn.getRedNodes());
	}
	
	@Test
	public void testConstructorA() {
		Time t = new Time(System.currentTimeMillis());
		BlueNodeEntry bn = new BlueNodeEntry(hostname, pub, address, port, t);
		assertEquals(bn.getName(),hostname);
		assertEquals(bn.getPhaddress(),address);
		assertEquals(bn.getPort(),port);
		assertEquals(bn.getPub(),pub);
		assertNotEquals(null, bn.getRedNodes());
		assertEquals(bn.getTimestamp().toString(),t.toString());
	}

	@Test
	public void testUpdateTimestamp() throws InterruptedException {
		Time t = new Time(System.currentTimeMillis());
		Thread.sleep(1000);
		BlueNodeEntry bn = new BlueNodeEntry(hostname, pub, address, port);
		System.out.println(bn);
		Time oldt = bn.getTimestamp();
		Thread.sleep(1000);
		bn.updateTimestamp();

		assertTrue(bn.getTimestamp().getTime() - t.getTime() > 0);
		assertTrue(bn.getTimestamp().getTime() - oldt.getTime() > 0);
		assertTrue(oldt.getTime() - t.getTime() > 0);
	}

	@Test
	public void testEquality() throws InterruptedException {
		BlueNodeEntry bn = new BlueNodeEntry(hostname, pub, address, port);
		BlueNodeEntry bn2 = new BlueNodeEntry("pakis", pub, address, port);
		BlueNodeEntry bn3 = new BlueNodeEntry("lakis", pub, address, port);

		BlueNodeEntry bn4 = new BlueNodeEntry(new String("ouiou"), pub, address, port);

		//----
		assertEquals(bn, bn);
		assertEquals(bn, bn4);
		assertEquals(bn4, bn);
		//----
		assertNotEquals(bn2, bn);
		assertNotEquals(bn, bn2);
		//----
		assertNotEquals(bn,bn2);
		assertNotEquals(bn3,bn);
	}
}
