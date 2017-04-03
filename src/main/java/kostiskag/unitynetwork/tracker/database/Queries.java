package kostiskag.unitynetwork.tracker.database;

import java.sql.ResultSet;
import java.sql.SQLException;

/*
 * WARNING MYSQL ON LINUX IS CASE SENSITIVE
 */
public class Queries {
	
	Database db;
	
	public Queries() throws SQLException {
		db = new Database();
	}
	
	//user queries
	public ResultSet selectAllFromUsers() throws SQLException {		
		return db.getResultSet("SELECT * FROM users");		
	}
	
	public ResultSet selectAllFromUsersWhereId(int id) throws SQLException {		
		return db.getResultSetFromPreparedStatement1ArgInt("SELECT * FROM users WHERE id = ?", id);		
	}
	
	public ResultSet selectIdUsernamePasswordFromUsers() throws SQLException {		
		return db.getResultSet("SELECT id, username, password FROM users");		
	}
	
	public void insertEntryUsers(String name, String password, int scope, String fullname) throws SQLException {		
		db.executePreparedStatement4ArgsStrStrIntStr("INSERT INTO users VALUES (NULL, ?, ?, ?, ?)", name, password, scope, fullname);
	}
	
	//hostname queries
	public ResultSet selectAllFromHostnames() throws SQLException {				
		return db.getResultSet("SELECT * FROM hostnames"); 
	}
	
	public ResultSet selectAllFromHostnamesWhereId(int id) throws SQLException {		
		return db.getResultSetFromPreparedStatement1ArgInt("SELECT * FROM hostnames WHERE id = ?", id);		
	}
	
	public void insertEntryBluenodes(String name, int userid) throws SQLException {		
		db.executePreparedStatement2ArgsStringInt("INSERT INTO bluenodes VALUES (NULL, ?, ?)", name, userid);
	}
	
	public ResultSet selectIdHostnamesFromHostnamesWithUserid(int userid) throws SQLException {		
		return db.getResultSetFromPreparedStatement1ArgInt("SELECT id, hostname FROM hostnames WHERE userid= ?", userid);
	}
	
	public void insertEntryHostnames(String hostname, int userid) throws SQLException {		
		db.executePreparedStatement2ArgsStringInt("INSERT INTO hostnames VALUES (NULL, ?, ?)", hostname, userid);
	}
	
	//bluenode queries
	public ResultSet selectAllFromBluenodes() throws SQLException {				
		return db.getResultSet("SELECT * FROM bluenodes"); 
	}
	
	public ResultSet selectAllFromBluenodesWhereId(int id) throws SQLException {		
		return db.getResultSetFromPreparedStatement1ArgInt("SELECT * FROM bluenodes WHERE id = ?", id);		
	}
	
	public ResultSet selectNameBluenodes() throws SQLException {				
		return db.getResultSet("SELECT name FROM bluenodes"); 
	}
	
	public ResultSet selectIdNameFromBluenodes() throws SQLException {		
		return db.getResultSet("SELECT id, name FROM bluenodes");		
	}
	
	public void closeQueries() throws SQLException {
		db.close();
	}
	
	public static void validateDatabase() throws SQLException {
		 Database db = new Database();
		 
		 String query = "CREATE TABLE IF NOT EXISTS bluenodes (\n"
	                + "	id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
	                + "	name CHAR(128) UNIQUE, \n"
	                + " userid INT(10) \n"
	                + ");";
		 
	     db.executeStatement(query);
	    
	     query = "CREATE TABLE IF NOT EXISTS hostnames (\n"
	                + "	id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
	                + "	hostname CHAR(128) UNIQUE,\n"
	                + " userid INT(11) NOT NULL\n"
	                + ");";
	        
	    db.executeStatement(query);
	        
       query = "CREATE TABLE IF NOT EXISTS users (\n"
               + "	id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
               + "	username CHAR(128) UNIQUE,\n"
               + "	password CHAR(256) DEFAULT NULL,\n"
               + "	scope INT(4) DEFAULT NULL,\n"
               + "	fullname CHAR(256) DEFAULT NULL\n"
               + ");";
       
	    db.executeStatement(query); 	    
	    db.close();
	}	
}