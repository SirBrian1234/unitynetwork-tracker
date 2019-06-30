package org.kostiskag.unitynetwork.tracker.database.logic;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;

import org.kostiskag.unitynetwork.common.entry.NodeType;
import org.kostiskag.unitynetwork.common.utilities.CryptoUtilities;

import org.kostiskag.unitynetwork.tracker.AppLogger;
import org.kostiskag.unitynetwork.tracker.database.Queries;


/**
 * The database upper logic which calls methods from Queries
 * 
 * @author Konstantinos Kagiampakis
 */
public final class Logic {

	public static PublicKey fetchPublicKey(NodeType type, String hostname) throws InterruptedException, GeneralSecurityException, SQLException, IOException {
		try (Queries q = Queries.getInstance()) {
			ResultSet r = null;
			if (type == NodeType.REDNODE) {
				r = q.selectHostnamePublicKeyFromHostnames(hostname);
			} else if (type == NodeType.BLUENODE) {
				r = q.selectNamePublicKeyFromBluenodes(hostname);
			}
			if (r.next()) {
				if (r.getString("hostname").equals(hostname)) {
					String key = r.getString("public");
					String[] parts = key.split("\\s+");
					if (!parts[0].equals("NOT_SET")) {
						return  (PublicKey) CryptoUtilities.base64StringRepresentationToObject(parts[1]);
					}
				}
			}
			return null;
		} catch (InterruptedException | GeneralSecurityException | IOException | SQLException e) {
			throw e;
		}
	}

	public static KeyState offerPublicKey(NodeType type, String hostname, String ticket, String publicKey) throws SQLException, InterruptedException {
		try (Queries q = Queries.getInstance()) {
			ResultSet r;
			if (type == NodeType.REDNODE) {
				r = q.selectAllFromHostnames(hostname);
			} else {
				r = q.selectAllFromBluenodes(hostname);
			}
			if (r.next()) {
				String storedKey = r.getString("public");
				String args[] = storedKey.split("\\s+");
				if (args[0].equals(KeyState.NOT_SET) && args[1].equals(ticket)) {
					if (type == NodeType.REDNODE) {
						q.updateEntryHostnamesPublic(hostname, KeyState.KEY_SET + " " + publicKey);
					} else {
						q.updateEntryBluenodesPublic(hostname, "KEY_SET" + " " + publicKey);
					}
					return KeyState.KEY_SET;
				} else if (args[0].equals("KEY_SET")) {
					return KeyState.KEY_IS_SET;
				} else {
					return KeyState.WRONG_TICKET;
				}
			}
			return KeyState.NOT_SET;
		} catch (InterruptedException | SQLException e) {
			throw e;
		}
	}

	public static void revokePublicKey(NodeType type, String hostname) throws InterruptedException, SQLException {
		String key = KeyState.NOT_SET.toString()+" "+ CryptoUtilities.generateQuestion();
		try (Queries q = Queries.getInstance()) {
			if (type == NodeType.REDNODE) {
				q.updateEntryHostnamesPublic(hostname, key);
			} else {
				q.updateEntryBluenodesPublic(hostname, key);
			}
		} catch (InterruptedException | SQLException e) {
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
