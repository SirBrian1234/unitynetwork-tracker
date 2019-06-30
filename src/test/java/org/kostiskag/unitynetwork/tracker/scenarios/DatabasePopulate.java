package org.kostiskag.unitynetwork.tracker.scenarios;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.*;
import org.kostiskag.unitynetwork.tracker.database.Queries;

//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DatabasePopulate {

	@BeforeClass
	public static void initDB() {
		File file = new File("local_database_file.db");
		if (file.exists()) {
			file.delete();
		}

		try {
			Queries.setDatabaseInstance("jdbc:sqlite:local_database_file.db","","");
		} catch (SQLException e) {
			e.printStackTrace();
			assertTrue(false);
		}

		try (Queries q = Queries.getInstance()) {
			q.validate();
		} catch (InterruptedException | SQLException e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@AfterClass
	public static void clean() throws SQLException {
		File file = new File("local_database_file.db");
		if (file.exists()) {
			file.delete();
		}
	}
	
    @Test
    public void test1InsertBluenodes() {
		try (Queries q = Queries.getInstance()) {
			q.insertEntryUsers("pakis", "1234", 0, "Dr. Pakis");
			ResultSet s = q.selectAllFromUsers();
			int id = 0;
			while (s.next()) {
				id = s.getInt("id");
			}
			for (int i=0; i<100; i++) {
				q.insertEntryBluenodes("Pakis"+i, id,"");
			}
		} catch (InterruptedException | SQLException e) {
			e.printStackTrace();
			assertTrue(false);
		}
		assertTrue( true );
    }
    
    @Test
    public void test2InsertHostnames() {
		try (Queries q = Queries.getInstance()) {
			ResultSet s = q.selectAllFromUsersWhereUsername("pakis");
			int id = 0;
			while (s.next()) {
				id = s.getInt("id");
			}
			for (int i=0; i<100; i++) {
				q.insertEntryHostnamesNoAddr("pakis-laptop"+i, id,"");
			}
		} catch (InterruptedException | SQLException e) {
			e.printStackTrace();
			assertTrue(false);
		}
		assertTrue( true );
    }


}
