package org.kostiskag.unitynetwork.tracker.database;

import java.security.GeneralSecurityException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;

import org.kostiskag.unitynetwork.common.utilities.CryptoUtilities;
import org.kostiskag.unitynetwork.common.utilities.HashUtilities;

import org.kostiskag.unitynetwork.tracker.App;
import org.kostiskag.unitynetwork.tracker.AppLogger;


/**
 * The database upper logic which calls methods from Queries
 * 
 * @author Konstantinos Kagiampakis
 */
public final class Logic {
	
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
			password = HashUtilities.SHA256(App.SALT + password);
		} catch (GeneralSecurityException e) {
			AppLogger.getLogger().consolePrint(Queries.class.getSimpleName() +": " + e.getLocalizedMessage());
			return;
		}
		//repetitive code

		try (Queries q = Queries.getInstance();) {
			q.insertEntryUsers(username, password, scope, fullname);
		}  catch (InterruptedException e) {
			AppLogger.getLogger().consolePrint("Could not acquire lock!");
		} catch (SQLException e) {
			AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
		}
	}
	
	public static void updateUserAndPassword(String username, String password, int scope, String fullname) throws SQLException {
		//we have to provide a salted and hashed password in the db along with the rest of the updates
		//to do in the hash branch
		//pass = hash(salt+pass)
		try {
			password = HashUtilities.SHA256(App.SALT + password);
		} catch (GeneralSecurityException e2) {
			e2.printStackTrace();
			return;
		}
		//repetitive code

		try (Queries q = Queries.getInstance();) {
			q.updateEntryUsersWithUsername(username, password, scope, fullname);
		} catch (InterruptedException e) {
			AppLogger.getLogger().consolePrint("Could not acquire lock!");
		} catch (SQLException e) {
			AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
		}
	}
	
	public static void removeUserAndAllHisItems(String username) throws SQLException {
		try (Queries q = Queries.getInstance()) {
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

		} catch (InterruptedException e) {
			AppLogger.getLogger().consolePrint("Could not acquire lock!");
		} catch (SQLException e) {
			AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
		}
	}
	
	
	
	public static void addNewHostname(String hostname, int userid) throws Exception {
		try (Queries q = Queries.getInstance()) {
			if (q.checkIfUserWithIdExists(userid)) {
				String publicStr = "NOT_SET "+ CryptoUtilities.generateQuestion();
				ResultSet r = q.selectAddressFromBurned();
				if (r.next()) {
					int address = r.getInt("address");
					q.deleteEntryAddressFromBurned(address);					
					q.insertEntryHostnamesWithAddr(address, hostname, userid, publicStr);										
				} else {
					System.out.println("no address");
					q.insertEntryHostnamesNoAddr(hostname, userid, publicStr);
				}
			} else {
				throw new Exception("no user found");
			}
		} catch (InterruptedException e) {
			AppLogger.getLogger().consolePrint("Could not acquire lock!");
		} catch (SQLException e) {
			AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
		}
	}
	
	public static void updateHostname(String hostname, int userid) throws Exception {
		try (Queries q = Queries.getInstance()) {
			if (q.checkIfUserWithIdExists(userid)) {
				q.updateEntryHostnamesWithHostname(hostname, userid);
			} else {
				throw new Exception("no user found");
			}
		} catch (InterruptedException e) {
			AppLogger.getLogger().consolePrint("Could not acquire lock!");
		} catch (SQLException e) {
			AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
		}
	}
	
	public static void removeHostname(String hostname) throws SQLException {
		try (Queries q = Queries.getInstance()) {
			int addressToStore = 0;
			ResultSet r = q.selectAddressFromHostnamesWithHostname(hostname);
			if (r.next()) {
				addressToStore = r.getInt("address");
				q.deleteEntryHostnamesWithHostname(hostname);
				q.insertEntryBurned(addressToStore);
			} else {
				q.deleteEntryHostnamesWithHostname(hostname);
			}
		} catch (InterruptedException e) {
			AppLogger.getLogger().consolePrint("Could not acquire lock!");
		} catch (SQLException e) {
			AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
			throw e;
		}
	}
	
	public static void removeBluenode(String name) throws SQLException {
		try (Queries q = Queries.getInstance()) {
			q.deleteEntryBluenodesWitName(name);
		} catch (InterruptedException e) {
			AppLogger.getLogger().consolePrint("Could not acquire lock!");
		} catch (SQLException e) {
			AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
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

		try (Queries q = Queries.getInstance()) {
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

		} catch (InterruptedException e) {
			AppLogger.getLogger().consolePrint("Could not acquire lock!");
		} catch (SQLException e) {
			AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
		}

		list.add(usersDbData);
		list.add(hostnamesDbData);
		list.add(blunodesDbData);
		return list;
	}
}
