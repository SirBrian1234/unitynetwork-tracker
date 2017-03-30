package kostiskag.unitynetwork.tracker.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import kostiskag.unitynetwork.tracker.App;

public class DBConnection {

    Connection con;
    boolean error = false;
    
    public DBConnection() {
        con = null;        

        String url = App.databaseUrl;
        String user = App.user;
        String password = App.password;

        try {
            con = DriverManager.getConnection(url, user, password);            
        }catch (SQLException ex) {
            Logger lgr = Logger.getLogger(DBConnection.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);            
            error = true;            
        } 
        if (error){
            App.ConsolePrint("Database error");
            try {                
                con.close();
                con = null;
            } catch (SQLException ex) {
                Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
        } 
    }

    public Connection getCon() {
        return con;
    }        
}