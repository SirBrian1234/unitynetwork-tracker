package org.kostiskag.unitynetwork.tracker.rundata;

import org.kostiskag.unitynetwork.tracker.address.PhysicalAddress;
import org.kostiskag.unitynetwork.tracker.service.sonar.BlueNodeClient;

import java.net.UnknownHostException;
import java.security.PublicKey;
import java.sql.Time;
import java.util.Objects;

/**
 * Each connected bluenode is represented by a BlueNode entry!
 * 
 * @author Konstantinos Kagiampakis
 */
public class BlueNodeEntry {
    
    private final String name;
    private final PhysicalAddress phAddress;
    private final int port;
    private final PublicKey pub;
    private final RedNodeTable rednodes;
    private final Object timeLock = new Object();
    private Time regTimestamp;
    private BlueNodeClient client;

    public BlueNodeEntry(String name, PublicKey pub, PhysicalAddress phAddress, int port) {
        this.name = name;
        this.pub = pub;
        this.phAddress = phAddress;
        this.port = port;
        this.regTimestamp = new Time(System.currentTimeMillis());
        this.rednodes = new RedNodeTable(this);
    }

    public BlueNodeEntry(String name, PublicKey pub, String phAddress, int port) throws UnknownHostException {
        this(name,pub,PhysicalAddress.valueOf(phAddress),port);
    }

    public String getName() {
        return name;
    }

    public PublicKey getPub() {
		return pub;
	}
    
    public PhysicalAddress getPhAddress() {
        return phAddress;
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

    public BlueNodeClient getClient() {
        return client;
    }

    public void setClient(BlueNodeClient client) {
        this.client = client;
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
    public int hashCode() {
        return Objects.hash(name, phAddress, port, pub);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": name: " + name + " phaddress: " + phAddress + " port: " + port+ " \nTime "+getTimestamp().getTime();
    }
}
