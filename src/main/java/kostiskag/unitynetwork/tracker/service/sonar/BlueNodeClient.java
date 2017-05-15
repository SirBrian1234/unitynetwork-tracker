package kostiskag.unitynetwork.tracker.service.sonar;

import java.io.BufferedReader;
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
 *  KILLSIG
 *  
 *  Exception handle policy: the task is to provide resilience for the network socket
 *  to keep it alive and block the internal system exceptions such as database
 *  therefore net exceptions should be thrown, system exception should be cached and a
 *  proper response has to be compiled.
 *
 *  @author Konstantinos Kagiampakis
 */
public class BlueNodeClient {

    public final String pre = "^BlueNodeClient ";
    public final int timeout = 3000;
    public final BlueNodeEntry bn;
    public boolean connected = false;
    private Socket socket;
    private BufferedReader socketReader;
    private PrintWriter socketWriter;
    
    public BlueNodeClient(BlueNodeEntry bn) {
    	this.bn = bn;
    	
    	try {
    		InetAddress address = SocketFunctions.getAddress(bn.getPhaddress());
			socket = SocketFunctions.absoluteConnect(address, bn.getPort());
			//socket.setSoTimeout(timeout);
			
			socketReader = SocketFunctions.makeReadWriter(socket);
	        socketWriter = SocketFunctions.makeWriteWriter(socket);       
	        String args[] = SocketFunctions.readData(socketReader);   
	        
	        String data = null;
	        String BlueNodeHostname = args[1];
	        int auth = BlueNodeGlobalFunctions.authBluenode(BlueNodeHostname);
	        
	        if (auth <= -2) {
	        	data = "SYSTEM_ERROR";
	            SocketFunctions.sendFinalData(data, socketWriter);
	            SocketFunctions.connectionClose(socket);
	            return;
	        } if (auth == -1) {
	            data = "NOT_REGISTERED";
	            SocketFunctions.sendFinalData(data, socketWriter);
	            SocketFunctions.connectionClose(socket);
	            return;
	        } else if (auth == 0){
	            data = "OFFLINE";
	            SocketFunctions.sendFinalData(data, socketWriter);
	            SocketFunctions.connectionClose(socket);
	            return;
	        }               
	        args = SocketFunctions.sendData("TRACKER", socketWriter, socketReader);

	        if (args[0].equals("OK")) {
	        	connected = true;
	        }
		} catch (Exception e) {
			//this is a silent exception
		}        
	}
    
    public boolean isConnected() {
		return connected;
	}
    
    public boolean checkBnOnline() throws Exception {
    	if (connected) {
	    	String[] args = SocketFunctions.sendData("CHECK", socketWriter, socketReader);    
	        SocketFunctions.connectionClose(socket);
	        if (args[0].equals("OK")) {
	        	SocketFunctions.connectionClose(socket);
	            return true;
	        }         
    	}
        return false;
	}
    
    public boolean sendkillsig() throws Exception  {
    	if (connected) {   
	    	String[] args = SocketFunctions.sendData("KILLSIG", socketWriter, socketReader); 
	    	SocketFunctions.connectionClose(socket);
	        if (args[0].equals("OK")) {
	        	SocketFunctions.connectionClose(socket);
	            return true;
	        }                 	        		
    	}
    	return false;
    }
    
    public LinkedList<RedNodeEntry> getRedNodes() throws Exception  {  
    	LinkedList<RedNodeEntry> list = new LinkedList<>();
    	if (connected) {    	
	    	String[] args = SocketFunctions.sendData("GETREDNODES", socketWriter, socketReader);        
	        int count = Integer.parseInt(args[1]);
	        for (int i = 0; i < count; i++) {
	            args = SocketFunctions.readData(socketReader);
	            RedNodeEntry r =  new RedNodeEntry(bn, args[0], args[1]);                    
	            list.add(r); 
	        }
	        SocketFunctions.connectionClose(socket);	        
    	}
    	return list;
	}    
}
