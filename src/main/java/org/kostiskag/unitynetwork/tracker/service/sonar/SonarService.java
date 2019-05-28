package org.kostiskag.unitynetwork.tracker.service.sonar;

import org.kostiskag.unitynetwork.tracker.AppLogger;
import org.kostiskag.unitynetwork.tracker.App;

/**
 * Works like the java garbage collector but for killed bluenodes and redonodes. The sonar
 * connects to the leased bluenodes and requests to get their respective rednodes back
 * When a dead bn is found it, and its rns are removed from the network
 * If a bn has died it has to wait for this duration in order to
 * be able to reconnect to the network.  
 * 
 * @author Konstantinos Kagiampakis
 */
public class SonarService extends Thread {

    private final String pre = "^Ping ";
    private boolean kill = false;
    private final int time;
    
    public SonarService(int time) {
        this.time = time;
    }

    @Override
    public void run() {
        AppLogger.getLogger().consolePrint(pre+"started in thread "+Thread.currentThread()+" with time "+time+" sec");
        while (!kill) {
            try {
                sleep(time*1000);
            } catch (InterruptedException ex) {
                AppLogger.getLogger().consolePrint(ex.getMessage());
            }
            if (kill) break;
            AppLogger.getLogger().consolePrint(pre+"Updating BN Tables via ping");
            App.TRACKER_APP.BNtable.rebuildTableViaAuthClient();
        }
    }
    
    public synchronized void kill(){
        kill = true;
    }
}
