/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kostiskag.unitynetwork.tracker.runData;

import kostiskag.unitynetwork.tracker.App;
import kostiskag.unitynetwork.tracker.trackClient.BlueNodeFunctions;

/**
 *
 * @author kostis
 */
public class TableFunctions {

    public static void updateTablesViaAuthClient() {
        App.RNtable.flushTable();
        int count = App.BNtable.getSize();

        for (int i=0; i< count; i++) {            
            String Hostname = App.BNtable.getBlueNodeEntry(i).getHostname();
            String Physical = App.BNtable.getBlueNodeEntry(i).getPhaddress();
            int port = App.BNtable.getBlueNodeEntry(i).getPort();
            
            if (BlueNodeFunctions.checkOnline(Hostname)) {
                App.BNtable.getBlueNodeEntryByHn(Hostname).takeATimestamp();
                BlueNodeFunctions.getRedNodes(Hostname, Physical, port);
            } else {                
                App.BNtable.release(Hostname); //efoswn egine release edw pera auto shmainei oti to count exei meiwthei... opote sth for tha kanei +1 vhma kai tha pesei eksw!!!                
                count--;
                i--;                
            }
        }
        App.BNtable.updateTable();
    }
}
