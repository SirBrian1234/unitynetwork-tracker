package org.kostiskag.unitynetwork.tracker;

import java.io.*;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.sql.SQLException;
import java.util.concurrent.locks.Lock;
import javax.swing.*;

import org.kostiskag.unitynetwork.common.calculated.NumericConstraints;
import org.kostiskag.unitynetwork.common.utilities.CryptoUtilities;
import org.kostiskag.unitynetwork.tracker.database.Queries;
import org.kostiskag.unitynetwork.tracker.gui.MainWindow;
import org.kostiskag.unitynetwork.tracker.rundata.table.BlueNodeTable;
import org.kostiskag.unitynetwork.tracker.service.sonar.SonarService;
import org.kostiskag.unitynetwork.tracker.service.track.TrackServer;

/**
 * @author Konstantinos Kagiampakis
 */
public final class App {

    // file names
    private enum FileNames {
        CONFIG_FILE("tracker.conf"),
        LOG_FILE("tracker.log"),
        KEY_PAIR_FILE("public_private.keypair");

        private File f;

        FileNames(String name) {
            this.f = new File(name);
        }

        public File getFile() {
            return f;
        }
    }

    //SALT
    //this has to be generated
    public static final String SALT = "=UrBN&RLJ=dBshBX3HFn!S^Au?yjqV8MBx7fMyg5p6U8T^%2kp^X-sk9EQeENgVEj%DP$jNnz&JeF?rU-*meW5yFkmAvYW_=mA+E$F$xwKmw=uSxTdznSTbunBKT*-&!";

    //this is a singleton although not enforced
    public static App TRACKER_APP;

    //These are the imported settings from file
    private final String netName;
    private final int auth;
    // database
    private final String databaseUrl;
    private final String user;
    private final String password;
    // capacity
    private final int bncap;
    private final int pingTime;
    private final boolean gui;
    private final boolean log;

    //keypair
    //the private key has to be secure at all times!, therefore it is going to be provided as a constructor dependency,
    //only to the objects requiring it in order to reach
    //BlueNodeClient, TrackService that use it
    private final KeyPair trackerKeys;

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

        // 1. log file
        File logFile = null;
        if (log) {
            System.out.println("Initializing log file " + FileNames.LOG_FILE.getFile());
            logFile = FileNames.LOG_FILE.getFile();
            try (FileWriter fw = new FileWriter(logFile, false)) {
                fw.write("---------------------------------------------------------------\n");
            } catch (IOException ex) {
                System.out.println(
                        "Log file error! If the error continues disable logging from the " + FileNames.CONFIG_FILE.getFile() + " file.");
                die();
            }
        }

        // 2. DB object
        try {
            Queries.setDatabaseInstance(this.databaseUrl, this.user, this.password);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            die();
        }

        // 3. gui (uses db object)
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
                                        + FileNames.CONFIG_FILE.getFile() + " config file.");
                        die();
                    }
                }
            }

            System.out.println("Initializing gui...");
            MainWindow.newInstance();
            MainWindow.getInstance().showWindow();
        }

        // 4. Logger (uses gui and log file)
        AppLogger.newInstance(MainWindow.getInstance(), logFile);
        //Time to verbose App
        AppLogger.getLogger().consolePrint(this.toString());

        // 5. RSA key pair
        File keyPairFile = FileNames.KEY_PAIR_FILE.getFile();
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

        // 6. BN table (uses keypair)
        System.out.println("Initializing BN table...");
        BlueNodeTable.newInstance(this.bncap, this.trackerKeys);

        // 7. database
        AppLogger.getLogger().consolePrint("Testing Database Connection on " + databaseUrl + " ... ");
        try (Queries q = Queries.getInstance()) {
            q.validate();
        } catch (InterruptedException e) {
            AppLogger.getLogger().consolePrint("Could not acquire lock!");
        } catch (SQLException e) {
            AppLogger.getLogger().consolePrint(e.getLocalizedMessage());
        }
        AppLogger.getLogger().consolePrint("Database validation complete.");

        // 8. sonar service
        try {
            SonarService.newInstance(pingTime).start();
        } catch (IllegalAccessException e) {
            AppLogger.getLogger().consolePrint("Non valid ping time detected. Please correct the "
                    + FileNames.CONFIG_FILE.getFile() + " file");
            die();
        }

        // 9. track service (uses keypair)
        AppLogger.getLogger().consolePrint("Initializing Track Service on port " + auth + " ...");
        try {
            TrackServer.newInstance(auth, trackerKeys).start();
        } catch (IllegalAccessException e) {
            AppLogger.getLogger().consolePrint("wrong tcp port range use from 1 to "
                    + NumericConstraints.MAX_ALLOWED_PORT_NUM.size() + ". Fix the " + FileNames.CONFIG_FILE.getFile());
            die();
        }
    }

    public synchronized void terminate() {
        //A tracker's responsibility on a soft exit is to inform all the connected bluenodes
        //for the upcoming exit
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

    public synchronized static void die() {
        System.out.println("Tracker is going to exit.");
        System.exit(1);
    }

    @Override
    public String toString() {
        return String.join("\n",
            "",
            "NetworkName is " + this.netName,
            "AuthPort is " + this.auth,
            "Database URL is " + this.databaseUrl,
            "BlueNodeLimit is " + this.bncap,
            "ping time is " + this.pingTime + " sec",
            "gui is " + this.gui,
            "logging is " + this.log,
            //tracker keys not showing
            ""
        );
    }

    /**
     * The app's main class here
     */
    public static void main(String... args) {
        System.out.println("@Started main at " + Thread.currentThread().getName());
        //Reading configuration settings from file
        System.out.println("Opening configuration file " + FileNames.CONFIG_FILE.getFile() + "...");
        File file = FileNames.CONFIG_FILE.getFile();
        ReadPreferencesFile prefFile = null;
        if (file.exists()) {
            try {
                prefFile = ReadPreferencesFile.ParseFile(file);
            } catch (IOException e) {
                System.err.println("File " + FileNames.CONFIG_FILE.getFile() + " although existing, could not be loaded");
                die();
            }
        } else {
            System.out.println("The " + FileNames.CONFIG_FILE.getFile()
                    + " file was not found in the dir. Generating new file with the default settings");
            try {
                ReadPreferencesFile.GenerateFile(file);
            } catch (IOException e) {
                System.err.println("File " + FileNames.CONFIG_FILE.getFile() + " could not be created.");
                die();
            }

            try {
                prefFile = ReadPreferencesFile.ParseFile(file);
            } catch (IOException e) {
                System.err.println("File " + FileNames.CONFIG_FILE.getFile() + " could not be loaded.");
                die();
            }
        }

        System.out.println("Starting UnityTracker... ");
        App.TRACKER_APP = new App(prefFile);
    }
}
