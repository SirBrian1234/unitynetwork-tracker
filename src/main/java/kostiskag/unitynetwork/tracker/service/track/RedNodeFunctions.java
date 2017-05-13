package kostiskag.unitynetwork.tracker.service.track;

import java.io.DataInputStream;
import java.io.DataOutputStream;
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

	public static void getRecomendedBlueNode(DataInputStream reader, DataOutputStream writer, Socket socket) throws Exception {
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
		SocketFunctions.sendStringlData(data, writer);
	}

	static void getAllConnectedBlueNodes(DataInputStream reader, DataOutputStream writer, Socket socket) throws Exception {
		int size = App.BNtable.getSize();
		if (App.BNtable.getSize() > 0) {
			SocketFunctions.sendStringlData("SENDING_BLUENODES " + size, writer);
			String fetched[][] = App.BNtable.buildStringInstanceObject();
			int i = 0;
			try {
				while(fetched[i] != null) {			
					SocketFunctions.sendStringlData(fetched[i][0] + " " + fetched[i][1] + " " + fetched[i][2] + " " + fetched[i][3], writer);
					i++;
				}	
			} catch (ArrayIndexOutOfBoundsException ex) {
				
			}
			SocketFunctions.sendStringlData("", writer);
		} else {
			String data = "NONE";
			SocketFunctions.sendStringlData(data, writer);
		}
	}

	public static void offerPublicKey(String hostname, String ticket, String publicKey, DataOutputStream writer) {
		Queries q = null;
		try {
			q = new Queries();
			ResultSet r = q.selectAllFromHostnamesWhereHostname(hostname);
			if (r.next()) {
				String storedKey = r.getString("public");
				String args[] = storedKey.split("\\s+");
				if (args[0].equals("NOT_SET") && args[1].equals(ticket)) {
					q.updateEntryHostnamesPublicWithHostname(hostname, "KEY_SET"+" "+publicKey);
					try {
						SocketFunctions.sendStringlData("KEY_SET", writer);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else if (args[0].equals("KEY_SET")) {
					try {
						SocketFunctions.sendStringlData("KEY_IS_SET", writer);
					} catch (Exception e) {
						e.printStackTrace();
					}
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
		try {
			SocketFunctions.sendStringlData("NOT_SET",writer);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void revokePublicKey(String hostname, DataOutputStream writer) {
		String key = "NOT_SET "+CryptoMethods.generateQuestion();
		Queries q = null;
		try {
			q = new Queries();
			q.updateEntryHostnamesPublicWithHostname(hostname, key);
			q.closeQueries();
			try {
				SocketFunctions.sendStringlData("KEY_REVOKED", writer);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				q.closeQueries();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		try {
			SocketFunctions.sendStringlData("NOT_SET", writer);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
