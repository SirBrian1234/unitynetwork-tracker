package org.kostiskag.unitynetwork.tracker.database.logic;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.kostiskag.unitynetwork.common.utilities.CryptoUtilities;
import org.kostiskag.unitynetwork.tracker.AppLogger;
import org.kostiskag.unitynetwork.tracker.database.Queries;
import org.kostiskag.unitynetwork.tracker.rundata.table.BlueNodeTable;


public class BluenodeLogic {

    /**
     * Collects a bluenode's public key.
     *
     * @returns the public key if its set OR null for a not set key
     * @throws  IllegalAccessException when fetch bn key is called for a non member
     * @throws  SQLException can not connect to database or db error
     */
    public static PublicKey fetchPubKey(String BlueNodeHostname) throws IllegalAccessException, GeneralSecurityException, IOException {
        boolean found = false;
        try (Queries q = Queries.getInstance()) {
            ResultSet getResults = q.selectAllFromBluenodes();

            while (getResults.next()) {
                if (getResults.getString("name").equals(BlueNodeHostname)) {
                    found = true;
                    String key = getResults.getString("public");
                    String[] parts = key.split("\\s+");
                    if (!parts[0].equals("NOT_SET")) {
                        return (PublicKey) CryptoUtilities.base64StringRepresentationToObject(parts[1]);
                    }
                    //break; //remember to break if you dont return!
                }
            }
            if (!found) {
                throw new IllegalAccessException("The Bn " + BlueNodeHostname + " is not a network member.");
            }
            return null;

        } catch (InterruptedException e) {
            AppLogger.getLogger().consolePrint("Could not acquire lock!");
            return null;
        } catch (SQLException e) {
            AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
            return null;
        }
    }

    public static boolean findBluenode(String bluenodeHostname) throws SQLException, InterruptedException {
        ResultSet getResults = null;
        try (Queries q = Queries.getInstance()) {
            getResults = q.selectNameFromBluenodes();
            boolean found = false;
            while (getResults.next() && !found) {
                if (getResults.getString("name").equals(bluenodeHostname)) {
                    return true;
                }
            }
            return false;
        } catch (InterruptedException | SQLException ex) {
            throw ex;
        }
    }

    public static void removeBluenode(String name) throws SQLException {
        try (Queries q = Queries.getInstance()) {
            q.deleteEntryBluenodes(name);
        } catch (InterruptedException e) {
            AppLogger.getLogger().consolePrint("Could not acquire lock!");
        } catch (SQLException e) {
            AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
            throw e;
        }
    }
}
