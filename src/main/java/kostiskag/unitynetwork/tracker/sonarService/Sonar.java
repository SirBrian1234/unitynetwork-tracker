package kostiskag.unitynetwork.tracker.sonarService;

import java.util.logging.Level;
import java.util.logging.Logger;
import kostiskag.unitynetwork.tracker.App;

/**
 *
 * @author kostis
 */
public class Sonar extends Thread {

    String pre = "^Ping ";
    boolean kill = false;
    int time;
    
    public Sonar(int time) {
        this.time = time;
    }

    @Override
    public void run() {
        App.ConsolePrint(pre+"started in thread "+Thread.currentThread()+" with time "+time+" sec");
        while (!kill) {
            try {
                sleep(time*1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Sonar.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (kill) break;
            App.ConsolePrint(pre+"Updating BN Tables via ping");
            kostiskag.unitynetwork.tracker.runData.TableFunctions.updateTablesViaAuthClient();
        }
    }
    
    public void kill(){
        kill = true;
    }
}
