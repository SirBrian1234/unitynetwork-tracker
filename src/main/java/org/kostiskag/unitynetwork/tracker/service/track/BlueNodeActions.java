package org.kostiskag.unitynetwork.tracker.service.track;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.PublicKey;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.locks.Lock;

import javax.crypto.SecretKey;

import org.kostiskag.unitynetwork.tracker.App;
import org.kostiskag.unitynetwork.tracker.AppLogger;
import org.kostiskag.unitynetwork.tracker.database.Queries;
import org.kostiskag.unitynetwork.tracker.utilities.CryptoUtilities;
import org.kostiskag.unitynetwork.tracker.utilities.HashUtilities;
import org.kostiskag.unitynetwork.tracker.utilities.SocketUtilities;
import org.kostiskag.unitynetwork.tracker.rundata.address.VirtualAddress;
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
public class BlueNodeActions {

    /**
     * lease a bluenode on the network
     *
     * @throws Exception
     */
    public static void BlueLease(Lock lock, String bluenodeHostname, PublicKey pub, Socket socket, String givenPort, DataOutputStream writer, SecretKey sessionKey) throws Exception {

        String data = null;
        Queries q = null;
        ResultSet getResults = null;

        try {
            q = new Queries();
            getResults = q.selectNameFromBluenodes();

            boolean found = false;
            while (getResults.next() && !found) {
                if (getResults.getString("name").equals(bluenodeHostname)) {
                    String address = socket.getInetAddress().getHostAddress();
                    int port = Integer.parseInt(givenPort);
                    if (!BlueNodeTable.getInstance().isOnline(lock, bluenodeHostname)) {
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
            q.closeQueries();
        } catch (SQLException ex) {
            data = "SYSTEM_ERROR";
            try {
                q.closeQueries();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        SocketUtilities.sendAESEncryptedStringData(data, writer, sessionKey);
    }

    /**
     * lease a rednode on the network over a bluenode
     * on a successful lease a full ip is returned
     *
     * @throws Exception
     */
    public static void RedLease(Lock bnTableLock, String bluenodeName, String givenHostname, String username, String password,
                                DataOutputStream writer, SecretKey sessionKey) throws Exception {
        int userauth = checkUser(password);

        BlueNodeEntry bn = BlueNodeTable.getInstance().getNodeEntry(bnTableLock, bluenodeName);
        if (bn != null) {
            String data = null;
            Queries q = null;
            ResultSet getResults = null;

            if (userauth > 0) {
                boolean found = false;
                try {
                    q = new Queries();
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
                                            RedNodeTable rns = bn.getRedNodes();
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
                    q.closeQueries();
                    SocketUtilities.sendAESEncryptedStringData(data, writer, sessionKey);
                } catch (SQLException ex) {
                    try {
                        q.closeQueries();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    SocketUtilities.sendAESEncryptedStringData("SYSTEM_ERROR", writer, sessionKey);
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
     * @throws Exception
     */
    public static void BlueRel(Lock bnTableLock, String hostname, DataOutputStream writer, SecretKey sessionKey) throws Exception {
        String data = null;
        if (BlueNodeTable.getInstance().isOnline(bnTableLock, hostname)) {
            try {
                BlueNodeTable.getInstance().release(bnTableLock, hostname);
            } catch (Exception e) {
                e.printStackTrace();
                data = "RELEASE_FAILED";
            }
            data = "RELEASED";
        } else {
            data = "RELEASE_FAILED";
        }
        SocketUtilities.sendAESEncryptedStringData(data, writer, sessionKey);
    }

    /**
     * releases a rednode from a bluenode
     *
     * @throws Exception
     */
    public static void RedRel(Lock bnTableLock, String bluenodeName, String hostname, DataOutputStream writer, SecretKey sessionKey) throws IOException, IllegalAccessException, InterruptedException {
        String data = null;
        boolean found = false;

        BlueNodeEntry bn = BlueNodeTable.getInstance().getNodeEntry(bnTableLock, bluenodeName);
        RedNodeTable rnt = bn.getRedNodes();

        try {
            Lock rlock = rnt.aquireLock();
            if (bn.getRedNodes().isOnline(rlock, hostname)) {
                bn.getRedNodes().release(rlock, hostname);
                data = "RELEASED";
            } else {
                data = "NOT_AUTHORIZED";
            }
        } catch (InterruptedException e) {
            data = "SYSTEM_ERROR";
        } finally {
            rnt.releaseLock();
        }

        SocketUtilities.sendAESEncryptedStringData(data, writer, sessionKey);
    }

    /**
     * provides the physical address and port of a known bluenode
     *
     * @throws Exception
     */
    public static void GetPh(Lock lock, String BNTargetHostname, DataOutputStream writer, SecretKey sessionKey) throws Exception {
        String data;
        BlueNodeEntry bn = BlueNodeTable.getInstance().getNodeEntry(lock, BNTargetHostname);
        if (bn != null) {
            data = bn.getAddress().asString() + " " + bn.getPort();
        } else {
            data = "OFFLINE";
        }
        SocketUtilities.sendAESEncryptedStringData(data, writer, sessionKey);
    }

    /**
     * checks whether a RN is ONLINE and from which BN is connected
     *
     * @throws Exception
     */
    public static void CheckRn(Lock lock, String hostname, DataOutputStream writer, SecretKey sessionKey) throws Exception {
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
     * @throws Exception
     */
    public static void CheckRnAddr(Lock lock, String vaddress, DataOutputStream writer, SecretKey sessionKey) throws Exception {
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
        Queries q = null;
        ResultSet getResults;

        try {
            q = new Queries();
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
            q.closeQueries();
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                q.closeQueries();
            } catch (SQLException e1) {
                e1.printStackTrace();
                return -1;
            }
            return -1;
        }
    }

    public static void LookupByHn(String hostname, DataOutputStream writer, SecretKey sessionKey) {
        Queries q = null;
        String vaddress = null;
        String retrievedHostname = null;
        try {
            q = new Queries();
            ResultSet r = q.selectAllFromHostnames();
            while (r.next()) {
                retrievedHostname = r.getString("hostname");
                if (retrievedHostname.equals(hostname)) {
                    //found!!!
                    int num_addr = r.getInt("address");

                    try {
                        vaddress = VirtualAddress.numberTo10ipAddr(num_addr);
                    } catch (UnknownHostException e) {
                        AppLogger.getLogger().consolePrint("Failed lookup by hosntame for BN " + hostname + " " + e.getMessage());
                        return;
                    } finally {
                        q.closeQueries();
                    }

                    try {
                        SocketUtilities.sendAESEncryptedStringData(vaddress, writer, sessionKey);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }
            q.closeQueries();
            try {
                SocketUtilities.sendAESEncryptedStringData("NOT_FOUND", writer, sessionKey);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                q.closeQueries();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            try {
                SocketUtilities.sendAESEncryptedStringData("NOT_FOUND", writer, sessionKey);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    public static void LookupByAddr(String vaddress, DataOutputStream writer, SecretKey sessionKey) {
        Queries q = null;
        String hostname = null;
        int addr_num = 0;
        try {
            addr_num = VirtualAddress._10IpAddrToNumber(vaddress);
        } catch (UnknownHostException e) {
            AppLogger.getLogger().consolePrint("Failed lookup by Address for address: " + vaddress + " " + e.getMessage());
            return;
        }
        int retrieved_addr_num = -1;

        try {
            q = new Queries();
            ResultSet r = q.selectAllFromHostnames();
            while (r.next()) {
                retrieved_addr_num = r.getInt("address");
                if (retrieved_addr_num == addr_num) {
                    //found!!!
                    hostname = r.getString("hostname");

                    q.closeQueries();
                    try {
                        SocketUtilities.sendAESEncryptedStringData(hostname, writer, sessionKey);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }
            q.closeQueries();
            try {
                SocketUtilities.sendAESEncryptedStringData("NOT_FOUND", writer, sessionKey);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                q.closeQueries();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            try {
                SocketUtilities.sendAESEncryptedStringData("NOT_FOUND", writer, sessionKey);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    public static void offerPublicKey(String blueNodeHostname, String ticket, String publicKey, DataOutputStream writer, SecretKey sessionKey) {
        Queries q = null;
        try {
            q = new Queries();
            ResultSet r = q.selectAllFromBluenodesWhereName(blueNodeHostname);
            if (r.next()) {
                String storedKey = r.getString("public");
                String args[] = storedKey.split("\\s+");
                if (args[0].equals("NOT_SET") && args[1].equals(ticket)) {
                    q.updateEntryBluenodesPublicWithName(blueNodeHostname, "KEY_SET" + " " + publicKey);
                    try {
                        SocketUtilities.sendAESEncryptedStringData("KEY_SET", writer, sessionKey);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (args[0].equals("KEY_SET")) {
                    try {
                        SocketUtilities.sendAESEncryptedStringData("KEY_IS_SET", writer, sessionKey);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        SocketUtilities.sendAESEncryptedStringData("WRONG_TICKET", writer, sessionKey);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            q.closeQueries();
            return;
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                q.closeQueries();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
    }

    public static void revokePublicKey(Lock lock, String blueNodeHostname, DataOutputStream writer, SecretKey sessionKey) throws InterruptedException {
        //first check whether the bn is a member and release from the network
        if (BlueNodeTable.getInstance().isOnline(lock, blueNodeHostname)) {
            try {
                BlueNodeTable.getInstance().release(lock, blueNodeHostname);
            } catch (IllegalAccessException e) {
                AppLogger.getLogger().consolePrint(e.getMessage());
            }
        }

        String key = "NOT_SET " + CryptoUtilities.generateQuestion();
        Queries q = null;
        try {
            q = new Queries();
            q.updateEntryBluenodesPublicWithName(blueNodeHostname, key);
            q.closeQueries();
            try {
                SocketUtilities.sendAESEncryptedStringData("KEY_REVOKED", writer, sessionKey);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                q.closeQueries();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
        try {
            SocketUtilities.sendAESEncryptedStringData("NOT_SET", writer, sessionKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
