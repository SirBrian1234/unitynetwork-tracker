package kostiskag.unitynetwork.tracker.functions;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import kostiskag.unitynetwork.tracker.App;

/**
 * low-level socket methods here
 *
 * @author Konstantinos Kagiampakis
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

    public static DataInputStream makeBufferedDataReader(Socket socket) throws IOException {
    	//BufferedInputStream bin = new BufferedInputStream();
		DataInputStream dataStream = new DataInputStream(socket.getInputStream());
		return dataStream;
    }

    public static DataOutputStream makeBufferedDataWriter(Socket socket) throws IOException {
        DataOutputStream dataStream = new DataOutputStream(socket.getOutputStream());
        return dataStream;
    }
    
    public static void sendData(byte[] toSend, DataOutputStream writer) throws IOException {
    	writer.write(toSend);
    }
    
    public static byte[] receiveData(DataInputStream reader) throws IOException {
    	byte[] byteT = null;
    	while (true) {
	    	byte[] bytes = new byte[2048];
			int read = reader.read(bytes);
			byteT = new byte[read];
			System.arraycopy(bytes, 0, byteT, 0, read);
		
			if (byteT[0] != (int)13 && byteT[0] != (int)10) {
				break;
			}
    	}
		return byteT;		
    }
    
    public static byte[] sendReceiveData(byte[] toSend, DataInputStream reader, DataOutputStream writer) throws Exception  {
    	sendData(toSend, writer);
    	byte[] received = receiveData(reader);
    	return received;
    }

    public static void sendStringlData(String data, DataOutputStream writer) throws Exception {
    	if (data == null) {
        	App.ConsolePrint(pre + "NO DATA TO SEND");
            throw new Exception(pre+"NO DATA TO SEND");
        } else if (data.isEmpty()) {
        	//line feed
        	data = "\n";
        }        
    	//include a line feed and a return char
    	data += "\n\r";
    	byte[] toSend = data.getBytes();        
        sendData(toSend, writer);
    }
    
    public static String[] receiveStringData(DataInputStream reader) throws IOException {
    	byte[] received = receiveData(reader);
    	String receivedMessage = new String(received, "utf-8");
        String[] args = receivedMessage.split("\\s+");
        return args;
    }
    
    public static String[] sendReceiveStringData(String data, DataInputStream reader, DataOutputStream writer) throws Exception  {
    	sendStringlData(data, writer);
    	String[] args = receiveStringData(reader);
    	return args;
    }

    public static void connectionClose(Socket socket) throws IOException {
        if (!socket.isClosed()) {
            socket.close();
        }
    }
}
