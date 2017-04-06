package kostiskag.unitynetwork.tracker.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

public class Logic {
	
	public static void addNewUser(String username, String password, int scope, String fullname) throws SQLException {
		//we have to provide a salted and hashed password in the db along with the rest of the updates
		//to do in the hash branch
		//pass = hash(salt+pass)
		Queries q = new Queries();
		q.insertEntryUsers(username, password, scope, fullname);
		q.closeQueries();
	}
	
	public static void updateUserAndPassword(String username, String password, int scope, String fullname) throws SQLException {
		//we have to provide a salted and hashed password in the db along with the rest of the updates
		//to do in the hash branch
		//pass = hash(salt+pass)
		Queries q = new Queries();
		q.insertEntryUsers(username, password, scope, fullname);
		q.closeQueries();
	}
	
	public static void removeUserAndAllHisItems(String username) throws SQLException {
		Queries q = new Queries();
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
	}
	
	public static void addNewHostname(String hostname, int userid) throws Exception {
		Queries q = new Queries();
		if (q.checkIfUserWithIdExists(userid)) {
			ResultSet r = q.selectAddressFromBurned();
			if (r == null ){
				System.out.println(">>>>null");
			}
			if (r.next()) {
				int address = r.getInt("address");
				System.out.println("wiii "+address);
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
	}
	
	public static void updateHostname(String hostname, int userid) throws Exception {
		Queries q = new Queries();
		if (q.checkIfUserWithIdExists(userid)) {
			q.updateEntryHostnamesWithHostname(hostname, userid);
		} else {
			q.closeQueries();
			throw new Exception("no user found");
		}
		q.closeQueries();
	}
	
	public static void removeHostname(String hostname) throws SQLException {
		Queries q = new Queries();
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
	}
	
	public static void removeBluenode(String name) throws SQLException {
		Queries q = new Queries();
		q.deleteEntryBluenodesWitName(name);
		q.closeQueries();
	}
}
