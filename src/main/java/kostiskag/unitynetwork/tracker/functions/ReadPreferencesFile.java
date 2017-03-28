package kostiskag.unitynetwork.tracker.functions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import kostiskag.unitynetwork.tracker.App;

/**
 *
 * @author kostis
 */
public class ReadPreferencesFile {       

    public static void ParseFile(InputStream file) {
        
        Properties cfg = new java.util.Properties();
        try {        
            cfg.load(file);
        } catch (IOException ex) {
            Logger.getLogger(ReadPreferencesFile.class.getName()).log(Level.SEVERE, null, ex);
        }
                
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
}