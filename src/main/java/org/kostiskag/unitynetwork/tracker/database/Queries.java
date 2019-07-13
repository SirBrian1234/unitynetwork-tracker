package org.kostiskag.unitynetwork.tracker.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.kostiskag.unitynetwork.tracker.AppLogger;

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
final class Queries implements AutoCloseable {

	private static Database DATABASE;
	private static int TIMEOUT_SECONDS = 4;
	private static final Lock orb = new ReentrantLock(true);

	public static void setDatabaseInstance(String url, String user, String password) throws SQLException {
		if (DATABASE == null) {
			DATABASE = new Database(url,user,password);
		}
	}

	private static Lock aquireLock() throws InterruptedException {
		orb.tryLock(TIMEOUT_SECONDS, TimeUnit.SECONDS);
		return orb;
	}

	private static void releaseLock() {
		orb.unlock();
	}

	public static Queries getInstance() throws InterruptedException, SQLException {
		try {
			Queries.aquireLock();
		} catch (InterruptedException e) {
			// we have to inform the caller that the lock has timed out and he will get
			// no object
			throw e;
		}
		try {
			return new Queries();
		} catch (SQLException e) {
			// The object is broken so the caller won't get anything
			// but before we leave we have to give the lock back!
			Queries.releaseLock();
			throw e;
		}
	}

	private Queries() throws SQLException {
		try {
			DATABASE.connect();
		} catch (SQLException e) {
			try {
				DATABASE.close();
			} catch (SQLException ex) {
				AppLogger.getLogger().consolePrint(ex.getMessage());
			}
			throw e;
		}
	}

	@Override
	public void close() throws SQLException {
		try {
			DATABASE.close();
		} catch (SQLException e) {
			AppLogger.getLogger().consolePrint(e.getMessage());
			throw e;
		} finally {
			Queries.releaseLock();
		}
	}

	//user queries
	//select all
	public ResultSet selectAllFromUsers() throws SQLException {		
		return DATABASE.getResultSet("SELECT * FROM users");
	}

	public ResultSet selectAllFromUsers(int id) throws SQLException {
		return DATABASE.getResultSet("SELECT * FROM users WHERE id = ?", id);
	}

	public ResultSet selectAllFromUsers(String username) throws SQLException {
		return DATABASE.getResultSet("SELECT * FROM users WHERE username = ?", username);
	}

	//select id
	//select username password
	//select id scope fullname
	//insert entry
	//update entry
	//delete entry

	public ResultSet selectIdFromUsers(int id) throws SQLException {
		return DATABASE.getResultSet("SELECT id FROM users WHERE id = ?", id);
	}

	public ResultSet selectIdFromUsers(String username) throws SQLException {
		return DATABASE.getResultSet("SELECT id FROM users WHERE username = ?", username);
	}

	public ResultSet selectUsernameFromUsers(int id) throws SQLException {
		return DATABASE.getResultSet("SELECT username FROM users WHERE id = ?", id);
	}

	public ResultSet selectIdPasswordFromUsers(String username) throws SQLException {
		return DATABASE.getResultSet("SELECT id, password FROM users WHERE username = ?", username);
	}

	public ResultSet selectIdUsernamePasswordFromUsers() throws SQLException {
		return DATABASE.getResultSet("SELECT id, username, password FROM users");
	}

	public ResultSet selectIdScopeFullnameFromUsers(String username) throws SQLException {
		return DATABASE.getResultSet("SELECT id, scope, fullname FROM users WHERE username = ?", username);
	}

	public void insertEntryUsers(String username, String password, int scope, String fullname) throws SQLException {
		DATABASE.executePreparedStatement("INSERT INTO users VALUES (NULL, ?, ?, ?, ?)", username, password, scope, fullname);
	}

	public void updateEntryUsers(String username, String password, int scope, String fullname) throws SQLException {
		DATABASE.executePreparedStatement("UPDATE users SET password = ?, scope = ?, fullname = ? WHERE username = ?", password, scope, fullname, username);
	}

	public void updateEntryUsers(String username, int scope, String fullname) throws SQLException {
		DATABASE.executePreparedStatement("UPDATE users SET scope = ?, fullname = ? WHERE username = ?", scope, fullname, username);
	}

	public void deleteEntryUsers(String username) throws SQLException {
		DATABASE.executePreparedStatement("DELETE FROM users where username = ?", username);
	}

