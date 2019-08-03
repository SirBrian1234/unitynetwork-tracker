package org.kostiskag.unitynetwork.tracker.service.track;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.locks.Lock;

import javax.crypto.SecretKey;

import org.kostiskag.unitynetwork.common.address.VirtualAddress;
import org.kostiskag.unitynetwork.common.utilities.CryptoUtilities;
import org.kostiskag.unitynetwork.common.utilities.HashUtilities;
import org.kostiskag.unitynetwork.common.utilities.SocketUtilities;
import org.kostiskag.unitynetwork.tracker.App;
import org.kostiskag.unitynetwork.tracker.AppLogger;
import org.kostiskag.unitynetwork.tracker.database.Queries;
import org.kostiskag.unitynetwork.tracker.rundata.entry.RedNodeEntry;
import org.kostiskag.unitynetwork.tracker.rundata.entry.BlueNodeEntry;
import org.kostiskag.unitynetwork.tracker.rundata.table.BlueNodeTable;
import org.kostiskag.unitynetwork.tracker.rundata.table.RedNodeTable;

/**
 * Bluenode queries:
 * <p>
 * LEASE BN
 * LEASE RN [HOSTNAME] [USERNAME] [PASSWORD]
 * RELEASE BN
 * RELEASE RN [HOSTNAME]
 * GETPH [BLUENODE_NAME]
 * CHECKRN [HOSTNAME]
 * CHECKRNA [VADDRESS]
 *
 * @author Konstantinos Kagiampakis
 */
final class BlueNodeActions {

    /**
     * Collects a bluenode's public key.
     *
     * @returns the public key if its set OR null for a not set key
     * @throws  IllegalAccessException when fetch bn key is called for a non member
     * @throws  SQLException can not connect to database or db error
     */
    public static PublicKey fetchPubKey(String BlueNodeHostname) throws IllegalAccessException, GeneralSecurityException, IOException {
        PublicKey pub = null;
        boolean found = false;
        try (Queries q = Queries.getInstance()) {
            ResultSet getResults = q.selectAllFromBluenodes();

            while (getResults.next()) {
                if (getResults.getString("name").equals(BlueNodeHostname)) {
                    found = true;
                    String key = getResults.getString("public");
                    String[] parts = key.split("\\s+");
                    if (!parts[0].equals("NOT_SET")) {
                        pub = (PublicKey) CryptoUtilities.base64StringRepresentationToObject(parts[1]);
                    }
                    //break; //remember to break if you dont return!
                }
            }

            if (!found) {
                throw new IllegalAccessException("The Bn " + BlueNodeHostname + " is not a network member.");
            }
            return pub;

        } catch (InterruptedException e) {
            AppLogger.getLogger().consolePrint("Could not acquire lock!");
            return null;
        } catch (SQLException e) {
            AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
            return null;
        }
    }

    /**
     * lease a bluenode on the network
     *
     * @throws
     */
    public static void BlueLease(Lock lock, String bluenodeHostname, PublicKey pub, Socket socket, String givenPort, DataOutputStream writer, SecretKey sessionKey) throws GeneralSecurityException, IOException {

        String data = null;

        ResultSet getResults = null;

        try (Queries q = Queries.getInstance()) {
            getResults = q.selectNameFromBluenodes();

            boolean found = false;
            while (getResults.next() && !found) {
                if (getResults.getString("name").equals(bluenodeHostname)) {
                    String address = socket.getInetAddress().getHostAddress();
                    int port = Integer.parseInt(givenPort);
                    if (!BlueNodeTable.getInstance().getOptionalNodeEntry(lock, bluenodeHostname).isPresent()) {
                        // normal connect for a non associated BN
                        try {
                            BlueNodeTable.getInstance().lease(lock, bluenodeHostname, pub, address, port);
                            data = "LEASED " + address;
                            found = true;
                            break;
                        } catch (Exception e) {
                            e.printStackTrace();
                            data = "LEASE_FAILED";
                            break;
                        }
                    }
                }
            }

            if (!found) {
                data = "LEASE_FAILED";
            }
        } catch (InterruptedException | SQLException ex) {
            data = "SYSTEM_ERROR";
        }
        SocketUtilities.sendAESEncryptedStringData(data, writer, sessionKey);
    }

