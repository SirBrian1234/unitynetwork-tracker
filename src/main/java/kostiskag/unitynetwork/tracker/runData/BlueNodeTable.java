package kostiskag.unitynetwork.tracker.runData;

import java.sql.Time;
import java.util.Iterator;
import java.util.LinkedList;
import kostiskag.unitynetwork.tracker.App;
import kostiskag.unitynetwork.tracker.service.sonar.BlueNodeFunctions;

/**
 * A bluenode table holds all the joined bluenodes on the network.
 * Each connected bluenode is represented by a BlueNode entry.
 * Where each BlueNode entry holds a RedNode list with all its connected 
 * rednodes.
 *
 * @author Konstantinos Kagiampakis
 */
public class BlueNodeTable {

    private final static String pre = "^BNTABLE ";
    private LinkedList<BlueNodeEntry> list;

    public BlueNodeTable() {
        list = new LinkedList<BlueNodeEntry>();
        App.ConsolePrint(pre + "INITIALIZED ");
    }

    public synchronized BlueNodeEntry getBlueNodeEntryByHn(String Hostname) {
    	Iterator<BlueNodeEntry> iterator = list.listIterator();
        while (iterator.hasNext()) {
        	BlueNodeEntry element = iterator.next();
            if (Hostname.equals(element.getName())) {
                return element;
            }
        }
        App.ConsolePrint(pre + "NO ENTRY FOR " + Hostname + " IN TABLE");
        return null;
    }
    
    //the combination of ph address and port is unique
    public synchronized BlueNodeEntry getBlueNodeEntryByPhAddrPort(String Phaddress, int port) {
    	Iterator<BlueNodeEntry> iterator = list.listIterator();
        while (iterator.hasNext()) {
        	BlueNodeEntry bn = iterator.next();
        	if (bn.getPhaddress().equals(Phaddress) && bn.getPort() == port) {
        		return bn;
        	}        	
        }
        return null;
    }
    
    public synchronized BlueNodeEntry getBlueNodeEntryByLowestLoad() {
    	BlueNodeEntry retrieved = null;
    	if (list.size() > 0) {
    		int min = list.getFirst().getLoad();
    		retrieved = list.getFirst();
    		
	    	Iterator<BlueNodeEntry> iterator = list.listIterator();
	        while (iterator.hasNext()) {
	        	BlueNodeEntry element = iterator.next();
	            if (element.getLoad() <= min) {
	                min = element.getLoad();
	                retrieved = element;
	            }
	        }	      
        } 
        return retrieved;
    }
    
    public synchronized BlueNodeEntry reverseLookupBnBasedOnRn(String hostname) {
    	Iterator<BlueNodeEntry> iterator = list.listIterator();
        while (iterator.hasNext()) {
        	BlueNodeEntry bn = iterator.next();
        	if (bn.rednodes.checkOnlineByHn(hostname)) {
        		return bn;
        	}        	
        }        
    	return null;
    }
    
    public synchronized LinkedList<String> getLeasedRedNodeHostnameList() {
    	LinkedList<String> fetched = new LinkedList<>();
    	Iterator<BlueNodeEntry> iterator = list.listIterator();
        while (iterator.hasNext()) {
        	BlueNodeEntry bn = iterator.next();
        	fetched.addAll(bn.getRedNodes().getLeasedRedNodeHostnameList());
        }
        return fetched;
    }
    
    public synchronized Boolean checkOnlineByName(String name) {
    	Iterator<BlueNodeEntry> iterator = list.listIterator();
    	while (iterator.hasNext()) {
        	BlueNodeEntry element = iterator.next();
            if (name.equals(element.getName())) {  
                return true;
            }
        }
        return false;
    }
    