	//hostname queries
	//select all
	public ResultSet selectAllFromHostnames() throws SQLException {
		return DATABASE.getResultSet("SELECT * FROM hostnames");
	}

	public ResultSet selectAllFromHostnames(int userid) throws SQLException {
		return DATABASE.getResultSet("SELECT * FROM hostnames WHERE userid = ?", userid);
	}

	public ResultSet selectAllFromHostnames(String hostname) throws SQLException {
		return DATABASE.getResultSet("SELECT * FROM hostnames WHERE hostname = ?", hostname);
	}

	public ResultSet selectAllFromHostnamesWhereAddress(int address) throws SQLException {
		return DATABASE.getResultSet("SELECT * FROM hostnames WHERE address = ?", address);
	}

	//select userid
	public ResultSet selectUseridFromHostnames() throws SQLException {
		return DATABASE.getResultSet("SELECT userid FROM hostnames");
	}

	public ResultSet selectUseridFromHostnames(String hostname) throws SQLException {
		return DATABASE.getResultSet("SELECT userid FROM hostnames WHERE hostname = ?", hostname);
	}

	//select hostname
	public ResultSet selectHostnameFromHostnames() throws SQLException {
		return DATABASE.getResultSet("SELECT hostname FROM hostnames");
	}

	public ResultSet selectHostnameFromHostnames(String hostname) throws SQLException {
		return DATABASE.getResultSet("SELECT hostname FROM hostnames WHERE hostname = ?", hostname);
	}

	public ResultSet selectHostnameFromHostnames(int userid) throws SQLException {
		return DATABASE.getResultSet("SELECT hostname FROM hostnames WHERE userid = ?", userid);
	}

	public ResultSet selectAddressFromHostnames(int userid) throws SQLException {
		return DATABASE.getResultSet("SELECT address FROM hostnames WHERE userid = ?", userid);
	}

	public ResultSet selectHostnameAddressFromHostnames(int userid) throws SQLException {
		return DATABASE.getResultSet("SELECT hostname, address FROM hostnames WHERE userid = ?", userid);
	}

	//select hostname publickey
	public ResultSet selectHostnamePublicKeyFromHostnames(String hostname) throws SQLException {
		return DATABASE.getResultSet("SELECT hostname, public FROM hostnames WHERE hostname = ?", hostname);
	}

	//select address
	public ResultSet selectAddressFromHostnames() throws SQLException {
		return DATABASE.getResultSet("SELECT address FROM hostnames");
	}

	public ResultSet selectAddressFromHostnames(String hostname) throws SQLException {
		return DATABASE.getResultSet("SELECT address FROM hostnames WHERE hostname = ?", hostname);
	}

	//insert entry
	public void insertEntryHostnames(int address, String hostname, int userid, String publicText) throws SQLException {
		DATABASE.executePreparedStatement("INSERT INTO hostnames VALUES (?, ?, ?, ?)", address, hostname, userid, publicText);
	}
	
	public void insertEntryHostnames(String hostname, int userid, String publicText) throws SQLException {
		DATABASE.executePreparedStatement("INSERT INTO hostnames VALUES (NULL, ?, ?, ?)", hostname, userid, publicText);
	}

	//update userid
	public void updateEntryHostnamesUserId(String hostname, int userid) throws SQLException {
		DATABASE.executePreparedStatement("UPDATE hostnames SET userid = ? WHERE hostname = ?", userid, hostname);
	}

	//update public
	public void updateEntryHostnamesPublic(String hostname, String publicText) throws SQLException {
		DATABASE.executePreparedStatement("UPDATE hostnames SET public = ? WHERE hostname = ?", publicText, hostname);
	}

	//delete entry
	public void deleteEntryHostname(int userid) throws SQLException {
		DATABASE.executePreparedStatement("DELETE FROM hostnames where userid = ?", userid);
	}

	public void deleteEntryHostname(String hostname) throws SQLException {
		DATABASE.executePreparedStatement("DELETE FROM hostnames where hostname = ?", hostname);
	}

	//burned
	//retrieve address
	public ResultSet selectAddressFromBurned() throws SQLException {				
		return DATABASE.getResultSet("SELECT * FROM burned");
	}
	
	//store address
	public void insertEntryBurned(int address) throws SQLException {	
		if (address > 0) {
			DATABASE.executePreparedStatement("INSERT INTO burned VALUES (?)", address);
		}
	}
	
