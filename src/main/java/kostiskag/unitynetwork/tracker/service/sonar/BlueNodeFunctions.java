package kostiskag.unitynetwork.tracker.service.sonar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.LinkedList;
import kostiskag.unitynetwork.tracker.functions.SocketFunctions;
import kostiskag.unitynetwork.tracker.runData.BlueNodeEntry;
import kostiskag.unitynetwork.tracker.runData.RedNodeEntry;
import kostiskag.unitynetwork.tracker.service.BlueNodeGlobalFunctions;

/**
 * These functions are being used from the sonar service - the tracker client
 * 
 *  CHECK
 *  GETREDNODES
 *  
 *  Exception handle policy: the task is to provide resilience for the network socket
 *  to keep it alive and block the internal system expeptions such as database
 *  therefore net execptions should be thrown, sys exception should be catched and a
 *  proper response has to be compiled.
 *
 *  @author Konstantinos Kagiampakis
 */
public class BlueNodeFunctions {

    public static String pre = "^AUTH CLIENT ";

    public static boolean checkBnOnline(BlueNodeEntry bn) throws Exception {
        InetAddress addr = SocketFunctions.getAddress(bn.getPhaddress());
        Socket socket = null;
		try {
			socket = SocketFunctions.absoluteConnect(addr, bn.getPort());
			
			BufferedReader inputReader = SocketFunctions.makeReadWriter(socket);
	        PrintWriter writer = SocketFunctions.makeWriteWriter(socket);       
	        String args[] = SocketFunctions.readData(inputReader);   
	        
	        String data = null;
	        String BlueNodeHostname = args[1];
	        int auth = BlueNodeGlobalFunctions.authBluenode(BlueNodeHostname);
	        if (auth <= -2) {
	        	data = "SYSTEM_ERROR";
	            SocketFunctions.sendFinalData(data, writer);
	            SocketFunctions.connectionClose(socket);
	            return false;
	        } if (auth == -1) {
	            data = "NOT_REGISTERED";
	            SocketFunctions.sendFinalData(data, writer);
	            SocketFunctions.connectionClose(socket);
	            return false;
	        } else if (auth == 0){
	            data = "OFFLINE";
	            SocketFunctions.sendFinalData(data, writer);
	            SocketFunctions.connectionClose(socket);
	            return false;
	        }               
	        args = SocketFunctions.sendData("TRACKER", writer, inputReader);

	        if (args[0].equals("OK")) {
	            args = SocketFunctions.sendData("CHECK", writer, inputReader);            
	            if (args[0].equals("OK")) {
	            	SocketFunctions.connectionClose(socket);
	                return true;
	            }            
	        }
	        
	        SocketFunctions.connectionClose(socket);
	        return false;
	        
		} catch (Exception e) {
			try {
				SocketFunctions.connectionClose(socket);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			throw e;
		}        
    }
    
    public static boolean sendkillsig(BlueNodeEntry bn) throws Exception  {
        InetAddress addr = SocketFunctions.getAddress(bn.getPhaddress());
        Socket socket = null;
		try {
			socket = SocketFunctions.absoluteConnect(addr, bn.getPort());
			BufferedReader inputReader = SocketFunctions.makeReadWriter(socket);
	        PrintWriter writer = SocketFunctions.makeWriteWriter(socket);       
	        String args[] = SocketFunctions.readData(inputReader);   
	        
	        String data = null;
	        String BlueNodeHostname = args[1];
	        int auth = BlueNodeGlobalFunctions.authBluenode(BlueNodeHostname);
	        if (auth <= -2) {
	        	data = "SYSTEM_ERROR";
	            SocketFunctions.sendFinalData(data, writer);
	            SocketFunctions.connectionClose(socket);
	            return false;
	        } if (auth == -1) {
	            data = "NOT_REGISTERED";
	            SocketFunctions.sendFinalData(data, writer);
	            SocketFunctions.connectionClose(socket);
	            return false;
	        } else if (auth == 0){
	            data = "OFFLINE";
	            SocketFunctions.sendFinalData(data, writer);
	            SocketFunctions.connectionClose(socket);
	            return false;
	        }                           
	        args = SocketFunctions.sendData("TRACKER", writer, inputReader);
	        
	        if (args[0].equals("OK")) {
	        	args = SocketFunctions.sendData("KILLSIG", writer, inputReader);            
	            if (args[0].equals("OK")) {
	            	SocketFunctions.connectionClose(socket);
	                return true;
	            }                 
	        }
	        
	        SocketFunctions.connectionClose(socket);
	        return false;
		} catch (Exception e) {
			try {
				SocketFunctions.connectionClose(socket);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			throw e;
		}               
    }
    
    public static LinkedList<RedNodeEntry> getRedNodes(BlueNodeEntry bn) throws Exception  {        
    	LinkedList<RedNodeEntry> list = new LinkedList<>();
    	InetAddress addr = SocketFunctions.getAddress(bn.getPhaddress());
        Socket socket = null;
		try {
			socket = SocketFunctions.absoluteConnect(addr, bn.getPort());
			
			BufferedReader inputReader = SocketFunctions.makeReadWriter(socket);
	        PrintWriter writer = SocketFunctions.makeWriteWriter(socket);
	        String args[] = SocketFunctions.readData(inputReader);

	        String data;
	        String bluenodeName = args[1];
	        int auth = BlueNodeGlobalFunctions.authBluenode(bluenodeName);
	        if (auth <= -2) {
	        	data = "SYSTEM_ERROR";
	            SocketFunctions.sendFinalData(data, writer);
	            SocketFunctions.connectionClose(socket);
	            throw new Exception("SYSTEM_ERROR");
	        } if (auth == -1) {
	            data = "NOT_REGISTERED";
	            SocketFunctions.sendFinalData(data, writer);
	            SocketFunctions.connectionClose(socket);
	            throw new Exception("NOT_REGISTERED");
	        } else if (auth == 0) {
	            data = "OFFLINE";
	            SocketFunctions.sendFinalData(data, writer);
	            SocketFunctions.connectionClose(socket);
	            throw new Exception("OFFLINE");
	        }    
	        args = SocketFunctions.sendData("TRACKER", writer, inputReader);

	        if (args[0].equals("OK")) {
	            args = SocketFunctions.sendData("GETREDNODES", writer, inputReader);
	            int count = Integer.parseInt(args[1]);
	            for (int i = 0; i < count; i++) {
	                args = SocketFunctions.readData(inputReader);
	                RedNodeEntry r =  new RedNodeEntry(bn, args[0], args[1]);                    
	                list.add(r); 
	            }
	        }
	        SocketFunctions.connectionClose(socket);
	        return list;
		} catch (Exception e) {
			try {
				SocketFunctions.connectionClose(socket);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			throw e;
		}
        
    }    
}
