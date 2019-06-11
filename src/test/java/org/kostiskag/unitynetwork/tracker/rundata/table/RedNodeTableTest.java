package org.kostiskag.unitynetwork.tracker.rundata.table;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.net.UnknownHostException;
import java.security.PublicKey;
import java.util.concurrent.locks.Lock;

import org.kostiskag.unitynetwork.tracker.AppLogger;
import org.kostiskag.unitynetwork.tracker.rundata.address.PhysicalAddress;
import org.kostiskag.unitynetwork.tracker.functions.CryptoMethods;
import org.kostiskag.unitynetwork.tracker.rundata.address.VirtualAddress;
import org.kostiskag.unitynetwork.tracker.rundata.entry.BlueNodeEntry;

import static org.junit.Assert.*;

public class RedNodeTableTest {

    @BeforeClass
    public static void init() {
        AppLogger.newInstance(null,null);
    }

	@Test
	public void initTest() throws UnknownHostException {
		BlueNodeEntry bn = null;
		RedNodeTable rns = new RedNodeTable(bn);
		try {
			Lock lock = rns.aquireLock();
			rns.lease(lock,"pakis", "10.200.1.1");
			rns.lease(lock, "pakis2", "10.200.1.2");
			rns.lease(lock, "pakis3", "10.200.1.3");
			assertTrue(rns.getSize(lock) == 3);
		} catch (IllegalAccessException | InterruptedException e) {
			assertTrue(false);
		} finally {
			rns.releaseLock();
		}
	}
	
	@Test
	public void uniqueHosnameTest() throws IllegalAccessException, UnknownHostException {
		PublicKey pub = CryptoMethods.generateRSAkeyPair().getPublic();
		BlueNodeEntry bn = new BlueNodeEntry("pakis", pub, "10.200.1.1", 33);
		RedNodeTable rns = new RedNodeTable(bn);
		try {
			Lock lock = rns.aquireLock();
			rns.lease(lock, "pakis", "10.200.1.1");
			rns.lease(lock, "pakis", "10.200.1.2");
		} catch (Exception e) {
			assertTrue(true);
			return;
		}
		assertTrue(false);
	}
	
	@Test
	public void uniqueAddressTest() throws IllegalAccessException, UnknownHostException, InterruptedException {
		PublicKey pub = CryptoMethods.generateRSAkeyPair().getPublic();
		BlueNodeEntry bn = new BlueNodeEntry("pakis", pub, "10.200.1.1", 33);
		RedNodeTable rns = new RedNodeTable(bn);
		Lock lock = rns.aquireLock();

		try {
			rns.lease(lock, "pakis", "10.200.1.1");
			rns.lease(lock, "makis", "10.200.1.1");
		} catch (Exception e) {
			assertEquals(rns.getSize(lock), 1);
			return;
		}
		assertTrue(false);
	}
	
