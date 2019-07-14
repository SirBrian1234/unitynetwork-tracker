package org.kostiskag.unitynetwork.tracker.database;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.sql.SQLException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.kostiskag.unitynetwork.common.entry.NodeType;
import org.kostiskag.unitynetwork.common.utilities.CryptoUtilities;

public class LogicTest {

    @BeforeClass
    public static void preSet() {
        File file = new File("local_database_file1.db");
        if (file.exists()) {
            file.delete();
        }

        try {
            Logic.setDatabaseInstanceWrapper("jdbc:sqlite:local_database_file1.db","","");
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

        List<Integer> shuffled = IntStream.range(0,20).boxed().collect(Collectors.toList());
        Collections.shuffle(shuffled);

        for (Integer i: shuffled) {
            System.out.println("building db entry "+i);
            try {
                UserLogic.addNewUser("pakis"+i, "12345", 1, "Dr. Pakis");
                BluenodeLogic.addNewBluenode("bluenode"+i+i, "pakis"+i);
                HostnameLogic.addNewHostname("rnpakis"+i,"pakis"+i);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @AfterClass
    public static void clean() throws SQLException {
        File file = new File("local_database_file1.db");
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    public void validateDatabaseTest() {
        assertTrue(Logic.validateDatabase());
    }

    @Test
    public void fetchPublicKeyTest() throws InterruptedException, GeneralSecurityException, SQLException, IOException {
        System.out.println();
        assertEquals(Optional.empty(), Logic.fetchPublicKey(NodeType.BLUENODE,"bluenode33"));
        //get the ticket...
        var ticket = BluenodeLogic.selectBluenodesUserPublicKey("bluenode33").get().getVal2().split(" ")[1];

        PublicKey pub = CryptoUtilities.generateRSAkeyPair().getPublic();
        Logic.offerPublicKey(NodeType.BLUENODE,"bluenode33",ticket, CryptoUtilities.objectToBase64StringRepresentation(pub));
        PublicKey fetched = Logic.fetchPublicKey(NodeType.BLUENODE,"bluenode33").get();
        assertEquals(pub,fetched);

        //malicious offer based on existing pub!
        ticket = CryptoUtilities.objectToBase64StringRepresentation(pub);
        var malicPub = CryptoUtilities.generateRSAkeyPair().getPublic();
        var malicStr = CryptoUtilities.objectToBase64StringRepresentation(malicPub);
        Logic.offerPublicKey(NodeType.BLUENODE,"bluenode33",ticket, malicStr);
        fetched = Logic.fetchPublicKey(NodeType.BLUENODE,"bluenode33").get();
        assertNotEquals(fetched,malicPub);
        assertEquals(fetched,pub);

        //rednode test
        assertEquals(Optional.empty(), Logic.fetchPublicKey(NodeType.REDNODE,"rnpakis18"));

        ticket = HostnameLogic.getHostnameEntry("rnpakis18").get().getVal3().split(" ")[1];
        pub = CryptoUtilities.generateRSAkeyPair().getPublic();
        Logic.offerPublicKey(NodeType.REDNODE,"rnpakis18",ticket, CryptoUtilities.objectToBase64StringRepresentation(pub));
        fetched = Logic.fetchPublicKey(NodeType.REDNODE,"rnpakis18").get();
        assertEquals(pub,fetched);

        //TODO: revoke!
    }

    @Test
    public void buildGUIObjectTest() {
        String[][][] guiobj = Logic.buildGUIObject();
        System.out.println(Arrays.deepToString(guiobj));

        assertNotEquals( null, guiobj[0]);
        assertNotEquals( null, guiobj[1]);
        assertNotEquals( null, guiobj[2]);

        assertEquals(20, guiobj[0].length);
        assertEquals(20, guiobj[1].length);
        assertEquals(20, guiobj[2].length);

        assertEquals(5, guiobj[0][0].length);
        assertEquals(3, guiobj[1][0].length);
        assertEquals(2, guiobj[2][0].length);

        for(int k=0; k < 3; k++) {
            for (int i=0; i < 20; i++) {
                for (int j = 0; j < 2; j++) {
                    assertNotNull(guiobj[k][i][j]);
                }
            }
        }
    }

}
