package kostiskag.unitynetwork.tracker.runData;

import java.security.PublicKey;
import java.sql.Time;

/**
 * Each connected bluenode is represented by a BlueNode entry.
 * 
 * @author Konstantinos Kagiampakis
 */
public class BlueNodeEntry {
    
    private final String name;
    private final String Phaddress;
    private final int port;
    private final PublicKey pub;
    private Time regTimestamp;
    public RedNodeTable rednodes;
    
    private Object timeLock = new Object();

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
        this.name = name;
        this.pub = pub;
        this.Phaddress = phAddress;
        this.port = port;
        this.regTimestamp = new Time(System.currentTimeMillis());
        this.rednodes = new RedNodeTable(this);
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
}
