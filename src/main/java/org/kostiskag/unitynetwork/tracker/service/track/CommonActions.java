package org.kostiskag.unitynetwork.tracker.service.track;

import java.io.DataOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.locks.Lock;

import javax.crypto.SecretKey;

import org.kostiskag.unitynetwork.common.entry.NodeType;
import org.kostiskag.unitynetwork.common.state.PublicKeyState;
import org.kostiskag.unitynetwork.common.utilities.CryptoUtilities;
import org.kostiskag.unitynetwork.common.utilities.SocketUtilities;

import org.kostiskag.unitynetwork.tracker.database.Logic;
import org.kostiskag.unitynetwork.tracker.rundata.entry.BlueNodeEntry;
import org.kostiskag.unitynetwork.tracker.rundata.table.BlueNodeTable;

final class CommonActions {
	
	/**
	 * For speed and for security the public keys should be retrieved from the active network table.
	 * In order from the one hand, to limit the queries in the database only for the login process and thus
	 * limit the level of its exposure. From the other hand, to offer pub keys only for online bluenodes. 
	 *
	 * @param BlueNodeName
	 * @param writer
	 * @param sessionKey
	 */
	public static void getBlueNodesPublic(Lock lock, String BlueNodeName, DataOutputStream writer, SecretKey sessionKey) throws InterruptedException, GeneralSecurityException, IOException {
		Optional<BlueNodeEntry> b = BlueNodeTable.getInstance().getOptionalNodeEntry(lock, BlueNodeName);
		if (b.isPresent()) {
			PublicKey pub = b.get().getPub();
			SocketUtilities.sendAESEncryptedStringData(CryptoUtilities.objectToBase64StringRepresentation(pub), writer, sessionKey);
		} else {
			SocketUtilities.sendAESEncryptedStringData("NONE", writer, sessionKey);
		}
	}

	public static void getRedNodesPublic(String hostname, DataOutputStream writer, SecretKey sessionKey) throws InterruptedException, GeneralSecurityException, IllegalAccessException, SQLException, IOException {
		try {
			Optional<PublicKey> pubOpt = Logic.fetchPublicKey(NodeType.REDNODE, hostname);
			if (pubOpt.isPresent()) {
				SocketUtilities.sendAESEncryptedStringData(CryptoUtilities.objectToBase64StringRepresentation(pubOpt.get()), writer, sessionKey);
			} else {
				SocketUtilities.sendAESEncryptedStringData("NONE", writer, sessionKey);
			}
		} catch (IOException | GeneralSecurityException | SQLException e) {
			try {
				SocketUtilities.sendAESEncryptedStringData("NONE", writer, sessionKey);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			throw e;
		}
	}

	public static void offerPublicKey(NodeType type, String hostname, String ticket, String publicKey, DataOutputStream writer, SecretKey sessionKey) throws GeneralSecurityException, IOException {
		PublicKeyState k = PublicKeyState.NOT_SET;
		try {
			k = Logic.offerPublicKey(type, hostname, ticket, publicKey);
		} catch (SQLException | InterruptedException e) {
			k = PublicKeyState.NOT_SET;
		} finally {
			SocketUtilities.sendAESEncryptedStringData(k.toString(), writer, sessionKey);
		}
	}

	public static void revokePublicKey(NodeType type, String hostname, DataOutputStream writer, SecretKey sessionKey) throws GeneralSecurityException, IOException {
		PublicKeyState answer = PublicKeyState.SYSTEM_ERROR;
		try {
			Logic.revokePublicKey(type, hostname);
			answer = PublicKeyState.KEY_REVOKED;
		} catch (InterruptedException | SQLException e) {
			answer = PublicKeyState.SYSTEM_ERROR;
		} finally {
			SocketUtilities.sendAESEncryptedStringData(answer.toString(), writer, sessionKey);
		}
	}
}
