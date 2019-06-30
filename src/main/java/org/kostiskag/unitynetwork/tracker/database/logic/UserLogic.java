package org.kostiskag.unitynetwork.tracker.database.logic;

import java.security.GeneralSecurityException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import org.kostiskag.unitynetwork.common.utilities.HashUtilities;
import org.kostiskag.unitynetwork.tracker.App;
import org.kostiskag.unitynetwork.tracker.AppLogger;
import org.kostiskag.unitynetwork.tracker.database.Queries;


public final class UserLogic {

    public static void addNewUser(String username, String password, int scope, String fullname) throws SQLException {
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
            q.updateEntryUsers(username, password, scope, fullname);
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
            ResultSet r = q.selectIdFromUsers(username);
            if (r.next()) {
                id = r.getInt("id");
                r = q.selectAllFromHostnames(id);
                if (r.next()) {
                    int address = r.getInt("address");
                    li.add(address);
                }
                q.deleteEntryHostname(id);
                while(!li.isEmpty()) {
                    int address = (int) li.pop();
                    q.insertEntryBurned(address);
                }
                q.deleteEntryBluenodes(id);
                q.deleteEntryUsers(username);
            }

        } catch (InterruptedException e) {
            AppLogger.getLogger().consolePrint("Could not acquire lock!");
        } catch (SQLException e) {
            AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
        }
    }
}
