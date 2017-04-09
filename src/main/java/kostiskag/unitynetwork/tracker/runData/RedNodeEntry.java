package kostiskag.unitynetwork.tracker.runData;

import java.sql.Time;

/**
 * Objects of a RedNodeEntry represent the connected
 * rednodes on the network
 * 
 * @author Konstantinos Kagiampakis
 */
public class RedNodeEntry {
        
    private final String hostname;
    private final String vAddress;
    private Time regTimestamp;
    
    private Object timeLock = new Object();

    public RedNodeEntry(String hostname, String Vaddress) {
        this.hostname = hostname;
        this.vAddress  = Vaddress;
        this.regTimestamp = new Time(System.currentTimeMillis());
    }

    public String getHostname() {
        return hostname;
    }

    public String getVaddress() {
        return vAddress;
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
