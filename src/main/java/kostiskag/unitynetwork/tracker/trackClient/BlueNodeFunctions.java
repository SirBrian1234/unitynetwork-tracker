package kostiskag.unitynetwork.tracker.trackClient;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.sql.Time;
import kostiskag.unitynetwork.tracker.App;
import kostiskag.unitynetwork.tracker.functions.SocketFunctions;

/**
 *
 * @author kostis
 *
 * These functions are helping a network to be identified in a matter o seconds
 * the tracker gets all the info about BNs and RNs
 *
 * we are on Tracker!!!
 */
public class BlueNodeFunctions {

    //edw tha dinei kai work load
    public static String pre = "^AUTH CLIENT ";

    public static boolean checkOnline(String BNHostname) {
        String phaddr = App.BNtable.getBlueNodeEntryByHn(BNHostname).getPhaddress();        
        int port = App.BNtable.getBlueNodeEntryByHn(BNHostname).getPort();        
        InetAddress addr = SocketFunctions.getAddress(phaddr);
                
        Socket socket = SocketFunctions.absoluteConnect(addr, port);
        if (socket == null) {
            return false;
        }

        BufferedReader inputReader = SocketFunctions.makeReadWriter(socket);
        PrintWriter writer = SocketFunctions.makeWriteWriter(socket);       
        String args[] = SocketFunctions.readData(inputReader);
        args = SocketFunctions.sendData("TRACKER", writer, inputReader);

        if (args[0].equals("OK")) {

            args = SocketFunctions.sendData("CHECK", writer, inputReader);
            SocketFunctions.connectionClose(socket);
            
            if (args[0].equals("OK")) {
                return true;
            }            
        }
        return false;
    }
    
    public static void getRedNodes(String BNHostname, String phaddr, int port ) {        

        InetAddress addr = SocketFunctions.getAddress(phaddr);
        Socket socket = SocketFunctions.absoluteConnect(addr, port);
        if (socket == null) {
            return;
        }

        BufferedReader inputReader = SocketFunctions.makeReadWriter(socket);
        PrintWriter writer = SocketFunctions.makeWriteWriter(socket);
        String args[] = SocketFunctions.readData(inputReader);
        args = SocketFunctions.sendData("TRACKER", writer, inputReader);

        if (args[0].equals("OK")) {

            args = SocketFunctions.sendData("GETREDNODES", writer, inputReader);

            int count = Integer.parseInt(args[1]);
            for (int i = 0; i < count; i++) {
                args = SocketFunctions.readData(inputReader);
                if (App.RNtable.getRedNodeEntryByHn(BNHostname) == null) {
                    App.RNtable.lease(args[0], args[1], BNHostname, new Time(System.currentTimeMillis()));                    
                } else {
                    App.ConsolePrint(pre + "ALLREADY REGISTERED REMOTE RED NODE, INTERNAL LOGICAL ERROR! TABLE SHOULD HAVE BEEN ERASED");
                }
            }
            App.BNtable.getBlueNodeEntryByHn(BNHostname).setLoad(count);           
        }
        SocketFunctions.connectionClose(socket);
    }
}
