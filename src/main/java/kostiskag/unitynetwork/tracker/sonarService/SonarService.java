package kostiskag.unitynetwork.tracker.sonarService;

import java.util.logging.Level;
import java.util.logging.Logger;
import kostiskag.unitynetwork.tracker.App;

/**
 *
 * @author kostis
 */
public class SonarService extends Thread {

    String pre = "^Ping ";
    boolean kill = false;
    int time;
    
    public SonarService(int time) {
        this.time = time;
    }
    
    /*
     * Works like the java garbage collector for killed bluenodes and redonodes
     * When a bn died and is not responding it has to wait for this duration in order to
     * be able to reconnect to the network.  
     * 
     * When a dead bn is found its leased rns are removed as well and the rns
     * may try to reconnect
     * 
     */

    @Override
    public void run() {
        App.ConsolePrint(pre+"started in thread "+Thread.currentThread()+" with time "+time+" sec");
        while (!kill) {
            try {
                sleep(time*1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(SonarService.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (kill) break;
            App.ConsolePrint(pre+"Updating BN Tables via ping");
            App.BNtable.rebuildTableViaAuthClient();
        }
    }
    
    public void kill(){
        kill = true;
    }
}
