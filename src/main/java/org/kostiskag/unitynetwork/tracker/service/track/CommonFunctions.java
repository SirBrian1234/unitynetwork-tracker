package org.kostiskag.unitynetwork.tracker.service.track;

import java.io.DataOutputStream;
import java.security.PublicKey;
import java.util.concurrent.locks.Lock;

import javax.crypto.SecretKey;

import org.kostiskag.unitynetwork.tracker.App;
import org.kostiskag.unitynetwork.tracker.functions.CryptoMethods;
import org.kostiskag.unitynetwork.tracker.functions.SocketFunctions;
import org.kostiskag.unitynetwork.tracker.rundata.BlueNodeTable;

public class CommonFunctions {
	
	/**
	 * For speed and for security the public keys should be retrieved from the active network table.
	 * In order from the one hand, to limit the queries in the database only for the login process and thus
	 * limit the level of its exposure. From the other hand, to offer pub keys only for online bluenodes. 
	 * Finally, its faster to retrieve from the active table. 
	 * 
	 * @param BlueNodeName
	 * @param writer
	 * @param sessionKey
	 */
	public static void getBlueNodesPublic(Lock lock, String BlueNodeName, DataOutputStream writer, SecretKey sessionKey) throws InterruptedException {
		if (BlueNodeTable.getInstance().checkOnlineByName(lock, BlueNodeName)) {
			try {
				PublicKey pub = BlueNodeTable.getInstance().getBlueNodeEntryByHn(lock, BlueNodeName).getPub();
				SocketFunctions.sendAESEncryptedStringData(CryptoMethods.objectToBase64StringRepresentation(pub), writer, sessionKey);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			SocketFunctions.sendAESEncryptedStringData("NONE", writer, sessionKey);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void getRedNodesPublic(String hostname, DataOutputStream writer, SecretKey sessionKey) {
		try {
			PublicKey pub = RedNodeFunctions.fetchPubKey(hostname);
			if (pub != null) {
				try {
					SocketFunctions.sendAESEncryptedStringData(CryptoMethods.objectToBase64StringRepresentation(pub), writer, sessionKey);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				try {
					SocketFunctions.sendAESEncryptedStringData("NONE", writer, sessionKey);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			try {
				SocketFunctions.sendAESEncryptedStringData("NONE", writer, sessionKey);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
