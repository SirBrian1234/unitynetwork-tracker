package org.kostiskag.unitynetwork.tracker.rundata.address;

import java.net.UnknownHostException;

/**
 *
 * @author Konstantinos Kagiampakis
 */
public final class PhysicalAddress extends NetworkAddress {

    private PhysicalAddress(String asString) throws UnknownHostException {
        super(asString);
    }

    public static PhysicalAddress valueOf(String address) throws UnknownHostException {
        return new PhysicalAddress(address);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PhysicalAddress)) return false;
        PhysicalAddress ph = (PhysicalAddress) o;
        return super.equals(o);
    }
}
