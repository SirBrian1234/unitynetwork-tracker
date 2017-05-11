package kostiskag.unitynetwork.tracker.service.track;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;

import kostiskag.unitynetwork.tracker.App;
import kostiskag.unitynetwork.tracker.database.Queries;
import kostiskag.unitynetwork.tracker.functions.CryptoMethods;
import kostiskag.unitynetwork.tracker.functions.SocketFunctions;
import kostiskag.unitynetwork.tracker.runData.BlueNodeEntry;

/**
* (unregistered) Rednode queries:
*
* GETBNS
* GETRBN
* 
* @author Konstantinos Kagiampakis
*/
public class RedNodeFunctions {

	public static void getRecomendedBlueNode(BufferedReader reader, PrintWriter writer, Socket socket) throws Exception {
		String data;
		if (App.BNtable.getSize() > 0) {
			BlueNodeEntry recomended = App.BNtable.getBlueNodeEntryByLowestLoad();
			String hostname = recomended.getName();
			String phaddress = recomended.getPhaddress();
			int port = recomended.getPort();
			int load = recomended.getLoad();
			data = hostname + " " + phaddress + " " + port + " " + load;
		} else {
			data = "NONE";
		}
		SocketFunctions.sendFinalData(data, writer);
	}

	static void getAllConnectedBlueNodes(BufferedReader reader, PrintWriter writer, Socket socket) throws Exception {
		int size = App.BNtable.getSize();
		if (App.BNtable.getSize() > 0) {
			SocketFunctions.sendFinalData("SENDING_BLUENODES " + size, writer);
			String fetched[][] = App.BNtable.buildStringInstanceObject();
			int i = 0;
			try {
				while(fetched[i] != null) {			
					SocketFunctions.sendFinalData(fetched[i][0] + " " + fetched[i][1] + " " + fetched[i][2] + " " + fetched[i][3], writer);
					i++;
				}	
			} catch (ArrayIndexOutOfBoundsException ex) {
				
			}
			SocketFunctions.sendFinalData("", writer);
		} else {
			String data = "NONE";
			SocketFunctions.sendFinalData(data, writer);
		}
	}

	public static void offerPublicKey(String hostname, String ticket, String publicKey, PrintWriter writer) {
		Queries q = null;
		try {
			q = new Queries();
			ResultSet r = q.selectAllFromHostnamesWhereHostname(hostname);
			if (r.next()) {
				String storedKey = r.getString("public");
				String args[] = storedKey.split("\\s+");
				if (args[0].equals("NOT_SET") && args[1].equals(ticket)) {
					q.updateEntryHostnamesPublicWithHostname(hostname, "KEY_SET"+" "+publicKey);
					writer.println("KEY_SET");
				} else if (args[0].equals("KEY_SET")) {
					writer.println("KEY_IS_SET");
				}
			}
			q.closeQueries();
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				q.closeQueries();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		writer.println("NOT_SET");
	}

	public static void revokePublicKey(String hostname, PrintWriter writer) {
		String key = "NOT_SET "+CryptoMethods.generateQuestion();
		Queries q = null;
		try {
			q = new Queries();
			q.updateEntryHostnamesPublicWithHostname(hostname, key);
			q.closeQueries();
			writer.println("KEY_REVOKED");
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				q.closeQueries();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		writer.println("NOT_SET");
	}
}
