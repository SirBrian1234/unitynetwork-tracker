package kostiskag.unitynetwork.tracker.runData;

import java.sql.Time;
import java.util.Iterator;
import java.util.LinkedList;
import kostiskag.unitynetwork.tracker.App;
import kostiskag.unitynetwork.tracker.sonarService.BlueNodeFunctions;

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
            if (Hostname.equals(element.getName())) {
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
    
    public synchronized BlueNodeEntry reverseLookupBnBasedOnRn(String hostname) {
    	Iterator<BlueNodeEntry> iterator = list.descendingIterator();
        while (iterator.hasNext()) {
        	BlueNodeEntry bn = iterator.next();
        	if (bn.rednodes.checkOnlineByHn(hostname)) {
        		return bn;
        	}        	
        }        
    	return null;
    }
    
    public synchronized LinkedList<String> getRedNodeHostnameList() {
    	LinkedList<String> fetched = new LinkedList<>();
    	Iterator<BlueNodeEntry> iterator = list.descendingIterator();
        while (iterator.hasNext()) {
        	BlueNodeEntry bn = iterator.next();
        	fetched.addAll(bn.getRedNodes().getLeasedHostnameList());
        }
        return fetched;
    }
    
    public synchronized Boolean checkOnlineByHn(String Hostname) {
    	Iterator<BlueNodeEntry> iterator = list.descendingIterator();
    	while (iterator.hasNext()) {
        	BlueNodeEntry element = iterator.next();
            if (Hostname.equals(element.getName())) {  
                return true;
            }
        }
        return false;
    }
    
    public synchronized boolean checkOnlineRnByHn(String hostname) {
    	LinkedList<String> fetched = new LinkedList<>();
    	Iterator<BlueNodeEntry> iterator = list.descendingIterator();
        while (iterator.hasNext()) {
        	BlueNodeEntry bn = iterator.next();
        	if (bn.rednodes.checkOnlineByHn(hostname)) {
        		return true;
        	}        	
        }
        return false;
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

    public synchronized int leaseBn(String Hostname, String Phaddress, int port, Time regTimestamp) {        
        BlueNodeEntry bn = new BlueNodeEntry(Hostname, Phaddress, port, regTimestamp);
        list.add(bn);
        App.ConsolePrint(pre + " LEASED " + Hostname + " WITH " + Phaddress + ":" + port);
        notifyGUI();
        return list.size();        
    }

    public synchronized int leaseBn(String Hostname, String Phaddress, int port) {        
        BlueNodeEntry bn = new BlueNodeEntry(Hostname, Phaddress, port);
        list.add(bn);
        App.ConsolePrint(pre + " LEASED " + Hostname + " WITH " + Phaddress + ":" + port);
        notifyGUI();
        return list.size();        
    }
    
    public synchronized boolean releaseBnByID(int id) {
    	if (id >= 0 && id < list.size()) {
    		BlueNodeEntry element = list.remove(id);              
            App.ConsolePrint(pre +element.getName()+" RELEASED ENTRY");
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
            if (Hostname.equals(element.getName())) {                
                    list.remove(i);              
                    App.ConsolePrint(pre +Hostname+" RELEASED ENTRY");
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
    
    public synchronized void flushBnTable() {
    	list.clear();
    }
    
    public synchronized void rebuildTableViaAuthClient() {
    	Iterator<BlueNodeEntry> iterator = list.descendingIterator();
    	int i = 0;
    	while (iterator.hasNext()) {
        	BlueNodeEntry element = iterator.next();            
            if (BlueNodeFunctions.checkBnOnline(element)) {
            	System.out.println(pre+"Fetching RNs from BN "+element.getName());
                element.updateTimestamp();
                LinkedList<RedNodeEntry> rns = BlueNodeFunctions.getRedNodes(element);
                element.rednodes = new RedNodeTable(element, rns);
            } else {                
                list.remove(i);               
            }
            i++;
        }
    	System.out.println(pre+" BN Table rebuilt");
    	notifyGUI();    	
    }

    //this will build the obj for gui
    //gui can handle itself
    public synchronized String[][] buildStringInstanceObject() {        
    	String obj[][] = new String[list.size()][];
        int i = 0;
        Iterator<BlueNodeEntry> iterator = list.descendingIterator();
    	while (iterator.hasNext()) {
    		BlueNodeEntry element = iterator.next();
            obj[i] = new String[]{element.getName(), element.getPhaddress(), ""+element.getPort(), ""+element.getLoad(), element.getTimestamp().toString()};
            i++;
        }
    	return obj;
    }
    
    public synchronized String[][] buildRednodeStringInstanceObject() {        
    	String obj[][] = new String[list.size()][];
        int i = 0;
        Iterator<BlueNodeEntry> iterator = list.descendingIterator();
    	while (iterator.hasNext()) {
    		BlueNodeEntry element = iterator.next();
    		Iterator<RedNodeEntry> redIterator = element.getRedNodes().getList().descendingIterator();
    		while (redIterator.hasNext()) {
        		RedNodeEntry redElement = redIterator.next();
                obj[i] = new String[]{redElement.getHostname(), redElement.getHostname(), ""+redElement.getVaddress(), ""+element.getName(), redElement.getTimestamp().toString()};
                i++;
            }
        }
    	return obj;
    }
    
    private void notifyGUI () {
    	if (App.gui) {
    		App.window.updateBlueNodeTable();
    		App.window.updateRedNodeTable();
    	}
    }
}
