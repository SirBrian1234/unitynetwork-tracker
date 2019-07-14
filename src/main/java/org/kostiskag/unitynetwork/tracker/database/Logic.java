package org.kostiskag.unitynetwork.tracker.database;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.security.PublicKey;
import java.security.GeneralSecurityException;
import java.io.IOException;

import org.kostiskag.unitynetwork.common.entry.NodeType;
import org.kostiskag.unitynetwork.common.state.PublicKeyState;
import org.kostiskag.unitynetwork.common.utilities.CryptoUtilities;

import org.kostiskag.unitynetwork.tracker.AppLogger;
import org.kostiskag.unitynetwork.tracker.database.data.InternalPublicKeyState;


/**
 * The database upper logic which calls methods from Queries
 * 
 * @author Konstantinos Kagiampakis
 */
public final class Logic {

	public static String  newPublicKeyEntryAlgorithm() {
		return InternalPublicKeyState.NOT_SET.toString() + " " + CryptoUtilities.generateQuestion();
	}

	public static void setDatabaseInstanceWrapper (String url, String user, String password) throws SQLException {
		if (url == null || user == null || password == null) {
			throw new IllegalArgumentException("null data were provided");
		}

		Queries.setDatabaseInstance(url, user, password);
	}

	public static boolean validateDatabase() {
		try (Queries q = Queries.getInstance()) {
			q.validate();
		} catch (InterruptedException e) {
			AppLogger.getLogger().consolePrint("Could not acquire lock!");
			return false;
		} catch (SQLException e) {
			AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
			return false;
		}
		return true;
	}

	public static Optional<PublicKey> fetchPublicKey(NodeType type, String hostname) throws InterruptedException, GeneralSecurityException, SQLException, IOException {
		if (type == null || hostname == null) {
			throw new IllegalArgumentException("null data were provided");
		}

		try (Queries q = Queries.getInstance()) {
			ResultSet r = null;
			if (type == NodeType.REDNODE) {
				r = q.selectPublicKeyFromHostnames(hostname);
			} else {
				r = q.selectPublicKeyFromBluenodes(hostname);
			}
			if (r.next()) {
				String key = r.getString("public");
				String[] parts = key.split("\\s+");
				if (!parts[0].equals(InternalPublicKeyState.NOT_SET.toString())) {
					return Optional.of(CryptoUtilities.base64StringRepresentationToObject(parts[1]));
				}
			}
		} catch (InterruptedException | GeneralSecurityException | IOException | SQLException e) {
			throw e;
		}
		return Optional.empty();
	}

	public static PublicKeyState offerPublicKey(NodeType type, String hostname, String ticket, PublicKey publicKey) throws IOException, SQLException, InterruptedException {
		String publicStr = CryptoUtilities.objectToBase64StringRepresentation(publicKey);
		return offerPublicKey(type, hostname, ticket, publicStr);
	}

	public static PublicKeyState offerPublicKey(NodeType type, String hostname, String ticket, String publicKey) throws SQLException, InterruptedException {
		if (type == null || hostname == null || ticket == null || publicKey == null) {
			throw new IllegalArgumentException("null data were provided");
		}

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
				if (args[0].equals(InternalPublicKeyState.NOT_SET.toString()) && args[1].equals(ticket)) {
					if (type == NodeType.REDNODE) {
						q.updateEntryHostnamesPublic(hostname, InternalPublicKeyState.KEY_SET.toString() + " " + publicKey);
					} else {
						q.updateEntryBluenodesPublic(hostname, InternalPublicKeyState.KEY_SET.toString() + " " + publicKey);
					}
					return PublicKeyState.KEY_SET;
				} else if (args[0].equals(InternalPublicKeyState.KEY_SET.toString())) {
					return PublicKeyState.KEY_IS_SET;
				} else {
					return PublicKeyState.WRONG_TICKET;
				}
			}
		} catch (InterruptedException | SQLException e) {
			throw e;
		}
		return PublicKeyState.NOT_SET;
	}

	public static void revokePublicKey(NodeType type, String hostname) throws InterruptedException, SQLException {
		if (type == null || hostname == null) {
			throw new IllegalArgumentException("null data were provided");
		}

		String key = Logic.newPublicKeyEntryAlgorithm();
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

	public static String[][][] buildGUIObject() {
		// reload database on gui
		String[][][] entriesForGui = new String[3][][];

		ResultSet bns = null, hnms = null, usrs = null;
		try (Queries q = Queries.getInstance()) {
			usrs = q.selectAllFromUsers();
			hnms = q.selectAllFromHostnames();
			bns = q.selectAllFromBluenodes();

			List<String[]> usrsList = new ArrayList<>();
			List<String[]> hnmsList = new ArrayList<>();
			List<String[]> bnsList = new ArrayList<>();

			//users
			for (int i=0; usrs.next(); i++) {
				String entry[] = new String[5];
				entry[0] = "" + usrs.getInt("id");
				entry[1] = usrs.getString("username");
				entry[2] = usrs.getString("password");
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
				entry[4] = usrs.getString("fullname");
				usrsList.add(entry);
			}
			entriesForGui[0] = usrsList.toArray(new String[][]{});

			//hostnames
			for (int i=0; hnms.next(); i++) {
				String entry[] = new String[3];
				entry[0] = "" + hnms.getInt("address");
				entry[1] = hnms.getString("hostname");
				entry[2] = "" + hnms.getInt("userid");
				hnmsList.add(entry);
			}
			entriesForGui[1] = hnmsList.toArray(new String[][]{});

			//bluenodes
			for (int i=0; bns.next(); i++) {
				String entry[] = new String[2];
				entry[0] = bns.getString("name");
				entry[1] = "" + bns.getInt("userid");
				bnsList.add(entry);
			}
			entriesForGui[2] = bnsList.toArray(new String[][]{});

		} catch (InterruptedException e) {
			AppLogger.getLogger().consolePrint("Could not acquire lock!");
		} catch (SQLException e) {
			AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
		}

		return entriesForGui;
	}
}
