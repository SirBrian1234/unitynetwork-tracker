package kostiskag.unitynetwork.tracker.runData;

import kostiskag.unitynetwork.tracker.App;
import kostiskag.unitynetwork.tracker.trackClient.BlueNodeFunctions;

/**
 *
 * @author kostis
 */
public class TableFunctions {

	//not good as the table might be needed in its fill time gap
	//you need to clone the table and then fill it
	//and switch it with the old
    public static void updateTablesViaAuthClient() {
        App.RNtable.flushTable();
        int count = App.BNtable.getSize();

        for (int i=0; i< count; i++) {            
            String Hostname = App.BNtable.getBlueNodeEntryById(i).getHostname();
            String Physical = App.BNtable.getBlueNodeEntryById(i).getPhaddress();
            int port = App.BNtable.getBlueNodeEntryById(i).getPort();
            
            if (BlueNodeFunctions.checkOnline(Hostname)) {
                App.BNtable.getBlueNodeEntryByHn(Hostname).updateTimestamp();
                BlueNodeFunctions.getRedNodes(Hostname, Physical, port);
            } else {                
                App.BNtable.releaseBnByHn(Hostname); //efoswn egine release edw pera auto shmainei oti to count exei meiwthei... opote sth for tha kanei +1 vhma kai tha pesei eksw!!!                
                count--;
                i--;                
            }
        }
        App.BNtable.updateTable();
    }
}
