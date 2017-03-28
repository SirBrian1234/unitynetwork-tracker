/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kostiskag.unitynetwork.tracker;

import kostiskag.unitynetwork.tracker.GUI.MainWindow;
import kostiskag.unitynetwork.tracker.database.DBConnection;
import kostiskag.unitynetwork.tracker.functions.*;
import kostiskag.unitynetwork.tracker.runData.BlueNodeTable;
import kostiskag.unitynetwork.tracker.runData.RedNodeTable;
import kostiskag.unitynetwork.tracker.sonarService.Sonar;
import kostiskag.unitynetwork.tracker.trackService.TrackServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;

/**
 *
 * @author kostis
 */
public class App {

    public static TrackServer track;
    private static MainWindow window;
    public static BlueNodeTable BNtable;
    public static RedNodeTable RNtable;
    public static boolean gui = true;
    public static String NetwName;
    public static int auth;
    //database
    public static String url;
    public static String user;
    public static String password;
    //capacity
    public static int bncap;
    public static int rncap;
    public static boolean autoScrollDown = true;
    private static int messageCount;
    public static boolean log;
    public static File logFile;
    public static int pingTime;

    public App() {

        //0. gui
        if (gui) {
            System.out.println("initializing gui...");
            window = new MainWindow();
            window.setVisible(true);
        }
        
        //1. log
        if (log) {
            ConsolePrint("initializing log file");
            logFile = new File("tracker.log");
            FileWriter fw;
            try {
                fw = new FileWriter(logFile, false);
                fw.write("---------------------------------------------------------------\n");
                fw.close();
            } catch (IOException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                ConsolePrint("file error! check for permissions.. if the error continues disable log from settings");
                die();
            }
        }

        //2. tables
        ConsolePrint("initializing tables...");
        BNtable = new BlueNodeTable(bncap);
        RNtable = new RedNodeTable(rncap);

        //3. db
        ConsolePrint("Testing Database Connection on " + url + " ... ");
        DBConnection con = new DBConnection();
        if (con != null) {
            ConsolePrint("Database OK");
            try {
                con.getCon().close();
            } catch (SQLException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            ConsolePrint("Database error! Check for valid address or valid credentials.");
            die();
        }
        
        //4. socket
        ConsolePrint("initializing AuthService on port " + auth + " ...");
        if (auth > 0 && auth <= 65535) {
            track = new TrackServer(auth);
            track.start();
        } else {
            ConsolePrint("wrong tcp port range use from 1 to 65535. Fix the .conf");
        }
        
        //5. sonar
        if (pingTime > 0) {
            Sonar sonar = new Sonar(pingTime);
            sonar.start();
        } else {
            ConsolePrint("Non valid ping time detected. correct the .conf");
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

    /*
     * THIS IS THE "MAIN" EVERYTHING STARTS FROM HERE    
     */
    public static void main(String[] args) {
        System.out.println("@Started main at " + Thread.currentThread().getName());
        System.out.println("Reading Configuration File... ");
        try {
            InputStream file = new FileInputStream("tracker.conf");
            ReadPreferencesFile.ParseFile(file);
        } catch (FileNotFoundException ex) {
            System.err.println("File not found make sure there is tracker.conf file in your dir");
            die();
        }

        if (gui) {
            System.out.println("checking gui libraries...");
            try {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            } catch (Exception e) {
                try {
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
                } catch (Exception ex) {
                    try {
                        UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
                    } catch (Exception ex1) {
                        System.err.println("Although requested for gui there are no gui libs on the machiene. please fix it or disable gui from .conf");
                        die();
                    }
                }
            }
        }

        System.out.println("Starting UnityTracker... ");
        App tracker = new App();
    }
}
