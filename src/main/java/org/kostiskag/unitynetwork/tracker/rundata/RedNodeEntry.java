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

    public RedNodeEntry(BlueNodeEntry bn, String hostname, String Vaddress) {
        this.hostname = hostname;
        this.vAddress  = Vaddress;
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
     * due to unity restrictions each hostname may be given a unique ip,
     * therefore if the same hostname for a RN entry is found or
     * the same virtual address is found the objects are considered
     * as equal
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
        return this.getClass().getName()+": hostname: "+hostname+" vaddress: "+vAddress;
    }
}
