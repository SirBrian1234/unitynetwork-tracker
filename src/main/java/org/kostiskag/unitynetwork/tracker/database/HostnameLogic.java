package org.kostiskag.unitynetwork.tracker.database;

import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.kostiskag.unitynetwork.common.address.VirtualAddress;
import org.kostiskag.unitynetwork.common.utilities.CryptoUtilities;
import org.kostiskag.unitynetwork.tracker.AppLogger;
import org.kostiskag.unitynetwork.tracker.database.data.Pair;

public class HostnameLogic {

    public static VirtualAddress lookupVaddress(String hostname) {
        try (Queries q = Queries.getInstance()) {
            ResultSet r = q.selectAddressFromHostnames(hostname);
            if (r.next()) {
                //found!!!
                return VirtualAddress.valueOf(r.getInt("address"));
            }
        } catch (UnknownHostException | InterruptedException | SQLException e) {
            AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
        }
        return null;
    }

    public static String lookupHostname(VirtualAddress address) {
        try (Queries q = Queries.getInstance()) {
            ResultSet r = q.selectAllFromHostnamesWhereAddress(address.asInt());
            if (r.next()) {
                //found!!!
                return r.getString("hostname");
            }
        } catch (InterruptedException | SQLException e) {
            AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
        }
        return null;
    }

    public static boolean addNewHostname(String hostname, int userid) throws Exception {
        try (Queries q = Queries.getInstance()) {
            ResultSet users = q.selectIdFromUsers(userid);
            if (users.next()) {
                if (users.getInt("id") == userid ) {
                    String publicStr = "NOT_SET "+ CryptoUtilities.generateQuestion();
                    ResultSet r = q.selectAddressFromBurned();
                    if (r.next()) {
                        int address = r.getInt("address");
                        q.deleteEntryAddressFromBurned(address);
                        q.insertEntryHostnames(address, hostname, userid, publicStr);
                    } else {
                        AppLogger.getLogger().consolePrint("No address on burned list!");
                        q.insertEntryHostnames(hostname, userid, publicStr);
                    }
                    return true;
                }
            }
        } catch (InterruptedException e) {
            AppLogger.getLogger().consolePrint("Could not acquire lock!");
        } catch (SQLException e) {
            AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
        }
        return false;
    }

    public static boolean updateHostname(String hostname, int userid) {
        try (Queries q = Queries.getInstance()) {
            ResultSet users = q.selectIdFromUsers(userid);
            if (users.next()) {
                if (users.getInt("id") == userid) {
                    q.updateEntryHostnamesUserId(hostname, userid);
                    return true;
                }
            }
        } catch (InterruptedException e) {
            AppLogger.getLogger().consolePrint("Could not acquire lock!");
        } catch (SQLException e) {
            AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
        }
        return false;
    }

    public static Pair<Integer, String> getHostnameEntry(String hostname) {
        try (Queries q = Queries.getInstance()) {
            ResultSet r = q.selectAllFromHostnames(hostname);
             if(r.next()) {
                return new Pair<>(r.getInt("userid"), r.getString("public"));
            }
        } catch (InterruptedException e) {
            AppLogger.getLogger().consolePrint("Could not acquire lock!");
        } catch (SQLException e) {
            AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
        }
        return null;
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
