/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kostiskag.unitynetwork.tracker.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kostis
 */
public class Queries {
        
    public static ResultSet GetResults(String Query,DBConnection con) {
        if (con == null){
            return null;
        }
        java.sql.Statement st = null;
        ResultSet rs  = null;        
        try {            
            st = con.getCon().createStatement();
            return rs = st.executeQuery(Query);        
        } catch (SQLException ex) {
            System.out.println("Get Results Error");
            Logger lgr = Logger.getLogger(DBConnection.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
            return null;
        } 
    }    
}
