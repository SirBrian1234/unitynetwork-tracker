package org.kostiskag.unitynetwork.tracker;

import java.io.*;
import java.security.KeyPair;
import java.sql.SQLException;
import javax.swing.UIManager;

import org.kostiskag.unitynetwork.tracker.database.Database;
import org.kostiskag.unitynetwork.tracker.database.Queries;
import org.kostiskag.unitynetwork.tracker.functions.CryptoMethods;
import org.kostiskag.unitynetwork.tracker.functions.ReadPreferencesFile;
import org.kostiskag.unitynetwork.tracker.gui.MainWindow;
import org.kostiskag.unitynetwork.tracker.rundata.BlueNodeTable;
import org.kostiskag.unitynetwork.tracker.service.sonar.SonarService;
import org.kostiskag.unitynetwork.tracker.service.track.TrackServer;

/**
 *
 * @author Konstantinos Kagiampakis
 */
public class App {

	// user input max sizes
	public static final int MAX_INT_STR_LEN = 32;
	public static final int MAX_STR_LEN_SMALL_SIZE = 128;
	public static final int MAX_STR_LEN_LARGE_SIZE = 256;
	public static final int MIN_PASSWORD_LEN = 5;
	public static final int MIN_USERNAME_LEN = 4;
	public static final int MAX_STR_ADDR_LEN = "255.255.255.255".length();
	public static final int MAX_ALLOWED_PORT_NUM = 65535;

	// network maths
	public static final int VIRTUAL_NETWORK_ADDRESS_CAPACITY = (int) (Math.pow(2, 24) - 2);
	public static final int SYSTEM_RESERVED_ADDRESS_NUMBER = 1;
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
	
	// data
	public TrackServer track;
	public MainWindow window;
	public BlueNodeTable BNtable;
	public SonarService sonar;
	public KeyPair trackerKeys;

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

		//0. db object
		try {
			Database.newInstance(this.databaseUrl,this.user, this.password);
		} catch (SQLException e) {
			System.out.println(e);
			die();
		}

		// 2. log
		File logFile = null;
		if (log) {
			System.out.println("initializing log file " + LOG_FILE_NAME);
			logFile = new File(LOG_FILE_NAME);
			try (FileWriter fw = new FileWriter(logFile, false)) {
				fw.write("---------------------------------------------------------------\n");
			} catch (IOException ex) {
				System.out.println(
						"Log file error! If the error continues disable logging from the " + CONFIG_FILE_NAME + " file.");
				die();
			}
		}

		// 1. gui
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

			System.out.println("initializing gui...");
			window = MainWindow.instanceOf();
			window.showWindow();
		}
		
		AppLogger.newInstance(window, logFile);

		AppLogger.getLogger().consolePrint("");
		AppLogger.getLogger().consolePrint("NetworkName is "+this.netName);
		AppLogger.getLogger().consolePrint("AuthPort is "+this.auth);
		AppLogger.getLogger().consolePrint("Database URL is "+this.databaseUrl);
		AppLogger.getLogger().consolePrint("BlueNodeLimit is "+this.bncap);
		AppLogger.getLogger().consolePrint("ping time is "+this.pingTime+" sec");
		AppLogger.getLogger().consolePrint("gui is "+this.gui);
		AppLogger.getLogger().consolePrint("logging is "+this.log);
		AppLogger.getLogger().consolePrint("");

		// 3. rsa key pair
		File keyPairFile = new File(KEY_PAIR_FILE_NAME);
		if (keyPairFile.exists()) {
			// the tracker has key pair
			AppLogger.getLogger().consolePrint("Loading RSA key pair from file...");
			trackerKeys = (KeyPair) CryptoMethods.fileToObject(keyPairFile);
			AppLogger.getLogger().consolePrint(
					"Your public key is:\n" + CryptoMethods.bytesToBase64String(trackerKeys.getPublic().getEncoded()));

		} else {
			// the tracker does not have a public private key pair
			// generating...
			AppLogger.getLogger().consolePrint("Generating RSA key pair...");
			trackerKeys = CryptoMethods.generateRSAkeyPair();
			// and storing
			AppLogger.getLogger().consolePrint("Generating key file...");
			CryptoMethods.objectToFile(trackerKeys, keyPairFile);
			AppLogger.getLogger().consolePrint(
					"Your public key is:\n" + CryptoMethods.bytesToBase64String(trackerKeys.getPublic().getEncoded()));
		}

		// 4. database
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

		// 5. tables
		AppLogger.getLogger().consolePrint("initializing tables...");
		BNtable = new BlueNodeTable();

		// 6. service
		AppLogger.getLogger().consolePrint("initializing AuthService on port " + auth + " ...");
		if (auth > 0 && auth <= App.MAX_ALLOWED_PORT_NUM) {
			track = new TrackServer(auth);
			track.start();
		} else {
			AppLogger.getLogger().consolePrint("wrong tcp port range use from 1 to "
					+App.MAX_ALLOWED_PORT_NUM+". Fix the " + CONFIG_FILE_NAME);
			die();
		}

		// 7. sonar service
		if (pingTime > 0) {
			sonar = new SonarService(pingTime);
			sonar.start();
		} else {
			AppLogger.getLogger().consolePrint("Non valid ping time detected. Please correct the " + CONFIG_FILE_NAME + " file");
			die();
		}
	}

	public void terminate() {
		sonar.kill();
		BNtable.sendKillSigsAndClearTable();
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
			} catch (FileNotFoundException | UnsupportedEncodingException e) {
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
