package kostiskag.unitynetwork.tracker.sonarService;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.sql.Time;
import java.util.LinkedList;

import kostiskag.unitynetwork.tracker.App;
import kostiskag.unitynetwork.tracker.functions.SocketFunctions;
import kostiskag.unitynetwork.tracker.runData.BlueNodeEntry;
import kostiskag.unitynetwork.tracker.runData.RedNodeEntry;

/**
 *
 * @author kostis
 *
 * This functions are being used from the sonar service - the tracker client
 */
public class BlueNodeFunctions {

    public static String pre = "^AUTH CLIENT ";

    public static boolean checkBnOnline(BlueNodeEntry bn) {
        InetAddress addr = SocketFunctions.getAddress(bn.getPhaddress());
                
        Socket socket = SocketFunctions.absoluteConnect(addr, bn.getPort());
        if (socket == null) {
            return false;
        }

        BufferedReader inputReader = SocketFunctions.makeReadWriter(socket);
        PrintWriter writer = SocketFunctions.makeWriteWriter(socket);       
        String args[] = SocketFunctions.readData(inputReader);        
        args = SocketFunctions.sendData("TRACKER", writer, inputReader);

        //there should be an auth here
        //you are the one who you claim to be
        if (args[0].equals("OK")) {
            args = SocketFunctions.sendData("CHECK", writer, inputReader);            
            if (args[0].equals("OK")) {
            	SocketFunctions.connectionClose(socket);
                return true;
            }            
        }
        
        SocketFunctions.connectionClose(socket);
        return false;
    }
    
    public static LinkedList<RedNodeEntry> getRedNodes(BlueNodeEntry bn) {        
    	LinkedList<RedNodeEntry> list = new LinkedList<>();
    	InetAddress addr = SocketFunctions.getAddress(bn.getPhaddress());
        Socket socket = SocketFunctions.absoluteConnect(addr, bn.getPort());
        if (socket == null) {
            return list;
        }

        BufferedReader inputReader = SocketFunctions.makeReadWriter(socket);
        PrintWriter writer = SocketFunctions.makeWriteWriter(socket);
        String args[] = SocketFunctions.readData(inputReader);
        args = SocketFunctions.sendData("TRACKER", writer, inputReader);

        //there should be an auth here
        //you are the one who you claim to be
        if (args[0].equals("OK")) {

            args = SocketFunctions.sendData("GETREDNODES", writer, inputReader);
            
            int count = Integer.parseInt(args[1]);
            for (int i = 0; i < count; i++) {
                args = SocketFunctions.readData(inputReader);
                RedNodeEntry r =  new RedNodeEntry(args[0], args[1], new Time(System.currentTimeMillis()));                    
                list.add(r); 
            }
        }
        SocketFunctions.connectionClose(socket);
        return list;
    }    
}
