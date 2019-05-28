package org.kostiskag.unitynetwork.tracker.rundata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.security.PublicKey;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kostiskag.unitynetwork.tracker.App;
import org.kostiskag.unitynetwork.tracker.AppLogger;
import org.kostiskag.unitynetwork.tracker.database.Database;
import org.kostiskag.unitynetwork.tracker.database.Queries;
import org.kostiskag.unitynetwork.tracker.functions.CryptoMethods;

public class BlueNodeTableTest {

	@BeforeClass
	public static void beforeClass() {
		System.out.println("Before");
		AppLogger.newInstance(null,null);
		File file = new File("bn_test.db");
    	if (file.exists()) {
    		file.delete();
    	}
    	try {
			Database db = Database.newInstance("jdbc:sqlite:bn_test.db","","");
			db.connect();
			Queries.validateDatabase(db);
			Queries q = new Queries();
			q.insertEntryUsers("Pakis", "1234", 2, "Dr. Pakis");
			ResultSet r = q.selectAllFromUsers();
			int id = 0;
			while(r.next()) {
				id = r.getInt("id");
			}
			q.insertEntryBluenodes("pakis1", id,"");
			q.insertEntryBluenodes("pakis2", id,"");
			q.insertEntryBluenodes("pakis3", id,"");
			q.insertEntryBluenodes("pakis4", id,"");
		    //q.closeQueries();
    	} catch (SQLException e) {
			e.printStackTrace();
			assertTrue(false);
		}
    }
	
	@AfterClass
	public static void afterClass() {
		File file = new File("bn_test.db");
    	if (file.exists()) {
    		file.delete();
    	}
	}
	
	@Before
	public void before() {

	}
	
	@Test
	public void initTest(){
		BlueNodeTable bns = new BlueNodeTable(0);
		assertEquals(bns.getSize(), 0);
		try {
			PublicKey pub = CryptoMethods.generateRSAkeyPair().getPublic();
			bns.lease("pakis", pub, "192.168.1.1", 1234);
			bns.lease("pakis2", pub, "192.168.1.2", 1234);
		} catch (Exception e) {
			e.printStackTrace();
		}		
		assertEquals(bns.getSize(), 2);
	}
	
	@Test
	public void maxCapacityTest(){
		System.out.println("cap test");
		BlueNodeTable bns = new BlueNodeTable(2);
		assertEquals(bns.getSize(), 0);
		PublicKey pub = CryptoMethods.generateRSAkeyPair().getPublic();
		try {
			bns.lease("pakis", pub, "192.168.1.1", 1234);
			bns.lease("pakis2", pub, "192.168.1.2", 1234);
			bns.lease("pakis3", pub, "192.168.1.3", 1234);
			bns.lease("pakis4", pub, "192.168.1.4", 1234);
		} catch (Exception e) {
			e.printStackTrace();
		}		
		System.out.println(bns.getSize());
		assertEquals(bns.getSize(), 2);
	}
	
	@Test
	public void uniqueHnTest(){
		BlueNodeTable bns = new BlueNodeTable(0);
		assertEquals(bns.getSize(), 0);
		PublicKey pub = CryptoMethods.generateRSAkeyPair().getPublic();
		try {
			bns.lease("pakis", pub, "192.168.1.1", 1234);
			bns.lease("pakis", pub, "192.168.1.2", 1234);
		} catch (Exception e) {
			assertEquals(bns.getSize(), 1);
			return;
		}		
		assertTrue(false);
	}
	
	@Test
	public void uniqueAddrTest(){
		BlueNodeTable bns = new BlueNodeTable(0);
		assertEquals(bns.getSize(), 0);
		PublicKey pub = CryptoMethods.generateRSAkeyPair().getPublic();
		try {
			bns.lease("pakis", pub, "192.168.1.1", 1234);
			bns.lease("pakis2", pub, "192.168.1.1", 1234);
		} catch (Exception e) {
			assertEquals(bns.getSize(), 1);
			return;
		}		
		assertTrue(false);
	}
	
	//@Test
	public void leaseRedNodeTest(){
		BlueNodeTable bns = new BlueNodeTable(0);
		assertEquals(bns.getSize(), 0);
		PublicKey pub = CryptoMethods.generateRSAkeyPair().getPublic();
		try {
			bns.lease("pakis", pub, "192.168.1.1", 1234);
			bns.lease("pakis2", pub, "192.168.1.2", 1234);
			bns.lease("pakis3", pub, "192.168.1.2", 1235);
			bns.lease("pakis4", pub, "192.168.1.4", 1234);
		} catch (Exception e) {
			e.printStackTrace();
		}		
		assertEquals(bns.getSize(), 4);
		
		try {
			bns.leaseRednode("pakis", "lakis", "10.0.0.1");
			bns.leaseRednode("pakis", "lakis2", "10.0.0.2");
			bns.leaseRednode("pakis", "lakis3", "10.0.0.3");
			bns.leaseRednode("pakis", "lakis4", "10.0.0.4");
			
			bns.leaseRednode("pakis2", "lakis5", "10.0.0.5");
			bns.leaseRednode("pakis2", "lakis6", "10.0.0.6");
			
			bns.leaseRednode("pakis3", "lakis7", "10.0.0.7");
			bns.leaseRednode("pakis3", "lakis8", "10.0.0.8");
			bns.leaseRednode("pakis3", "lakis9", "10.0.0.9");
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		assertEquals(bns.getBlueNodeEntryByHn("pakis").getLoad(), 4);
		assertEquals(bns.getBlueNodeEntryByHn("pakis2").getLoad(), 2);
		assertEquals(bns.getBlueNodeEntryByHn("pakis3").getLoad(), 3);
		assertEquals(bns.getBlueNodeEntryByHn("pakis4").getLoad(), 0);
		assertEquals(bns.getBlueNodeEntryByLowestLoad().getName(), "pakis4");
	}

}
