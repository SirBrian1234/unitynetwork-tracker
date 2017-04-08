package kostiskag.unitynetwork.tracker.runData;


import static org.junit.Assert.assertTrue;

import java.util.LinkedList;

import static org.junit.Assert.assertEquals;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.junit.FixMethodOrder;

public class BlueNodeEntryTest {
	@BeforeClass
	public static void linkedTest() {
		//counting starts from zero - an id may never exceed size - no excuses here!!!
		String hostname = "ouiou";
		String address = "10.0.0.1";
		int port = 4440;
		BlueNodeEntry bn = new BlueNodeEntry(hostname, address, port);
		LinkedList<BlueNodeEntry> li= new LinkedList<>();
		li.add(bn);
		System.out.println("size "+li.size());
		System.out.println("counting "+li.get(0).getName());
	}
	
	@Test
	public void testInitBlueNodeEntry() {
		String hostname = "ouiou";
		String address = "10.0.0.1";
		int port = 4440;
		BlueNodeEntry bn = new BlueNodeEntry(hostname, address, port);
		assertTrue(bn.getName().equals(hostname));
	}
	
	@Test
	public void testInit2BlueNodeEntry() {
		String hostname = "ouiou";
		String address = "10.0.0.1";
		int port = 4440;
		BlueNodeEntry bn = new BlueNodeEntry(hostname, address, port);
		assertTrue(bn.getPhaddress().equals(address));
	}
	
	@Test
	public void testInit3BlueNodeEntry() {
		String hostname = "ouiou";
		String address = "10.0.0.1";
		int port = 4440;
		BlueNodeEntry bn = new BlueNodeEntry(hostname, address, port);
		assertTrue(bn.getPort() == port);
	}
	
	@Test
	public void testBnTable() {
		BlueNodeTable bnt = new BlueNodeTable();
		System.out.println(bnt.getSize());
		String hostname = "ouiou";
		String address = "10.0.0.1";
		int port = 4440;
		BlueNodeEntry bn = new BlueNodeEntry(hostname, address, port);
	}
}
