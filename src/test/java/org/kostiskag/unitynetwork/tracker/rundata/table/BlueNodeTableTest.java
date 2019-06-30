package org.kostiskag.unitynetwork.tracker.rundata.table;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.locks.Lock;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kostiskag.unitynetwork.tracker.AppLogger;
import org.kostiskag.unitynetwork.tracker.database.Queries;
import org.kostiskag.unitynetwork.tracker.rundata.utilities.CryptoUtilities;

public class BlueNodeTableTest {

    static PublicKey pub;

    @BeforeClass
    public static void beforeClass() throws SQLException, GeneralSecurityException {
        AppLogger.newInstance(null, null);
        pub = CryptoUtilities.generateRSAkeyPair().getPublic();
        File file = new File("bn_test.db");
        if (file.exists()) {
            file.delete();
        }

        try {
            Queries.setDatabaseInstance("jdbc:sqlite:local_database_file.db","","");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (Queries q = Queries.getInstance()) {
            q.validate();
        } catch (InterruptedException | SQLException e) {
            e.printStackTrace();
        }

        try (Queries q = Queries.getInstance()) {
            q.insertEntryUsers("Pakis", "1234", 2, "Dr. Pakis");
            ResultSet r = q.selectAllFromUsers();
            int id = 0;
            while (r.next()) {
                id = r.getInt("id");
            }
            q.insertEntryBluenodes("pakis1", id, "");
            q.insertEntryBluenodes("pakis2", id, "");
            q.insertEntryBluenodes("pakis3", id, "");
            q.insertEntryBluenodes("pakis4", id, "");

        } catch (InterruptedException | SQLException e) {
            e.printStackTrace();
        }

        BlueNodeTable.newInstance(2, null);
    }

    @AfterClass
    public static void afterClass() {
        File file = new File("bn_test.db");
        if (file.exists()) {
            file.delete();
        }
    }

    @Before
    public void before() {

    }

    @Test
    public void initTest() throws InterruptedException, UnknownHostException, IllegalAccessException {
        BlueNodeTable bns = BlueNodeTable.getInstance();
        try {
            Lock lock = bns.aquireLock();
            assertEquals(0, bns.getSize(lock));
            bns.lease(lock, "pakis", pub, "192.168.1.1", 1234);
            bns.lease(lock, "pakis2", pub, "192.168.1.2", 1234);
            assertEquals(bns.getSize(lock), 2);
            bns.release(lock,"pakis");
            bns.release(lock,"pakis2");
        } finally {
            bns.releaseLock();
        }
    }

    @Test
    public void maxCapacityTest() throws InterruptedException, UnknownHostException, IllegalAccessException {
        BlueNodeTable bns = BlueNodeTable.getInstance();
        Lock lock = null;
        try {
            lock = bns.aquireLock();
            assertEquals(bns.getSize(lock), 0);
            bns.lease(lock, "pakis", pub, "192.168.1.1", 1234);
            bns.lease(lock, "pakis2", pub, "192.168.1.2", 1234);
            bns.lease(lock, "pakis3", pub, "192.168.1.3", 1234);
            bns.lease(lock, "pakis4", pub, "192.168.1.4", 1234);
        } catch (IllegalAccessException e) {
            System.out.println(e.getMessage());
        } finally {
            assertEquals(2, bns.getSize(lock));
            bns.release(lock, "pakis");
            bns.release(lock, "pakis2");
            bns.releaseLock();
        }
    }

    @Test
    public void uniqueHostnameTest() throws InterruptedException, UnknownHostException, IllegalAccessException {
        BlueNodeTable bns = BlueNodeTable.getInstance();
        Lock lock = null;
        try {
            lock = bns.aquireLock();
            assertEquals(bns.getSize(lock), 0);
            bns.lease(lock, "pakis", pub, "192.168.1.1", 1234);
            bns.lease(lock, "pakis", pub, "192.168.1.2", 1234);
        } catch (IllegalAccessException e) {
            System.out.println(e.getMessage());
        } finally {
            assertEquals(bns.getSize(lock), 1);
            bns.release(lock,"pakis");
            bns.releaseLock();
        }
    }

    @Test
    public void uniqueAddressTest() throws InterruptedException, UnknownHostException, IllegalAccessException {
        BlueNodeTable bns = BlueNodeTable.getInstance();
        Lock lock = null;
        try {
            lock = bns.aquireLock();
            assertEquals(bns.getSize(lock), 0);

            bns.lease(lock, "pakis", pub, "192.168.1.1", 1234);
            bns.lease(lock, "pakis2", pub, "192.168.1.1", 1234);
        } catch (IllegalAccessException e) {
            System.out.println(e.getMessage());
        } finally {
            assertEquals(bns.getSize(lock), 1);
            bns.release(lock,"pakis");
            bns.releaseLock();
        }
    }

    //@Test
    public void leaseRedNodeTest() throws InterruptedException, UnknownHostException, IllegalAccessException {
        BlueNodeTable bns = BlueNodeTable.getInstance();
        try {
            Lock lock = bns.aquireLock();
            assertEquals(bns.getSize(lock), 0);
            bns.lease(lock, "pakis", pub, "192.168.1.1", 1234);
            bns.lease(lock, "pakis2", pub, "192.168.1.2", 1234);
            bns.lease(lock, "pakis3", pub, "192.168.1.2", 1235);
            bns.lease(lock, "pakis4", pub, "192.168.1.4", 1234);
            assertEquals(bns.getSize(lock), 4);

            bns.leaseRednode(lock, "pakis", "lakis", "10.0.0.1");
            bns.leaseRednode(lock, "pakis", "lakis2", "10.0.0.2");
            bns.leaseRednode(lock, "pakis", "lakis3", "10.0.0.3");
            bns.leaseRednode(lock, "pakis", "lakis4", "10.0.0.4");

            bns.leaseRednode(lock, "pakis2", "lakis5", "10.0.0.5");
            bns.leaseRednode(lock, "pakis2", "lakis6", "10.0.0.6");

            bns.leaseRednode(lock, "pakis3", "lakis7", "10.0.0.7");
            bns.leaseRednode(lock, "pakis3", "lakis8", "10.0.0.8");
            bns.leaseRednode(lock, "pakis3", "lakis9", "10.0.0.9");

            assertEquals(bns.getOptionalNodeEntry(lock, "pakis").get().getLoad(), 4);
            assertEquals(bns.getOptionalNodeEntry(lock, "pakis2").get().getLoad(), 2);
            assertEquals(bns.getOptionalNodeEntry(lock, "pakis3").get().getLoad(), 3);
            assertEquals(bns.getOptionalNodeEntry(lock, "pakis4").get().getLoad(), 0);
            assertEquals(bns.getBlueNodeEntryByLowestLoad(lock).getHostname(), "pakis4");
        } finally {
            bns.releaseLock();
        }
    }

}
