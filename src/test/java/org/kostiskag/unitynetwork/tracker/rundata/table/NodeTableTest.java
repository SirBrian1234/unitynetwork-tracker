package org.kostiskag.unitynetwork.tracker.rundata.table;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kostiskag.unitynetwork.tracker.rundata.entry.BlueNodeEntry;
import org.kostiskag.unitynetwork.tracker.rundata.entry.NodeEntry;
import org.kostiskag.unitynetwork.tracker.rundata.entry.RedNodeEntry;

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
