package org.kostiskag.unitynetwork.tracker.service.track;

import java.io.DataOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.locks.Lock;

import javax.crypto.SecretKey;

import org.kostiskag.unitynetwork.common.utilities.CryptoUtilities;
import org.kostiskag.unitynetwork.common.utilities.SocketUtilities;

import org.kostiskag.unitynetwork.tracker.rundata.entry.BlueNodeEntry;
import org.kostiskag.unitynetwork.tracker.rundata.table.BlueNodeTable;

/**
* Rednode queries:
*
* getARecomendedBlueNode
* getAllConnectedBlueNodes
* offerPublicKey
* revokePublicKey
* 
* @author Konstantinos Kagiampakis
*/
final class RedNodeActions {

	public static void getARecomendedBlueNode(Lock lock, DataOutputStream writer, SecretKey sessionKey) throws InterruptedException, GeneralSecurityException, IOException {
		String data;
		if (BlueNodeTable.getInstance().getSize(lock) > 0) {
			BlueNodeEntry recomended = BlueNodeTable.getInstance().getBlueNodeEntryByLowestLoad(lock);
			String hostname = recomended.getHostname();
			String phaddress = recomended.getAddress().asString();
			int port = recomended.getPort();
			int load = recomended.getLoad();
			String pubkey = CryptoUtilities.objectToBase64StringRepresentation(recomended.getPub());
			data = hostname+" "+phaddress+" "+port+" "+load+" "+pubkey;
		} else {
			data = "NONE";
		}
		SocketUtilities.sendAESEncryptedStringData(data, writer, sessionKey);
	}

	public static void getAllConnectedBlueNodes(Lock lock, DataOutputStream writer, SecretKey sessionKey) throws InterruptedException, GeneralSecurityException, IOException {
		int size = BlueNodeTable.getInstance().getSize(lock);
		StringBuilder str = new StringBuilder();
		str.append("SENDING_BLUENODES " + size+"\n");
		
		String fetched[][] = BlueNodeTable.getInstance().buildStringInstanceObject(lock);
		for(int i=0; i<fetched.length; i++) {			
			str.append(fetched[i][0]+" "+fetched[i][1]+" "+fetched[i][2]+" "+fetched[i][3]+"\n");
		}	
		SocketUtilities.sendAESEncryptedStringData(str.toString(), writer, sessionKey);
	}
}
