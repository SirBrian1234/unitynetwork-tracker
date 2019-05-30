package org.kostiskag.unitynetwork.tracker.rundata;

import java.sql.Time;

/**
 * Objects of a RedNodeEntry represent the connected
 * rednodes on the network
 * 
 * @author Konstantinos Kagiampakis
 */
public class RedNodeEntry {

    private final BlueNodeEntry bn;
    private final String hostname;
    private final String vAddress;
    private Time regTimestamp;
    private final Object timeLock = new Object();

    public RedNodeEntry(BlueNodeEntry bn, String hostname, String vAddress) {
        this.hostname = hostname;
        this.vAddress  = vAddress;
        this.bn = bn;
        this.regTimestamp = new Time(System.currentTimeMillis());
    }

    public String getHostname() {
        return hostname;
    }

    public String getVaddress() {
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
     * due to unity restrictions each hostname may be given only a unique ip,
     * this method should not be used to proofread the RedNodeTable for
     * whether there is a duplicate vaddress it compares ONLY by rn hostname
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
    public String toString() {
        return this.getClass().getSimpleName()+": hostname: "+hostname+" vaddress: "+vAddress;
    }
}
