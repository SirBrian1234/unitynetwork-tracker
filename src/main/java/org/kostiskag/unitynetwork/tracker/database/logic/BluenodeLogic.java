package org.kostiskag.unitynetwork.tracker.database.logic;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.kostiskag.unitynetwork.tracker.AppLogger;
import org.kostiskag.unitynetwork.tracker.database.Queries;


public class BluenodeLogic {

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
