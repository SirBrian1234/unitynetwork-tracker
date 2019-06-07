package org.kostiskag.unitynetwork.tracker.rundata.address;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kostiskag.unitynetwork.tracker.AppLogger;

import java.net.UnknownHostException;

public class VirtualAddressTest {

	@BeforeClass
	public static void before() {
		AppLogger.newInstance(null,null);
	}

	@Test
	public void stringConstructor() {
		String address = "10.0.0.4";
		VirtualAddress v = null;
		try {
			 v = VirtualAddress.valueOf(address);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			assertTrue(false);
		}
		assertEquals(v.asInt(),3);
		assertEquals(v.asString(),address);
	}

	@Test
	public void numericConstructor() {
		int address = 15;
		VirtualAddress v = null;
		try {
			v = VirtualAddress.valueOf(15);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			assertTrue(false);
		}
		assertEquals(v.asInt(),15);
		assertEquals(v.asString(),"10.0.0.16");
	}

	@Test
	public void equalityTest() throws UnknownHostException {
		VirtualAddress v1, v2, v3;
		v1 = VirtualAddress.valueOf(28);
		v2 = VirtualAddress.valueOf("10.0.0.29");

		assertEquals(v1, v1);
		assertEquals(v1, VirtualAddress.valueOf(28));
		assertEquals(v2, v1);
		assertEquals(v1, v2);

		v2 = VirtualAddress.valueOf("10.0.0.30");
		v3 = VirtualAddress.valueOf(50);

		assertNotEquals(v1, v2);
		assertNotEquals(v2, v1);
		assertNotEquals(v1, v3);
		assertNotEquals(v2, v3);
	}

	@Test
	public void hashCodeTest() throws UnknownHostException {
		VirtualAddress v1, v2, v3;
		v1 = VirtualAddress.valueOf(28);
		v2 = VirtualAddress.valueOf("10.0.0.29");
		v3 = VirtualAddress.valueOf(50);

		assertEquals(v1.hashCode(), v2.hashCode());
		assertNotEquals(v1.hashCode(), v3.hashCode());
		assertNotEquals(v2.hashCode(), v3.hashCode());
	}

	@Test
	public void _10IpAddrToNumberTest() {
		try {
			assertEquals(VirtualAddress._10IpAddrToNumber("10.0.0.3"), 2);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			assertFalse(true);
		}
	}

	@Test
	public void numberTo10ipAddrTest() {
		try {
			assertEquals(VirtualAddress.numberTo10ipAddr(2), "10.0.0.3");
		} catch (UnknownHostException e) {
			e.printStackTrace();
			assertFalse(true);
		}
	}
}
