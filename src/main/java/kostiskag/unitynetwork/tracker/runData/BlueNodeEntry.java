package kostiskag.unitynetwork.tracker.runData;

import java.sql.Time;

/**
 *
 * @author kostis
 */
public class BlueNodeEntry {
    
    private final String hostname;
    private final String Phaddress;
    private final int port;
    private int load; //number of clients
    private Time regTimestamp;
    private RedNodeTable rednodes;
    
    private Object loadLock = new Object();
    private Object timeLock = new Object();

    public BlueNodeEntry(String hostname, String Phaddress, int port, int load, Time regTimestamp) {
        this.hostname = hostname;
        this.Phaddress = Phaddress;
        this.port = port;
        this.load = load;
        this.regTimestamp = regTimestamp;
        this.rednodes = new RedNodeTable(this);
    }
    
    //auto timestamp
    public BlueNodeEntry(String hostname, String Phaddress, int port, int load) {
        this.hostname = hostname;
        this.Phaddress = Phaddress;
        this.port = port;
        this.load = load;
        this.regTimestamp = new Time(System.currentTimeMillis());
        this.rednodes = new RedNodeTable(this);
    }
    
    //auto timestamp, no load
    public BlueNodeEntry(String hostname, String Phaddress, int port) {
        this.hostname = hostname;
        this.Phaddress = Phaddress;
        this.port = port;
        this.load = 0;
        this.regTimestamp = new Time(System.currentTimeMillis());
        this.rednodes = new RedNodeTable(this);
    }

    public String getHostname() {
        return hostname;
    }

    public String getPhaddress() {
        return Phaddress;
    }

    public int getPort() {
        return port;
    }

    public int getLoad() {
    	synchronized (loadLock) {
    		return load;
    	}
    }

    public Time getTimestamp() {
    	synchronized (timeLock) {
    		return regTimestamp;
    	}
    }

    public void setLoad(int load) {
    	synchronized (loadLock) {
    		this.load = load;
    	}
    }

    public void increaseLoad() {
    	synchronized (loadLock) {
    		load++;
		}        
    }
    
    public synchronized void decreaseLoad() {
    	synchronized (loadLock) {
    		load--;
    	}
    }
    
    public void updateTimestamp(){
    	synchronized (timeLock) {
    		this.regTimestamp = new Time(System.currentTimeMillis());
    	}
    }    
}
