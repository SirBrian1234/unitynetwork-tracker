package org.kostiskag.unitynetwork.tracker.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.locks.Lock;

import org.kostiskag.unitynetwork.tracker.database.Queries;
import org.kostiskag.unitynetwork.tracker.utilities.CryptoUtilities;
import org.kostiskag.unitynetwork.tracker.rundata.table.BlueNodeTable;

/**
 * 
 * @author Konstantinos Kagiampakis
 */
public class BlueNodeGlobalFunctions {
	
	/**
	 * Collects a bluenode's public key.
	 * 
	 * @returns the public key if its set OR null for a not set key
	 * @throws  IllegalAccessException when fetch bn key is called for a non member
	 * @throws  SQLException can not connect to database or db error
	 */
	public static PublicKey fetchPubKey(String BlueNodeHostname) throws IllegalAccessException, GeneralSecurityException, IOException {
		PublicKey pub = null;
		boolean found = false;
		Queries q = null;
		try {
			q = new Queries();
			ResultSet getResults = q.selectAllFromBluenodes();

			while (getResults.next()) {
				if (getResults.getString("name").equals(BlueNodeHostname)) {
					found = true;
					String key = getResults.getString("public");
					String[] parts = key.split("\\s+");
					if (!parts[0].equals("NOT_SET")) {
						pub = (PublicKey) CryptoUtilities.base64StringRepresentationToObject(parts[1]);
					}
					//break; //remember to break if you dont return!
				}
			}

			if (!found) {
				throw new IllegalAccessException("The Bn " + BlueNodeHostname + " is not a network member.");
			}
			return pub;

		} catch (SQLException e) {
			return null;
		} finally {
			try {
				q.closeQueries();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	/**
	 * Provides the level of authorization for a bluenode.
	 * 
	 * @returns 1 for an already leased member, 0 for a non leased member, -1 for a non member
	 */
	public static int authBluenode(Lock lock, String BlueNodeHostname) throws InterruptedException {
		Queries q = null;
		ResultSet getResults;
		try {
			q = new Queries();
			getResults = q.selectNameFromBluenodes();

			if (getResults == null) {
				return -2;
			}

			while (getResults.next()) {
				if (getResults.getString("name").equals(BlueNodeHostname)) {
					q.closeQueries();
					if (BlueNodeTable.getInstance().getOptionalNodeEntry(lock, BlueNodeHostname).isPresent()) {
						return 1;
					} else {						
						return 0;
					}
				}
			}
			q.closeQueries();
			return -1;

		} catch (SQLException e) {
			e.printStackTrace();
			try {
				q.closeQueries();
			} catch (SQLException e1) {
				e1.printStackTrace();				
			}
			return -2;
		}
	}
}
