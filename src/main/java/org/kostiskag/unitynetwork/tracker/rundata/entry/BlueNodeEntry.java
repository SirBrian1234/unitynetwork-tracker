package org.kostiskag.unitynetwork.tracker.rundata.entry;

import java.net.UnknownHostException;
import java.security.PublicKey;
import java.util.Objects;
import java.util.concurrent.locks.Lock;

import org.kostiskag.unitynetwork.common.address.PhysicalAddress;
import org.kostiskag.unitynetwork.common.entry.NodeEntry;
import org.kostiskag.unitynetwork.common.calculated.NumericConstraints;

import org.kostiskag.unitynetwork.tracker.rundata.table.RedNodeTable;
import org.kostiskag.unitynetwork.tracker.service.sonar.BlueNodeClient;

/**
 * Each connected bluenode is represented by a BlueNode entry!
 * 
 * @author Konstantinos Kagiampakis
 */
public class BlueNodeEntry  extends NodeEntry<PhysicalAddress> {
    
    private final int port;
    private final PublicKey pub;
    private final RedNodeTable rednodes;
    private BlueNodeClient client;

    public BlueNodeEntry(String hostname, PublicKey pub, PhysicalAddress phAddress, int port) throws IllegalAccessException {
        super(hostname, phAddress);
        if (pub == null) {
            throw new IllegalAccessException("given data where null!");
        } else if (port <= 0 || port > NumericConstraints.MAX_ALLOWED_PORT_NUM.size()) {
            throw new IllegalAccessException("given data where not valid!");
        } else {
            this.pub = pub;
            this.port = port;
            this.rednodes = new RedNodeTable(this);
        }
    }

    public BlueNodeEntry(String hostname, PublicKey pub, String phAddress, int port) throws UnknownHostException, IllegalAccessException {
        this(hostname,pub,PhysicalAddress.valueOf(phAddress),port);
    }

    public PublicKey getPub() {
		return pub;
	}

    public int getPort() {
        return port;
    }
    
    public int getLoad() {
        try {
            Lock lock = getRedNodes().aquireLock();
            return rednodes.getSize(lock);
        } catch (InterruptedException e) {
            return 999;
        } finally {
            getRedNodes().releaseLock();
        }
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

    /**
     * there can only be one bluenode with the same hostname and the same key
     * according to the network's limitations!
     * Consequently, hostname OR key OR pair of port:address
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
            return given.getHostname().equals(this.getHostname());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getHostname(), this.getAddress(), port, pub);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": hostname: " + this.getHostname() + " phaddress: " + this.getAddress() + " port: " + port+ " \nTime "+getTimestamp().getTime();
    }
}
