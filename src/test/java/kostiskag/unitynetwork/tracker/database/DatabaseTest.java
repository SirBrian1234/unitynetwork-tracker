package kostiskag.unitynetwork.tracker.database;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.junit.FixMethodOrder;

import kostiskag.unitynetwork.tracker.App;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DatabaseTest {
	
	@BeforeClass
	public static void testDB() {
		System.out.println("before class");
		File file = new File("local_database_file.db");
    	if (file.exists()) {
    		file.delete();
    	}
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
	
	@Before
    public void testValidateDB() {
		System.out.println("Before");
		File file = new File("local_database_file.db");
    	if (file.exists()) {
    		file.delete();
    	}
    	App.databaseUrl = "jdbc:sqlite:local_database_file.db";    		
		try {
			Queries.validateDatabase();
		} catch (SQLException e) {			
			e.printStackTrace();
			assertTrue(false);
		}
    	assertTrue( true );
    }
    
	@AfterClass
	public static void testCloseDeleteDb() {
		System.out.println("After class");
		File file = new File("local_database_file.db");
    	if (file.exists()) {
    		file.delete();
    	}
    	assertTrue(true);
	}
	
	@Test
    public void test1QueryObj() {
    	try {
			Queries q = new Queries();
			q.closeQueries();
		} catch (SQLException e) {			
			e.printStackTrace();
			assertTrue(false);
		}
    	assertTrue( true );
    }
    
	@Test
    public void test2SelectAll() {
    	try {
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
    
	@Test
    public void test3UserQuery() {
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
	
	@Test
    public void test4HostnameQuery() {
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
	
    @Test
    public void test5BluenodeQuery() {
    	try {
    		Queries.validateDatabase();
			Queries q = new Queries();
			q.insertEntryBluenodes("3Pakis", 0);
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
    
    @Test
    public void test6HostnameUseridQuery() {
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
}
