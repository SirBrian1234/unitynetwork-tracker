package kostiskag.unitynetwork.tracker;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import kostiskag.unitynetwork.tracker.database.*;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }
    
    public void testInit() {
    	assertTrue(true);
    }

    /*
    public void test1DB() {
    	
    	File file = new File("/local_database_file.db");
    	if (file.exists()) {
    		file.delete();
    	}
    	assertTrue( true );
    	
    	try {
    		App.databaseUrl = "jdbc:sqlite:local_database_file.db";    		
			Database db = new Database();
			db.close();
		} catch (SQLException e) {			
			e.printStackTrace();
			assertTrue(false);
		}
    	assertTrue( true );
    }
    
    public void test2ValidateDB() {
    	App.databaseUrl = "jdbc:sqlite:local_database_file.db";    		
		try {
			Queries.validateDatabase();
		} catch (SQLException e) {			
			e.printStackTrace();
			assertTrue(false);
		}
    	assertTrue( true );
    }
    
    public void test3Query() {
    	App.databaseUrl = "jdbc:sqlite:local_database_file.db";
    	try {
    		Queries.validateDatabase();
			Queries q = new Queries();
			q.closeQueries();
		} catch (SQLException e) {			
			e.printStackTrace();
			assertTrue(false);
		}
    	assertTrue( true );
    }
    
    public void test4Query() {
    	App.databaseUrl = "jdbc:sqlite:local_database_file.db";
    	try {
    		Queries.validateDatabase();
			Queries q = new Queries();
			q.selectAllFromBluenodes();
			q.selectAllFromUsers();
			q.selectAllFromHostnames();
			q.closeQueries();
		} catch (SQLException e) {			
			e.printStackTrace();
			assertTrue(false);
		}
    	assertTrue( true );
    }
    
   */
    
    /*
    public void test5bluenodeQuery() {
    	App.databaseUrl = "jdbc:sqlite:unity.db";
    	try {
    		Queries.validateDatabase();
			Queries q = new Queries();
			q.insertEntryBluenodes("Pakis", 0);
			q.insertEntryBluenodes("Lakis", 15);
			q.insertEntryBluenodes("Makis", 2);
			ResultSet s = q.selectAllFromBluenodes();			
			while (s.next()) {              
			    System.out.println(s.getInt("id"));
			    System.out.println(s.getString("name"));
			    System.out.println(s.getInt("userid"));
			}
			q.closeQueries();
		} catch (SQLException e) {			
			e.printStackTrace();
			assertTrue(false);
		}
    	assertTrue( true );
    }
    */
    
    /*
    public void test6hostnameQuery() {
    	App.databaseUrl = "jdbc:sqlite:local_database_file.db";
    	try {
    		Queries.validateDatabase();
			Queries q = new Queries();
			q.insertEntryHostnames("Pakis", 0);
			q.insertEntryHostnames("Makis", 0);
			q.insertEntryHostnames("Lakis", 1);
			ResultSet s = q.selectAllFromHostnames();			
			while (s.next()) {              
			    System.out.println(s.getInt("id"));
			    System.out.println(s.getString("hostname"));
			    System.out.println(s.getInt("userid"));
			}
			q.closeQueries();
		} catch (SQLException e) {			
			e.printStackTrace();
			assertTrue(false);
		}
    	assertTrue( true );
    }
    */
    
    /*
    public void test7userQuery() {
    	App.databaseUrl = "jdbc:sqlite:unity.db";
    	try {
    		Queries.validateDatabase();
			Queries q = new Queries();
			q.insertEntryUsers("Pakis", "hahaha", 0, "P Pakis");
			q.insertEntryUsers("Lakis", "hohoho", 1, "sir Lakis");
			q.insertEntryUsers("Makis", "hihihihi", 1, "Dr Makis");
			ResultSet s = q.selectAllFromUsers();			
			while (s.next()) {              
			    System.out.println(s.getInt("id"));
			    System.out.println(s.getString("username"));
			    System.out.println(s.getString("password"));
			    System.out.println(s.getInt("scope"));
			    System.out.println(s.getString("fullname"));
			}
			q.closeQueries();
		} catch (SQLException e) {			
			e.printStackTrace();
			assertTrue(false);
		}
    	assertTrue( true );
    }
    */
    /*
    public void test8hostnameUseridQuery() {
    	App.databaseUrl = "jdbc:sqlite:unity.db";
    	try {
    		Queries.validateDatabase();
			Queries q = new Queries();
			q.insertEntryHostnames("Pakis", 0);
			q.insertEntryHostnames("Makis", 0);
			q.insertEntryHostnames("Lakis", 1);
			q.insertEntryHostnames("Lakis2", 2);
			q.insertEntryHostnames("Lakis3", 4);
			q.insertEntryHostnames("Bamias", 2);
			ResultSet s = q.selectIdHostnamesFromHostnamesWithUserid(2);			
			while (s.next()) {              
			    System.out.println(s.getInt("id"));
			    System.out.println(s.getString("hostname"));			    
			}
			q.closeQueries();
		} catch (SQLException e) {			
			e.printStackTrace();
			assertTrue(false);
		}
    	assertTrue( true );
    }
    */
}
