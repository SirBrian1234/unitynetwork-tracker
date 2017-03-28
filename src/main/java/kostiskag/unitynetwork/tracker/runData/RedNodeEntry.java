package kostiskag.unitynetwork.tracker.runData;

import java.sql.Time;

/**
 *
 * @author kostis
 */
public class RedNodeEntry {
        
    private String hostname;
    private String Vaddress;
    private String BNhostname;
    private Time regTimestamp;

    public RedNodeEntry(String hostname, String Vaddress, String BNhostname, Time regTimestamp) {
        this.hostname = hostname;
        this.BNhostname = BNhostname;
        this.Vaddress  = Vaddress;
        this.regTimestamp = regTimestamp;        
    }

    public void init(String hostname, String Vaddress, String BNhostname, Time regTimestamp) {
        this.hostname = hostname;
        this.BNhostname = BNhostname;
        this.Vaddress  = Vaddress;
        this.regTimestamp = regTimestamp;
    }
    
    public String getHostname() {
        return hostname;
    }

    public String getVaddress() {
        return Vaddress;
    }        

    public String getBNhostname() {
        return BNhostname;
    }    

    public Time getRegTimestamp() {
        return regTimestamp;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setBNhostname(String BNhostname) {
        this.BNhostname = BNhostname;
    }    

    public void setRegTimestamp(Time regTimestamp) {
        this.regTimestamp = regTimestamp;
    }        
           
    public void takeATimestamp(){
        this.regTimestamp = new Time(System.currentTimeMillis());
    } 
}
