package org.kostiskag.unitynetwork.tracker;

import java.io.*;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.sql.SQLException;
import java.util.concurrent.locks.Lock;
import javax.swing.*;

import org.kostiskag.unitynetwork.tracker.database.Database;
import org.kostiskag.unitynetwork.tracker.database.Queries;
import org.kostiskag.unitynetwork.tracker.rundata.calculated.NumericConstraints;
import org.kostiskag.unitynetwork.tracker.rundata.utilities.CryptoUtilities;
import org.kostiskag.unitynetwork.tracker.gui.MainWindow;
import org.kostiskag.unitynetwork.tracker.rundata.table.BlueNodeTable;
import org.kostiskag.unitynetwork.tracker.service.sonar.SonarService;
import org.kostiskag.unitynetwork.tracker.service.track.TrackServer;

/**
 * @author Konstantinos Kagiampakis
 */
public class App {

    // salt
    // you will have to wait for the network branch for this to change
    public static final String SALT = "=UrBN&RLJ=dBshBX3HFn!S^Au?yjqV8MBx7fMyg5p6U8T^%2kp^X-sk9EQeENgVEj%DP$jNnz&JeF?rU-*meW5yFkmAvYW_=mA+E$F$xwKmw=uSxTdznSTbunBKT*-&!";
    // file names
    public static final String CONFIG_FILE_NAME = "tracker.conf";
    public static final String LOG_FILE_NAME = "tracker.log";
    public static final String KEY_PAIR_FILE_NAME = "public_private.keypair";

    //this is a singleton although not enforced
    public static App TRACKER_APP;

    //These are the imported settings from file
    public final String netName;
    public final int auth;
    // database
    public final String databaseUrl;
    public final String user;
    public final String password;
    // capacity
    public final int bncap;
    public final int pingTime;
    public final boolean gui;
    public final boolean log;
    //keypair
    public final KeyPair trackerKeys;

    private App(ReadPreferencesFile pref) {
        this.netName = pref.netName;
        this.auth = pref.auth;
        this.databaseUrl = pref.databaseUrl;
        this.user = pref.user;
        this.password = pref.password;
        this.bncap = pref.bncap;
        this.pingTime = pref.pingTime;
        this.gui = pref.gui;
        this.log = pref.log;

        //1. DB object
        try {
            Database.newInstance(this.databaseUrl, this.user, this.password);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            die();
        }

        // 2. BN table
        System.out.println("Initializing BN table...");
        BlueNodeTable.newInstance(this.bncap);

        // 3. log
        File logFile = null;
        if (log) {
            System.out.println("Initializing log file " + LOG_FILE_NAME);
            logFile = new File(LOG_FILE_NAME);
            try (FileWriter fw = new FileWriter(logFile, false)) {
                fw.write("---------------------------------------------------------------\n");
            } catch (IOException ex) {
                System.out.println(
                        "Log file error! If the error continues disable logging from the " + CONFIG_FILE_NAME + " file.");
                die();
            }
        }

        // 4. gui
        if (gui) {
            System.out.println("Checking GUI libraries...");
            try {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            } catch (Exception e) {
                try {
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
                } catch (Exception ex) {
                    try {
                        UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
                    } catch (Exception ex1) {
                        System.err.println(
                                "Although requested for GUI there are no Java GUI libs on the system. If you are unable to solve this error you may disable the GUI from the "
                                        + CONFIG_FILE_NAME + " config file.");
                        die();
                    }
                }
            }

            System.out.println("Initializing gui...");
            MainWindow.newInstance();
            MainWindow.getInstance().showWindow();
        }

        //5. Logger
        AppLogger.newInstance(MainWindow.getInstance(), logFile);

        AppLogger.getLogger().consolePrint("");
        AppLogger.getLogger().consolePrint("NetworkName is " + this.netName);
        AppLogger.getLogger().consolePrint("AuthPort is " + this.auth);
        AppLogger.getLogger().consolePrint("Database URL is " + this.databaseUrl);
        AppLogger.getLogger().consolePrint("BlueNodeLimit is " + this.bncap);
        AppLogger.getLogger().consolePrint("ping time is " + this.pingTime + " sec");
        AppLogger.getLogger().consolePrint("gui is " + this.gui);
        AppLogger.getLogger().consolePrint("logging is " + this.log);
        AppLogger.getLogger().consolePrint("");

        // 6. RSA key pair
        File keyPairFile = new File(KEY_PAIR_FILE_NAME);
        KeyPair keys = null;
        if (keyPairFile.exists()) {
            // the tracker has key pair
            AppLogger.getLogger().consolePrint("Loading RSA key pair from file...");
            try {
                keys = CryptoUtilities.fileToObject(keyPairFile);
                AppLogger.getLogger().consolePrint(
                        "Your public key is:\n" + CryptoUtilities.bytesToBase64String(keys.getPublic().getEncoded()));
            } catch (GeneralSecurityException | IOException e) {
                AppLogger.getLogger().consolePrint("Public / Private key failed to read from file");
                die();
            }
        } else {
            // the tracker does not have a public private key pair
            // generating...
            try {
                AppLogger.getLogger().consolePrint("Generating RSA key pair...");
                keys = CryptoUtilities.generateRSAkeyPair();
                // and storing
                AppLogger.getLogger().consolePrint("Generating key file...");
                CryptoUtilities.objectToFile(keys, keyPairFile);
                AppLogger.getLogger().consolePrint(
                        "Your public key is:\n" + CryptoUtilities.bytesToBase64String(
                                keys.getPublic().getEncoded()));
            } catch (GeneralSecurityException | IOException e) {
                AppLogger.getLogger().consolePrint("Public / Private key pair failed to be generated");
                die();
            }
        }
        //finalizing!
        trackerKeys = keys;

        // 7. database
        AppLogger.getLogger().consolePrint("Testing Database Connection on " + databaseUrl + " ... ");
        testDbConnection();
        try {
            Queries.validateDatabase();
        } catch (SQLException e) {
            AppLogger.getLogger().consolePrint("Database validation failed.");
            e.printStackTrace();
            die();
        }
        AppLogger.getLogger().consolePrint("Database validation complete.");

        // 8. track service
        AppLogger.getLogger().consolePrint("Initializing AuthService on port " + auth + " ...");
        try {
            TrackServer.newInstance(auth).start();
        } catch (IllegalAccessException e) {
            AppLogger.getLogger().consolePrint("wrong tcp port range use from 1 to "
                    + NumericConstraints.MAX_ALLOWED_PORT_NUM.size() + ". Fix the " + CONFIG_FILE_NAME);
            die();
        }

        // 9. sonar service
        try {
            SonarService.newInstance(pingTime).start();
        } catch (IllegalAccessException e) {
            AppLogger.getLogger().consolePrint("Non valid ping time detected. Please correct the "
                    + CONFIG_FILE_NAME + " file");
            die();
        }

    }

