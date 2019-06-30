package org.kostiskag.unitynetwork.tracker.database.logic;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.kostiskag.unitynetwork.common.entry.NodeType;
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


}
