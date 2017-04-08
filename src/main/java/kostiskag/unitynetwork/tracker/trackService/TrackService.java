package kostiskag.unitynetwork.tracker.trackService;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import kostiskag.unitynetwork.tracker.database.Database;
import kostiskag.unitynetwork.tracker.functions.SocketFunctions;

/**
 *
 * @author kostis
 *
 */
public class TrackService extends Thread {

    private Socket socket;    
    private BufferedReader reader;
    private PrintWriter writer;
    private Database con;

    TrackService(Socket connectionSocket) {
        socket = connectionSocket;        
    }

    /*
     * CENTRAL TRACK SERVICE 
     * first it determines if it is a rednode or a bluenode
     * 
     * for an (unregistered) rednode 
     * a. it indicates the closest bluenode to connect
     * b. or a list of the available bluenodes and their respective load
     * 
     * for a bluenode it can
     * a. lease the BN itself
     * b. auth/lease an RN connected to him
     * c. resolve some queries
     */
    
    @Override
    public void run() {
        System.out.println("@Started auth service at " + Thread.currentThread().getName());
        
        reader = SocketFunctions.makeReadWriter(socket);
        writer = SocketFunctions.makeWriteWriter(socket);
        
        String[] args;
        String data;
        
        data = "UnityTracker";
        
        args = SocketFunctions.sendData(data, writer, reader);

        if (args.length == 2 && args[0].equals("BLUENODE")) {
            BlueNodeService(args[1]);
        } else if (args.length == 2 && args[0].equals("REDNODE")) {
            RedNodeService(args[1]);
        } else {
            data = "WRONG_COMMAND";
            SocketFunctions.sendFinalData(data, writer);            
        }
        SocketFunctions.connectionClose(socket);                      
    }    
    
    public void BlueNodeService(String BlueNodeHostname) {        
        String data;
        
        int auth = BlueNodeFunctions.authBluenode(BlueNodeHostname);
        if (auth > -1) {
            data = "OK";
        } else if (auth == -1) {
            data = "NOT_REGISTERED";
            SocketFunctions.sendFinalData(data, writer);
            SocketFunctions.connectionClose(socket);
            return;
        } else {
            data = "SYSTEM_ERROR";
            SocketFunctions.sendFinalData(data, writer);
            SocketFunctions.connectionClose(socket);
            return;
        }        
        String[] args = SocketFunctions.sendData("OK", writer, reader);
        
        if (args.length == 3 && args[0].equals("LEASE") && args[1].equals("BN")) {
            BlueNodeFunctions.BlueLease(BlueNodeHostname, args[2], writer, socket);
        } else if (args.length == 5 && args[0].equals("LEASE") && args[1].equals("RN")) {
            BlueNodeFunctions.RedLease(BlueNodeHostname, args[2], args[3], args[4], writer);
        } else if (args.length == 2 && args[0].equals("RELEASE") && args[1].equals("BN")) {
            BlueNodeFunctions.BlueRel(BlueNodeHostname, writer);
        } else if (args.length == 3 && args[0].equals("RELEASE") && args[1].equals("RN")) {
            BlueNodeFunctions.RedRel(BlueNodeHostname, args[2], writer);
        } else if (args.length == 2 && args[0].equals("GETPH")) {
            BlueNodeFunctions.GetPh(args[1], writer);
        } else if (args.length == 2 && args[0].equals("CHECKRN")) {
            BlueNodeFunctions.CheckRn(args[1], writer);
        } else if (args.length == 2 && args[0].equals("CHECKRNA")) {
            BlueNodeFunctions.CheckRnAddr(args[1], writer);
        } else {
            data = "WRONG_COMMAND";
            SocketFunctions.sendFinalData(data, writer);
        }
        SocketFunctions.connectionClose(socket);
    }

    private void RedNodeService(String hostname) {
        String[] args = SocketFunctions.sendData("OK", writer, reader);
        if (args.length == 1 && args[0].equals("GETBNS")) {
             RedNodeFunctions.getAllConnectedBlueNodes(reader, writer, socket);
        } else if (args.length == 1 && args[0].equals("GETRBN")) {
            RedNodeFunctions.getRecomendedBlueNode(reader, writer, socket);
        } else {
            String data = "WRONG_COMMAND";
            SocketFunctions.sendFinalData(data, writer);
        }
        SocketFunctions.connectionClose(socket);
    }    
}
