package org.kostiskag.unitynetwork.tracker.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This is the database low level. An external method 
 * should no call methods from here it should use Queries and Logic instead.
 *
 * This class should not be visible outside of package!
 * Queries is the wrapper class!!!
 *
 * @author Konstantinos Kagiampakis
 */
final class Database implements AutoCloseable {

	private final String url;
	private final String user;
	private final String password;

	private Connection con;

	public Database(String url, String user, String password) {
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

    //get result set
	public synchronized ResultSet getResultSet(String Query) throws SQLException {
	    Statement st = null;
	    st = con.createStatement();
	    return st.executeQuery(Query);        
	}
	
	public synchronized ResultSet getResultSet(String query, int arg) throws SQLException {
		PreparedStatement pst = con.prepareStatement(query);
	    pst.setInt(1, arg);
	    return pst.executeQuery();
	}

	public synchronized ResultSet getResultSet(String query, String arg) throws SQLException {
		PreparedStatement pst = con.prepareStatement(query);
	    pst.setString(1, arg);
	    return pst.executeQuery();
	}

	//execute prepared statement
	public synchronized void executePreparedStatement(String Query) throws SQLException {
		Statement st = null;
		st = con.createStatement();
		st.execute(Query);
	}

	public synchronized void executePreparedStatement(String query, int arg) throws SQLException{
		PreparedStatement pst = con.prepareStatement(query);
		pst.setInt(1, arg);
		pst.execute();
	}

	public synchronized void executePreparedStatement(String query, String arg) throws SQLException{
		PreparedStatement pst = con.prepareStatement(query);
	    pst.setString(1, arg);
	    pst.execute();
	}

	public synchronized void executePreparedStatement(String query, String arg1, int arg2) throws SQLException{
		PreparedStatement pst = con.prepareStatement(query);	           	              
	    pst.setString(1, arg1);
	    pst.setInt(2, arg2);
	    pst.execute();
	}
	
	public synchronized void executePreparedStatement(String query, int arg1, String arg2) throws SQLException {
		PreparedStatement pst = con.prepareStatement(query);	           	              
	    pst.setInt(1, arg1);
	    pst.setString(2, arg2);
	    pst.execute();
	}

	public synchronized void executePreparedStatement(String query, String arg1, String arg2) throws SQLException {
		PreparedStatement pst = con.prepareStatement(query);	           	              
	    pst.setString(1, arg1);
	    pst.setString(2, arg2);
	    pst.execute();		
	}
	
	public synchronized void executePreparedStatement(String query, int arg1, String arg2, int arg3) throws SQLException {
		PreparedStatement pst = con.prepareStatement(query);	           	              
	    pst.setInt(1, arg1);
	    pst.setString(2, arg2);
	    pst.setInt(3, arg3);
	    pst.execute();
	}
	
	public synchronized void executePreparedStatement(String query, String arg1, int arg2, int arg3) throws SQLException {
		PreparedStatement pst = con.prepareStatement(query);	           	              
	    pst.setString(1, arg1);
	    pst.setInt(2, arg2);
	    pst.setInt(3, arg3);
	    pst.execute();
	}
	
	public synchronized void executePreparedStatement(String query, int arg1, String arg2,
													  String arg3) throws SQLException {
		PreparedStatement pst = con.prepareStatement(query);	           	              
	    pst.setInt(1, arg1);
	    pst.setString(2, arg2);
	    pst.setString(3, arg3);
	    pst.execute();		
	}
	
	public synchronized void executePreparedStatement(String query, String arg1, int arg2, String arg3) throws SQLException {
		PreparedStatement pst = con.prepareStatement(query);	           	              
	    pst.setString(1, arg1);
	    pst.setInt(2, arg2);
	    pst.setString(3, arg3);
	    pst.execute();	
	}

	public synchronized void executePreparedStatement(String query, int arg1, String arg2, int arg3, String arg4) throws SQLException {
		PreparedStatement pst = con.prepareStatement(query);
		pst.setInt(1, arg1);
		pst.setString(2, arg2);
		pst.setInt(3, arg3);
		pst.setString(4, arg4);
		pst.execute();
	}

	public synchronized void executePreparedStatement(String query, String arg1, String arg2, int arg3, String arg4) throws SQLException{
		PreparedStatement pst = con.prepareStatement(query);
	    pst.setString(1, arg1);
	    pst.setString(2, arg2);
	    pst.setInt(3, arg3);
	    pst.setString(4, arg4);
	    pst.execute();
	}

	public synchronized void executePreparedStatement(String query, String arg1, int arg2, String arg3,
													  String arg4) throws SQLException {
		PreparedStatement pst = con.prepareStatement(query);
	    pst.setString(1, arg1);
	    pst.setInt(2, arg2);
	    pst.setString(3, arg3);
	    pst.setString(4, arg4);
	    pst.execute();
	}
}