	@Test
	public void getByHostnameTest() throws UnknownHostException, InterruptedException, IllegalAccessException {
		BlueNodeEntry bn = null;
		RedNodeTable rns = new RedNodeTable(bn);
		Lock lock = rns.aquireLock();

		try {
			rns.lease(lock, "pakis", "10.200.1.1");
			rns.lease(lock, "pakis2", "10.200.1.2");
			rns.lease(lock, "pakis3", "10.200.1.3");
			rns.lease(lock, "pakis4", "10.200.1.4");
			rns.lease(lock, "pakis5", "10.200.1.5");
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		assertEquals(rns.getNodeEntry(lock, "pakis3").getHostname(),"pakis3");
		assertEquals(rns.getNodeEntry(lock, "pakis3").getAddress().asString(),"10.200.1.3");
		assertEquals(rns.getNodeEntry(lock, "pakis3").getAddress(),VirtualAddress.valueOf("10.200.1.3"));

		try {
			rns.getNodeEntry(lock, "pakis15");
		} catch (IllegalAccessException ex) {
			assertTrue(true);
		}
	}
	
	@Test
	public void getByVAddressTest() throws IllegalAccessException, UnknownHostException, InterruptedException {
		BlueNodeEntry bn = null;
		RedNodeTable rns = new RedNodeTable(bn);
		Lock lock = rns.aquireLock();

		try {
			rns.lease(lock, "pakis", "10.200.1.1");
			rns.lease(lock, "pakis2", "10.200.1.2");
			rns.lease(lock, "pakis3", "10.200.1.3");
			rns.lease(lock, "pakis4", "10.200.1.4");
			rns.lease(lock, "pakis5", "10.200.1.5");
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		rns.getRedNodeEntryByVAddr(lock, "10.200.1.2").getHostname().equals("pakis2");
		assertTrue(rns.getRedNodeEntryByVAddr(lock, "10.200.1.2").getAddress().asString().equals("10.200.1.2"));
	}
	
	@Test
	public void getStringListTest() throws InterruptedException {
		BlueNodeEntry bn = null;
		RedNodeTable rns = new RedNodeTable(bn);
		Lock lock = rns.aquireLock();

		try {
			rns.lease(lock, "pakis", "10.200.1.1");
			rns.lease(lock, "pakis2", "10.200.1.2");
			rns.lease(lock, "pakis3", "10.200.1.3");
			rns.lease(lock, "pakis4", "10.200.1.4");
			rns.lease(lock, "pakis5", "10.200.1.5");
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		List<String> list = rns.getLeasedRedNodeHostnameList(lock);
		assertTrue(list.size() == 5);
		assertTrue(list.get(0).equals("pakis"));
		assertTrue(list.get(1).equals("pakis2"));
		assertTrue(list.get(2).equals("pakis3"));
		assertTrue(list.get(3).equals("pakis4"));
		assertTrue(list.get(4).equals("pakis5"));
	}
	
	@Test
	public void checkOnlineByHnTest() throws InterruptedException {
		BlueNodeEntry bn = null;
		RedNodeTable rns = new RedNodeTable(bn);
		Lock lock = rns.aquireLock();

		try {

			rns.lease(lock, "pakis", "10.200.1.1");
			rns.lease(lock, "pakis2", "10.200.1.2");
			rns.lease(lock, "pakis3", "10.200.1.3");
			rns.lease(lock, "pakis4", "10.200.1.4");
			rns.lease(lock, "pakis5", "10.200.1.5");
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		assertTrue(rns.isOnline(lock, "pakis"));
		assertTrue(rns.isOnline(lock, "pakis3"));
		assertTrue(rns.isOnline(lock, "pakis4"));
		assertTrue(rns.isOnline(lock, "pakis2"));
		assertTrue(rns.isOnline(lock, "pakis5"));
		assertTrue(!rns.isOnline(lock, "bob"));
	}
	
	@Test
	public void checkOnlineByVaddressTest() throws UnknownHostException, InterruptedException {
		BlueNodeEntry bn = null;
		RedNodeTable rns = new RedNodeTable(bn);
		Lock lock = rns.aquireLock();

		try {
			rns.lease(lock, "pakis", "10.200.1.1");
			rns.lease(lock, "pakis2", "10.200.1.2");
			rns.lease(lock, "pakis3", "10.200.1.3");
			rns.lease(lock, "pakis4", "10.200.1.4");
			rns.lease(lock, "pakis5", "10.200.1.5");
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		assertTrue(rns.isOnlineByVaddress(lock, "10.200.1.3"));
		assertTrue(rns.isOnlineByVaddress(lock, "10.200.1.1"));
		assertTrue(rns.isOnlineByVaddress(lock, "10.200.1.2"));
		assertTrue(rns.isOnlineByVaddress(lock, "10.200.1.5"));
		assertTrue(rns.isOnlineByVaddress(lock, "10.200.1.4"));
		assertTrue(!rns.isOnlineByVaddress(lock, "10.200.1.20"));
	}
	
	@Test
	public void releaseByHostnameTest() throws UnknownHostException, InterruptedException, IllegalAccessException {
		BlueNodeEntry bn = null;
		RedNodeTable rns = new RedNodeTable(bn);
		Lock lock = rns.aquireLock();
		
		try {
			rns.lease(lock, "pakis", "10.200.1.1");
			rns.lease(lock, "pakis2", "10.200.1.2");
			rns.lease(lock, "pakis3", "10.200.1.3");
			rns.lease(lock, "pakis4", "10.200.1.4");
			rns.lease(lock, "pakis5", "10.200.1.5");
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		rns.release(lock, "pakis");
		rns.release(lock, "pakis3");
		rns.release(lock, "pakis5");
		
		assertTrue(!rns.isOnlineByVaddress(lock, "10.200.1.1"));
		assertTrue(!rns.isOnlineByVaddress(lock, "10.200.1.3"));
		assertTrue(!rns.isOnlineByVaddress(lock, "10.200.1.5"));
		assertTrue(rns.isOnlineByVaddress(lock, "10.200.1.2"));
		assertTrue(rns.isOnlineByVaddress(lock, "10.200.1.4"));
		assertEquals(rns.getSize(lock), 2);
	}
	
	@Test
	public void releaseByVaddressTest() throws IllegalAccessException, UnknownHostException, InterruptedException {
		PublicKey pub = CryptoMethods.generateRSAkeyPair().getPublic();
    	BlueNodeEntry bn = new BlueNodeEntry("bnpakis",pub, PhysicalAddress.valueOf("1.2.3.4"), 1000);
		RedNodeTable rns = new RedNodeTable(bn);
		Lock lock = rns.aquireLock();

		try {
			rns.lease(lock, "pakis", "10.200.1.1");
			rns.lease(lock, "pakis2", "10.200.1.2");
			rns.lease(lock, "pakis3", "10.200.1.3");
			rns.lease(lock, "pakis4", "10.200.1.4");
			rns.lease(lock, "pakis5", "10.200.1.5");
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}

		assertEquals(rns.getSize(lock), 5);


		rns.getRedNodeEntryByVAddr(lock, "10.200.1.2");
		rns.getRedNodeEntryByVAddr(lock, "10.200.1.4");

		//these two are released
		rns.releaseByVAddress(lock, "10.200.1.2");
		rns.releaseByVAddress(lock, "10.200.1.4");
		//they should not be found
		assertTrue(!rns.isOnline(lock, "pakis2"));
		assertTrue(!rns.isOnline(lock, "pakis4"));

		//these should be there
		assertTrue(rns.isOnline(lock, "pakis3"));
		assertTrue(rns.isOnline(lock, "pakis"));
		assertTrue(rns.isOnline(lock, "pakis5"));
		assertEquals(rns.getSize(lock), 3);
	}

}
