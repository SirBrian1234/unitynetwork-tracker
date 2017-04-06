package kostiskag.unitynetwork.tracker.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

public class Logic {
	
	/*
	 * the prblem is that when an exception is occured 
	 * the queries obj remains open and needs to close
	 * this is why we have to tory to close the obj before leaving the method
	 * so the method cathes the exception to close it and then throws it to the
	 * unknown in order to catch it from the method caller as well 
	 */
	
	public static void addNewUser(String username, String password, int scope, String fullname) throws SQLException{
		//we have to provide a salted and hashed password in the db along with the rest of the updates
		//to do in the hash branch
		//pass = hash(salt+pass)
		Queries q = null;
		try {
			q = new Queries();
			q.insertEntryUsers(username, password, scope, fullname);
			q.closeQueries();
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
	
	public static void updateUserAndPassword(String username, String password, int scope, String fullname) throws SQLException {
		//we have to provide a salted and hashed password in the db along with the rest of the updates
		//to do in the hash branch
		//pass = hash(salt+pass)
		Queries q = null;
		try {
			q = new Queries();
			q.insertEntryUsers(username, password, scope, fullname);
			q.closeQueries();
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
	
	public static void removeUserAndAllHisItems(String username) throws SQLException {
		Queries q = null;
		try {
			q = new Queries();
			int id = 0;
			LinkedList li = new LinkedList<>();
			ResultSet r = q.selectIdFromUsersWhereUsername(username);
			if (r.next()) {
				id = r.getInt("id");
				r = q.selectAllFromHostnamesWhereUserid(id);
				if (r.next()) {
					int address = r.getInt("address");
					li.add(address);
				}
				q.deleteAllHostnamesWithUserid(id);
				while(!li.isEmpty()) {
					int address = (int) li.pop();
					q.insertEntryBurned(address);
				}
				q.deleteAllBluenodesWithUserid(id);
				q.deleteEntryUsersWithUsername(username);
			}
			q.closeQueries();
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
	
	public static void addNewHostname(String hostname, int userid) throws Exception {
		Queries q = null;
		try {
			q = new Queries();
			if (q.checkIfUserWithIdExists(userid)) {
				ResultSet r = q.selectAddressFromBurned();
				if (r.next()) {
					int address = r.getInt("address");
					q.deleteEntryAddressFromBurned(address);
					q.insertEntryHostnamesWithAddr(address, hostname, userid);										
				} else {
					System.out.println("no address");
					q.insertEntryHostnamesNoAddr(hostname, userid);
				}
			} else {
				q.closeQueries();
				throw new Exception("no user found");
			}
			q.closeQueries();
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
	
	public static void updateHostname(String hostname, int userid) throws Exception {
		
		Queries q = null;
		try {
			q = new Queries();
			if (q.checkIfUserWithIdExists(userid)) {
				q.updateEntryHostnamesWithHostname(hostname, userid);
			} else {
				q.closeQueries();
				throw new Exception("no user found");
			}
			q.closeQueries();
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
	
	public static void removeHostname(String hostname) throws SQLException {
		Queries q = null;
		try {
			q = new Queries();
			int addressToStore = 0;
			ResultSet r = q.selectAddressFromHostnamesWithHostname(hostname);
			if (r.next()) {
				addressToStore = r.getInt("address");
				q.deleteEntryHostnamesWithHostname(hostname);
				q.insertEntryBurned(addressToStore);
			} else {
				q.deleteEntryHostnamesWithHostname(hostname);
			}
			q.closeQueries();
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
	
	public static void removeBluenode(String name) throws SQLException {
		Queries q = null;
		try {
			q =new Queries();
			q.deleteEntryBluenodesWitName(name);
			q.closeQueries();
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
}
