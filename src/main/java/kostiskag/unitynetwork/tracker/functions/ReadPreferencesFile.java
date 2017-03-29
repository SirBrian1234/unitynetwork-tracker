package kostiskag.unitynetwork.tracker.functions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import kostiskag.unitynetwork.tracker.App;

/**
 *
 * @author kostis
 */
public class ReadPreferencesFile {       

    public static void ParseFile(InputStream file) throws IOException {
        
        Properties cfg = new java.util.Properties();
        cfg.load(file);
       
        String NetworkName = cfg.getProperty("NetworkName");
        String AuthPort = cfg.getProperty("AuthPort");
        
        String DatabaseUrl = cfg.getProperty("DatabaseUrl");
        String DatabaseUser = cfg.getProperty("DatabaseUser");
        String DatabsePassword = cfg.getProperty("DatabsePassword");
        
        String enableGUI = cfg.getProperty("enableGUI");
        
        String RedNodeCapacity = cfg.getProperty("RedNodeCapacity");
        String BlueNodeCapacity = cfg.getProperty("BlueNodeCapacity");      
        
        String Log = cfg.getProperty("log");  
        String ping = cfg.getProperty("ping");  
        
        App.NetwName = NetworkName; 
        App.auth = Integer.parseInt(AuthPort);
        
        App.url = DatabaseUrl;
        App.user = DatabaseUser;
        App.password = DatabsePassword;
        
        App.gui = Boolean.parseBoolean(enableGUI);        
        App.log = Boolean.parseBoolean(Log);
        
        App.rncap = Integer.parseInt(RedNodeCapacity);
        App.bncap = Integer.parseInt(BlueNodeCapacity);
        
        App.pingTime = Integer.parseInt(ping);
                
        
        System.out.println("");
        System.out.println("NetworkName is "+App.NetwName);        
        System.out.println("AuthPort is "+App.auth);                
        System.out.println("gui is "+App.gui);
        System.out.println("RedNodeLimit is "+App.rncap);
        System.out.println("BlueNodeLimit is "+App.bncap);        
        System.out.println("logging is "+App.log);        
        System.out.println("ping time is "+App.pingTime+" sec");        
        System.out.println("");                
    }

	public static void GenerateFile(File file) throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter writer = new PrintWriter(file, "UTF-8");
	    writer.print(""
	    		+ "#############################################\n"
	    		+ "#         Unity Tracker Config File         #\n"
	    		+ "#############################################\n"
	    		+ "\n"
	    		+ "# please do not comment any variable nor remove any. this will result in error\n"
	    		+ "# instead only change the value to an appropriate input as described\n"
	    		+ "# WARNING DO NOT MAKE SPACES AFTER A NUMBER\n"
	    		+ "\n"
	    		+ "# first of all what shall be the name of the netwrok?\n"
	    		+ "# and the authport! default 8000\n"
	    		+ "\n"
	    		+ "NetworkName = UnityNetwork\n"
	    		+ "AuthPort = 8000\n"
	    		+ "\n"
	    		+ "#\n"
	    		+ "# database settings\n"
	    		+ "#\n"
	    		+ "# the url should be in this type of form for mysql\n"
	    		+ "# DatabaseUrl = jdbc:mysql://IPaddress:port/database\n"
	    		+ "# DatabaseUser = username\n"
	    		+ "# DatabsePassword = password\n"
	    		+ "#\n"
	    		+ "# the url should be in this type of form for sqlite\n"
	    		+ "# DatabaseUrl = jdbc:sqlite:local_database_file.db\n"
	    		+ "#\n"
	    		+ "\n"
	    		+ "DatabaseUrl = jdbc:sqlite:unity.db\n"
	    		+ "\n"
	    		+ "# enable GUI\n"
	    		+ "enableGUI = true\n"
	    		+ "\n"
	    		+ "# network capacity\n"
	    		+ "RedNodeCapacity = 1000\n"
	    		+ "BlueNodeCapacity = 20\n"
	    		+ "\n"
	    		+ "# logging... in tracker.log\n"
	    		+ "# true ~ false\n"
	    		+ "log = true\n"
	    		+ "\n"
	    		+ "# ping time in sec\n"
	    		+ "# ping is the function where the tracker searches for all active BNs ion order\n"
	    		+ "# to find if someone does not respond\n"
	    		+ "# because a BN is ought to be connectionless with tracker\n"
	    		+ "ping = 20\n"
	    		+ "");    	    
	    writer.close();		
	}   
}