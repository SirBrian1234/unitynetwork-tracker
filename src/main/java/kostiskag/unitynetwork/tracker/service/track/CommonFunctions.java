package kostiskag.unitynetwork.tracker.service.track;

import java.io.DataOutputStream;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import kostiskag.unitynetwork.tracker.App;
import kostiskag.unitynetwork.tracker.functions.CryptoMethods;
import kostiskag.unitynetwork.tracker.functions.SocketFunctions;

public class CommonFunctions {
	public static void getBlueNodesPublic(String BlueNodeName, DataOutputStream writer, SecretKey sessionKey) {
		if (App.BNtable.checkOnlineByName(BlueNodeName)) {
			try {
				PublicKey pub = App.BNtable.getBlueNodeEntryByHn(BlueNodeName).getPub();
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