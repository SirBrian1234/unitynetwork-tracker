package org.kostiskag.unitynetwork.tracker.service;

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
	public static PublicKey fetchPubKey(String BlueNodeHostname) throws IllegalAccessException, SQLException {
		Queries q = null;
		ResultSet getResults;
		try {
			q = new Queries();
			getResults = q.selectAllFromBluenodes();

			while (getResults.next()) {
				if (getResults.getString("name").equals(BlueNodeHostname)) {									
					String key = getResults.getString("public");
					q.closeQueries();
					String[] parts = key.split("\\s+");
					if (parts[0].equals("NOT_SET")) {
						return null;
					} else {
						return (PublicKey) CryptoUtilities.base64StringRepresentationToObject(parts[1]);
					}				
				}
			}
			q.closeQueries();
			throw new IllegalAccessException("The Bn "+BlueNodeHostname+" is not a network member.");

		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
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
					if (BlueNodeTable.getInstance().isOnline(lock, BlueNodeHostname)) {
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
