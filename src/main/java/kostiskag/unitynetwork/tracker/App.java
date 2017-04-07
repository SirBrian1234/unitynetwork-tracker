package kostiskag.unitynetwork.tracker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import kostiskag.unitynetwork.tracker.GUI.MainWindow;
import kostiskag.unitynetwork.tracker.database.Database;
import kostiskag.unitynetwork.tracker.database.Queries;
import kostiskag.unitynetwork.tracker.functions.ReadPreferencesFile;
import kostiskag.unitynetwork.tracker.runData.BlueNodeTable;
import kostiskag.unitynetwork.tracker.runData.RedNodeTable;
import kostiskag.unitynetwork.tracker.sonarService.Sonar;
import kostiskag.unitynetwork.tracker.sync.Lock;
import kostiskag.unitynetwork.tracker.trackService.TrackServer;

/**
 *
 * @author Konstantinos Kagiampakis
 */
public class App {

	//this are our locks which will assure that no thread accesses
	//the same data pools concurrently
	public static Lock databaseLock;
	//user input max sizes
	public static int max_int_str_len = 32;
	public static int max_str_len_small_size = 128;
	public static int max_str_len_large_size = 256;
	// file names
	public static String configFileName = "tracker.conf";
	public static String logFileName = "tracker.log";
	// data
	public static TrackServer track;
	public static MainWindow window;
	public static BlueNodeTable BNtable;
	public static RedNodeTable RNtable;
	public static boolean gui = true;
	public static String netName;
	public static int auth;
	// database
	public static String databaseUrl;
	public static String user;
	public static String password;
	// capacity
	public static int bncap;
	public static int rncap;
	public static boolean autoScrollDown = true;
	private static int messageCount;
	public static boolean log;
	public static File logFile;
	public static int pingTime;

	public App() {

		// 1. log
		if (log) {
			ConsolePrint("initializing log file "+logFileName);
			File logFile = new File(logFileName);
			FileWriter fw;
			try {
				fw = new FileWriter(logFile, false);
				fw.write("---------------------------------------------------------------\n");
				fw.close();
			} catch (IOException ex) {
				Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
				ConsolePrint("Log file error! If the error continues disable logging from the "+configFileName+" file.");
				die();
			}
		}

		// 2. database
		databaseLock = new Lock();
		ConsolePrint("Testing Database Connection on " + databaseUrl + " ... ");
		testDbConnection();
		try {
			Queries.validateDatabase();
		} catch (SQLException | InterruptedException e) {
			ConsolePrint("Database validation failed.");
			e.printStackTrace();
			die();
		}
		ConsolePrint("Database validation complete.");
		
		// 3. tables
		ConsolePrint("initializing tables...");
		BNtable = new BlueNodeTable(bncap);
		RNtable = new RedNodeTable(rncap);
			
		// 4. gui
		if (gui) {
			System.out.println("initializing gui...");
			window = new MainWindow();
			window.setVisible(true);
		}
		
		// 5. service
		ConsolePrint("initializing AuthService on port " + auth + " ...");
		if (auth > 0 && auth <= 65535) {
			track = new TrackServer(auth);
			track.start();
		} else {
			ConsolePrint("wrong tcp port range use from 1 to 65535. Fix the "+configFileName);
		}

		// 6. sonar
		if (pingTime > 0) {
			Sonar sonar = new Sonar(pingTime);
			sonar.start();
		} else {
			ConsolePrint("Non valid ping time detected. Please correct the "+configFileName+" file");
			die();
		}
	}

	public static void ConsolePrint(String Message) {
		if (gui && window != null) {
			System.out.println(Message);
			window.jTextArea1.append(Message + "\n");
			messageCount++;
			if (messageCount > 10000) {
				messageCount = 0;
				window.jTextArea1.setText("");
			}
			if (autoScrollDown) {
				window.jTextArea1.select(window.jTextArea1.getHeight() + 10000, 0);
			}
		} else {
			System.out.println(Message);
		}

		if (log) {
			FileWriter fw;
			try {
				fw = new FileWriter("tracker.log", true);
				fw.append(Message + "\n");
				fw.close();
			} catch (IOException ex) {
				Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	public static void die() {
		ConsolePrint("Tracker will die in 3 secs");
		try {
			Thread.sleep(3000);
		} catch (InterruptedException ex) {
			Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
		}
		System.exit(1);
	}

	public static void testDbConnection() {		
		Database db = null;
		try {
			db = new Database();
		} catch (SQLException e) {
			ConsolePrint("Database error! Check for valid address or valid credentials.");
			e.printStackTrace();
			try {
				db.close();
			} catch (SQLException ex) {
				e.printStackTrace();
				die();
			}			
			die();
		}		
		
		try {
			db.close();
		} catch (SQLException e) {			
			e.printStackTrace();
			die();
		}
	}

	/*
	 * main class here
	 */
	public static void main(String[] args) {
		System.out.println("@Started main at " + Thread.currentThread().getName());

		InputStream filein = null;
		System.out.println("Opening configuration file " + configFileName + "...");
		File file = new File(configFileName);
		if (file.exists()) {
			try {
				filein = new FileInputStream(file);
				ReadPreferencesFile.ParseFile(filein);
				filein.close();
			} catch (Exception e) {
				System.err.println("File " + configFileName + " could not be loaded");
				e.printStackTrace();
				die();
			}
		} else {
			System.out.println("The "+configFileName 
					+ " file was not found in the dir. Generating new file with the default settings");
			try {
				ReadPreferencesFile.GenerateFile(file);
				filein = new FileInputStream(file);
				ReadPreferencesFile.ParseFile(filein);
				filein.close();
			} catch (Exception e) {
				System.err.println("File " + configFileName + " could not be loaded.");
				e.printStackTrace();
				die();
			}
		}

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
								"Although requested for GUI there are no Java GUI libs on the system. If you are unable to solve this error you may disable the GUI from the "+configFileName+" config file.");
						die();
					}
				}
			}
		}

		System.out.println("Starting UnityTracker... ");
		App tracker = new App();
	}
}
