package kostiskag.unitynetwork.tracker.functions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import kostiskag.unitynetwork.tracker.App;

/**
 *
 * @author kostis
 */
public class ReadPreferencesFile {       

    public static void ParseFile(InputStream file) throws IOException {
        
        Properties cfg = new java.util.Properties();
        cfg.load(file);
       
        String networkName = cfg.getProperty("NetworkName").replaceAll("\\s+","");
        String authPort = cfg.getProperty("AuthPort").replaceAll("\\s+","");        
        String databaseUrl = cfg.getProperty("DatabaseUrl").replaceAll("\\s+","");
        String databaseUser = cfg.getProperty("DatabaseUser").replaceAll("\\s+","");
        String databsePassword = cfg.getProperty("DatabsePassword").replaceAll("\\s+","");
        String blueNodeCapacity = cfg.getProperty("BlueNodeCapacity").replaceAll("\\s+","");     
        String ping = cfg.getProperty("Ping").replaceAll("\\s+","");        
        String useGUI = cfg.getProperty("UseGUI").replaceAll("\\s+","");
        String log = cfg.getProperty("Log").replaceAll("\\s+","");  
        
        App.netName = networkName; 
        App.auth = Integer.parseInt(authPort);        
        App.databaseUrl = databaseUrl;
        App.user = databaseUser;
        App.password = databsePassword;        
        App.bncap = Integer.parseInt(blueNodeCapacity);
        App.pingTime = Integer.parseInt(ping);
        App.gui = Boolean.parseBoolean(useGUI);        
        App.log = Boolean.parseBoolean(log);        
        
        System.out.println("");
        System.out.println("NetworkName is "+App.netName);        
        System.out.println("AuthPort is "+App.auth);                
        System.out.println("Database URL is "+App.databaseUrl);
        System.out.println("BlueNodeLimit is "+App.bncap);        
        System.out.println("ping time is "+App.pingTime+" sec");
        System.out.println("gui is "+App.gui);
        System.out.println("logging is "+App.log);                        
        System.out.println("");                
    }

	public static void GenerateFile(File file) throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter writer = new PrintWriter(file, "UTF-8");
	    writer.print(""
		+ "#############################################\n"
		+ "#         Unity Tracker Config File         #\n"
		+ "#############################################\n"
		+ "\n"
		+ "#\n"
		+ "# Insructions for setting up the config file\n"
		+ "#\n"
		+ "# Do not comment any variable nor remove any from this file as this will result\n"
		+ "# in an application error. Change the value to an appropriate input as described\n"
		+ "# instead. If this file gets messed up, you may delete it and it will be\n"
		+ "# auto-generated from the app in its next run.\n"
		+ "#\n"
		+ "\n"
		+ "#\n"
		+ "# Network and Tracker Settings\n"
		+ "#\n"
		+ "# First of all what shall be the name of the netwrok?\n"
		+ "# Provide a TCP auth port as well. The default is 8000.\n"
		+ "NetworkName = UnityNetwork\n"
		+ "AuthPort = 8000\n"
		+ "\n"
		+ "#\n"
		+ "# Database Settings\n"
		+ "#\n"
		+ "# the url should be in this type of form for mysql\n"
		+ "# DatabaseUrl = jdbc:mysql://IPaddress:port/database\n"
		+ "# DatabaseUser = username\n"
		+ "# DatabsePassword = password\n"
		+ "#\n"
		+ "# the url should be in this type of form for sqlite\n"
		+ "# DatabaseUrl = jdbc:sqlite:local_database_file.db\n"
		+ "#\n"
		+ "DatabaseUrl = jdbc:sqlite:unity.db\n"
		+ "DatabaseUser = username\n"
		+ "DatabsePassword = password\n"
		+ "\n"
		+ "#\n"
		+ "# Load and Capacity\n"
		+ "# Leave zero for an infinite number of connected Blue Nodes\n"
		+ "# otherwise set a maximum limit, ex. 100.\n"
		+ "#\n"
		+ "BlueNodeCapacity = 0\n"
		+ "\n"
		+ "# ping time in sec\n"
		+ "# ping is the time gap where the tracker may search for all active Blue Nodes\n"
		+ "# in order to detect whether someone is not responding\n"
		+ "Ping = 180\n"
		+ "\n"
		+ "#\n"
		+ "# Application behaviour\n"
		+ "#\n"
		+ "\n"
		+ "# set GUI or command line\n"
		+ "# use true or false\n"
		+ "UseGUI = true\n"
		+ "\n"
		+ "# Logging in tracker.log\n"
		+ "# use true or false\n"
		+ "Log = true\n"
	    + "");
	    writer.close();		
	}   
}