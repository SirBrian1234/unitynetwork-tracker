package org.kostiskag.unitynetwork.tracker.rundata;

import java.net.UnknownHostException;
import java.sql.Time;
import java.util.Objects;

import org.kostiskag.unitynetwork.tracker.address.VirtualAddress;

/**
 * Objects of a RedNodeEntry represent the connected
 * rednodes on the network
 *
 * For safety reasons, this class should not be extended!
 *
 * @author Konstantinos Kagiampakis
 */
public class RedNodeEntry {

    private final BlueNodeEntry bn;
    private final String hostname;
    private final VirtualAddress vAddress;
    private final Object timeLock = new Object();
    private Time regTimestamp;

    public RedNodeEntry(BlueNodeEntry bn, String hostname, VirtualAddress vAddress) {
        this.bn = bn;
        this.hostname = hostname;
        this.vAddress  = vAddress;
        this.regTimestamp = new Time(System.currentTimeMillis());
    }

    public RedNodeEntry(BlueNodeEntry bn, String hostname, String vAddress) throws UnknownHostException {
        this(bn, hostname, VirtualAddress.valueOf(vAddress));
    }

    public String getHostname() {
        return hostname;
    }

    public VirtualAddress getVaddress() {
        return vAddress;
    }

    public BlueNodeEntry getParentBlueNode() {
        return bn;
    }

    public Time getTimestamp() {
    	synchronized (timeLock) {
    		return regTimestamp;
		}        
    }
       
    public void updateTimestamp(){
    	synchronized (timeLock) {
    		this.regTimestamp = new Time(System.currentTimeMillis());
    	}
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
            return hostname.equals(given.hostname) || vAddress.equals(given.vAddress);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bn, hostname, vAddress);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+": hostname: "+hostname+" vaddress: "+vAddress;
    }
}
