package kostiskag.unitynetwork.tracker.runData;

import java.sql.Time;
import java.util.Iterator;
import java.util.LinkedList;
import kostiskag.unitynetwork.tracker.App;

/**
 *
 * @author kostis
 */
public class BlueNodeTable {

    private static String pre = "^BNTABLE ";
    private LinkedList<BlueNodeEntry> list;

    public BlueNodeTable() {
        list = new LinkedList<BlueNodeEntry>();
        App.ConsolePrint(pre + "INITIALIZED ");
    }

    public synchronized BlueNodeEntry getBlueNodeEntryById(int id) {
        if (id >= 0 && id < list.size()) {
    	    return list.get(id);
        } else {
            App.ConsolePrint(pre + "NO ENTRY " + id + " IN TABLE");
            return null;
        }
    }

    //get getBlueNodeEntryById should be preferred if possible for speed
    public synchronized BlueNodeEntry getBlueNodeEntryByHn(String Hostname) {
    	Iterator<BlueNodeEntry> iterator = list.descendingIterator();
        while (iterator.hasNext()) {
        	BlueNodeEntry element = iterator.next();
            if (Hostname.equals(element.getHostname())) {
                return element;
            }
        }
        App.ConsolePrint(pre + "NO ENTRY FOR " + Hostname + " IN TABLE");
        return null;
    }
    
    public synchronized BlueNodeEntry getBlueNodeEntryByLowestLoad() {
    	BlueNodeEntry retrieved = null;
    	if (list.size() > 0) {
    		int min = list.getFirst().getLoad();
    		retrieved = list.getFirst();
    		
	    	Iterator<BlueNodeEntry> iterator = list.descendingIterator();
	        int i = 0;
	        while (iterator.hasNext()) {
	        	BlueNodeEntry element = iterator.next();
	            if (element.getLoad() <= min) {
	                min = element.getLoad();
	                retrieved = element;
	            }
	            i++;
	        }	      
        } 
        return retrieved;
    }

    //WARNING!!! Physical address is not expected to be unique
    //therefore the table may return all the instances with the same phaddress
    public synchronized LinkedList<BlueNodeEntry> getBlueNodeEntriesByPhAddr(String Phaddress) {
    	LinkedList<BlueNodeEntry> found = new LinkedList<>();
    	Iterator<BlueNodeEntry> iterator = list.descendingIterator();
        while (iterator.hasNext()) {
        	BlueNodeEntry element = iterator.next();
            if (Phaddress.equals(element.getPhaddress())) {
                found.add(element);
            }
        }
        App.ConsolePrint(pre + "NO ENTRY FOR " + Phaddress + " IN TABLE");
        return found;
    }

    public synchronized int getSize() {
        return list.size();
    }

    public synchronized int leaseBn(String Hostname, String Phaddress, int port, int load, Time regTimestamp) {        
        BlueNodeEntry bn = new BlueNodeEntry(Hostname, Phaddress, port, load, regTimestamp);
        list.add(bn);
        App.ConsolePrint(pre + " LEASED " + Hostname + " WITH " + Phaddress + ":" + port);
        notifyGUI();
        return list.size();        
    }

    public synchronized int leaseBn(String Hostname, String Phaddress, int port, int load) {        
        BlueNodeEntry bn = new BlueNodeEntry(Hostname, Phaddress, port, load);
        list.add(bn);
        App.ConsolePrint(pre + " LEASED " + Hostname + " WITH " + Phaddress + ":" + port);
        notifyGUI();
        return list.size();        
    }
    
    public synchronized boolean releaseBnByID(int id) {
    	if (id >= 0 && id < list.size()) {
    		BlueNodeEntry element = list.remove(id);              
            App.ConsolePrint(pre +element.getHostname()+" RELEASED ENTRY");
            App.ConsolePrint(pre +element.getHostname()+" REMOVING BN ITEMS");
            App.RNtable.releaseByBluenodeName(element.getHostname());
            notifyGUI();
            return true;
        } else {
            App.ConsolePrint(pre + "NO ENTRY " + id + " IN TABLE");
            return false;
        }
    }
    
    //releaseBnByID should be preferred for speed
    public synchronized boolean releaseBnByHn(String Hostname) {
        boolean released = false;
        Iterator<BlueNodeEntry> iterator = list.descendingIterator();
        int i = 0;
        while (iterator.hasNext()) {
        	BlueNodeEntry element = iterator.next();
            if (Hostname.equals(element.getHostname())) {                
                    list.remove(i);              
                    App.ConsolePrint(pre +Hostname+" RELEASED ENTRY");
                    App.ConsolePrint(pre +Hostname+" REMOVING BN ITEMS");
                    App.RNtable.releaseByBluenodeName(Hostname);
                    released = true;
                    notifyGUI();
                    break;
            }
            i++;
        }
        
        if (!released)
            App.ConsolePrint(pre + "NO ENTRY FOR " + Hostname + " IN TABLE");
        return released;
    }

    public synchronized Boolean checkOnlineByHn(String Hostname) {
    	Iterator<BlueNodeEntry> iterator = list.descendingIterator();
    	while (iterator.hasNext()) {
        	BlueNodeEntry element = iterator.next();
            if (Hostname.equals(element.getHostname())) {  
                return true;
            }
        }
        return false;
    }
    
    //you should also call flush RN malakies den tha uparhe rn table ka ikathe bn tha exei tous dikous tou rns
    public synchronized void flushBnTable() {
    	list.clear();
    }

    //this will build the obj for gui
    //gui can handle itself
    public synchronized String[][] buildGUIObject() {        
    	String obj[][] = new String[list.size()][];
        int i = 0;
        Iterator<BlueNodeEntry> iterator = list.descendingIterator();
    	while (iterator.hasNext()) {
    		BlueNodeEntry element = iterator.next();
            obj[i] = new String[]{element.getHostname(), element.getPhaddress(), ""+element.getPort(), ""+element.getLoad(), element.getTimestamp().toString()};
        }
    	return obj;
    }
    
    private void notifyGUI () {
    	if (App.gui) {
    		App.window.updateBlueNodeTable();
    	}
    }
}
