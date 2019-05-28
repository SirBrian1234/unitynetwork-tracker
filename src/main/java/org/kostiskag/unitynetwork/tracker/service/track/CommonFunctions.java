package org.kostiskag.unitynetwork.tracker.service.track;

import java.io.DataOutputStream;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import org.kostiskag.unitynetwork.tracker.App;
import org.kostiskag.unitynetwork.tracker.functions.CryptoMethods;
import org.kostiskag.unitynetwork.tracker.functions.SocketFunctions;

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
	public static void getBlueNodesPublic(String BlueNodeName, DataOutputStream writer, SecretKey sessionKey) {
		if (App.TRACKER_APP.BNtable.checkOnlineByName(BlueNodeName)) {
			try {
				PublicKey pub = App.TRACKER_APP.BNtable.getBlueNodeEntryByHn(BlueNodeName).getPub();
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
