package kostiskag.unitynetwork.tracker.runData;

import java.sql.Time;
import java.util.logging.Level;
import java.util.logging.Logger;
import kostiskag.unitynetwork.tracker.App;
import kostiskag.unitynetwork.tracker.GUI.MainWindow;

/**
 *
 * @author kostis
 */
public class RedNodeTable {

    private static String pre = "^RNTABLE ";
    private RedNodeEntry[] table;
    private int size;
    private int count;
    private RedNodeEntry temp;

    public RedNodeTable(int size) {
        this.size = size;
        table = new RedNodeEntry[size];
        for (int i = 0; i < size; i++) {
            table[i] = new RedNodeEntry("none", "none", "none", null);
        }
        App.ConsolePrint(pre + "INITIALIZED " + size);
    }

    public RedNodeEntry getRedNodeEntry(int id) {
        if (table.length > id) {
            return table[id];
        } else {
            App.ConsolePrint(pre + "NO ENTRY " + id + " IN TABLE");
            return null;
        }
    }

    public RedNodeEntry getRedNodeEntryByHn(String Hostname) {
        for (int i = 0; i < size; i++) {
            if (Hostname.equals(table[i].getHostname())) {
                return table[i];
            }
        }
        App.ConsolePrint(pre + "NO ENTRY FOR " + Hostname + " IN TABLE");
        return null;
    }
    
    public RedNodeEntry getRedNodeEntryByAddr(String vaddress) {
        for (int i = 0; i < size; i++) {
            if (vaddress.equals(table[i].getVaddress())) {
                return table[i];
            }
        }
        App.ConsolePrint(pre + "NO ENTRY FOR " + vaddress + " IN TABLE");
        return null;
    }

    public int getSize() {
        return count;
    }

    //WARNING!!! There may be more than one entry with the same BlueNode this method will return
    //only the first result in table
    public RedNodeEntry getRedNodeEntryByBN(String BNHostname) {
        for (int i = 0; i < size; i++) {
            if (BNHostname.equals(table[i].getBNhostname())) {
                return table[i];
            }
        }
        App.ConsolePrint(pre + "NO ASSOCIATIONS FOR " + BNHostname + " IN TABLE");
        return null;
    }

    public int getNumOfAssociations(String BNhostname) {
        int j = 0;
        for (int i = 0; i < size; i++) {
            if (BNhostname.equals(table[i].getBNhostname())) {
                j++;
            }
        }
        return j;
    }

    public int lease(String hostname, String Vaddress, String BNhostname, Time regTimestamp) {
        if (count < size) {
            table[count].init(hostname, Vaddress, BNhostname, regTimestamp);
            App.ConsolePrint(pre + count + " LEASED " + hostname + " ON " + BNhostname);
            count++;
            updateTable();
            return count;
        } else {
            App.ConsolePrint(pre + "NO MORE SPACE INSIDE RNTABLE");
            return -1;
        }
    }

    public void release(String Hostname) {
        for (int i = 0; i < count; i++) {
            if (Hostname.equals(table[i].getHostname())) //release                                                
            {
                if (count != 0) {

                    temp = table[count - 1];
                    table[count - 1] = table[i];
                    table[i] = temp;
                    table[count - 1].init("none", "none", "none", null);
                    count--;
                    updateTable();
                    App.ConsolePrint(pre + "RELEASED ENTRY");
                    return;
                }
            }
        }
        App.ConsolePrint(pre + "NO ENTRY FOR " + Hostname + " IN TABLE");
    }

    public void releaseByBN(String BNhostname) {
        for (int i = 0; i < count; i++) {
            if (BNhostname.equals(table[i].getBNhostname())) {
                if (count != 0) {
                    temp = table[count - 1];
                    table[count - 1] = table[i];
                    table[i] = temp;
                    table[count - 1].init("none", "none", "none", null);
                    count--;
                }
            }
        }
    }

    public Boolean checkOnlineByHn(String Hostname) {
        for (int i = 0; i < count; i++) {
            if (Hostname.equals(table[i].getHostname())) {
                return true;
            }
        }
        return false;
    }
    
     public Boolean checkOnlineByAddr(String vaddress) {
        for (int i = 0; i < count; i++) {
            if (vaddress.equals(table[i].getVaddress())) {
                return true;
            }
        }
        return false;
    }

    //this basically searches if a BN has online RNS
    public Boolean checkOnlineByBN(String BNHostname) {
        for (int i = 0; i < count; i++) {
            if (BNHostname.equals(table[i].getBNhostname())) {
                return true;
            }
        }
        return false;
    }

    public void delete(int[] delTable) {
        App.ConsolePrint(pre + "FORCE DELETING " + delTable.length + " LOCAL RED NODES");
        for (int i = delTable.length; i > 0; i--) {
            String Hostname = getRedNodeEntry(delTable[i - 1]).getHostname();
            App.ConsolePrint(pre + "DELETING RN " + Hostname);
            release(Hostname);
            updateTable();
        }
    }

    public void flushTable() {
        for (int i = 0; i < size; i++) {
            table[i] = null;
        }
        table = new RedNodeEntry[size];
        for (int i = 0; i < size; i++) {
            table[i] = new RedNodeEntry("none", "none", "none", null);
        }
        count = 0;
        App.ConsolePrint(pre + "INITIALIZED " + size);
    }
    
    public void updateTable() {
        if (App.gui) {
            int rows = MainWindow.rednodes.getRowCount();
            for (int i = 0; i < rows; i++) {
                MainWindow.rednodes.removeRow(0);
            }
            try {
                Thread.sleep(800); //waiting after removal just for the user feeling so tha he can see the table empty for a couple of time
            } catch (InterruptedException ex) {
                Logger.getLogger(RedNodeTable.class.getName()).log(Level.SEVERE, null, ex);
            }
            for (int i = 0; i < count; i++) {                
                MainWindow.rednodes.addRow(new Object[]{table[i].getHostname(), table[i].getVaddress(), table[i].getBNhostname(), table[i].getRegTimestamp().toString()});
            }
        }
    }
}
