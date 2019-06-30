package org.kostiskag.unitynetwork.tracker.database.logic;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.kostiskag.unitynetwork.common.utilities.CryptoUtilities;
import org.kostiskag.unitynetwork.tracker.AppLogger;
import org.kostiskag.unitynetwork.tracker.database.Queries;

public class HostnameLogic {

    public static void addNewHostname(String hostname, int userid) throws Exception {
        try (Queries q = Queries.getInstance()) {
            if (q.checkIfUserExists(userid)) {
                String publicStr = "NOT_SET "+ CryptoUtilities.generateQuestion();
                ResultSet r = q.selectAddressFromBurned();
                if (r.next()) {
                    int address = r.getInt("address");
                    q.deleteEntryAddressFromBurned(address);
                    q.insertEntryHostnames(address, hostname, userid, publicStr);
                } else {
                    System.out.println("no address");
                    q.insertEntryHostnames(hostname, userid, publicStr);
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
            if (q.checkIfUserExists(userid)) {
                q.updateEntryHostnamesUserId(hostname, userid);
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
            ResultSet r = q.selectAddressFromHostnames(hostname);
            if (r.next()) {
                addressToStore = r.getInt("address");
                q.deleteEntryHostname(hostname);
                q.insertEntryBurned(addressToStore);
            } else {
                q.deleteEntryHostname(hostname);
            }
        } catch (InterruptedException e) {
            AppLogger.getLogger().consolePrint("Could not acquire lock!");
        } catch (SQLException e) {
            AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
            throw e;
        }
    }

    public static PublicKey fetchPublicKey(String hostname) throws InterruptedException, GeneralSecurityException, SQLException, IOException, IllegalAccessException {
        try (Queries q = Queries.getInstance()) {
            ResultSet getResults = q.selectAllFromHostnames();
            while (getResults.next()) {
                if (getResults.getString("hostname").equals(hostname)) {
                    String key = getResults.getString("public");
                    String[] parts = key.split("\\s+");
                    if (!parts[0].equals("NOT_SET")) {
                        return  (PublicKey) CryptoUtilities.base64StringRepresentationToObject(parts[1]);
                    }
                }
            }
            throw new IllegalAccessException("The RN " + hostname + " is not a network member.");
        } catch (InterruptedException | IllegalAccessException | GeneralSecurityException | IOException | SQLException e) {
            throw e;
        }
    }

    public static KeyState offerPublicKey(String hostname, String ticket, String publicKey) throws SQLException, InterruptedException {
        try (Queries q = Queries.getInstance()) {
            ResultSet r = q.selectAllFromHostnames(hostname);
            if (r.next()) {
                String storedKey = r.getString("public");
                String args[] = storedKey.split("\\s+");
                if (args[0].equals(KeyState.NOT_SET) && args[1].equals(ticket)) {
                    q.updateEntryHostnamesPublic(hostname, KeyState.KEY_SET + " " + publicKey);
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

    public static void revokePublicKey(String hostname) throws InterruptedException, SQLException {
        String key = KeyState.NOT_SET.toString()+" "+ CryptoUtilities.generateQuestion();
        try (Queries q = Queries.getInstance()) {
            q.updateEntryHostnamesPublic(hostname, key);
        } catch (InterruptedException | SQLException e) {
            throw e;
        }
    }
}
