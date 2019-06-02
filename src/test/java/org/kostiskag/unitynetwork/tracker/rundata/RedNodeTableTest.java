package org.kostiskag.unitynetwork.tracker.rundata;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.net.UnknownHostException;
import java.security.PublicKey;

import org.kostiskag.unitynetwork.tracker.AppLogger;
import org.kostiskag.unitynetwork.tracker.functions.CryptoMethods;
import org.kostiskag.unitynetwork.tracker.address.VirtualAddress;

import static org.junit.Assert.*;

public class RedNodeTableTest {

    @BeforeClass
    public static void init() {
        AppLogger.newInstance(null,null);
    }

	@Test
	public void initTest() {
		BlueNodeEntry bn = null;
		RedNodeTable rns = new RedNodeTable(bn);
		try {
			rns.lease("pakis", "10.200.1.1");
			rns.lease("pakis2", "10.200.1.2");
			rns.lease("pakis3", "10.200.1.3");
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
			return;
		}
		assertTrue(rns.getSize() == 3);
	}
	
	@Test
	public void uniqueHosnameTest() {
		PublicKey pub = CryptoMethods.generateRSAkeyPair().getPublic();
		BlueNodeEntry bn = new BlueNodeEntry("pakis", pub, "10.200.1.1", 33);
		RedNodeTable rns = new RedNodeTable(bn);
		try {
			rns.lease("pakis", "10.200.1.1");
			rns.lease("pakis", "10.200.1.2");
		} catch (Exception e) {
			assertTrue(true);
			return;
		}
		assertTrue(false);
	}
	
	@Test
	public void uniqueAddressTest() {
		PublicKey pub = CryptoMethods.generateRSAkeyPair().getPublic();
		BlueNodeEntry bn = new BlueNodeEntry("pakis", pub, "10.200.1.1", 33);
		RedNodeTable rns = new RedNodeTable(bn);
		try {
			rns.lease("pakis", "10.200.1.1");
			rns.lease("makis", "10.200.1.1");
		} catch (Exception e) {
			assertEquals(rns.getSize(), 1);
			return;
		}
		assertTrue(false);
	}
	
