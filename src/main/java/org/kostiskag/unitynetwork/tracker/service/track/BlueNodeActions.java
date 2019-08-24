package org.kostiskag.unitynetwork.tracker.service.track;

import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.sql.SQLException;

import javax.crypto.SecretKey;

import org.kostiskag.unitynetwork.common.address.VirtualAddress;
import org.kostiskag.unitynetwork.common.entry.NodeType;
import org.kostiskag.unitynetwork.common.utilities.SocketUtilities;

import org.kostiskag.unitynetwork.tracker.database.BluenodeLogic;
import org.kostiskag.unitynetwork.tracker.database.HostnameLogic;
import org.kostiskag.unitynetwork.tracker.rundata.entry.RedNodeEntry;
import org.kostiskag.unitynetwork.tracker.rundata.entry.BlueNodeEntry;
import org.kostiskag.unitynetwork.tracker.rundata.table.BlueNodeTable;
import org.kostiskag.unitynetwork.tracker.rundata.table.RedNodeTable;
import org.kostiskag.unitynetwork.tracker.AppLogger;


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
            if (BluenodeLogic.lookupBluenode(bluenodeHostname)) {
                String address = socket.getInetAddress().getHostAddress();
                int port = Integer.parseInt(givenPort);
                if (!BlueNodeTable.getInstance().getOptionalEntry(lock, bluenodeHostname).isPresent()) {
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
                                DataOutputStream writer, SecretKey sessionKey) throws InterruptedException, GeneralSecurityException, IOException {
        String data;
        Optional<BlueNodeEntry> b = BlueNodeTable.getInstance().getOptionalEntry(bnTableLock, bluenodeName);
        if (b.isPresent()) {
            Optional<VirtualAddress> vAddressOpt = HostnameLogic.validateHostname(username, givenHostname, givenHash);
            if (vAddressOpt.isPresent()) {
                if (!BlueNodeTable.getInstance().isOnlineRnByHostname(bnTableLock, givenHostname)) {
                    RedNodeTable rns = b.get().getRedNodes();
                    try {
                        Lock rl = rns.aquireLock();
                        try {
                            rns.lease(rl, givenHostname, vAddressOpt.get());
                            data = "LEASED " + vAddressOpt.get().asString();
                        } catch (IllegalAccessException e) {
                            data = "AUTH_FAILED " + vAddressOpt.get().asString();
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
        Optional<BlueNodeEntry> b = BlueNodeTable.getInstance().getOptionalEntry(bnTableLock, hostname);
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
        Optional<BlueNodeEntry> b = BlueNodeTable.getInstance().getOptionalEntry(bnTableLock, bluenodeName);
        if (b.isPresent()) {
            RedNodeTable rnt = b.get().getRedNodes();
            try {
                Lock rlock = rnt.aquireLock();
                Optional<RedNodeEntry> r = rnt.getOptionalEntry(rlock, hostname);
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
        Optional<BlueNodeEntry> b = BlueNodeTable.getInstance().getOptionalEntry(lock, BNTargetHostname);
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
        Optional<VirtualAddress> vaddrOpt = HostnameLogic.lookupVaddress(hostname);
        if(vaddrOpt.isPresent()) {
            try {
                SocketUtilities.sendAESEncryptedStringData(vaddrOpt.get().asString(), writer, sessionKey);
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

        Optional<String> hostnameOpt = HostnameLogic.lookupHostname(v);
        if (hostnameOpt.isPresent()) {
            try {
                SocketUtilities.sendAESEncryptedStringData(hostnameOpt.get(), writer, sessionKey);
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
        Optional<BlueNodeEntry> b = BlueNodeTable.getInstance().getOptionalEntry(lock, blueNodeHostname);
        if (b.isPresent()) {
            BlueNodeTable.getInstance().release(lock, b.get());
        }
        CommonActions.revokePublicKey(NodeType.BLUENODE, blueNodeHostname, writer, sessionKey);
    }
}
