package kostiskag.unitynetwork.tracker.functions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import kostiskag.unitynetwork.tracker.App;

/**
 * low-level socket methods here
 *
 * @author kostis
 */
public class SocketFunctions {

	public static final String pre = "^SOCKET METHODS ";       
    
    public static InetAddress getAddress(String PhAddress) throws UnknownHostException {
        InetAddress IPaddress = null;
        try {
            IPaddress = InetAddress.getByName(PhAddress);
        } catch (UnknownHostException ex) {            
        	 App.ConsolePrint(pre + "UNKNOWN HOST "+PhAddress);
        	 throw ex;
        }
        return IPaddress;
    }
    
    public static Socket absoluteConnect(InetAddress IPaddress, int authPort) throws Exception {
        Socket socket = null;
        try {
            socket = new Socket(IPaddress, authPort);
            socket.setSoTimeout(3000);
        } catch (java.net.NoRouteToHostException ex) {
            App.ConsolePrint(pre + "NO ROUTE FOR "+IPaddress.getHostAddress()+" "+authPort);
            throw ex;
        } catch (java.net.ConnectException ex) {
            App.ConsolePrint(pre + "CONNECTION REFUSED FOR "+IPaddress.getHostAddress()+" "+authPort);
            throw ex;
        } catch (java.net.SocketTimeoutException ex) {
            App.ConsolePrint(pre + "CONNECTION TIMED OUT FOR "+IPaddress.getHostAddress()+" "+authPort);
            throw ex;
        } catch (IOException ex) {
            App.ConsolePrint(pre + "CONNECTION ERROR FOR "+IPaddress.getHostAddress()+" "+authPort);
            throw ex;
        }
        return socket;
    }

    public static BufferedReader makeReadWriter(Socket socket) throws IOException {
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        return inputReader;
    }

    public static PrintWriter makeWriteWriter(Socket socket) throws IOException {
        PrintWriter outputWriter = new PrintWriter(socket.getOutputStream(), true);
        return outputWriter;
    }

    public static String[] sendData(String data,PrintWriter outputWriter,BufferedReader inputReader) throws Exception  {
        if (outputWriter==null) {
        	App.ConsolePrint(pre + "SEND DATA FAILED, NO CONNECTION");            
        	throw new Exception(pre + "SEND DATA FAILED, NO CONNECTION");
        } else if (inputReader==null){ 
        	App.ConsolePrint(pre + "SEND DATA FAILED, NO CONNECTION");            
        	throw new Exception(pre + "SEND DATA FAILED, NO CONNECTION");
        } else if (data == null) {
        	App.ConsolePrint(pre + "NO DATA TO SEND");
            throw new Exception(pre+"NO DATA TO SEND");
        } else if (data.isEmpty()) {
        	App.ConsolePrint(pre + "NO DATA TO SEND");
            throw new Exception(pre+"NO DATA TO SEND");
        }
        
        outputWriter.println(data);
        String receivedMessage = null;
        String[] args = null;
        
        try {
			receivedMessage = inputReader.readLine();
		} catch (IOException e) {
			throw e;
		}
        
        App.ConsolePrint(pre + receivedMessage);
        args = receivedMessage.split("\\s+");
        return args;
    }

    public static void sendFinalData(String data,PrintWriter outputWriter) throws Exception {
        if (data == null) {
        	App.ConsolePrint(pre + "NO DATA TO SEND");
            throw new Exception(pre + "NO DATA TO SEND");
        } else if (data.isEmpty()) {
        	App.ConsolePrint(pre + "NO DATA TO SEND");
            throw new Exception(pre + "NO DATA TO SEND");
        }
        outputWriter.println(data);
    }

    public static String[] readData(BufferedReader inputReader) throws Exception  {
    	if (inputReader == null){
    		App.ConsolePrint(pre + "READ DATA FAILED, NO CONNECTION");            
            throw new Exception(pre + "READ DATA FAILED, NO CONNECTION");
    	}
        
        String receivedMessage = null;
        String[] args = null;
        
        try {
			receivedMessage = inputReader.readLine();
		} catch (IOException e) {
			throw e;
		}
               
        System.out.println(pre + receivedMessage);
        args = receivedMessage.split("\\s+");
        return args;
    }

    public static void connectionClose(Socket socket) throws IOException {
        if (!socket.isClosed()) {
            socket.close();
        }
    }
}