    public synchronized boolean checkOnlineRnByHn(String hostname) {
    	LinkedList<String> fetched = new LinkedList<>();
    	Iterator<BlueNodeEntry> iterator = list.listIterator();
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
    	Iterator<BlueNodeEntry> iterator = list.listIterator();
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

    public synchronized void lease(String name, String phAddress, int port) throws Exception {     
    	if (App.bncap == 0 || App.bncap > list.size()) {
	    	Iterator<BlueNodeEntry> iterator = list.listIterator();
	        while (iterator.hasNext()) {
	        	BlueNodeEntry element = iterator.next();
	        	if (element.getName().equals(name) || (element.getPhaddress().equals(phAddress) && element.getPort() == port)) {
	        		throw new Exception("Attempted to insert a non unique bluenode entry.");
	        	}
	        }
	    	
	    	BlueNodeEntry bn = new BlueNodeEntry(name, phAddress, port);
	        list.add(bn);
	        App.ConsolePrint(pre + " LEASED " + name + " WITH " + phAddress + ":" + port);
	        notifyGUI();
    	} else {
    		throw new Exception(pre + "Maximum Blue Node upper limit reached.");
    	}
    }
    
    public synchronized void leaseRednode(String bluenodeName, String hostname, String vAddress) throws Exception {
    	BlueNodeEntry element = null;
    	boolean found = false;
    	Iterator<BlueNodeEntry> iterator = list.listIterator();
    	while (iterator.hasNext()) {
        	element = iterator.next();
            if (bluenodeName.equals(element.getName())) {
                found = true;
            	break;                
            }
        }
        
    	if (!found) {
    		throw new Exception("Attempted to lease over a non existong bluenode.");
    	}
    	
    	iterator = list.listIterator();
        while (iterator.hasNext()) {
        	BlueNodeEntry bn = iterator.next();
        	if (bn.rednodes.checkOnlineByHn(hostname)) {
        		throw new Exception("Attempted to lease a non unique rednode entry.");
        	} else if (bn.rednodes.checkOnlineByVaddress(vAddress)) {
        		throw new Exception("Attempted to lease a non unique rednode entry.");
        	}
        }
        
        element.rednodes.lease(hostname, vAddress);
        App.ConsolePrint(pre + " LEASED RN " + hostname + " OVER "+bluenodeName);
    	notifyGUI();
    }
    
    public synchronized void release(String name) throws Exception {
        Iterator<BlueNodeEntry> iterator = list.listIterator();
        while (iterator.hasNext()) {
        	BlueNodeEntry element = iterator.next();
            if (name.equals(element.getName())) {                
                    iterator.remove();              
                    App.ConsolePrint(pre +name+" RELEASED ENTRY");
                    notifyGUI();
                    return;
            }
        }        
        throw new Exception("NO BLUENODE ENTRY FOR " + name + " IN TABLE");                   
    }
    
    public synchronized void releaseRednode(String hostname) throws Exception {
        Iterator<BlueNodeEntry> iterator = list.listIterator();
        while (iterator.hasNext()) {
        	BlueNodeEntry element = iterator.next();
            if (element.rednodes.checkOnlineByHn(hostname)) {                
                    element.rednodes.release(hostname);              
                    return;
            }
        }        
        throw new Exception("NO REDNODE ENTRY FOR " + hostname + " IN TABLE");                   
    }
    
    public synchronized void flushBnTable() {
    	list.clear();
    }
    
    public synchronized void rebuildTableViaAuthClient() {
    	Iterator<BlueNodeEntry> iterator = list.listIterator();
    	while (iterator.hasNext()) {
        	BlueNodeEntry element = iterator.next();            
            if (BlueNodeFunctions.checkBnOnline(element)) {
            	System.out.println(pre+"Fetching RNs from BN "+element.getName());
                element.updateTimestamp();
                LinkedList<RedNodeEntry> rns = BlueNodeFunctions.getRedNodes(element);
                element.rednodes = new RedNodeTable(element, rns);
            } else {                
            	iterator.remove();               
            }
        }
    	System.out.println(pre+" BN Table rebuilt");
    	notifyGUI();    	
    }

    //this will build the obj for gui
    //gui can handle itself
    public synchronized String[][] buildStringInstanceObject() {        
    	String obj[][] = new String[list.size()][];
        int i = 0;
        Iterator<BlueNodeEntry> iterator = list.listIterator();
    	while (iterator.hasNext()) {
    		BlueNodeEntry element = iterator.next();
            obj[i] = new String[]{element.getName(), element.getPhaddress(), ""+element.getPort(), ""+element.getLoad(), element.getTimestamp().toString()};
            i++;
        }
    	return obj;
    }
    
    public synchronized String[][] buildRednodeStringInstanceObject() {        
    	String obj[][] = null;
        int i = 0;
        Iterator<BlueNodeEntry> iterator = list.listIterator();
    	while (iterator.hasNext()) {
    		BlueNodeEntry element = iterator.next();
    		Iterator<RedNodeEntry> redIterator = element.getRedNodes().getList().listIterator();
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
