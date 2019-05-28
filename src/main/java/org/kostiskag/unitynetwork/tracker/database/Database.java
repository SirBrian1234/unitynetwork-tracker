package org.kostiskag.unitynetwork.tracker.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.sun.xml.internal.ws.addressing.model.ActionNotSupportedException;
import org.kostiskag.unitynetwork.tracker.App;

/**
 * This is the database low level. An external method 
 * should no call methods from here it should use Queries and Logic instead.
 * 
 * @author Konstantinos Kagiampakis
 */
public class Database {

	private static Database DATABASE;

	private final String url;
	private final String user;
	private final String password;

	private Connection con;

    public static Database newInstance(String url, String user, String password) throws SQLException {
    	if (DATABASE == null) {
    		DATABASE = new Database(url,user,password);
		}
    	return DATABASE;
	}

	public static Database getInstance() {
    	if (DATABASE == null) {
    		throw new ActionNotSupportedException("Database was called before its initialization");
		}
    	return DATABASE;
	}

    private Database(String url, String user, String password) throws SQLException {
    	this.url = url;
    	this.user = user;
    	this.password = password;
	}

	public synchronized void connect() throws SQLException {
		con = DriverManager.getConnection(url, user, password);
	}

    public synchronized void close() throws SQLException {
    	con.close();
    }
   
	public synchronized ResultSet getResultSet(String Query) throws SQLException {
	    Statement st = null;
	    st = con.createStatement();
	    return st.executeQuery(Query);        
	}
	
	public synchronized void executeStatement(String Query) throws SQLException {
	    Statement st = null;	           	              
	    st = con.createStatement();
	    st.execute(Query);	    
	}
	
	public synchronized ResultSet getResultSetFromPreparedStatement1ArgInt(String query, int arg) throws SQLException {
		PreparedStatement pst = con.prepareStatement(query);	           	              
	    pst.setInt(1, arg);
	    return pst.executeQuery();	    
	}
	
	public synchronized ResultSet getResultSetFromPreparedStatement1ArgString(String query, String arg) throws SQLException {
		PreparedStatement pst = con.prepareStatement(query);	           	              
	    pst.setString(1, arg);
	    return pst.executeQuery();	   
	}
	
	public synchronized void executePreparedStatement1ArgString(String query, String arg1) throws SQLException{
		PreparedStatement pst = con.prepareStatement(query);	           	              
	    pst.setString(1, arg1);
	    pst.execute();
	}
	
	public synchronized void executePreparedStatement1ArgInt(String query, int arg1) throws SQLException{
		PreparedStatement pst = con.prepareStatement(query);	           	              
	    pst.setInt(1, arg1);
	    pst.execute();
	}
	
	public synchronized void executePreparedStatement2ArgsStringInt(String query, String arg1, int arg2) throws SQLException{
		PreparedStatement pst = con.prepareStatement(query);	           	              
	    pst.setString(1, arg1);
	    pst.setInt(2, arg2);
	    pst.execute();
	}
	
	public synchronized void executePreparedStatement2ArgsIntString(String query, int arg1, String arg2) throws SQLException {
		PreparedStatement pst = con.prepareStatement(query);	           	              
	    pst.setInt(1, arg1);
	    pst.setString(2, arg2);
	    pst.execute();
	}
	
	public void executePreparedStatement2ArgsStringString(String query, String arg1, String arg2) throws SQLException {
		PreparedStatement pst = con.prepareStatement(query);	           	              
	    pst.setString(1, arg1);
	    pst.setString(2, arg2);
	    pst.execute();		
	}
	
	public synchronized void executePreparedStatement3ArgsIntStringInt(String query, int arg1, String arg2, int arg3) throws SQLException {
		PreparedStatement pst = con.prepareStatement(query);	           	              
	    pst.setInt(1, arg1);
	    pst.setString(2, arg2);
	    pst.setInt(3, arg3);
	    pst.execute();
	}
	
	public synchronized void executePreparedStatement3ArgsStringIntInt(String query, String arg1, int arg2, int arg3) throws SQLException {
		PreparedStatement pst = con.prepareStatement(query);	           	              
	    pst.setString(1, arg1);
	    pst.setInt(2, arg2);
	    pst.setInt(3, arg3);
	    pst.execute();
	}
	
	public synchronized void executePreparedStatement3ArgsIntStringString(String query, int arg1, String arg2,
			String arg3) throws SQLException {
		PreparedStatement pst = con.prepareStatement(query);	           	              
	    pst.setInt(1, arg1);
	    pst.setString(2, arg2);
	    pst.setString(3, arg3);
	    pst.execute();		
	}
	
	public void executePreparedStatement3ArgsStringIntString(String query, String arg1, int arg2, String arg3) throws SQLException {
		PreparedStatement pst = con.prepareStatement(query);	           	              
	    pst.setString(1, arg1);
	    pst.setInt(2, arg2);
	    pst.setString(3, arg3);
	    pst.execute();	
	}

	public synchronized void executePreparedStatement4ArgsStrStrIntStr(String query, String arg1, String arg2, int arg3, String arg4) throws SQLException{
		PreparedStatement pst = con.prepareStatement(query);	           	              
	    pst.setString(1, arg1);
	    pst.setString(2, arg2);
	    pst.setInt(3, arg3);
	    pst.setString(4, arg4);
	    pst.execute();
	}

	public synchronized void executePreparedStatement4ArgsStrIntStrStr(String query, String arg1, int arg2, String arg3,
			String arg4) throws SQLException {
		PreparedStatement pst = con.prepareStatement(query);	           	              
	    pst.setString(1, arg1);
	    pst.setInt(2, arg2);
	    pst.setString(3, arg3);
	    pst.setString(4, arg4);
	    pst.execute();		
	}

	public void executePreparedStatement4ArgsIntStringIntString(String query, int arg1, String arg2, int arg3, String arg4) throws SQLException {
		PreparedStatement pst = con.prepareStatement(query);	           	              
	    pst.setInt(1, arg1);
	    pst.setString(2, arg2);
	    pst.setInt(3, arg3);
	    pst.setString(4, arg4);
	    pst.execute();	
	}		
}