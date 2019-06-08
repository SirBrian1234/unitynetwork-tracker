package org.kostiskag.unitynetwork.tracker.rundata.address;

import org.junit.Assert;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.Assert.*;

public class NetworkAddressTest {

    @Test
    public void constructorA() throws UnknownHostException {
        NetworkAddress n = new NetworkAddress("10.0.0.1");
        Assert.assertEquals(n.asString(), "10.0.0.1");
        Assert.assertArrayEquals(n.asByte(),new byte[] {10,0,0,1});
        Assert.assertEquals(n.asInet(), InetAddress.getByName("10.0.0.1"));
    }

    @Test
    public void constructorB() throws UnknownHostException {
        NetworkAddress n = new NetworkAddress(InetAddress.getByName("10.0.0.1"));
        Assert.assertEquals(n.asString(), "10.0.0.1");
        Assert.assertArrayEquals(n.asByte(),new byte[] {10,0,0,1});
        Assert.assertEquals(n.asInet(), InetAddress.getByName("10.0.0.1"));
    }

    @Test
    public void constructorC() throws UnknownHostException {
        NetworkAddress n = new NetworkAddress("10.0.0.1", new byte[] {10,0,0,1}, InetAddress.getByName("10.0.0.1"));
        Assert.assertEquals(n.asString(), "10.0.0.1");
        Assert.assertArrayEquals(n.asByte(),new byte[] {10,0,0,1});
        Assert.assertEquals(n.asInet(), InetAddress.getByName("10.0.0.1"));
        
        boolean triggered = false;
        try {
            NetworkAddress faulty = new NetworkAddress("10.1.0.1", new byte[]{10, 0, 0, 1}, InetAddress.getByName("10.0.0.1"));
        } catch (UnknownHostException e) {
            triggered = true;
        }
        assertTrue(triggered);
        triggered = false;
        try {
            NetworkAddress faulty = new NetworkAddress("10.0.0.1", new byte[]{10, 0, 1, 1}, InetAddress.getByName("10.0.0.1"));
        } catch (UnknownHostException e) {
            triggered = true;
        }
        assertTrue(triggered);
    }

    @Test
    public void equalityTest() throws UnknownHostException {
        NetworkAddress n1 = new NetworkAddress(InetAddress.getByName("10.0.0.1"));
        NetworkAddress n2 = new NetworkAddress(InetAddress.getByName("10.0.0.1"));

        assertEquals(n1, n2);
        assertEquals(n2, n1);

        NetworkAddress n3 = new NetworkAddress("10.1.0.1");

        assertNotEquals(n1, n3);
        assertNotEquals(n2, n3);
        assertEquals(n1, n2);
    }

    @Test
    public void sublclassEqualityTest() throws UnknownHostException {
        NetworkAddress n = new NetworkAddress("10.0.0.1");
        PhysicalAddress ph = PhysicalAddress.valueOf("10.0.0.1");
        VirtualAddress v = VirtualAddress.valueOf("10.0.0.1");

        assertTrue(n.equals(ph));
        assertFalse(ph.equals(v));
        assertFalse(v.equals(n));
    }

    @Test
    public void hashTest() throws UnknownHostException {
        NetworkAddress n1 = new NetworkAddress(InetAddress.getByName("10.0.0.1"));
        NetworkAddress n2 = new NetworkAddress(InetAddress.getByName("10.0.0.2"));

        assertEquals(n1.hashCode(), new NetworkAddress(InetAddress.getByName("10.0.0.1")).hashCode());
        assertNotEquals(n1.hashCode(), n2.hashCode());
    }
}
