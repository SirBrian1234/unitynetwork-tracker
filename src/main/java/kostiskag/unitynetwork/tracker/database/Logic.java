package kostiskag.unitynetwork.tracker.database;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;

import kostiskag.unitynetwork.tracker.App;
import kostiskag.unitynetwork.tracker.functions.HashFunctions;

/**
 * The database upper logic which calls methods from Queries
 * 
 * @author kosti
 */
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
		try {
			password = HashFunctions.SHA256(App.SALT + password);
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e2) {
			e2.printStackTrace();
			return;
		}
		
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
		try {
			password = HashFunctions.SHA256(App.SALT + password);
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e2) {
			e2.printStackTrace();
			return;
		}
		
		Queries q = null;
		try {
			q = new Queries();
			q.updateEntryUsersWithUsername(username, password, scope, fullname);
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
	
	public static LinkedList<String[][]> buildGUIObject() {
		// reload database on gui
		LinkedList<String[][]> list = new LinkedList<>();
		String[][] usersDbData = new String[5][1];
		String[][] hostnamesDbData = new String[3][1];
		String[][] blunodesDbData = new String[2][1];
		ResultSet bns = null, hnms = null, usrs = null;
		Queries q = null;

		try {
			q = new Queries();
			usrs = q.selectAllFromUsers();
			hnms = q.selectAllFromHostnames();
			bns = q.selectAllFromBluenodes();

			ArrayList<String[]> usrsList = new ArrayList<String[]>();
			ArrayList<String[]> hnmsList = new ArrayList<String[]>();
			ArrayList<String[]> bnsList = new ArrayList<String[]>();

			int i = 0;
			while (usrs.next()) {
				String entry[] = new String[5];
				entry[0] = new String("" + usrs.getInt("id"));
				entry[1] = new String(usrs.getString("username"));
				entry[2] = new String(usrs.getString("password"));
				int scope = usrs.getInt("scope");
				if (scope == 0) {
					entry[3] = "system";
				} else if (scope == 1) {
					entry[3] = "user";
				} else if (scope == 2) {
					entry[3] = "robot";
				} else if (scope == 3) {
					entry[3] = "gov/org/comp";
				}
				entry[4] = new String(usrs.getString("fullname"));
				usrsList.add(entry);
				i++;
			}

			usersDbData = new String[usrsList.size()][5];
			i = 0;
			while (i < usrsList.size()) {
				usersDbData[i] = usrsList.get(i);
				i++;
			}

			i = 0;
			while (hnms.next()) {
				String entry[] = new String[3];
				entry[0] = "" + hnms.getInt("address");
				entry[1] = hnms.getString("hostname");
				entry[2] = "" + hnms.getInt("userid");
				hnmsList.add(entry);
				i++;
			}

			hostnamesDbData = new String[hnmsList.size()][3];
			i = 0;
			while (i < hnmsList.size()) {
				hostnamesDbData[i] = hnmsList.get(i);
				i++;
			}

			i = 0;
			while (bns.next()) {
				String entry[] = new String[2];
				entry[0] = bns.getString("name");
				entry[1] = "" + bns.getInt("userid");
				bnsList.add(entry);
				i++;
			}

			blunodesDbData = new String[bnsList.size()][2];
			i = 0;
			while (i < bnsList.size()) {
				blunodesDbData[i] = bnsList.get(i);
				i++;
			}

			q.closeQueries();
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				q.closeQueries();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		list.add(usersDbData);
		list.add(hostnamesDbData);
		list.add(blunodesDbData);
		return list;
	}
}
