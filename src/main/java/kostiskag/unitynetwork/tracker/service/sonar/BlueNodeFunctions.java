package kostiskag.unitynetwork.tracker.service.sonar;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.LinkedList;
import kostiskag.unitynetwork.tracker.functions.SocketFunctions;
import kostiskag.unitynetwork.tracker.runData.BlueNodeEntry;
import kostiskag.unitynetwork.tracker.runData.RedNodeEntry;

/**
 * These functions are being used from the sonar service - the tracker client
 * 
 *  CHECK
 *  GETREDNODES
 *
 *  @author Konstantinos Kagiampakis
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
        
        /*
        String data = null;
        String BlueNodeHostname = args[1];
        int auth = BlueNodeGlobalFunctions.authBluenode(BlueNodeHostname);
        if (auth == -1) {
            data = "NOT_REGISTERED";
            SocketFunctions.sendFinalData(data, writer);
            SocketFunctions.connectionClose(socket);
            return false;
        } else if (auth == 0){
            data = "SYSTEM_ERROR";
            SocketFunctions.sendFinalData(data, writer);
            SocketFunctions.connectionClose(socket);
            return false;
        }*/               
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
        /*
        String data;
        String bluenodeName = args[1];
        int auth = BlueNodeGlobalFunctions.authBluenode(bluenodeName);
        if (auth == -1) {
            data = "NOT_REGISTERED";
            SocketFunctions.sendFinalData(data, writer);
            SocketFunctions.connectionClose(socket);
            return null;
        } else if (auth == 0){
            data = "SYSTEM_ERROR";
            SocketFunctions.sendFinalData(data, writer);
            SocketFunctions.connectionClose(socket);
            return null;
        }*/    
        args = SocketFunctions.sendData("TRACKER", writer, inputReader);

        //there should be an auth here
        //you are the one who you claim to be
        if (args[0].equals("OK")) {

            args = SocketFunctions.sendData("GETREDNODES", writer, inputReader);
            
            int count = Integer.parseInt(args[1]);
            for (int i = 0; i < count; i++) {
                args = SocketFunctions.readData(inputReader);
                //RedNodeEntry r =  new RedNodeEntry(args[0], args[1], new Time(System.currentTimeMillis()));                    
                //list.add(r); 
            }
        }
        SocketFunctions.connectionClose(socket);
        return list;
    }    
}
