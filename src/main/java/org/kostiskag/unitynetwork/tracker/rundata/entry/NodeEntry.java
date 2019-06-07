package org.kostiskag.unitynetwork.tracker.rundata.entry;

import org.kostiskag.unitynetwork.tracker.App;
import org.kostiskag.unitynetwork.tracker.rundata.address.NetworkAddress;

import java.sql.Time;

public class NodeEntry<A extends NetworkAddress> {

    private final String hostname;
    private final A address;
    private final Object timeLock = new Object();
    private Time regTimestamp;

    public NodeEntry(String hostname, A address) throws IllegalAccessException {
        if (hostname == null || address == null) {
            throw new IllegalAccessException("given hostname address were null!");
        } else if (hostname.isEmpty() || hostname.length() > App.MAX_STR_LEN_SMALL_SIZE) {
            throw new IllegalAccessException("given hostname not valid!");
        }
        this.hostname = hostname;
        this.address = address;
        this.regTimestamp = new Time(System.currentTimeMillis());
    }

    public String getHostname() {
        return hostname;
    }

    public A getAddress() {
        return address;
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
}
