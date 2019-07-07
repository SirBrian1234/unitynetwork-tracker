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
import org.kostiskag.unitynetwork.common.entry.NodeType;
import org.kostiskag.unitynetwork.common.utilities.SocketUtilities;

import org.kostiskag.unitynetwork.tracker.AppLogger;
import org.kostiskag.unitynetwork.tracker.database.BluenodeLogic;
import org.kostiskag.unitynetwork.tracker.database.HostnameLogic;
import org.kostiskag.unitynetwork.tracker.database.UserLogic;
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
     * lease a bluenode on the network
     *
     * @throws
     */
    public static void BlueLease(Lock lock, String bluenodeHostname, PublicKey pub, Socket socket, String givenPort, DataOutputStream writer, SecretKey sessionKey) throws GeneralSecurityException, IOException {
        String data = null;
        try {
            if (BluenodeLogic.findIfBluenodeExists(bluenodeHostname)) {
                String address = socket.getInetAddress().getHostAddress();
                int port = Integer.parseInt(givenPort);
                if (!BlueNodeTable.getInstance().getOptionalNodeEntry(lock, bluenodeHostname).isPresent()) {
                    // normal connect for a non associated BN
                    try {
                        BlueNodeTable.getInstance().lease(lock, bluenodeHostname, pub, address, port);
                        data = "LEASED " + address;
                    } catch (Exception e) {
                        data = "LEASE_FAILED";
                    }
                }
            } else {
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
    public static void RedLease(Lock bnTableLock, String bluenodeName, String givenHostname, String username, String givenHash,
                                DataOutputStream writer, SecretKey sessionKey) throws InterruptedException, GeneralSecurityException, IOException, SQLException {
        String data;
        Optional<BlueNodeEntry> b = BlueNodeTable.getInstance().getOptionalNodeEntry(bnTableLock, bluenodeName);
        if (b.isPresent()) {
            VirtualAddress vAddress = UserLogic.validateUserHostname(username, givenHostname, givenHash);
            if (vAddress != null) {
                if (!BlueNodeTable.getInstance().isOnlineRnByHostname(bnTableLock, givenHostname)) {
                    RedNodeTable rns = b.get().getRedNodes();
                    try {
                        Lock rl = rns.aquireLock();
                        try {
                            rns.lease(rl, givenHostname, vAddress);
                            data = "LEASED " + vAddress.asString();
                        } catch (IllegalAccessException e) {
                            data = "AUTH_FAILED " + vAddress.asString();
                        }
                    } finally {
                        rns.releaseLock();
                    }
                } else {
                    data = "ALLREADY_LEASED";
                }
            } else {
                data = "AUTH_FAILED";
            }
        } else {
            data = "AUTH_FAILED";
        }
        SocketUtilities.sendAESEncryptedStringData(data, writer, sessionKey);
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




    public static void LookupByHostname(String hostname, DataOutputStream writer, SecretKey sessionKey) {
        VirtualAddress vaddr = HostnameLogic.lookupVaddress(hostname);
        if(vaddr != null) {
            try {
                SocketUtilities.sendAESEncryptedStringData(vaddr.asString(), writer, sessionKey);
            } catch (GeneralSecurityException | IOException e) {
                AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
                return;
            }
        } else {
            try {
                SocketUtilities.sendAESEncryptedStringData("NOT_FOUND", writer, sessionKey);
            } catch (GeneralSecurityException | IOException ex) {
                AppLogger.getLogger().consolePrint(ex.getLocalizedMessage());
            }
        }

    }

    public static void LookupByAddr(String vaddress, DataOutputStream writer, SecretKey sessionKey) {
        VirtualAddress v = null;
        try {
             v = VirtualAddress.valueOf(vaddress);
        } catch (UnknownHostException e) {
            try {
                SocketUtilities.sendAESEncryptedStringData("NOT_FOUND", writer, sessionKey);
            }  catch (GeneralSecurityException | IOException ex) {
                AppLogger.getLogger().consolePrint(ex.getLocalizedMessage());
                return;
            }
        }

        String hostname = HostnameLogic.lookupHostname(v);
        if (hostname != null) {
            try {
                SocketUtilities.sendAESEncryptedStringData(hostname, writer, sessionKey);
            } catch (GeneralSecurityException | IOException e) {
                AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
                return;
            }
        } else {
            try {
                SocketUtilities.sendAESEncryptedStringData("NOT_FOUND", writer, sessionKey);
            }  catch (GeneralSecurityException | IOException ex) {
                AppLogger.getLogger().consolePrint(ex.getLocalizedMessage());
                return;
            }
        }
    }

    public static void revokePublicKey(Lock lock, String blueNodeHostname, DataOutputStream writer, SecretKey sessionKey) throws InterruptedException, IllegalAccessException, GeneralSecurityException, IOException {
        //first check whether the bn is a member and release from the network
        Optional<BlueNodeEntry> b = BlueNodeTable.getInstance().getOptionalNodeEntry(lock, blueNodeHostname);
        if (b.isPresent()) {
            BlueNodeTable.getInstance().release(lock, b.get());
        }
        CommonActions.revokePublicKey(NodeType.BLUENODE, blueNodeHostname, writer, sessionKey);
    }
}