    /**
     * lease a rednode on the network over a bluenode
     * on a successful lease a full ip is returned
     *
     * @throws
     */
    public static void RedLease(Lock bnTableLock, String bluenodeName, String givenHostname, String username, String password,
                                DataOutputStream writer, SecretKey sessionKey) throws InterruptedException, GeneralSecurityException, IOException, SQLException {
        int userauth = checkUser(password);

        Optional<BlueNodeEntry> b = BlueNodeTable.getInstance().getOptionalNodeEntry(bnTableLock, bluenodeName);
        if (b.isPresent()) {
            String data = null;

            ResultSet getResults = null;

            if (userauth > 0) {
                boolean found = false;

                try (Queries q = Queries.getInstance()) {
                    getResults = q.selectAllFromHostnamesWhereUserid(userauth);
                    if (getResults == null) {
                        data = "SYSTEM_ERROR";
                    } else {
                        while (getResults.next() && !found) {
                            String hostname = getResults.getString("hostname");
                            if (hostname.equals(givenHostname)) {
                                found = true;
                                if (!BlueNodeTable.getInstance().isOnlineRnByHostname(bnTableLock, hostname)) {
                                    //the id from hostnames is the hostname's virtual address
                                    int num_addr = getResults.getInt("address");
                                    int inuserid = getResults.getInt("userid");
                                    if (userauth == inuserid) {
                                        try {
                                            String vaddress = VirtualAddress.numberTo10ipAddr(num_addr);
                                            RedNodeTable rns = b.get().getRedNodes();
                                            try {
                                                Lock rl = rns.aquireLock();
                                                rns.lease(rl,hostname, vaddress);
                                                data = "LEASED " + vaddress;
                                            } finally {
                                                rns.releaseLock();
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            data = "ALLREADY_LEASED";
                                        }
                                    } else {
                                        //a user tried to lease another user's hostname
                                        data = "USER_HOSTNAME_MISSMATCH";
                                    }
                                } else {
                                    data = "ALLREADY_LEASED";
                                }
                            }
                        }

                        if (!found) {
                            data = "LEASE_FAILED";
                        }
                    }
                    SocketUtilities.sendAESEncryptedStringData(data, writer, sessionKey);

                } catch (SQLException | GeneralSecurityException | IOException e) {
                    SocketUtilities.sendAESEncryptedStringData("SYSTEM_ERROR", writer, sessionKey);
                    throw e;
                }

            } else {
                SocketUtilities.sendAESEncryptedStringData("AUTH_FAILED", writer, sessionKey);
            }
        } else {
            SocketUtilities.sendAESEncryptedStringData("AUTH_FAILED", writer, sessionKey);
        }
    }

    /**
     * releases a bluenode from the network
     *
     * @throws
     */
    public static void BlueRel(Lock bnTableLock, String hostname, DataOutputStream writer, SecretKey sessionKey) throws InterruptedException, GeneralSecurityException, IOException {
        String data = null;
        Optional<BlueNodeEntry> b = BlueNodeTable.getInstance().getOptionalNodeEntry(bnTableLock, hostname);
        if (b.isPresent()) {
            try {
                BlueNodeTable.getInstance().release(bnTableLock, b.get());
                data = "RELEASED";
            } catch (IllegalAccessException e) {
                data = "RELEASE_FAILED";
            }
        } else {
            data = "RELEASE_FAILED";
        }
        SocketUtilities.sendAESEncryptedStringData(data, writer, sessionKey);
    }

    /**
     * releases a rednode from a bluenode
     *
     * @throws
     */
    public static void RedRel(Lock bnTableLock, String bluenodeName, String hostname, DataOutputStream writer, SecretKey sessionKey) throws IOException, GeneralSecurityException, IllegalAccessException, InterruptedException {
        String data = null;
        Optional<BlueNodeEntry> b = BlueNodeTable.getInstance().getOptionalNodeEntry(bnTableLock, bluenodeName);
        if (b.isPresent()) {
            RedNodeTable rnt = b.get().getRedNodes();
            try {
                Lock rlock = rnt.aquireLock();
                Optional<RedNodeEntry> r = rnt.getOptionalNodeEntry(rlock, hostname);
                if (r.isPresent()) {
                    rnt.release(rlock, r.get());
                    data = "RELEASED";
                } else {
                    data = "NOT_AUTHORIZED";
                }
            } catch (InterruptedException e) {
                data = "SYSTEM_ERROR";
            } finally {
                rnt.releaseLock();
            }
        } else {
            data = "NOT_AUTHORIZED";
        }
        SocketUtilities.sendAESEncryptedStringData(data, writer, sessionKey);
    }

    /**
     * provides the physical address and port of a known bluenode
     *
     * @throws
     */
    public static void GetPh(Lock lock, String BNTargetHostname, DataOutputStream writer, SecretKey sessionKey) throws InterruptedException, GeneralSecurityException, IOException {
        String data;
        Optional<BlueNodeEntry> b = BlueNodeTable.getInstance().getOptionalNodeEntry(lock, BNTargetHostname);
        if (b.isPresent()) {
            BlueNodeEntry bn = b.get();
            data = bn.getAddress().asString() + " " + bn.getPort();
        } else {
            data = "OFFLINE";
        }
        SocketUtilities.sendAESEncryptedStringData(data, writer, sessionKey);
    }

    /**
     * checks whether a RN is ONLINE and from which BN is connected
     *
     * @throws
     */
    public static void CheckRn(Lock lock, String hostname, DataOutputStream writer, SecretKey sessionKey) throws InterruptedException, GeneralSecurityException, IOException {
        String data;
        BlueNodeEntry bn = BlueNodeTable.getInstance().reverseLookupBnBasedOnRn(lock, hostname);
        if (bn != null) {
            data = "ONLINE " + bn.getHostname() + " " + bn.getAddress().asString() + " " + bn.getPort();
        } else {
            data = "OFFLINE";
        }
        SocketUtilities.sendAESEncryptedStringData(data, writer, sessionKey);
    }

    /**
     * checks whether a RN based on its virtual address is ONLINE and from which BN is connected
     *
     * @throws
     */
    public static void CheckRnAddr(Lock lock, String vaddress, DataOutputStream writer, SecretKey sessionKey) throws InterruptedException, GeneralSecurityException, IOException {
        Queries q = null;
        String data = null;
        String hostname = null;

        BlueNodeEntry bn = BlueNodeTable.getInstance().reverseLookupBnBasedOnRnVaddr(lock, vaddress);
        if (bn != null) {
            data = "ONLINE " + bn.getHostname() + " " + bn.getAddress().asString() + " " + bn.getPort();
        } else {
            data = "OFFLINE";
        }
        SocketUtilities.sendAESEncryptedStringData(data, writer, sessionKey);
    }


    /**
     * validates a network user to a bluenode
     */
    public static int checkUser(String outhash) {
        String data = null;

        ResultSet getResults;

        try (Queries q = Queries.getInstance()) {

            getResults = q.selectIdUsernamePasswordFromUsers();
            if (getResults == null) {
                return -1;
            }

            int i = 0;
            while (getResults.next()) {
                try {
                    data = HashUtilities.SHA256(App.SALT) + HashUtilities.SHA256(getResults.getString("username")) + getResults.getString("password");
                    data = HashUtilities.SHA256(data);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                if (outhash.equals(data)) {
                    return getResults.getInt("id");
                }
            }
            return 0;
        }  catch (InterruptedException e) {
            AppLogger.getLogger().consolePrint("Could not acquire lock!");
            return -1;
        } catch (SQLException e) {
            AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
            return -1;
        }
    }

    public static void LookupByHn(String hostname, DataOutputStream writer, SecretKey sessionKey) {
        String vaddress = null;
        String retrievedHostname = null;

        try (Queries q = Queries.getInstance()) {
            ResultSet r = q.selectAllFromHostnames();
            while (r.next()) {
                retrievedHostname = r.getString("hostname");
                if (retrievedHostname.equals(hostname)) {
                    //found!!!
                    int num_addr = r.getInt("address");

                    try {
                        vaddress = VirtualAddress.numberTo10ipAddr(num_addr);
                        try {
                            SocketUtilities.sendAESEncryptedStringData(vaddress, writer, sessionKey);
                        }  catch (GeneralSecurityException | IOException e) {
                            AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
                            return;
                        }
                        return;
                    } catch (UnknownHostException e) {
                        AppLogger.getLogger().consolePrint("Failed lookup by hosntame for BN " + hostname + " " + e.getMessage());
                        return;
                    }

                }
            }

            try {
                SocketUtilities.sendAESEncryptedStringData("NOT_FOUND", writer, sessionKey);
            } catch (GeneralSecurityException | IOException ex) {
                AppLogger.getLogger().consolePrint(ex.getLocalizedMessage());
            }

        } catch (InterruptedException | SQLException e) {
            try {
                SocketUtilities.sendAESEncryptedStringData("NOT_FOUND", writer, sessionKey);
            } catch (GeneralSecurityException | IOException ex) {
                AppLogger.getLogger().consolePrint(ex.getLocalizedMessage());
            }
        }
    }

    public static void LookupByAddr(String vaddress, DataOutputStream writer, SecretKey sessionKey) {
        String hostname = null;
        int addr_num = 0;
        try {
            addr_num = VirtualAddress._10IpAddrToNumber(vaddress);
        } catch (UnknownHostException e) {
            AppLogger.getLogger().consolePrint("Failed lookup by Address for address: " + vaddress + " " + e.getMessage());
            return;
        }
        int retrieved_addr_num = -1;

        try (Queries q = Queries.getInstance()) {
            ResultSet r = q.selectAllFromHostnames();
            while (r.next()) {
                retrieved_addr_num = r.getInt("address");
                if (retrieved_addr_num == addr_num) {
                    //found!!!
                    hostname = r.getString("hostname");
                    try {
                        SocketUtilities.sendAESEncryptedStringData(hostname, writer, sessionKey);
                    } catch (GeneralSecurityException | IOException e) {
                        AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
                        return;
                    }
                    return;
                }
            }

            try {
                SocketUtilities.sendAESEncryptedStringData("NOT_FOUND", writer, sessionKey);
            } catch (GeneralSecurityException | IOException e) {
                AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
            }

        } catch (InterruptedException | SQLException e) {
            AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
            try {
                SocketUtilities.sendAESEncryptedStringData("NOT_FOUND", writer, sessionKey);
            }  catch (GeneralSecurityException | IOException ex) {
                AppLogger.getLogger().consolePrint(ex.getLocalizedMessage());
                return;
            }
        }
    }

    public static void offerPublicKey(String blueNodeHostname, String ticket, String publicKey, DataOutputStream writer, SecretKey sessionKey) {
        try (Queries q = Queries.getInstance()) {
            ResultSet r = q.selectAllFromBluenodesWhereName(blueNodeHostname);
            if (r.next()) {
                String storedKey = r.getString("public");
                String args[] = storedKey.split("\\s+");
                if (args[0].equals("NOT_SET") && args[1].equals(ticket)) {
                    q.updateEntryBluenodesPublicWithName(blueNodeHostname, "KEY_SET" + " " + publicKey);
                    try {
                        SocketUtilities.sendAESEncryptedStringData("KEY_SET", writer, sessionKey);
                    }  catch (GeneralSecurityException | IOException e) {
                        AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
                        return;
                    }
                } else if (args[0].equals("KEY_SET")) {
                    try {
                        SocketUtilities.sendAESEncryptedStringData("KEY_IS_SET", writer, sessionKey);
                    }  catch (GeneralSecurityException | IOException e) {
                        AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
                        return;
                    }
                } else {
                    try {
                        SocketUtilities.sendAESEncryptedStringData("WRONG_TICKET", writer, sessionKey);
                    }  catch (GeneralSecurityException | IOException e) {
                        AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
                        return;
                    }
                }
            }
        } catch (InterruptedException e) {
            AppLogger.getLogger().consolePrint("Could not acquire lock!");
        } catch (SQLException e) {
            AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
        }
    }

    public static void revokePublicKey(Lock lock, String blueNodeHostname, DataOutputStream writer, SecretKey sessionKey) throws InterruptedException, IllegalAccessException {
        //first check whether the bn is a member and release from the network
        Optional<BlueNodeEntry> b = BlueNodeTable.getInstance().getOptionalNodeEntry(lock, blueNodeHostname);
        if (b.isPresent()) {
            BlueNodeTable.getInstance().release(lock, b.get());
        }

        String key = "NOT_SET " + CryptoUtilities.generateQuestion();
        try (Queries q = Queries.getInstance()) {
            q.updateEntryBluenodesPublicWithName(blueNodeHostname, key);
            try {
                SocketUtilities.sendAESEncryptedStringData("KEY_REVOKED", writer, sessionKey);
            } catch (GeneralSecurityException | IOException e) {
                AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
                return;
            }
        } catch (InterruptedException e) {
            AppLogger.getLogger().consolePrint("Could not acquire lock!");
        } catch (SQLException e) {
            AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
        }

        try {
            SocketUtilities.sendAESEncryptedStringData("NOT_SET", writer, sessionKey);
        } catch (GeneralSecurityException | IOException e) {
            AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
        }
    }
}
