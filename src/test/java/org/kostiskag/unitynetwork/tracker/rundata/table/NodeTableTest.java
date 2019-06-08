package org.kostiskag.unitynetwork.tracker.rundata.table;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kostiskag.unitynetwork.tracker.AppLogger;
import org.kostiskag.unitynetwork.tracker.database.Database;
import org.kostiskag.unitynetwork.tracker.database.Queries;
import org.kostiskag.unitynetwork.tracker.functions.CryptoMethods;
import org.kostiskag.unitynetwork.tracker.rundata.entry.BlueNodeEntry;
import org.kostiskag.unitynetwork.tracker.rundata.entry.NodeEntry;
import org.kostiskag.unitynetwork.tracker.rundata.entry.RedNodeEntry;

import java.io.File;
import java.net.UnknownHostException;
import java.security.PublicKey;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.locks.Lock;

import static org.junit.Assert.assertEquals;

public class NodeTableTest {

    @BeforeClass
    public static void beforeClass() {

    }

    @AfterClass
    public static void afterClass() {

    }

    @Before
    public void before() {

    }

    @Test
    public void initTest() {
        NodeTable<BlueNodeEntry> nt = new NodeTable<>();
        NodeTable<RedNodeEntry> nt2 = new NodeTable<>();
        NodeTable<NodeEntry> nt3 = new NodeTable<>();
    }

}
