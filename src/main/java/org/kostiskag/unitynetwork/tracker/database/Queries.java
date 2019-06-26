package org.kostiskag.unitynetwork.tracker.database;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This is the database middle level
 * all methods should call methods from here. The methods
 * here may call the database low level.
 * WARNING MYSQL ON LINUX IS CASE SENSITIVE
 * 
 * Each Queries object may run multiple queries and exit
 * as long as it does the database is reserved which ensures that
 * there can be only one active Queries object at a time.
 * 
 * @author Konstantinos Kagiampakis
 */
public class Queries {
	
	Database db;
	
	public Queries() throws SQLException {
		db = Database.getInstance();
		db.connect();
	}
	
	//user queries
	public ResultSet selectAllFromUsers() throws SQLException {		
		return db.getResultSet("SELECT * FROM users");		
	}
	
	public ResultSet selectIdUsernamePasswordFromUsers() throws SQLException {		
		return db.getResultSet("SELECT id, username, password FROM users");		
	}
	
	public ResultSet selectIdScopeFullnameFromUsersWhereUsername(String username) throws SQLException {
		return db.getResultSetFromPreparedStatement1ArgString("SELECT id, scope, fullname FROM users WHERE username = ?", username);
	}	
	
	public ResultSet selectAllFromUsersWhereId(int id) throws SQLException {		
		return db.getResultSetFromPreparedStatement1ArgInt("SELECT * FROM users WHERE id = ?", id);		
	}
	
