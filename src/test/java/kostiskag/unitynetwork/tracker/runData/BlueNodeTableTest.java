package kostiskag.unitynetwork.tracker.runData;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.sql.Time;
import java.util.LinkedList;
import org.junit.BeforeClass;
import org.junit.Test;
import kostiskag.unitynetwork.tracker.App;

public class BlueNodeTableTest {
	@Test
	public void initTest(){
		App.gui = false;
		BlueNodeTable bns = new BlueNodeTable();
		assertEquals(bns.getSize(), 0);
		try {
			bns.lease("pakis", "192.168.1.1", 1234);
			bns.lease("pakis2", "192.168.1.2", 1234);
		} catch (Exception e) {
			e.printStackTrace();
		}		
		assertEquals(bns.getSize(), 2);
	}
	
	@Test
	public void uniqueHnTest(){
		App.gui = false;
		BlueNodeTable bns = new BlueNodeTable();
		assertEquals(bns.getSize(), 0);
		try {
			bns.lease("pakis", "192.168.1.1", 1234);
			bns.lease("pakis", "192.168.1.2", 1234);
		} catch (Exception e) {
			assertEquals(bns.getSize(), 1);
			return;
		}		
		assertTrue(false);
	}
	
	@Test
	public void uniqueAddrTest(){
		App.gui = false;
		BlueNodeTable bns = new BlueNodeTable();
		assertEquals(bns.getSize(), 0);
		try {
			bns.lease("pakis", "192.168.1.1", 1234);
			bns.lease("pakis2", "192.168.1.1", 1234);
		} catch (Exception e) {
			assertEquals(bns.getSize(), 1);
			return;
		}		
		assertTrue(false);
	}
	
	@Test
	public void loadTest(){
		App.gui = false;
		BlueNodeTable bns = new BlueNodeTable();
		assertEquals(bns.getSize(), 0);
		try {
			bns.lease("pakis", "192.168.1.1", 1234);
			bns.lease("pakis2", "192.168.1.2", 1234);
			bns.lease("pakis3", "192.168.1.2", 1235);
			bns.lease("pakis4", "192.168.1.4", 1234);
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
