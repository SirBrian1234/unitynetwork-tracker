package org.kostiskag.unitynetwork.tracker.database;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.*;
import org.junit.runners.MethodSorters;

import org.kostiskag.unitynetwork.tracker.App;


//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DatabaseTest {

	@BeforeClass
	public static void preSet() {
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
    public void test1QueryObj() {
		try (Queries q = Queries.getInstance()) {

		} catch (InterruptedException | SQLException e) {
			e.printStackTrace();
			assertTrue(false);
		}
    	assertTrue( true );
    }

	@Test
    public void test2SelectAll() {
		try (Queries q = Queries.getInstance()) {
			q.selectAllFromBluenodes();
			q.selectAllFromUsers();
			q.selectAllFromHostnames();

		} catch (InterruptedException | SQLException e) {
			e.printStackTrace();
			assertTrue(false);
		}
		assertTrue( true );
    }
    
	@Test
    public void test3UserQuery() {
		try (Queries q = Queries.getInstance()) {
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
		} catch (InterruptedException | SQLException e) {
			e.printStackTrace();
			assertTrue(false);
		}
		assertTrue( true );
    }
	
	@Test
    public void test4HostnameQuery() {
		try (Queries q = Queries.getInstance()) {
			q.insertEntryHostnamesNoAddr("Pakis", 0,"");
			q.insertEntryHostnamesNoAddr("Makis", 0,"");
			q.insertEntryHostnamesNoAddr("Lakis", 1,"");
			ResultSet s = q.selectAllFromHostnames();			
			while (s.next()) {              
				System.out.println(s.getString("address"));
			    System.out.println(s.getString("hostname"));
			    System.out.println(s.getInt("userid"));
			}
		} catch (InterruptedException | SQLException e) {
			e.printStackTrace();
			assertTrue(false);
		}
		assertTrue( true );
    }
	
    @Test
    public void test5BluenodeQuery() {
		try (Queries q = Queries.getInstance()) {
			q.insertEntryBluenodes("3Pakis", 0,"");
			q.insertEntryBluenodes("Lakis", 15,"");
			q.insertEntryBluenodes("Makis", 2,"");
			ResultSet s = q.selectAllFromBluenodes();			
			while (s.next()) {              
			    System.out.println(s.getString("name"));
			    System.out.println(s.getInt("userid"));
			}
		} catch (InterruptedException | SQLException e) {
			e.printStackTrace();
			assertTrue(false);
		}
		assertTrue( true );
    }
    
    @Test
    public void test6HostnameUseridQuery() {
		try (Queries q = Queries.getInstance()) {
			q.insertEntryHostnamesNoAddr("Pakis55", 0,"");
			q.insertEntryHostnamesNoAddr("Makis44", 0,"");
			q.insertEntryHostnamesNoAddr("Lakis22", 1,"");
			q.insertEntryHostnamesNoAddr("Lakis2", 2,"");
			q.insertEntryHostnamesNoAddr("Lakis3", 4,"");
			q.insertEntryHostnamesNoAddr("Bamias", 2,"");
			ResultSet s = q.selectAllFromHostnamesWhereUserid(2);			
			while (s.next()) {              
			    System.out.println(s.getString("hostname"));			    
			}
		} catch (InterruptedException | SQLException e) {
			e.printStackTrace();
			assertTrue(false);
		}
		assertTrue( true );
    }    
    
    @Test
    public void test7UpdateHostnames() {
		try (Queries q = Queries.getInstance()) {
			q.insertEntryHostnamesNoAddr("Bamias55", 22, "");
			q.insertEntryHostnamesNoAddr("Pakis44", 23, "");
			ResultSet s = q.selectAllFromHostnames();
			while (s.next()) {
				System.out.println(s.getString("hostname"));
			}
			q.updateEntryHostnamesWithHostname("Bamias", 44);
			s = q.selectAllFromHostnames();
			while (s.next()) {
				System.out.println(s.getString("hostname"));
			}
		} catch (InterruptedException | SQLException e) {
			e.printStackTrace();
			assertTrue(false);
		}
    	assertTrue(true);
    }
    
    @Test
    public void test8UpdateUsers() {
		try (Queries q = Queries.getInstance()) {
			q.insertEntryUsers("bamias", "1234", 0, "mr bamias");
			q.insertEntryUsers("Baaamias", "1424344", 0, "dr bamias");
			ResultSet s = q.selectAllFromUsers();			
			while (s.next()) {   
				 System.out.println(s.getInt("id"));
				 System.out.println(s.getString("username"));	
				 System.out.println(s.getString("password"));
				 System.out.println(s.getInt("scope"));
				 System.out.println(s.getString("fullname"));			    
			}
			q.updateEntryUsersWithUsername("bamias", "1234", 1, "miss putty");
			s = q.selectAllFromUsers();
			while (s.next()) {              
				System.out.println(s.getInt("id"));
			    System.out.println(s.getString("username"));	
			    System.out.println(s.getString("password"));
			    System.out.println(s.getInt("scope"));
			    System.out.println(s.getString("fullname"));
			}
		} catch (InterruptedException | SQLException e) {
			e.printStackTrace();
			assertTrue(false);
		}
		assertTrue( true );
    }
    
    @Test
    public void test8burned() {
		try (Queries q = Queries.getInstance()) {
			q.insertEntryBurned(15);
			q.insertEntryBurned(22);
			q.insertEntryBurned(125);
			q.insertEntryBurned(34);
			q.insertEntryBurned(40);
			ResultSet r = q.selectAddressFromBurned();
			while(r.next()) {
				System.out.println(""+r.getInt("address"));
			}
			q.deleteEntryAddressFromBurned(34);
			q.deleteEntryAddressFromBurned(15);
			r = q.selectAddressFromBurned();
			while(r.next()) {
				System.out.println(""+r.getInt("address"));
			}

		} catch (InterruptedException | SQLException e) {
			e.printStackTrace();
			assertTrue(false);
		}
		assertTrue( true );
    }

}
