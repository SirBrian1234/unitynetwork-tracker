package kostiskag.unitynetwork.tracker.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import kostiskag.unitynetwork.tracker.App;

/**
 * This is the database low level. An external method 
 * should no call methods from here it should use Queries and Logic instead.
 * 
 * @author Konstantinos Kagiampakis
 */
public class Database {

    Connection con;
    
    public Database() throws SQLException {
        String url = App.databaseUrl;
        String user = App.user;
        String password = App.password;
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
}