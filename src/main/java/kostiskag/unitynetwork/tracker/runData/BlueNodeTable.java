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
public class BlueNodeTable {

    private static String pre = "^BNTABLE ";
    private BlueNodeEntry[] table;
    private int size;
    private int count;
    private BlueNodeEntry temp;

    public BlueNodeTable(int size) {
        this.size = size;
        table = new BlueNodeEntry[size];
        for (int i = 0; i < size; i++) {
            table[i] = new BlueNodeEntry("none", "none", 0, 0, null);
        }
        App.ConsolePrint(pre + "INITIALIZED " + size);
    }

    public synchronized BlueNodeEntry getBlueNodeEntry(int id) {
        if (table.length > id) {
            return table[id];
        } else {
            App.ConsolePrint(pre + "NO ENTRY " + id + " IN TABLE");
            return null;
        }
    }

    public synchronized BlueNodeEntry getBlueNodeEntryByHn(String Hostname) {
        for (int i = 0; i < count; i++) {
            if (Hostname.equals(table[i].getHostname())) {
                return table[i];
            }
        }
        App.ConsolePrint(pre + "NO ENTRY FOR " + Hostname + " IN TABLE");
        return null;
    }

    //WARNING!!! Physical address may not be unique
    public synchronized BlueNodeEntry getBlueNodeEntryByAddr(String Phaddress) {
        for (int i = 0; i < count; i++) {
            if (Phaddress.equals(table[i].getPhaddress())) {
                return table[i];
            }
        }
        App.ConsolePrint(pre + "NO ENTRY FOR " + Phaddress + " IN TABLE");
        return null;
    }

    public synchronized int getBlueNodeIdByLowestLoad() {
        int min = table[0].getLoad();
        int id = 0;

        for (int i = 0; i < count; i++) {
            if (table[i].getLoad() <= min) {
                min = table[i].getLoad();
                id = i;
            }
        }
        return id;
    }

    public int getSize() {
        return count;
    }

    public synchronized int lease(String Hostname, String Phaddress, int port, int load, Time regTimestamp) {
        if (count < size) {
            table[count].init(Hostname, Phaddress, port, load, regTimestamp);
            App.ConsolePrint(pre + count + " LEASED " + Hostname + " WITH " + Phaddress + ":" + port);
            count++;
            updateTable();
            return count;
        } else {
            App.ConsolePrint(pre + "NO MORE SPACE INSIDE ADDRESSTABLE");
            return -1;
        }
    }

    public synchronized void release(String Hostname) {
        boolean released = false;
        for (int i = 0; i < count; i++) {
            if (Hostname.equals(table[i].getHostname())) {                
                    table[i].init("none", "none", 0, 0, null);
                
                    temp = table[i];                    
                    table[i] = table[count-1];                    
                    table[count-1] = temp;
                    
                    count--;                    
                    App.ConsolePrint(pre +Hostname+" RELEASED ENTRY");
                    released = true;
                    break;
            }
        }
        
        if (!released)
            App.ConsolePrint(pre + "NO ENTRY FOR " + Hostname + " IN TABLE");
    }

    public synchronized void Renew(String BlueNodeHostname, String address, int port, int load, Time time) {
        release(BlueNodeHostname);
        lease(BlueNodeHostname, address, port, load, time);
        updateTable();
    }

    public synchronized void setLoad(String Hostname, int load) {
        for (int i = 0; i < count; i++) {
            if (Hostname.equals(table[i].getHostname())) {
                table[i].setLoad(load);
                return;
            }
        }
        App.ConsolePrint(pre + "NO ENTRY FOR " + Hostname + " IN TABLE");
    }

    public synchronized Boolean checkOnlineByHn(String Hostname) {
        for (int i = 0; i < count; i++) {
            if (Hostname.equals(table[i].getHostname())) {
                return true;
            }
        }
        return false;
    }

    //WARNING!!! Multiple BN may have the same physical address the first result found in table will be returned
    public synchronized Boolean checkOnlineByAddr(String Phaddress) {
        for (int i = 0; i < count; i++) {
            if (Phaddress.equals(table[i].getPhaddress())) {
                return true;
            }
        }
        return false;
    }

    public synchronized void delete(int[] delTable) {
        App.ConsolePrint(pre + "FORCE DELETING " + delTable.length + " LOCAL RED NODES");
        for (int i = delTable.length; i > 0; i--) {
            String Hostname = getBlueNodeEntry(delTable[i - 1]).getHostname();
            App.ConsolePrint(pre + "DELETING " + Hostname);
            release(Hostname);
            updateTable();
        }
    }

    public synchronized void flushTable() {
        for (int i = 0; i < size; i++) {
            table[i] = null;
        }
        table = new BlueNodeEntry[size];
        for (int i = 0; i < size; i++) {
            table[i] = new BlueNodeEntry("none", "none", 0, 0, null);
        }
        count = 0;
        App.ConsolePrint(pre + "INITIALIZED " + size);
    }

    public synchronized void updateTable() {
        //MainWindow.hostable.
        if (App.gui) {
            int rows = MainWindow.bluenodes.getRowCount();
            for (int i = 0; i < rows; i++) {
                MainWindow.bluenodes.removeRow(0);
            }
            try {
                Thread.sleep(800); //waiting after removal just for the user feeling so tha he can see the table empty for a couple of time
            } catch (InterruptedException ex) {
                Logger.getLogger(BlueNodeTable.class.getName()).log(Level.SEVERE, null, ex);
            }
            for (int i = 0; i < count; i++) {
                MainWindow.bluenodes.addRow(new Object[]{table[i].getHostname(), table[i].getPhaddress(), table[i].getPort(), table[i].getLoad(), table[i].getRegTimestamp().toString()});
            }
        }
    }
}
