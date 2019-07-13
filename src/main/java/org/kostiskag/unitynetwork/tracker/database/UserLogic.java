package org.kostiskag.unitynetwork.tracker.database;

import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.kostiskag.unitynetwork.common.address.VirtualAddress;
import org.kostiskag.unitynetwork.common.utilities.CryptoUtilities;
import org.kostiskag.unitynetwork.common.utilities.HashUtilities;
import org.kostiskag.unitynetwork.tracker.App;
import org.kostiskag.unitynetwork.tracker.AppLogger;
import org.kostiskag.unitynetwork.tracker.database.data.Tuple;


public final class UserLogic {

    public static Tuple<Integer, Integer, String> getUserDetails(String username) {
        try (Queries q = Queries.getInstance()) {
            ResultSet r = q.selectIdScopeFullnameFromUsers(username);
            if (r.next()) {
                return new Tuple<>(r.getInt("id"), r.getInt("scope"), r.getString("fullname"));
            }
        } catch (InterruptedException e) {
            AppLogger.getLogger().consolePrint("Could not acquire lock!");
        } catch (SQLException e) {
            AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
        }
        return null;
    }

    public static boolean checkExistingUserId(int userId) {
        try (Queries q = Queries.getInstance()) {
            ResultSet r = q.selectIdFromUsers(userId);
            if (r.next()){
                return r.getInt("id") == userId ? true: false;
            }
            return false;
        } catch (InterruptedException e) {
            AppLogger.getLogger().consolePrint("Could not acquire lock!");
        } catch (SQLException e) {
            AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
        }
        return false;
    }

    /**
     *
     * @param username
     * @param hostname
     * @param outhash
     * @return If the user is valid he will get a virtual address for its hostname, else he will get a null address
     */
    public static VirtualAddress validateUserHostname(String username, String hostname, String outhash) {
        ResultSet userResults;
        try (Queries q = Queries.getInstance()) {
            userResults = q.selectIdUsernamePasswordFromUsers();
            while (userResults.next()) {
                String data;
                try {
                    //unnecessary complexity it should not be calculated for each user
                    //it has to be kept stored
                    data = HashUtilities.SHA256(App.SALT) + HashUtilities.SHA256(userResults.getString("username")) + userResults.getString("password");
                    data = HashUtilities.SHA256(data);
                    if (outhash.equals(data)) {
                        int userId = userResults.getInt("id");
                        ResultSet hostnameResults = q.selectAddressFromHostnames(userId);
                        int numAddr = hostnameResults.getInt("address");
                        try {
                            return VirtualAddress.valueOf(numAddr);
                        } catch (UnknownHostException e) {
                            return null;
                        }
                    }
                } catch (NoSuchAlgorithmException e) {
                    return null;
                }
            }
        }  catch (InterruptedException e) {
            AppLogger.getLogger().consolePrint("Could not acquire lock!");
        } catch (SQLException e) {
            AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
        }
        return null;
    }

    public static int addNewUser(String username, String password, int scope, String fullname) throws SQLException {
        //we have to provide a salted and hashed password in the db along with the rest of the updates
        //to do in the hash branch
        //pass = hash(salt+pass)
        try {
            password = CryptoUtilities.storagePasswordAlgorithm(password);
        } catch (GeneralSecurityException e) {
            AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
            return -1;
        }

        try (Queries q = Queries.getInstance()) {
            q.insertEntryUsers(username, password, scope, fullname);
            ResultSet r = q.selectIdFromUsers(username);
            if (r.next()) {
                return r.getInt("id");
            }
        }  catch (InterruptedException e) {
            AppLogger.getLogger().consolePrint("Could not acquire lock!");
        } catch (SQLException e) {
            AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
        }
        return -1;
    }

    public static void updateUserAndPassword(String username, String password, int scope, String fullname) throws SQLException {
        //we have to provide a salted and hashed password in the db along with the rest of the updates
        //to do in the hash branch
        //pass = hash(salt+pass)
        try {
            password = CryptoUtilities.storagePasswordAlgorithm(password);
        } catch (GeneralSecurityException e) {
            AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
            return;
        }

        try (Queries q = Queries.getInstance();) {
            q.updateEntryUsers(username, password, scope, fullname);
        } catch (InterruptedException e) {
            AppLogger.getLogger().consolePrint("Could not acquire lock!");
        } catch (SQLException e) {
            AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
        }
    }

    public static void updateUserScopeFullName(String username, int scope, String fullName) {
        try (Queries q = Queries.getInstance()) {
            q.updateEntryUsers(username, scope, fullName);
        } catch (InterruptedException e1) {
            AppLogger.getLogger().consolePrint("Could not acquire lock!");
        } catch (SQLException e2) {
            AppLogger.getLogger().consolePrint(e2.getLocalizedMessage());
        }
    }

    public static void removeUserAndAllHisItems(String username) throws SQLException {
        try (Queries q = Queries.getInstance()) {
            Deque<Integer> stack = new ArrayDeque<>();
            ResultSet results = q.selectIdFromUsers(username);
            if (results.next()) {
                int id = results.getInt("id");
                results = q.selectAllFromHostnames(id);
                if (results.next()) {
                    stack.add(results.getInt("address"));
                }
                q.deleteEntryHostname(id);
                while(!stack.isEmpty()) {
                    q.insertEntryBurned(stack.pop());
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
