package kostiskag.unitynetwork.tracker.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.sqlite.core.DB;

import kostiskag.unitynetwork.tracker.App;

public class Database {

    Connection con;
    boolean error = false;
    
    public Database() throws SQLException {
        con = null;        

        String url = App.databaseUrl;
        String user = App.user;
        String password = App.password;

        con = DriverManager.getConnection(url, user, password);                    
    }
    
    public void close() throws SQLException {
    	con.close();
    }
   
	public ResultSet getResultSet(String Query) throws SQLException {
	    Statement st = null;
	    ResultSet rs  = null;        
	    st = con.createStatement();
	    return rs = st.executeQuery(Query);        
	}
	
	public void executeStatement(String Query) throws SQLException {
	    Statement st = null;	           	              
	    st = con.createStatement();
	    st.execute(Query);	    
	}
	
	public ResultSet getResultSetFromPreparedStatement1ArgInt(String query, int arg) throws SQLException {
		PreparedStatement pst = con.prepareStatement(query);	           	              
	    pst.setInt(1, arg);
	    return pst.executeQuery();	    
	}
	
	public ResultSet getResultSetFromPreparedStatement1ArgString(String query, String arg) throws SQLException {
		PreparedStatement pst = con.prepareStatement(query);	           	              
	    pst.setString(1, arg);
	    return pst.executeQuery();	   
	}
	
	public void executePreparedStatement1ArgString(String query, String arg1) throws SQLException{
		PreparedStatement pst = con.prepareStatement(query);	           	              
	    pst.setString(1, arg1);
	    pst.execute();
	}
	
	public void executePreparedStatement1ArgInt(String query, int arg1) throws SQLException{
		PreparedStatement pst = con.prepareStatement(query);	           	              
	    pst.setInt(1, arg1);
	    pst.execute();
	}
	
	public void executePreparedStatement2ArgsStringInt(String query, String arg1, int arg2) throws SQLException{
		PreparedStatement pst = con.prepareStatement(query);	           	              
	    pst.setString(1, arg1);
	    pst.setInt(2, arg2);
	    pst.execute();
	}
	
	public void executePreparedStatement2ArgsIntString(String query, int arg1, String arg2) throws SQLException {
		PreparedStatement pst = con.prepareStatement(query);	           	              
	    pst.setInt(1, arg1);
	    pst.setString(2, arg2);
	    pst.execute();
	}
	
	public void executePreparedStatement3ArgsIntStringInt(String query, String arg1, int arg2, int arg3) throws SQLException {
		PreparedStatement pst = con.prepareStatement(query);	           	              
	    pst.setString(1, arg1);
	    pst.setInt(2, arg2);
	    pst.setInt(3, arg3);
	    pst.execute();
	}
	
	public void executePreparedStatement3ArgsIntStringString(String query, int arg1, String arg2,
			String arg3) throws SQLException {
		PreparedStatement pst = con.prepareStatement(query);	           	              
	    pst.setInt(1, arg1);
	    pst.setString(2, arg2);
	    pst.setString(3, arg3);
	    pst.execute();		
	}

	public void executePreparedStatement4ArgsStrStrIntStr(String query, String arg1, String arg2, int arg3, String arg4) throws SQLException{
		PreparedStatement pst = con.prepareStatement(query);	           	              
	    pst.setString(1, arg1);
	    pst.setString(2, arg2);
	    pst.setInt(3, arg3);
	    pst.setString(4, arg4);
	    pst.execute();
	}

	public void executePreparedStatement4ArgsStrIntStrStr(String query, String arg1, int arg2, String arg3,
			String arg4) throws SQLException {
		PreparedStatement pst = con.prepareStatement(query);	           	              
	    pst.setString(1, arg1);
	    pst.setInt(2, arg2);
	    pst.setString(3, arg3);
	    pst.setString(4, arg4);
	    pst.execute();		
	}

}