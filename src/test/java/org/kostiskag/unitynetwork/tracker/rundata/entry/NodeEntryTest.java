package org.kostiskag.unitynetwork.tracker.rundata.entry;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import java.net.UnknownHostException;
import java.sql.Time;

import org.kostiskag.unitynetwork.tracker.rundata.address.NetworkAddress;
import org.kostiskag.unitynetwork.tracker.rundata.address.PhysicalAddress;
import org.kostiskag.unitynetwork.tracker.rundata.address.VirtualAddress;

public class NodeEntryTest {

	@BeforeClass
	public static void before() {

	}

	@Test
	public void testConstructor() throws UnknownHostException, IllegalAccessException {
		var n1 = new NodeEntry<PhysicalAddress>("ouiou", PhysicalAddress.valueOf("10.0.0.1"));
		var n2 = new NodeEntry<VirtualAddress>("ouiou", VirtualAddress.valueOf("10.0.0.1"));
	}

	@Test
	public void testBadInputConstructor()  {
		try {
			var n = new NodeEntry<NetworkAddress>("ouiou", new NetworkAddress("10.0.0.1"));
		} catch (UnknownHostException e) {
			assertTrue(false);
		} catch (IllegalAccessException e2) {
			assertTrue(true);
		}

		try {
			var n = new NodeEntry<NetworkAddress>("", PhysicalAddress.valueOf("10.0.0.1"));
		} catch (UnknownHostException e) {
			assertTrue(false);
		} catch (IllegalAccessException e) {
			assertTrue(true);
		}

		try {
			var n = new NodeEntry<NetworkAddress>(null, PhysicalAddress.valueOf("10.0.0.1"));
		} catch (UnknownHostException e) {
			assertTrue(false);
		} catch (IllegalAccessException e) {
			assertTrue(true);
		}

		try {
			var n = new NodeEntry<NetworkAddress>("pakis", null);
		} catch (IllegalAccessException e) {
			assertTrue(true);
		}

		try {
			var n = new NodeEntry<NetworkAddress>("pakis",  PhysicalAddress.valueOf("aaaa!"));
		} catch (UnknownHostException e) {
			//unknown host triggers first
			assertTrue(true);
		} catch (IllegalAccessException e) {
			assertTrue(false);
		}
	}

	@Test
	public void getAddressTest() throws UnknownHostException, IllegalAccessException {
		var n1 = new NodeEntry<VirtualAddress>("ouiou", VirtualAddress.valueOf("10.1.0.1"));
		assertEquals(new NetworkAddress("10.1.0.1"),  n1.getAddress());
		assertNotEquals(new NetworkAddress("10.0.0.1"),  n1.getAddress());

		var n2 = new NodeEntry<PhysicalAddress>("ouiou", PhysicalAddress.valueOf("10.1.0.1"));
		assertNotEquals(new NetworkAddress("10.0.0.1"),  n2.getAddress());
		assertEquals(new NetworkAddress("10.1.0.1"),  n2.getAddress());
	}

	@Test
	public void getHostnameTest() throws UnknownHostException, IllegalAccessException {
		var n1 = new NodeEntry<VirtualAddress>("ouiou", VirtualAddress.valueOf("10.1.0.1"));
		assertEquals("ouiou",  n1.getHostname());

		var n2 = new NodeEntry<PhysicalAddress>("ouiou", PhysicalAddress.valueOf("10.1.0.1"));
		assertEquals("ouiou",  n2.getHostname());
	}

	@Test
	public void testUpdateTimestamp() throws IllegalAccessException, InterruptedException, UnknownHostException {
		Time t = new Time(System.currentTimeMillis());
		Thread.sleep(1000);
		NodeEntry<VirtualAddress> n = new NodeEntry<>("ouiou", VirtualAddress.valueOf("10.0.0.1"));
		Time oldt = n.getTimestamp();
		Thread.sleep(1000);
		n.updateTimestamp();

		assertTrue(n.getTimestamp().getTime() - t.getTime() > 0);
		assertTrue(n.getTimestamp().getTime() - oldt.getTime() > 0);
		assertTrue(oldt.getTime() - t.getTime() > 0);
	}

}
