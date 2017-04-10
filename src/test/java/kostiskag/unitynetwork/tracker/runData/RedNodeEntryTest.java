package kostiskag.unitynetwork.tracker.runData;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.sql.Time;
import org.junit.BeforeClass;
import org.junit.Test;

public class RedNodeEntryTest {
	@BeforeClass
	public static void linkedTest() {
		
	}
	
	@Test
	public void testRedNodeEntryHostname() {
		RedNodeEntry rn = new RedNodeEntry(null, "ouiou", "10.0.0.1");
		assertEquals(rn.getHostname(), "ouiou");		
	}
	
	@Test
	public void testRedNodeEntryVaddr() {
		RedNodeEntry rn = new RedNodeEntry(null, "ouiou", "10.0.0.1");
		assertEquals(rn.getVaddress(), "10.0.0.1");		
	}
	
	@Test
	public void testRedNodeTime() {
		RedNodeEntry rn = new RedNodeEntry(null, "ouiou", "10.0.0.1");
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
			assertTrue(false);	
		}
		if (rn.getTimestamp().equals(new Time(System.currentTimeMillis()))) {
			assertTrue(false);		
		} else {
			assertTrue(true);
		}
	}
}
