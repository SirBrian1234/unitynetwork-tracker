package org.kostiskag.unitynetwork.tracker.rundata;

import java.sql.Time;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kostiskag.unitynetwork.tracker.AppLogger;

import static org.junit.Assert.*;

public class RedNodeEntryTest {
	@BeforeClass
	public static void before() {
		AppLogger.newInstance(null,null);
	}

	@Test
	public void constructorAccessorsTest() {
		BlueNodeEntry bn = new BlueNodeEntry("pakis",null,null,0);
		RedNodeEntry rn = new RedNodeEntry(bn, "ouiou", "10.0.0.1");
		assertEquals(rn.getHostname(), "ouiou");
		assertEquals(rn.getVaddress(), "10.0.0.1");
		assertEquals(bn, rn.getParentBlueNode());
		System.out.println(rn);
	}

	@Test
	public void testUpdateTimestamp() throws InterruptedException {
		Time t = new Time(System.currentTimeMillis());
		Thread.sleep(1000);
		BlueNodeEntry bn = new BlueNodeEntry("pakis",null,null,0);
		RedNodeEntry rn = new RedNodeEntry(bn, "ouiou", "10.0.0.1");
		System.out.println(bn);
		Time oldt = rn.getTimestamp();
		Thread.sleep(1000);
		rn.updateTimestamp();

		assertTrue(rn.getTimestamp().getTime() - t.getTime() > 0);
		assertTrue(rn.getTimestamp().getTime() - oldt.getTime() > 0);
		assertTrue(oldt.getTime() - t.getTime() > 0);
	}

	@Test
	public void testEquality() throws InterruptedException {
		BlueNodeEntry bn = new BlueNodeEntry("pakis",null,null,0);
		RedNodeEntry rn = new RedNodeEntry(bn, "ouiou", "10.0.0.1");
		RedNodeEntry rn2 = new RedNodeEntry(null, "ouiou", "10.0.0.2");
		RedNodeEntry rn3 = new RedNodeEntry(null, "ouiou", "10.0.0.3");

		RedNodeEntry rn4 = new RedNodeEntry(null, "pakis", "10.0.0.3");
		RedNodeEntry rn5 = new RedNodeEntry(null, "lakis", "10.0.0.3");


		assertEquals(rn, rn);

		assertEquals(rn, rn2);
		assertEquals(rn2, rn);

		assertEquals(rn3, rn);
		assertEquals(rn3, rn2);

		assertNotEquals(rn, rn4);
		assertNotEquals(rn4, rn);

		assertNotEquals(rn, rn5);
		assertNotEquals(rn5, rn4);
	}
}
