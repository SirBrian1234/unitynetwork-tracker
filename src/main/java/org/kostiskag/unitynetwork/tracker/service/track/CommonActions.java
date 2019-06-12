package org.kostiskag.unitynetwork.tracker.service.track;

import java.io.DataOutputStream;
import java.security.PublicKey;
import java.util.concurrent.locks.Lock;

import javax.crypto.SecretKey;

import org.kostiskag.unitynetwork.tracker.utilities.CryptoUtilities;
import org.kostiskag.unitynetwork.tracker.utilities.SocketUtilities;
import org.kostiskag.unitynetwork.tracker.rundata.table.BlueNodeTable;

public class CommonActions {
	
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
		if (BlueNodeTable.getInstance().isOnline(lock, BlueNodeName)) {
			try {
				PublicKey pub = BlueNodeTable.getInstance().getNodeEntry(lock, BlueNodeName).getPub();
				SocketUtilities.sendAESEncryptedStringData(CryptoUtilities.objectToBase64StringRepresentation(pub), writer, sessionKey);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			SocketUtilities.sendAESEncryptedStringData("NONE", writer, sessionKey);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void getRedNodesPublic(String hostname, DataOutputStream writer, SecretKey sessionKey) {
		try {
			PublicKey pub = RedNodeActions.fetchPubKey(hostname);
			if (pub != null) {
				try {
					SocketUtilities.sendAESEncryptedStringData(CryptoUtilities.objectToBase64StringRepresentation(pub), writer, sessionKey);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				try {
					SocketUtilities.sendAESEncryptedStringData("NONE", writer, sessionKey);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			try {
				SocketUtilities.sendAESEncryptedStringData("NONE", writer, sessionKey);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
