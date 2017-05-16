package kostiskag.unitynetwork.tracker.service.track;

import java.io.DataOutputStream;
import java.security.PublicKey;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.crypto.SecretKey;

import kostiskag.unitynetwork.tracker.App;
import kostiskag.unitynetwork.tracker.database.Queries;
import kostiskag.unitynetwork.tracker.functions.CryptoMethods;
import kostiskag.unitynetwork.tracker.functions.SocketFunctions;
import kostiskag.unitynetwork.tracker.runData.BlueNodeEntry;

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
	public static void getRecomendedBlueNode(DataOutputStream writer, SecretKey sessionKey) throws Exception {
		String data;
		if (App.BNtable.getSize() > 0) {
			BlueNodeEntry recomended = App.BNtable.getBlueNodeEntryByLowestLoad();
			String hostname = recomended.getName();
			String phaddress = recomended.getPhaddress();
			int port = recomended.getPort();
			int load = recomended.getLoad();
			String pubkey = CryptoMethods.objectToBase64StringRepresentation(recomended.getPub());
			data = hostname+" "+phaddress+" "+port+" "+load+" "+pubkey;
		} else {
			data = "NONE";
		}
		SocketFunctions.sendAESEncryptedStringData(data, writer, sessionKey);
	}

	static void getAllConnectedBlueNodes(DataOutputStream writer, SecretKey sessionKey) throws Exception {
		int size = App.BNtable.getSize();
		if (App.BNtable.getSize() > 0) {
			SocketFunctions.sendAESEncryptedStringData("SENDING_BLUENODES " + size, writer, sessionKey);
			String fetched[][] = App.BNtable.buildStringInstanceObject();
			int i = 0;
			try {
				while(fetched[i] != null) {			
					SocketFunctions.sendAESEncryptedStringData(fetched[i][0] + " " + fetched[i][1] + " " + fetched[i][2] + " " + fetched[i][3], writer, sessionKey);
					i++;
				}	
			} catch (ArrayIndexOutOfBoundsException ex) {
				
			}
			SocketFunctions.sendAESEncryptedStringData("", writer, sessionKey);
		} else {
			SocketFunctions.sendAESEncryptedStringData("NONE", writer, sessionKey);
		}
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
