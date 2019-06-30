package org.kostiskag.unitynetwork.tracker.rundata.entry;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.sql.Time;

import org.kostiskag.unitynetwork.common.address.VirtualAddress;
import org.kostiskag.unitynetwork.common.utilities.CryptoUtilities;
import org.kostiskag.unitynetwork.tracker.AppLogger;


public class BlueNodeEntryTest {

	static String hostname = "ouiou";
	static String address = "11.0.0.1";
	static int port = 4440;
	static PublicKey pub;

	@BeforeClass
	public static void before() throws GeneralSecurityException {
		AppLogger.newInstance(null,null);
		pub = CryptoUtilities.generateRSAkeyPair().getPublic();
	}

	@Test
	public void testgetipInBytes() throws UnknownHostException {
		String ip = "10.255.255.254";

		byte[] b = VirtualAddress.networkAddressToInetAddress(ip).getAddress();
		for(byte bb : b) {
			System.out.println(Integer.toHexString(bb & 0xff));
		}

		System.out.println(VirtualAddress.byteTo10IpAddrNumber(b));
		System.out.println(VirtualAddress.byteTo10IpAddrNumber(new byte[] {(byte) 0x0a, (byte) 0xff, (byte) 0xff, (byte) 0xfe}));
	}

	@Test
	public void testConstructorB() throws IllegalAccessException, UnknownHostException {
		BlueNodeEntry bn = new BlueNodeEntry(hostname, pub, address, port);
		assertEquals(bn.getHostname(),hostname);
		assertEquals(bn.getAddress().asString(),address);
		assertEquals(bn.getPort(),port);
		assertEquals(bn.getPub(),pub);
		assertNotEquals(null, bn.getRedNodes());
	}
	
	@Test
	public void testConstructorA() throws IllegalAccessException, UnknownHostException {
		Time t = new Time(System.currentTimeMillis());
		BlueNodeEntry bn = new BlueNodeEntry(hostname, pub, address, port);
		assertEquals(bn.getHostname(),hostname);
		assertEquals(bn.getAddress().asString(),address);
		assertEquals(bn.getPort(),port);
		assertEquals(bn.getPub(),pub);
		assertNotEquals(null, bn.getRedNodes());
		assertEquals(bn.getTimestamp().toString(),t.toString());
	}

	@Test
	public void testUpdateTimestamp() throws IllegalAccessException, InterruptedException, UnknownHostException {
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
	public void testEquality() throws IllegalAccessException, UnknownHostException {
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
