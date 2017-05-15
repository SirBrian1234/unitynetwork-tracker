package kostiskag.unitynetwork.tracker.service;

import java.security.PublicKey;
import java.sql.ResultSet;
import java.sql.SQLException;
import kostiskag.unitynetwork.tracker.App;
import kostiskag.unitynetwork.tracker.database.Queries;
import kostiskag.unitynetwork.tracker.functions.CryptoMethods;

/**
 * 
 * @author Konstantinos Kagiampakis
 */
public class BlueNodeGlobalFunctions {
	
	/**
	 * Collects a bluenode's public key.
	 * 
	 * @returns the public key if its set OR null for a not set key
	 * @throws exception when fetch bn key is called for a non member
	 */
	public static PublicKey fetchPubKey(String BlueNodeHostname) throws Exception {
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
						return (PublicKey) CryptoMethods.base64StringRepresentationToObject(parts[1]);
					}				
				}
			}
			q.closeQueries();
			throw new Exception("The Bn "+BlueNodeHostname+" is not a network member.");

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
	
	/**
	 * Provides the level of authorization for a bluenode.
	 * 
	 * @returns 1 for an already leased member, 0 for a non leased member, -1 for a non member
	 */
	public static int authBluenode(String BlueNodeHostname) {
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
					if (App.BNtable.checkOnlineByName(BlueNodeHostname)) {						
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
