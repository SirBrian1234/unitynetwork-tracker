package org.kostiskag.unitynetwork.tracker.rundata.address;

import org.junit.Assert.*;
import org.junit.Test;
import org.kostiskag.unitynetwork.common.address.NetworkAddress;
import org.kostiskag.unitynetwork.common.address.PhysicalAddress;
import org.kostiskag.unitynetwork.common.address.VirtualAddress;

import java.net.UnknownHostException;

import static org.junit.Assert.*;

public class PhysicalAddressTest {

    @Test
    public void equalityTest() throws UnknownHostException {
        NetworkAddress n = new NetworkAddress("10.0.0.1");
        PhysicalAddress ph = PhysicalAddress.valueOf("10.0.0.1");
        VirtualAddress v = VirtualAddress.valueOf("10.0.0.1");

        assertTrue(ph.equals(PhysicalAddress.valueOf("10.0.0.1")));
        assertFalse(ph.equals(n));
        assertFalse(ph.equals(v));
    }
}
