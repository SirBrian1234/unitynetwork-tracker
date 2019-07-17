package org.kostiskag.unitynetwork.tracker.database;

import java.util.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.security.GeneralSecurityException;

import org.kostiskag.unitynetwork.common.utilities.CryptoUtilities;
import org.kostiskag.unitynetwork.common.pairs.Pair;

import org.kostiskag.unitynetwork.tracker.AppLogger;


public final class UserLogic {

    public static boolean lookupExistingUser(String username) {
        if (username == null) {
            throw new IllegalArgumentException("null data were provided");
        }

        try (Queries q = Queries.getInstance()) {
            ResultSet r = q.selectIdFromUsers(username);
            return r.next();
        } catch (InterruptedException | SQLException e) {
            AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
        }
        return false;
    }

    public static Optional<Pair<Integer, String>> getUserEntry(String username) {
        if (username == null) {
            throw new IllegalArgumentException("null data were provided");
        }

        try (Queries q = Queries.getInstance()) {
            ResultSet r = q.selectIdScopeFullnameFromUsers(username);
            if (r.next()) {
                return Optional.of(new Pair<>(r.getInt("scope"), r.getString("fullname")));
            }
        } catch (InterruptedException | SQLException e) {
            AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
        }
        return Optional.empty();
    }

    public static boolean addNewUser(String username, String password, int scope, String fullname) throws SQLException {
        if (username == null || password == null || scope < 0 || fullname == null) {
            throw new IllegalArgumentException("null data were provided");
        }

        try (Queries q = Queries.getInstance()) {
            //we have to provide a salted and hashed password in the db along with the rest of the updates
            //to do in the hash branch
            //pass = hash(salt+pass)
            String hashedPassword = CryptoUtilities.hashedPasswordAlgorithm(password);
            q.insertEntryUsers(username, hashedPassword, scope, fullname);
            ResultSet r = q.selectIdFromUsers(username);
            return r.next();
        }  catch (GeneralSecurityException | InterruptedException | SQLException e) {
            AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
        }
        return false;
    }

    public static boolean updateUser(String username, String password, int scope, String fullname) {
        if (username == null || password == null || scope < 0 || fullname == null) {
            throw new IllegalArgumentException("null data were provided");
        }

        try (Queries q = Queries.getInstance()) {
            //we have to provide a salted and hashed password in the db along with the rest of the updates
            //to do in the hash branch
            //pass = hash(salt+pass)
            String hashedPassword = CryptoUtilities.hashedPasswordAlgorithm(password);
            q.updateEntryUsers(username, hashedPassword, scope, fullname);
            return true;
        } catch (GeneralSecurityException | InterruptedException | SQLException e) {
            AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
        }
        return false;
    }

    public static boolean updateUser(String username, int scope, String fullName) {
        if (username == null || scope < 0 || fullName == null) {
            throw new IllegalArgumentException("null data were provided");
        }

        try (Queries q = Queries.getInstance()) {
            q.updateEntryUsers(username, scope, fullName);
            return true;
        } catch (InterruptedException | SQLException e) {
            AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
        }
        return false;
    }

    public static boolean removeUserAndAllHisItems(String username) {
        if (username == null) {
            throw new IllegalArgumentException("null data were provided");
        }

        try (Queries q = Queries.getInstance()) {
            Deque<Integer> stack = new ArrayDeque<>();
            ResultSet userResults = q.selectIdFromUsers(username);
            if (userResults.next()) {
                int userId = userResults.getInt("id");
                ResultSet hostnameResults = q.selectAllFromHostnames(userId);
                if (hostnameResults.next()) {
                    stack.add(hostnameResults.getInt("address"));
                }
                q.deleteEntryHostname(userId);
                while(!stack.isEmpty()) {
                    q.insertEntryBurned(stack.pop());
                }
                q.deleteEntryBluenodes(userId);
                q.deleteEntryUsers(username);
                return true;
            }
        } catch (InterruptedException | SQLException e) {
            AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
        }
        return false;
    }
}
