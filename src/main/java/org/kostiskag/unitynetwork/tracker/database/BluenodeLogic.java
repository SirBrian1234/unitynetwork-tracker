package org.kostiskag.unitynetwork.tracker.database;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.kostiskag.unitynetwork.common.utilities.CryptoUtilities;
import org.kostiskag.unitynetwork.tracker.AppLogger;
import org.kostiskag.unitynetwork.tracker.database.data.Pair;


public class BluenodeLogic {

    public static Pair<Integer, String> selectBluenodeDetails(String name) {
        try (Queries q = Queries.getInstance()) {
            ResultSet r = q.selectAllFromBluenodes(name);
            if (r.next()) {
                return new Pair<>(r.getInt("userid"), r.getString("public"));
            }
        } catch (InterruptedException e) {
            AppLogger.getLogger().consolePrint("Could not acquire lock!");
        } catch (SQLException e) {
            AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
        }
        return null;
    }

    public static boolean findIfBluenodeExists(String bluenodeHostname) throws SQLException, InterruptedException {
        ResultSet getResults = null;
        try (Queries q = Queries.getInstance()) {
            getResults = q.selectNameFromBluenodes(bluenodeHostname);
            boolean found = false;
            if (getResults.next() && !found) {
                if (getResults.getString("name").equals(bluenodeHostname)) {
                    return true;
                }
            }
            return false;
        } catch (InterruptedException | SQLException ex) {
            throw ex;
        }
    }

    public static boolean addNewBluenode(String name, int userId) {
        try (Queries q = Queries.getInstance()) {
            ResultSet r = q.selectIdFromUsers(userId);
            if (r.next()){
                String publicKey = "NOT_SET " + CryptoUtilities.generateQuestion();
                q.insertEntryBluenodes(name, userId, publicKey);
                return true;
            }
        } catch (InterruptedException e) {
            AppLogger.getLogger().consolePrint("Could not acquire lock!");
        } catch (SQLException e) {
            AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
        }
        return false;
    }

    public static boolean updateBluenode(String name, int userId) {
        try (Queries q = Queries.getInstance()) {
            ResultSet r = q.selectIdFromUsers(userId);
            if (r.next()){
                String publicKey = "NOT_SET " + CryptoUtilities.generateQuestion();
                q.insertEntryBluenodes(name, userId, publicKey);
                return true;
            }
        } catch (InterruptedException e) {
            AppLogger.getLogger().consolePrint("Could not acquire lock!");
        } catch (SQLException e) {
            AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
        }
        return false;
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
