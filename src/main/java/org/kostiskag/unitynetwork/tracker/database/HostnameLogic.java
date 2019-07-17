package org.kostiskag.unitynetwork.tracker.database;

import java.security.GeneralSecurityException;
import java.util.Optional;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.net.UnknownHostException;

import org.kostiskag.unitynetwork.common.address.VirtualAddress;
import org.kostiskag.unitynetwork.common.utilities.CryptoUtilities;
import org.kostiskag.unitynetwork.common.pairs.Tuple;

import org.kostiskag.unitynetwork.tracker.AppLogger;

public class HostnameLogic {

    public static Optional<VirtualAddress> validateHostname(String username, String hostname, String outhash) {
        ResultSet userResults;
        try (Queries q = Queries.getInstance()) {
            userResults = q.selectIdPasswordFromUsers(username);
            if (userResults.next()) {

                String internalHash = CryptoUtilities.validatePasswordAlgorithm(userResults.getString("username"),
                        userResults.getString("password"));

                if (internalHash.equals(outhash)) {
                    int userId = userResults.getInt("id");
                    ResultSet hostnameResults = q.selectHostnameAddressFromHostnames(userId);
                    while(hostnameResults.next()) {
                        String retrievedHostname = hostnameResults.getString("hostname");
                        if (retrievedHostname.equals(hostname)) {
                            int numAddr = hostnameResults.getInt("address");
                            try {
                                return Optional.of(VirtualAddress.valueOf(numAddr));
                            } catch (UnknownHostException e) {
                                AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
                            }
                        }
                    }
                }
            }
        }  catch (GeneralSecurityException | InterruptedException | SQLException e) {
            AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
        }
        return Optional.empty();
    }

    public static Optional<VirtualAddress> lookupVaddress(String hostname) {
        if (hostname == null) {
            throw new IllegalArgumentException("null data were provided");
        }

        try (Queries q = Queries.getInstance()) {
            ResultSet r = q.selectAddressFromHostnames(hostname);
            if (r.next()) {
                //found!!!
                return Optional.of(VirtualAddress.valueOf(r.getInt("address")));
            }
        } catch (UnknownHostException | InterruptedException | SQLException e) {
            AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
        }
        return Optional.empty();
    }

    public static Optional<String> lookupHostname(VirtualAddress address) {
        if (address == null) {
            throw new IllegalArgumentException("null data were provided");
        }

        try (Queries q = Queries.getInstance()) {
            ResultSet r = q.selectAllFromHostnamesWhereAddress(address.asInt());
            if (r.next()) {
                //found!!!
                return Optional.of(r.getString("hostname"));
            }
        } catch (InterruptedException | SQLException e) {
            AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
        }
        return Optional.empty();
    }

    public static Optional<Tuple<String, VirtualAddress, String>> getHostnameEntry(String hostname) {
        if (hostname == null) {
            throw new IllegalArgumentException("null data were provided");
        }

        try (Queries q = Queries.getInstance()) {
            ResultSet r = q.selectAllFromHostnames(hostname);
            if(r.next()) {
                ResultSet users = q.selectUsernameFromUsers(r.getInt("userid"));

                return Optional.of(new Tuple<>(users.getString("username"), VirtualAddress.valueOf(r.getInt("address")), r.getString("public")));
            }
        } catch (UnknownHostException | InterruptedException | SQLException e) {
            AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
        }
        return Optional.empty();
    }

    public static boolean addNewHostname(String hostname, String username) {
        if (hostname == null || username == null) {
            throw new IllegalArgumentException("null data were provided");
        }

        try (Queries q = Queries.getInstance()) {
            ResultSet users = q.selectIdFromUsers(username);
            if (users.next()) {
                int userId = users.getInt("id");
                String publicStr = Logic.newPublicKeyEntryAlgorithm();
                ResultSet r = q.selectAddressFromBurned();
                if (r.next()) {
                    // First look on burned address table to see if there are any addresses available!
                    int address = r.getInt("address");
                    q.deleteEntryAddressFromBurned(address);
                    q.insertEntryHostnames(address, hostname, userId, publicStr);
                } else {
                    // No address on burned list!
                    q.insertEntryHostnames(hostname, userId, publicStr);
                }
                return true;
            }
        } catch (InterruptedException | SQLException e) {
            AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
        }
        return false;
    }

    public static boolean removeHostname(String hostname) {
        if (hostname == null) {
            throw new IllegalArgumentException("null data were provided");
        }

        try (Queries q = Queries.getInstance()) {
            ResultSet r = q.selectAddressFromHostnames(hostname);
            if (r.next()) {
                q.deleteEntryHostname(hostname);
                q.insertEntryBurned(r.getInt("address"));
                return true;
            }
        } catch (InterruptedException | SQLException e) {
            AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
        }
        return false;
    }

}