	@Test
	public void getByHostnameTest() throws UnknownHostException, RedNodeTableException {
		BlueNodeEntry bn = null;
		RedNodeTable rns = new RedNodeTable(bn);
		try {
			rns.lease("pakis", "10.200.1.1");
			rns.lease("pakis2", "10.200.1.2");
			rns.lease("pakis3", "10.200.1.3");
			rns.lease("pakis4", "10.200.1.4");
			rns.lease("pakis5", "10.200.1.5");
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		assertEquals(rns.getRedNodeEntry("pakis3").getHostname(),"pakis3");
		assertEquals(rns.getRedNodeEntry("pakis3").getVaddress().asString(),"10.200.1.3");
		assertEquals(rns.getRedNodeEntry("pakis3").getVaddress(),VirtualAddress.valueOf("10.200.1.3"));

		try {
			rns.getRedNodeEntry("pakis15");
		} catch (RedNodeTableException ex) {
			assertTrue(true);
		}
	}
	
	@Test
	public void getByVAddressTest() throws UnknownHostException, RedNodeTableException {
		BlueNodeEntry bn = null;
		RedNodeTable rns = new RedNodeTable(bn);
		try {
			rns.lease("pakis", "10.200.1.1");
			rns.lease("pakis2", "10.200.1.2");
			rns.lease("pakis3", "10.200.1.3");
			rns.lease("pakis4", "10.200.1.4");
			rns.lease("pakis5", "10.200.1.5");
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		assertNotNull(rns.getRedNodeEntryByVAddr("10.200.1.2").getHostname().equals("pakis2"));
		assertTrue(rns.getRedNodeEntryByVAddr("10.200.1.2").getVaddress().asString().equals("10.200.1.2"));
	}
	
	@Test
	public void getStringListTest() {
		BlueNodeEntry bn = null;
		RedNodeTable rns = new RedNodeTable(bn);
		try {
			rns.lease("pakis", "10.200.1.1");
			rns.lease("pakis2", "10.200.1.2");
			rns.lease("pakis3", "10.200.1.3");
			rns.lease("pakis4", "10.200.1.4");
			rns.lease("pakis5", "10.200.1.5");
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		List<String> list = rns.getLeasedRedNodeHostnameList();
		assertTrue(list.size() == 5);
		assertTrue(list.get(0).equals("pakis"));
		assertTrue(list.get(1).equals("pakis2"));
		assertTrue(list.get(2).equals("pakis3"));
		assertTrue(list.get(3).equals("pakis4"));
		assertTrue(list.get(4).equals("pakis5"));
	}
	
	@Test
	public void checkOnlineByHnTest() {
		BlueNodeEntry bn = null;
		RedNodeTable rns = new RedNodeTable(bn);
		try {
			rns.lease("pakis", "10.200.1.1");
			rns.lease("pakis2", "10.200.1.2");
			rns.lease("pakis3", "10.200.1.3");
			rns.lease("pakis4", "10.200.1.4");
			rns.lease("pakis5", "10.200.1.5");
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		assertTrue(rns.isOnline("pakis"));
		assertTrue(rns.isOnline("pakis3"));
		assertTrue(rns.isOnline("pakis4"));
		assertTrue(rns.isOnline("pakis2"));
		assertTrue(rns.isOnline("pakis5"));
		assertTrue(!rns.isOnline("bob"));
	}
	
	@Test
	public void checkOnlineByVaddressTest() throws UnknownHostException {
		BlueNodeEntry bn = null;
		RedNodeTable rns = new RedNodeTable(bn);
		try {
			rns.lease("pakis", "10.200.1.1");
			rns.lease("pakis2", "10.200.1.2");
			rns.lease("pakis3", "10.200.1.3");
			rns.lease("pakis4", "10.200.1.4");
			rns.lease("pakis5", "10.200.1.5");
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		assertTrue(rns.isOnlineByVaddress("10.200.1.3"));
		assertTrue(rns.isOnlineByVaddress("10.200.1.1"));
		assertTrue(rns.isOnlineByVaddress("10.200.1.2"));
		assertTrue(rns.isOnlineByVaddress("10.200.1.5"));
		assertTrue(rns.isOnlineByVaddress("10.200.1.4"));
		assertTrue(!rns.isOnlineByVaddress("10.200.1.20"));
	}
	
	@Test
	public void releaseByHostnameTest() throws UnknownHostException, RedNodeTableException {
		BlueNodeEntry bn = null;
		RedNodeTable rns = new RedNodeTable(bn);
		try {
			rns.lease("pakis", "10.200.1.1");
			rns.lease("pakis2", "10.200.1.2");
			rns.lease("pakis3", "10.200.1.3");
			rns.lease("pakis4", "10.200.1.4");
			rns.lease("pakis5", "10.200.1.5");
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		rns.release("pakis");
		rns.release("pakis3");
		rns.release("pakis5");
		
		assertTrue(!rns.isOnlineByVaddress("10.200.1.1"));
		assertTrue(!rns.isOnlineByVaddress("10.200.1.3"));
		assertTrue(!rns.isOnlineByVaddress("10.200.1.5"));
		assertTrue(rns.isOnlineByVaddress("10.200.1.2"));
		assertTrue(rns.isOnlineByVaddress("10.200.1.4"));
		assertEquals(rns.getSize(), 2);
	}
	
	@Test
	public void releaseByVaddressTest() throws UnknownHostException, RedNodeTableException {
		BlueNodeEntry bn = new BlueNodeEntry("bnpakis",null,null, 0);
		RedNodeTable rns = new RedNodeTable(bn);
		try {
			rns.lease("pakis", "10.200.1.1");
			rns.lease("pakis2", "10.200.1.2");
			rns.lease("pakis3", "10.200.1.3");
			rns.lease("pakis4", "10.200.1.4");
			rns.lease("pakis5", "10.200.1.5");
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}

		assertEquals(rns.getSize(), 5);


		rns.getRedNodeEntryByVAddr("10.200.1.2");
		rns.getRedNodeEntryByVAddr("10.200.1.4");

		//these two are released
		rns.releaseByVAddress("10.200.1.2");
		rns.releaseByVAddress("10.200.1.4");
		//they should not be found
		assertTrue(!rns.isOnline("pakis2"));
		assertTrue(!rns.isOnline("pakis4"));

		//these should be there
		assertTrue(rns.isOnline("pakis3"));
		assertTrue(rns.isOnline("pakis"));
		assertTrue(rns.isOnline("pakis5"));
		assertEquals(rns.getSize(), 3);
	}

}
