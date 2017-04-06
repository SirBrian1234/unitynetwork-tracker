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
	
	public ResultSet selectIdUsernamePasswordFromUsers() throws SQLException {		
		return db.getResultSet("SELECT id, username, password FROM users");		
	}
	
	public ResultSet selectAllFromUsersWhereId(int id) throws SQLException {		
		return db.getResultSetFromPreparedStatement1ArgInt("SELECT * FROM users WHERE id = ?", id);		
	}
	
	public boolean checkIfUserWithIdExists(int id) throws SQLException {		
		ResultSet r = db.getResultSetFromPreparedStatement1ArgInt("SELECT username FROM users WHERE id = ?", id);	
		if (r.next()){
			if (r.getString("username") != null)
				return true;
		}
		return false;
	}
	
	public ResultSet selectAllFromUsersWhereUsername(String username) throws SQLException {		
		return db.getResultSetFromPreparedStatement1ArgString("SELECT * FROM users WHERE username = ?", username);		
	}
	
	public void insertEntryUsers(String username, String password, int scope, String fullname) throws SQLException {		
		db.executePreparedStatement4ArgsStrStrIntStr("INSERT INTO users VALUES (NULL, ?, ?, ?, ?)", username, password, scope, fullname);
	}
	
	public void updateEntryUsersWithUsername(String username, String password, int scope, String fullname) throws SQLException {
		db.executePreparedStatement4ArgsStrIntStrStr("UPDATE users SET password = ?, scope = ?, fullname = ? WHERE username = ?", password, scope, fullname, username);
	}
	
	public void deleteEntryUsersWithUsername(String username) throws SQLException {
		db.executePreparedStatement1ArgString("DELETE FROM users where username = ?", username);
	}
	
	//hostname queries
	public ResultSet selectAllFromHostnames() throws SQLException {				
		return db.getResultSet("SELECT * FROM hostnames"); 
	}
	
	public ResultSet selectHostnameFromHostnames() throws SQLException {				
		return db.getResultSet("SELECT hostname FROM hostnames"); 
	}
	
	public ResultSet selectUseridFromHostnames() throws SQLException {				
		return db.getResultSet("SELECT userid FROM hostnames"); 
	}
	
	public ResultSet selectAllFromHostnamesWhereHostname(String hostname) throws SQLException {		
		return db.getResultSetFromPreparedStatement1ArgString("SELECT * FROM hostnames WHERE hostname = ?", hostname);		
	}
	
	public ResultSet selectAllFromHostnamesWhereUserid(int userid) throws SQLException {		
		return db.getResultSetFromPreparedStatement1ArgInt("SELECT * FROM hostnames WHERE userid = ?", userid);		
	}
	
	public ResultSet selectUseridFromHostnamesWithHostname(String hostname) throws SQLException {		
		return db.getResultSetFromPreparedStatement1ArgString("SELECT userid FROM hostnames WHERE hostname = ?", hostname);
	}
	
	public ResultSet selectHostnameFromHostnamesWithUserid(int userid) throws SQLException {		
		return db.getResultSetFromPreparedStatement1ArgInt("SELECT hostname FROM hostnames WHERE userid = ?", userid);
	}
	
	public void insertEntryHostnames(String hostname, int userid) throws SQLException {		
		db.executePreparedStatement2ArgsStringInt("INSERT INTO hostnames VALUES (?, ?)", hostname, userid);
	}
	
	public void updateEntryHostnamesWithHostname(String hostname, int userid) throws SQLException {		
		db.executePreparedStatement2ArgsIntString("UPDATE hostnames SET userid = ? WHERE hostname = ?", userid, hostname);
	}
	
	public void deleteEntryHostnamesWithHostname(String hostname) throws SQLException {
		db.executePreparedStatement1ArgString("DELETE FROM hostnames where hostname = ?", hostname);
	}
	
	//bluenode queries
	public ResultSet selectAllFromBluenodes() throws SQLException {				
		return db.getResultSet("SELECT * FROM bluenodes"); 
	}
	
	public ResultSet selectNameFromBluenodes() throws SQLException {				
		return db.getResultSet("SELECT name FROM bluenodes"); 
	}
	
	public ResultSet selectUseridFromBluenodes() throws SQLException {		
		return db.getResultSet("SELECT userid FROM bluenodes");		
	}
	
	public ResultSet selectAllFromBluenodesWhereName(String name) throws SQLException {		
		return db.getResultSetFromPreparedStatement1ArgString("SELECT * FROM bluenodes WHERE name = ?", name);		
	}
	
	public ResultSet selectAllFromBluenodesWhereUserid(int userid) throws SQLException {		
		return db.getResultSetFromPreparedStatement1ArgInt("SELECT * FROM bluenodes WHERE userid = ?", userid);		
	}
	
	public ResultSet selectUseridFromBluenodesWhereName(String name) throws SQLException {		
		return db.getResultSetFromPreparedStatement1ArgString("SELECT userid FROM bluenodes WHERE name = ?", name);		
	}
	
	public ResultSet selectNameFromBluenodesWhereUserid(int userid) throws SQLException {		
		return db.getResultSetFromPreparedStatement1ArgInt("SELECT name FROM bluenodes WHERE userid = ?", userid);		
	}
	
	public void insertEntryBluenodes(String name, int userid) throws SQLException {		
		db.executePreparedStatement2ArgsStringInt("INSERT INTO bluenodes VALUES (?, ?)", name, userid);
	}
	
	public void updateEntryBluenodesWithName(String name, int userid) throws SQLException {		
		db.executePreparedStatement2ArgsIntString("UPDATE bluenodes SET userid = ? WHERE name = ?", userid, name);
	}
	
	public void deleteEntryBluenodesWitName(String name) throws SQLException {
		db.executePreparedStatement1ArgString("DELETE FROM bluenodes where name = ?", name);
	}
	
	public void closeQueries() throws SQLException {
		db.close();
	}
	
	public static void validateDatabase() throws SQLException {
		 Database db = new Database();
		 
		String query = "CREATE TABLE IF NOT EXISTS users (\n"
			   + "	id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
			   + "	username CHAR(128) UNIQUE,\n"
               + "	password CHAR(256) DEFAULT NULL,\n"
               + "	scope INT(4) DEFAULT NULL,\n"
               + "	fullname CHAR(256) DEFAULT NULL\n"
               + ");";
       
	    db.executeStatement(query); 	    
	
	    query = "CREATE TABLE IF NOT EXISTS hostnames (\n"
                + "	hostname CHAR(128) PRIMARY KEY,\n"
                + " userid INT(10) \n"
                + ");";
        
	    db.executeStatement(query);
    
	   query = "CREATE TABLE IF NOT EXISTS bluenodes (\n"
                + "	name CHAR(128) PRIMARY KEY, \n"
                + " userid INT(10) \n"
                + ");";
	 
	   db.executeStatement(query);
	   db.close();
	}		
}