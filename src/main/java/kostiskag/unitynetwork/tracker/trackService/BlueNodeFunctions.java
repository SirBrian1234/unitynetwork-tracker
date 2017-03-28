/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kostiskag.unitynetwork.tracker.trackService;

import kostiskag.unitynetwork.tracker.App;
import kostiskag.unitynetwork.tracker.database.DBConnection;
import kostiskag.unitynetwork.tracker.database.Queries;
import kostiskag.unitynetwork.tracker.functions.MD5Functions;
import kostiskag.unitynetwork.tracker.functions.SocketFunctions;
import kostiskag.unitynetwork.tracker.functions.VAddressFunctions;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kostis These are all the queries a bluenode may do
 *
 * LEASE BN LEASE RN [NAME] RELEASE BN RELEASE RN [NAME] GETPH CHECKRN
 */
public class BlueNodeFunctions {
    

    public static void BlueLease(String BlueNodeHostname, String givenPort, DBConnection con, ResultSet GetResults, PrintWriter writer, Socket socket) {

        String data = null;
        GetResults = Queries.GetResults("SELECT name from bluenodes", con);
        if (GetResults == null) {
            return;
        }
        boolean found = false;
        try {
            while (GetResults.next() && !found) {
                if (GetResults.getString("name").equals(BlueNodeHostname)) {
                    found = true;
                    String address = socket.getInetAddress().getHostAddress();
                    int port = Integer.parseInt(givenPort);
                    if (!App.BNtable.checkOnlineByHn(BlueNodeHostname)) {
                        //normal connect for a non associated BN
                        App.BNtable.lease(BlueNodeHostname, address, port, 0, new Time(System.currentTimeMillis()));
                    } else {
                        //the BN crashed and asks to reconnect to the system
                        App.RNtable.releaseByBN(BlueNodeHostname);
                        App.BNtable.Renew(BlueNodeHostname, address, port, 0, new Time(System.currentTimeMillis()));
                    }
                    data = "LEASED " + address;
                }
            }
            if (found == false) {
                data = "LEASE_FAILED";
            }
        } catch (SQLException ex) {
            Logger.getLogger(TrackService.class.getName()).log(Level.SEVERE, null, ex);
            data = "SYSTEM_ERROR";
        }
        SocketFunctions.sendFinalData(data, writer);
    }

    public static void RedLease(String BlueNodeHostname, String givenHostname, String username, String password, DBConnection con, ResultSet GetResults, PrintWriter writer) {
        int userauth = checkUser(password, con, GetResults);
        String data = null;
        if (userauth > 0) {
            boolean found = false;
            try {
                GetResults = Queries.GetResults("SELECT id, hostname FROM hostnames where userid='" + userauth + "'", con);
                if (GetResults == null) {
                    return;
                }
                while (GetResults.next() && !found) {
                    String hostname = GetResults.getString("hostname");                    
                    if (hostname.equals(givenHostname)) {
                        found = true;
                        if (!App.RNtable.checkOnlineByHn(hostname)) {
                            int id = GetResults.getInt("id");
                            int inuserid = checkUser(password, con, GetResults);
                            if (userauth == inuserid) {
                                App.RNtable.lease(hostname, VAddressFunctions.numberTo10ipAddr(""+id), BlueNodeHostname, new Time(System.currentTimeMillis()));
                                data = "LEASED " + id;
                                App.BNtable.getBlueNodeEntryByHn(BlueNodeHostname).increaseLoad();
                            } else {
                                data = "USER_HOSTNAME_MISSMATCH";
                            }
                        } else {
                            data = "ALLREADY_LEASED";
                        }
                    }
                }
                if (found == false) {
                    data = "LEASE_FAILED";
                }
            } catch (SQLException ex) {
                Logger.getLogger(BlueNodeFunctions.class.getName()).log(Level.SEVERE, null, ex);
                data = "SYSTEM_ERROR";
            }
            SocketFunctions.sendFinalData(data, writer);
        } else {
            SocketFunctions.sendFinalData("AUTH_FAILED", writer);
        }
    }

    public static void BlueRel(String hostname, PrintWriter writer) {
        String data = null;
        if (App.BNtable.checkOnlineByHn(hostname)) {
            App.RNtable.releaseByBN(hostname);
            App.BNtable.release(hostname);
            data = "RELEASED";
        } else {
            data = "RELEASE_FAILED";
        }
        SocketFunctions.sendFinalData(data, writer);
    }

