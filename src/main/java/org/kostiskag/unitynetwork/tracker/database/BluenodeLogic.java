package org.kostiskag.unitynetwork.tracker.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import org.kostiskag.unitynetwork.common.pairs.Pair;

import org.kostiskag.unitynetwork.tracker.AppLogger;


public final class BluenodeLogic {

    public static boolean lookupBluenode(String name) throws SQLException, InterruptedException {
        try (Queries q = Queries.getInstance()) {
            ResultSet getResults = q.selectNameFromBluenodes(name);
            return getResults.next();
        } catch (InterruptedException | SQLException ex) {
            throw ex;
        }
    }

    public static Optional<Pair<String, String>> selectBluenodesUserPublicKey(String name) {
        try (Queries q = Queries.getInstance()) {
            ResultSet bluenodeResults = q.selectAllFromBluenodes(name);
            if (bluenodeResults.next()) {
                ResultSet userResults = q.selectUsernameFromUsers(bluenodeResults.getInt("userid"));
                if (userResults.next()) {
                    return Optional.of(new Pair<>(userResults.getString("username"), bluenodeResults.getString("public")));
                }
            }
        } catch (InterruptedException | SQLException e) {
            AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
        }
        return Optional.empty();
    }

    public static boolean addNewBluenode(String name, String username) {
        try (Queries q = Queries.getInstance()) {
            ResultSet r = q.selectIdFromUsers(username);
            if (r.next()){
                String publicKey = Logic.newPublicKeyEntryAlgorithm();
                q.insertEntryBluenodes(name, r.getInt("id"), publicKey);
                return true;
            }
        } catch (InterruptedException | SQLException e) {
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
