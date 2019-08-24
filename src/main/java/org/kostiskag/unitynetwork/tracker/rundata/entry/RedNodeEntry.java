package org.kostiskag.unitynetwork.tracker.rundata.entry;

import java.util.Objects;
import java.net.UnknownHostException;

import org.kostiskag.unitynetwork.common.address.VirtualAddress;
import org.kostiskag.unitynetwork.common.entry.NodeEntry;


/**
 * Objects of a RedNodeEntry represent the connected
 * rednodes on the network
 *
 * For safety reasons, this class should not be extended!
 *
 * @author Konstantinos Kagiampakis
 */
public class RedNodeEntry extends NodeEntry<VirtualAddress>{

    private final BlueNodeEntry bn;

    public RedNodeEntry(BlueNodeEntry bn, String hostname, VirtualAddress vAddress) throws IllegalAccessException {
        super(hostname,vAddress);
        this.bn = bn;
    }

    public RedNodeEntry(BlueNodeEntry bn, String hostname, String vAddress) throws UnknownHostException, IllegalAccessException {
        this(bn, hostname, VirtualAddress.valueOf(vAddress));
    }

    public BlueNodeEntry getParentBlueNode() {
        return bn;
    }

    /**
     * due to unity restrictions the Transitive property of equality for this object is broken!
     * as each hostname may be given only a unique ip,
     * this method is used to proofread the RedNodeTable for a new element's entry
     * one obj is found as equal to another if their hostnames match OR if their addresses match
     * this means that obj
     * "hostnameA" "10.0.1.1" is equal to "hostnameB" "10.0.0.15" as of same hostname
     * "hostnameC" "10.0.1.1" is equal to "hostnameA" "10.0.1.1" as of same vaddress
     * BUT "hostnameC" "10.0.1.1" is NOT equal to "hostnameB" "10.0.0.15"
     * be cautious when using it!
     * It is meant to compare the elements from inside the table
     * to an entering element from the outside of table!
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof RedNodeEntry) {
            RedNodeEntry given = (RedNodeEntry) obj;
            return this.getHostname().equals(given.getHostname()) || this.getAddress().equals(given.getAddress());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bn, this.getHostname(), this.getAddress());
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+": hostname: "+this.getHostname()+" vaddress: "+this.getAddress();
    }
}
