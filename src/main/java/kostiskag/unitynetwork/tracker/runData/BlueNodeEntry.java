/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kostiskag.unitynetwork.tracker.runData;

import java.sql.Time;

/**
 *
 * @author kostis
 */
public class BlueNodeEntry {
    
    private String hostname;
    private String Phaddress;
    private int port;
    private int load; //number of clients
    private Time regTimestamp;

    public BlueNodeEntry(String hostname, String Phaddress, int port, int load, Time regTimestamp) {
        this.hostname = hostname;
        this.Phaddress = Phaddress;
        this.port = port;
        this.load = load;
        this.regTimestamp = regTimestamp;
    }

    public void init(String hostname, String Phaddress, int port, int load, Time regTimestamp) {
        this.hostname = hostname;
        this.Phaddress = Phaddress;
        this.port = port;
        this.load = load;
        this.regTimestamp = regTimestamp;
    }
    
    public String getHostname() {
        return hostname;
    }

    public String getPhaddress() {
        return Phaddress;
    }

    public int getLoad() {
        return load;
    }

    public int getPort() {
        return port;
    }

    public Time getRegTimestamp() {
        return regTimestamp;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setPhaddress(String Phaddress) {
        this.Phaddress = Phaddress;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setLoad(int load) {
        this.load = load;
    }

    public void setRegTimestamp(Time regTimestamp) {
        this.regTimestamp = regTimestamp;
    }        
        
    public void takeATimestamp(){
        this.regTimestamp = new Time(System.currentTimeMillis());
    }       

    public void increaseLoad() {
        load++;
    }
    
    public void decreaseLoad() {
        load--;
    }
}