	public ResultSet selectIdFromUsersWhereUsername(String username) throws SQLException {		
		return db.getResultSetFromPreparedStatement1ArgString("SELECT * FROM users WHERE username = ?", username);		
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
	
	public void updateEntryUsersWhitoutPasswordWithUsername(String username, int scope, String fullname) throws SQLException {
		db.executePreparedStatement3ArgsIntStringString("UPDATE users SET scope = ?, fullname = ? WHERE username = ?", scope, fullname, username);
	}	
	
	public void deleteEntryUsersWithUsername(String username) throws SQLException {
		db.executePreparedStatement1ArgString("DELETE FROM users where username = ?", username);
	}
	
	//hostname queries
	public ResultSet selectAllFromHostnames() throws SQLException {				
		return db.getResultSet("SELECT * FROM hostnames"); 
	}
	
	public ResultSet selectAddressFromHostnames() throws SQLException {				
		return db.getResultSet("SELECT address FROM hostnames"); 
	}
	
	public ResultSet selectHostnameFromHostnames() throws SQLException {				
		return db.getResultSet("SELECT hostname FROM hostnames"); 
	}
	
	public ResultSet selectUseridFromHostnames() throws SQLException {				
		return db.getResultSet("SELECT userid FROM hostnames"); 
	}
	
	public ResultSet selectAllFromHostnamesWhereAddress(int address) throws SQLException {		
		return db.getResultSetFromPreparedStatement1ArgInt("SELECT * FROM hostnames WHERE address = ?", address);		
	}
	
	public ResultSet selectAllFromHostnamesWhereHostname(String hostname) throws SQLException {		
		return db.getResultSetFromPreparedStatement1ArgString("SELECT * FROM hostnames WHERE hostname = ?", hostname);		
	}
	
	public ResultSet selectAllFromHostnamesWhereUserid(int userid) throws SQLException {		
		return db.getResultSetFromPreparedStatement1ArgInt("SELECT * FROM hostnames WHERE userid = ?", userid);		
	}
	
	public ResultSet selectAddressFromHostnamesWithHostname(String hostname) throws SQLException {		
		return db.getResultSetFromPreparedStatement1ArgString("SELECT address FROM hostnames WHERE hostname = ?", hostname);
	}
	
	public ResultSet selectUseridFromHostnamesWithHostname(String hostname) throws SQLException {		
		return db.getResultSetFromPreparedStatement1ArgString("SELECT userid FROM hostnames WHERE hostname = ?", hostname);
	}
	
	public ResultSet selectHostnameFromHostnamesWithUserid(int userid) throws SQLException {		
		return db.getResultSetFromPreparedStatement1ArgInt("SELECT hostname FROM hostnames WHERE userid = ?", userid);
	}
	
	public void insertEntryHostnamesWithAddr(int address, String hostname, int userid, String publicText) throws SQLException {		
		db.executePreparedStatement4ArgsIntStringIntString("INSERT INTO hostnames VALUES (?, ?, ?, ?)", address, hostname, userid, publicText);
	}
	
	public void insertEntryHostnamesNoAddr(String hostname, int userid, String publicText) throws SQLException {		
		db.executePreparedStatement3ArgsStringIntString("INSERT INTO hostnames VALUES (NULL, ?, ?, ?)", hostname, userid, publicText);
	}
	
	public void updateEntryHostnamesWithHostname(String hostname, int userid) throws SQLException {		
		db.executePreparedStatement2ArgsIntString("UPDATE hostnames SET userid = ? WHERE hostname = ?", userid, hostname);
	}
	
	public void updateEntryHostnamesPublicWithHostname(String hostname, String publicText) throws SQLException {		
		db.executePreparedStatement2ArgsStringString("UPDATE hostnames SET public = ? WHERE hostname = ?", publicText, hostname);
	}
	
	public void deleteEntryHostnamesWithHostname(String hostname) throws SQLException {
		db.executePreparedStatement1ArgString("DELETE FROM hostnames where hostname = ?", hostname);
	}
	
	public void deleteAllHostnamesWithUserid(int userid) throws SQLException {
		db.executePreparedStatement1ArgInt("DELETE FROM hostnames where userid = ?", userid);
	}
	
	//burned
	//retrieve address
	public ResultSet selectAddressFromBurned() throws SQLException {				
		return db.getResultSet("SELECT * FROM burned"); 
	}
	
	//store address
	public void insertEntryBurned(int address) throws SQLException {	
		if (address > 0) {
			db.executePreparedStatement1ArgInt("INSERT INTO burned VALUES (?)", address);
		}
	}
	
	//delete address
	public void deleteEntryAddressFromBurned(int address) throws SQLException {
		db.executePreparedStatement1ArgInt("DELETE FROM burned where address = ?", address);
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
	
	public void insertEntryBluenodes(String name, int userid, String publicKey) throws SQLException {		
		db.executePreparedStatement3ArgsStringIntString("INSERT INTO bluenodes VALUES (?, ?, ?)", name, userid, publicKey);
	}
	
	public void updateEntryBluenodesWithName(String name, int userid) throws SQLException {		
		db.executePreparedStatement2ArgsIntString("UPDATE bluenodes SET userid = ? WHERE name = ?", userid, name);
	}
	
	public void updateEntryBluenodesPublicWithName(String name, String publicKey) throws SQLException {		
		db.executePreparedStatement2ArgsStringString("UPDATE bluenodes SET public = ? WHERE name = ?", publicKey, name);
	}
	
	public void deleteEntryBluenodesWitName(String name) throws SQLException {
		db.executePreparedStatement1ArgString("DELETE FROM bluenodes where name = ?", name);
	}
	
	public void deleteAllBluenodesWithUserid(int userid) throws SQLException {
		db.executePreparedStatement1ArgInt("DELETE FROM bluenodes where userid = ?", userid);
	}
	
	public void closeQueries() throws SQLException {
		db.close();
	}

	@Deprecated
	public static void validateDatabase(Database db) throws SQLException {
		validate(db);
	}

	public static void validateDatabase() throws SQLException {
		Database db = Database.getInstance();
		db.connect();
		validate(db);
		db.close();
	}

	public static void validate(Database db) throws SQLException {
		String query = "CREATE TABLE IF NOT EXISTS users (\n"
			   + "	id INTEGER PRIMARY KEY AUTOINCREMENT, \n"
			   + "	username CHAR(128) UNIQUE, \n"
               + "	password CHAR(256), \n"
               + "	scope INT(4), \n"
               + "	fullname CHAR(256) \n"
               + ");";
       
	    db.executeStatement(query); 	    
	
	    query = "CREATE TABLE IF NOT EXISTS hostnames (\n"
	    		+ "	address INTEGER PRIMARY KEY AUTOINCREMENT, \n"
                + "	hostname CHAR(128) UNIQUE, \n"
                + " userid INTEGER, \n"
                + "	public TEXT \n"
                + ");";
        
	    db.executeStatement(query);
	    
	    //when a hostname is deleted the address has to bee stored
	    //in order to be used again. this table keeps
	    //all the burned addresses in order to appoin them to new hostnames
	    query = "CREATE TABLE IF NOT EXISTS burned (\n"
	    		+ "	address INTEGER PRIMARY KEY \n"
                + ");";
        
	    db.executeStatement(query);
    
	   query = "CREATE TABLE IF NOT EXISTS bluenodes (\n"
                + "	name CHAR(128) PRIMARY KEY, \n"
                + " userid INTEGER, \n"
                + "	public TEXT \n"
                + ");";
	 
	   db.executeStatement(query);
	}
}