package kostiskag.unitynetwork.tracker.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import kostiskag.unitynetwork.tracker.App;
import kostiskag.unitynetwork.tracker.database.Queries;

/**
 * 
 * @author Konstantinos Kagiampakis
 */
public class BlueNodeGlobalFunctions {
	
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
