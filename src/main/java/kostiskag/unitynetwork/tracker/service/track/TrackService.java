package kostiskag.unitynetwork.tracker.service.track;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.PublicKey;

import kostiskag.unitynetwork.tracker.App;
import kostiskag.unitynetwork.tracker.functions.CryptoMethods;
import kostiskag.unitynetwork.tracker.functions.SocketFunctions;
import kostiskag.unitynetwork.tracker.service.BlueNodeGlobalFunctions;
import kostiskag.unitynetwork.tracker.service.track.BlueNodeFunctions;

/**
 * CENTRAL TRACK SERVICE 
 * 
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
 * 
 * Bluenode queries:
 *
 *  LEASE BN 
 *  LEASE RN [HOSTNAME] [USERNAME] [PASSWORD]
 *  RELEASE BN 
 *  RELEASE RN [HOSTNAME] 
 *  GETPH [BLUENODE_NAME] -> IP_ADDRESS PORT
 *  CHECKRN [HOSTNAME] -> BLUENODE_NAME IP_ADDRESS PORT
 *  CHECKRNA [VADDRESS] -> ONLINE/OFFLINE BLUENODE_NAME IP_ADDRESS PORT
 * 
 * (unregistered) Rednode queries:
 *
 * GETBNS -> RETURNS A LIST OF BNS
 * GETRBN -> RETURNS BN WITH THE LOWEST LOAD
 *
 * @author Konstantinos Kagiampakis
 */
public class TrackService extends Thread {

    private Socket socket;    
    private BufferedReader reader;
    private PrintWriter writer;

    TrackService(Socket connectionSocket) {
        socket = connectionSocket;        
    }

    @Override
    public void run() {
        System.out.println("@Started auth service at " + Thread.currentThread().getName());
        
        try {
			reader = SocketFunctions.makeReadWriter(socket);
			writer = SocketFunctions.makeWriteWriter(socket);
		        
	        String[] args;
	        String data;
	        
	        data = "UnityTracker";
	        args = SocketFunctions.sendData(data, writer, reader);

	        if (args.length == 1 && args[0].equals("GETPUB")) {
	        	//plain data transfer no encryption
	        	SocketFunctions.sendFinalData(CryptoMethods.objectToBase64StringRepresentation(App.trackerKeys.getPublic()), writer);
	        } else {
		        if (args.length == 2 && args[0].equals("BLUENODE")) {
		            BlueNodeService(args[1]);
		        } else if (args.length == 2 && args[0].equals("REDNODE")) {
		            RedNodeService(args[1]);
		        } else {
		            data = "WRONG_COMMAND";
		            SocketFunctions.sendFinalData(data, writer);            
		        }
	        }
	        SocketFunctions.connectionClose(socket);   
	        
		} catch (Exception e) {
			e.printStackTrace();
			try {
				SocketFunctions.connectionClose(socket);
			} catch (IOException e1) {
				e1.printStackTrace();
			}   
		}                         
    }    
    
    public void BlueNodeService(String BlueNodeHostname) throws Exception {        
        
    	PublicKey pub = BlueNodeGlobalFunctions.fetchBluenodePubKey(BlueNodeHostname);
        if (pub == null) {
            SocketFunctions.sendFinalData("NOT_ALLOWED", writer);
            SocketFunctions.connectionClose(socket);
            return;
        } 
        
        //here is the place to offer an auth question
        //generate a random question
        String question = CryptoMethods.generateQuestion();
		
        //encrypt with target bluenode's public
        byte[] questionb = CryptoMethods.encryptWithPublic(question, pub);
        
        //encode to base 64
		String encq = CryptoMethods.bytesToBase64String(questionb);
		
		//send
		writer.println(encq);
		String returnedQ = reader.readLine();
		
		String args[];
		if (returnedQ.equals(question)) {
			//now this is a proper RSA authentication
			args = SocketFunctions.sendData("OK", writer, reader);
		} else {
			 SocketFunctions.sendFinalData("NOT_ALLOWED", writer);
			 SocketFunctions.connectionClose(socket);
			 return;
		}
        
        if (args.length == 2 && args[0].equals("LEASE")) {
            BlueNodeFunctions.BlueLease(BlueNodeHostname, args[1], writer, socket);
        } else if (args.length == 4 && args[0].equals("LEASE_RN")) {
            BlueNodeFunctions.RedLease(BlueNodeHostname, args[1], args[2], args[3], writer);
        } else if (args.length == 1 && args[0].equals("RELEASE")) {
            BlueNodeFunctions.BlueRel(BlueNodeHostname, writer);
        } else if (args.length == 2 && args[0].equals("RELEASE_RN")) {
            BlueNodeFunctions.RedRel(BlueNodeHostname, args[1], writer);
        } else if (args.length == 2 && args[0].equals("GETPH")) {
            BlueNodeFunctions.GetPh(args[1], writer);
        } else if (args.length == 2 && args[0].equals("CHECK_RN")) {
            BlueNodeFunctions.CheckRn(args[1], writer);
        } else if (args.length == 2 && args[0].equals("CHECK_RNA")) {
            BlueNodeFunctions.CheckRnAddr(args[1], writer);
        } else if (args.length == 2 && args[0].equals("LOOKUP_H")) {
        	BlueNodeFunctions.LookupByHn(args[1],writer);
        } else if (args.length == 2 && args[0].equals("LOOKUP_V")) {
        	BlueNodeFunctions.LookupByAddr(args[1], writer);
        } else if (args.length == 3 && args[0].equals("OFFERPUB")) { 
        	//bluenode offers its pub based on a ticket
        	BlueNodeFunctions.offerPublicKey(BlueNodeHostname, args[1], args[2], writer);
        } else if (args.length == 1 && args[0].equals("REVOKEPUB")) {
        	//bluenode may be compromised and decides to revoke its public
        	BlueNodeFunctions.revokePublicKey(BlueNodeHostname, writer);        
        } else {
            SocketFunctions.sendFinalData("WRONG_COMMAND", writer);
        }
        SocketFunctions.connectionClose(socket);
    }

    private void RedNodeService(String hostname) throws Exception {
    	//here is the place to offer an auth question
        String[] args = SocketFunctions.sendData("OK", writer, reader);
        
        if (args.length == 1 && args[0].equals("GETBNS")) {
             RedNodeFunctions.getAllConnectedBlueNodes(reader, writer, socket);
        } else if (args.length == 1 && args[0].equals("GETRBN")) {
            RedNodeFunctions.getRecomendedBlueNode(reader, writer, socket);
        } else if (args.length == 3 && args[0].equals("OFFERPUB")) { 
        	//rednode offers its pubkey based on a ticket
        	RedNodeFunctions.offerPublicKey(hostname, args[1], args[2], writer);
        } else if (args.length == 1 && args[0].equals("REVOKEPUB")) {
        	//rednode may be compromised and decides to revoke its public
        	RedNodeFunctions.revokePublicKey(hostname, writer);        
        } else {
            String data = "WRONG_COMMAND";
            SocketFunctions.sendFinalData(data, writer);
        }
        SocketFunctions.connectionClose(socket);
    }    
}