	//delete address
	public void deleteEntryAddressFromBurned(int address) throws SQLException {
		DATABASE.executePreparedStatement("DELETE FROM burned where address = ?", address);
	}	
	
	//bluenode queries
	//select all
	public ResultSet selectAllFromBluenodes() throws SQLException {				
		return DATABASE.getResultSet("SELECT * FROM bluenodes");
	}

	public ResultSet selectAllFromBluenodes(int userid) throws SQLException {
		return DATABASE.getResultSet("SELECT * FROM bluenodes WHERE userid = ?", userid);
	}

	public ResultSet selectAllFromBluenodes(String name) throws SQLException {
		return DATABASE.getResultSet("SELECT * FROM bluenodes WHERE name = ?", name);
	}

	//select name
	public ResultSet selectNameFromBluenodes() throws SQLException {
		return DATABASE.getResultSet("SELECT name FROM bluenodes");
	}

	public ResultSet selectNameFromBluenodes(String name) throws SQLException {
		return DATABASE.getResultSet("SELECT name FROM bluenodes WHERE name = ?", name);
	}

	public ResultSet selectNameFromBluenodes(int userid) throws SQLException {
		return DATABASE.getResultSet("SELECT name FROM bluenodes WHERE userid = ?", userid);
	}

	//select userid
	public ResultSet selectUseridFromBluenodes() throws SQLException {
		return DATABASE.getResultSet("SELECT userid FROM bluenodes");
	}

	public ResultSet selectUseridFromBluenodes(String name) throws SQLException {
		return DATABASE.getResultSet("SELECT userid FROM bluenodes WHERE name = ?", name);
	}

	//select name, publicKey
	public ResultSet selectNamePublicKeyFromBluenodes(String name) throws SQLException {
		return DATABASE.getResultSet("SELECT name, public FROM bluenodes WHERE name = ?", name);
	}

	//insert entry
	public void insertEntryBluenodes(String name, int userid, String publicKey) throws SQLException {		
		DATABASE.executePreparedStatement("INSERT INTO bluenodes VALUES (?, ?, ?)", name, userid, publicKey);
	}

	//update userid
	public void updateEntryBluenodesUserId(String name, int userid) throws SQLException {
		DATABASE.executePreparedStatement("UPDATE bluenodes SET userid = ? WHERE name = ?", userid, name);
	}

	//update public
	public void updateEntryBluenodesPublic(String name, String publicKey) throws SQLException {
		DATABASE.executePreparedStatement("UPDATE bluenodes SET public = ? WHERE name = ?", publicKey, name);
	}

	//delete entry
	public void deleteEntryBluenodes(int userid) throws SQLException {
		DATABASE.executePreparedStatement("DELETE FROM bluenodes where userid = ?", userid);
	}

	public void deleteEntryBluenodes(String name) throws SQLException {
		DATABASE.executePreparedStatement("DELETE FROM bluenodes where name = ?", name);
	}

	public void validate() throws SQLException {
		String query = "CREATE TABLE IF NOT EXISTS users (\n"
			   + "	id INTEGER PRIMARY KEY AUTOINCREMENT, \n"
			   + "	username CHAR(128) UNIQUE, \n"
               + "	password CHAR(256), \n"
               + "	scope INT(4), \n"
               + "	fullname CHAR(256) \n"
               + ");";
       
	    DATABASE.executePreparedStatement(query);
	
	    query = "CREATE TABLE IF NOT EXISTS hostnames (\n"
	    		+ "	address INTEGER PRIMARY KEY AUTOINCREMENT, \n"
                + "	hostname CHAR(128) UNIQUE, \n"
                + " userid INTEGER, \n"
                + "	public TEXT \n"
                + ");";
        
	    DATABASE.executePreparedStatement(query);
	    
	    //when a hostname is deleted the address has to bee stored
	    //in order to be used again. this table keeps
	    //all the burned addresses in order to appoin them to new hostnames
	    query = "CREATE TABLE IF NOT EXISTS burned (\n"
	    		+ "	address INTEGER PRIMARY KEY \n"
                + ");";
        
	    DATABASE.executePreparedStatement(query);
    
	    query = "CREATE TABLE IF NOT EXISTS bluenodes (\n"
                + "	name CHAR(128) PRIMARY KEY, \n"
                + " userid INTEGER, \n"
                + "	public TEXT \n"
                + ");";
	 
	   DATABASE.executePreparedStatement(query);
	}
}
