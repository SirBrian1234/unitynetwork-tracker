package org.kostiskag.unitynetwork.tracker.service.track;

import java.io.DataOutputStream;
import java.io.IOException;
import java.security.PublicKey;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.locks.Lock;

import javax.crypto.SecretKey;

import org.kostiskag.unitynetwork.tracker.App;
import org.kostiskag.unitynetwork.tracker.database.Queries;
import org.kostiskag.unitynetwork.tracker.functions.CryptoMethods;
import org.kostiskag.unitynetwork.tracker.functions.SocketFunctions;
import org.kostiskag.unitynetwork.tracker.rundata.BlueNodeEntry;
import org.kostiskag.unitynetwork.tracker.rundata.BlueNodeTable;

/**
* Rednode queries:
*
* GETBNS
* GETRBN
* REVOKEPUB
* OFFERPUB
* 
* @author Konstantinos Kagiampakis
*/
public class RedNodeFunctions {

	/*
	 * To be changed from deprecated methods
	 */
	public static void getRecomendedBlueNode(Lock lock, DataOutputStream writer, SecretKey sessionKey) throws InterruptedException, IOException {
		String data;
		if (BlueNodeTable.getInstance().getSize(lock) > 0) {
			BlueNodeEntry recomended = BlueNodeTable.getInstance().getBlueNodeEntryByLowestLoad(lock);
			String hostname = recomended.getName();
			String phaddress = recomended.getPhAddress().asString();
			int port = recomended.getPort();
			int load = recomended.getLoad();
			String pubkey = CryptoMethods.objectToBase64StringRepresentation(recomended.getPub());
			data = hostname+" "+phaddress+" "+port+" "+load+" "+pubkey;
		} else {
			data = "NONE";
		}
		SocketFunctions.sendAESEncryptedStringData(data, writer, sessionKey);
	}

	public static void getAllConnectedBlueNodes(Lock lock, DataOutputStream writer, SecretKey sessionKey) throws InterruptedException, IOException {
		int size = BlueNodeTable.getInstance().getSize(lock);
		StringBuilder str = new StringBuilder();
		str.append("SENDING_BLUENODES " + size+"\n");
		
		String fetched[][] = BlueNodeTable.getInstance().buildStringInstanceObject(lock);
		for(int i=0; i<fetched.length; i++) {			
			str.append(fetched[i][0]+" "+fetched[i][1]+" "+fetched[i][2]+" "+fetched[i][3]+"\n");
		}	
		SocketFunctions.sendAESEncryptedStringData(str.toString(), writer, sessionKey);
	}
	
	/*
	 * To be changed from send plain string data into AES
	 */
	public static void offerPublicKey(String hostname, String ticket, String publicKey, DataOutputStream writer, SecretKey sessionKey) {
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
						SocketFunctions.sendAESEncryptedStringData("KEY_SET", writer, sessionKey);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else if (args[0].equals("KEY_SET")) {
					try {
						SocketFunctions.sendAESEncryptedStringData("KEY_IS_SET", writer, sessionKey);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					try {
						SocketFunctions.sendAESEncryptedStringData("WRONG_TICKET", writer, sessionKey);
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
			SocketFunctions.sendAESEncryptedStringData("NOT_SET",writer, sessionKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void revokePublicKey(String hostname, DataOutputStream writer, SecretKey sessionKey) {
		String key = "NOT_SET "+CryptoMethods.generateQuestion();
		Queries q = null;
		try {
			q = new Queries();
			q.updateEntryHostnamesPublicWithHostname(hostname, key);
			q.closeQueries();
			try {
				SocketFunctions.sendAESEncryptedStringData("KEY_REVOKED", writer, sessionKey);
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
			SocketFunctions.sendAESEncryptedStringData("NOT_SET", writer, sessionKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static PublicKey fetchPubKey(String hostname) throws Exception {
		Queries q = null;
		ResultSet getResults;
		try {
			q = new Queries();
			getResults = q.selectAllFromHostnames();

			while (getResults.next()) {
				if (getResults.getString("hostname").equals(hostname)) {									
					String key = getResults.getString("public");
					q.closeQueries();
					String[] parts = key.split("\\s+");
					if (parts[0].equals("NOT_SET")) {
						return null;
					} else {
						return (PublicKey) CryptoMethods.base64StringRepresentationToObject(parts[1]);
					}				
				}
			}
			q.closeQueries();
			throw new Exception("The RN "+hostname+" is not a network member.");
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				q.closeQueries();
			} catch (SQLException e1) {
				e1.printStackTrace();				
			}
			throw e;
		}
	}
}
