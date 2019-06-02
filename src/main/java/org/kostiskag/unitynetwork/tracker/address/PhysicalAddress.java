package org.kostiskag.unitynetwork.tracker.address;

import java.net.UnknownHostException;

/**
 *
 * @author Konstantinos Kagiampakis
 */
public class PhysicalAddress extends NetworkAddress {

    private PhysicalAddress(String asString) throws UnknownHostException {
        super(asString);
    }

    public static PhysicalAddress valueOf(String address) throws UnknownHostException {
        return new PhysicalAddress(address);
    }
}