    public void terminate() {
        SonarService.getInstance().kill();
        try {
            Lock lock = BlueNodeTable.getInstance().aquireLock();
            BlueNodeTable.getInstance().sendKillSigsAndClearTable(lock);
        } catch (InterruptedException e) {
            AppLogger.getLogger().consolePrint(e.getMessage());
        } finally {
            BlueNodeTable.getInstance().releaseLock();
        }
        AppLogger.getLogger().consolePrint("Tracker is going to terminate.");
        die();
    }

    public static void die() {
        System.out.println("Tracker is going to exit.");
        System.exit(1);
    }

    private void testDbConnection() {
        Database db = Database.getInstance();
        try {
            db.connect();
            db.close();
        } catch (SQLException e) {
            AppLogger.getLogger().consolePrint(e.getMessage());
            try {
                db.close();
            } catch (SQLException e1) {
                AppLogger.getLogger().consolePrint(e1.getMessage());
            } finally {
                die();
            }
        }
    }

    /**
     * The app's main class here
     */
    public static void main(String[] args) {
        System.out.println("@Started main at " + Thread.currentThread().getName());
        System.out.println("Opening configuration file " + CONFIG_FILE_NAME + "...");
        File file = new File(CONFIG_FILE_NAME);
        ReadPreferencesFile prefFile = null;
        if (file.exists()) {
            try (InputStream filein = new FileInputStream(file)) {
                prefFile = ReadPreferencesFile.ParseFile(filein);
            } catch (Exception e) {
                System.err.println("File " + CONFIG_FILE_NAME + " could not be loaded");
            }
        } else {
            System.out.println("The " + CONFIG_FILE_NAME
                    + " file was not found in the dir. Generating new file with the default settings");
            try {
                ReadPreferencesFile.GenerateFile(file);
            } catch (IOException e) {
                System.err.println("File " + CONFIG_FILE_NAME + " could not be created.");
                die();
            }

            try (InputStream filein = new FileInputStream(file)) {
                prefFile = ReadPreferencesFile.ParseFile(filein);
            } catch (IOException e) {
                System.err.println("File " + CONFIG_FILE_NAME + " could not be loaded.");
                die();
            }
        }

        System.out.println("Starting UnityTracker... ");
        App.TRACKER_APP = new App(prefFile);
    }
}
