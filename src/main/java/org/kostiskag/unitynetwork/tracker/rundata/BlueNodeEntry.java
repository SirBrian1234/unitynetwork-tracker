package org.kostiskag.unitynetwork.tracker.rundata;

import java.security.PublicKey;
import java.sql.Time;

/**
 * Each connected bluenode is represented by a BlueNode entry!
 * 
 * @author Konstantinos Kagiampakis
 */
public class BlueNodeEntry {
    
    private final String name;
    private final String Phaddress;
    private final int port;
    private final PublicKey pub;
    private final RedNodeTable rednodes;
    private Time regTimestamp;
    private final Object timeLock = new Object();

    public BlueNodeEntry(String name, PublicKey pub, String phAddress, int port, Time regTimestamp) {
        this.name = name;
        this.pub = pub;
        this.Phaddress = phAddress;
        this.port = port;
        this.regTimestamp = regTimestamp;
        this.rednodes = new RedNodeTable(this);
    }
    
    //auto timestamp
    public BlueNodeEntry(String name, PublicKey pub, String phAddress, int port) {
        this(name, pub, phAddress, port,new Time(System.currentTimeMillis()));
    }

    public String getName() {
        return name;
    }

    public PublicKey getPub() {
		return pub;
	}
    
    public String getPhaddress() {
        return Phaddress;
    }

    public int getPort() {
        return port;
    }
    
    public int getLoad() {
    	return rednodes.getSize();
    }
    
    public RedNodeTable getRedNodes() {
    	return rednodes;
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
     * there can only be one bluenode with the same name and the same key
     * according to the network's limitations!
     * Consequently, name OR key OR pair of port:address
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof BlueNodeEntry) {
            BlueNodeEntry given = (BlueNodeEntry) obj;
            return given.getName().equals(name);
        }
        return false;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": name: " + name + " phaddress: " + Phaddress + " port: " + port+ " \nTime "+getTimestamp().getTime();
    }
}