    public static void RedRel(String givenBNhostname, String givenHostname, PrintWriter writer) {
        String data = null;
        boolean found = false;

        if (App.RNtable.checkOnlineByHn(givenHostname)) {
            found = true;
            if (App.RNtable.getRedNodeEntryByHn(givenHostname).getBNhostname().equals(givenBNhostname)) {
                App.RNtable.release(givenHostname);
                App.BNtable.getBlueNodeEntryByHn(givenBNhostname).decreaseLoad();
                data = "RELEASED";
            } else {
                data = "NOT_AUTHORIZED";
            }
        }
        if (found == false) {
            data = "NOT_AUTHORIZED";
        }

        SocketFunctions.sendFinalData(data, writer);
    }

    //gets the phaddress and port for a known BN
    public static void GetPh(String BNTargetHostname, PrintWriter writer) {

        String data;

        if (App.BNtable.checkOnlineByHn(BNTargetHostname)) {
            data = App.BNtable.getBlueNodeEntryByHn(BNTargetHostname).getPhaddress() + " " + App.BNtable.getBlueNodeEntryByHn(BNTargetHostname).getPort();
        } else {
            data = "NOT_FOUND";
        }
        SocketFunctions.sendFinalData(data, writer);
    }

    //checks if a RN is ONLINE 
    public static void CheckRn(String RNHostname, PrintWriter writer) {

        String data;
        if (App.RNtable.checkOnlineByHn(RNHostname)) {
            data = "ONLINE " + App.RNtable.getRedNodeEntryByHn(RNHostname).getBNhostname();
        } else {
            data = "OFFLINE";
        }
        SocketFunctions.sendFinalData(data, writer);
    }
    
    public static void CheckRnAddr(String vaddress, PrintWriter writer) {

        String data;
        if (App.RNtable.checkOnlineByAddr(vaddress)) {
            data = "ONLINE " + App.RNtable.getRedNodeEntryByAddr(vaddress).getBNhostname();
        } else {
            data = "OFFLINE";
        }
        SocketFunctions.sendFinalData(data, writer);
    }

    public static int authBN(String BlueNodeHostname, DBConnection con, ResultSet GetResults) {
        GetResults = Queries.GetResults("SELECT id, name FROM bluenodes", con);
        if (GetResults == null) {
            return -2;
        }
        try {
            while (GetResults.next()) {
                if (GetResults.getString("name").equals(BlueNodeHostname)) {
                    if (App.BNtable.checkOnlineByHn(BlueNodeHostname)) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            }
            return -1;
        } catch (SQLException ex) {
            Logger.getLogger(BlueNodeFunctions.class.getName()).log(Level.SEVERE, null, ex);
            return -2;
        }
    }

    public static int checkUser(String outhash, DBConnection con, ResultSet GetResults) {
        String data;
        GetResults = Queries.GetResults("SELECT id, username, password FROM users", con);
        if (GetResults == null) {
            return -1;
        }
        try {
            int i = 0;
            while (GetResults.next()) {
                data = GetResults.getString("username") + "lol!_you_just_cant_copy_hashes_and_use_them_from_the_webpage" + GetResults.getString("password");
                try {
                    data = MD5Functions.MD5(data);
                } catch (NoSuchAlgorithmException ex) {
                    Logger.getLogger(BlueNodeFunctions.class.getName()).log(Level.SEVERE, null, ex);
                    data = "SYSTEM_ERROR";
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(BlueNodeFunctions.class.getName()).log(Level.SEVERE, null, ex);
                    data = "SYSTEM_ERROR";
                }
                if (outhash.equals(data)) {
                    return GetResults.getInt("id");
                }
            }
            return 0;
        } catch (SQLException ex) {
            Logger.getLogger(BlueNodeFunctions.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
    }

    //updates known address, works like dyndns 
    public static void UpdatePh(String BNhostname, PrintWriter writer, Socket socket) {
        if (App.BNtable.checkOnlineByHn(BNhostname)) {
            String data = "OK";
            App.BNtable.getBlueNodeEntryByHn(BNhostname).setPhaddress(socket.getInetAddress().getHostAddress());
            SocketFunctions.sendFinalData(data, writer);
        } else {
            String data = "NOT_FOUND";
            SocketFunctions.sendFinalData(data, writer);
        }
    }

    public static void Report(String BNhostname, String hostname, PrintWriter writer) {
        if (App.BNtable.checkOnlineByHn(BNhostname)) {
            if (App.BNtable.checkOnlineByHn(hostname)) {
                String data = "OK";
                SocketFunctions.sendFinalData(data, writer);
            } else {
                String data = "ERROR DST NOT_ONLINE";
                SocketFunctions.sendFinalData(data, writer);
            }
        } else {
            String data = "ERROR SOURCE NOT_ONLINE";
            SocketFunctions.sendFinalData(data, writer);
        }
    }
}
