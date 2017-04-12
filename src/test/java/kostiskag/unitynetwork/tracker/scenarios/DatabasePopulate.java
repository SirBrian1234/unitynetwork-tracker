package kostiskag.unitynetwork.tracker.scenarios;

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
import org.junit.Ignore;

import kostiskag.unitynetwork.tracker.App;
import kostiskag.unitynetwork.tracker.database.Database;
import kostiskag.unitynetwork.tracker.database.Queries;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DatabasePopulate {
	
	
	@BeforeClass
	public static void initDB() {
		System.out.println("before class");
		File file = new File("unity.db");
    	if (file.exists()) {
    		file.delete();
    	}
    	try {
    		App.databaseUrl = "jdbc:sqlite:unity.db";    		
    		Queries.validateDatabase();    		
		} catch (SQLException e) {			
			e.printStackTrace();
			assertTrue(false);
		}
    	assertTrue( true );
	}
	
    @Ignore
    public void test1InsertBluenodes() {
    	Queries q;
		try {
			q = new Queries();
			q.insertEntryUsers("pakis", "1234", 0, "Dr. Pakis");
			ResultSet s = q.selectAllFromUsers();			
			int id = 0;
			while (s.next()) {   
				 id = s.getInt("id");
			}
			for (int i=0; i<100; i++) {
				q.insertEntryBluenodes("Pakis"+i, id);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			assertTrue(false);
		}
    	assertTrue(true);
    }
    
    @Ignore
    public void test2InsertHostnames() {
    	Queries q;
		try {
			q = new Queries();
			ResultSet s = q.selectAllFromUsersWhereUsername("pakis");			
			int id = 0;
			while (s.next()) {   
				 id = s.getInt("id");
			}
			for (int i=0; i<100; i++) {
				q.insertEntryHostnamesNoAddr("pakis-laptop"+i, id);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			assertTrue(false);
		}
    	assertTrue(true);
    }
}
