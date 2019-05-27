package kostiskag.unitynetwork.tracker.runData;

import java.util.Iterator;
import java.util.LinkedList;
import kostiskag.unitynetwork.tracker.App;

/**
 * Each BlueNodeEntry owns an object of a RedNodeTable.
 * In other words an object of a RedNodeTable represents all the connected rns over a bn
 * 
 * @author Konstantinos Kagiampakis
 */
public class RedNodeTable {

    private final static String pre = "^RNTABLE ";
    private BlueNodeEntry bluenode;
    private LinkedList<RedNodeEntry> list = new LinkedList<>();
    
    public RedNodeTable(BlueNodeEntry bluenode) {
    	this.bluenode = bluenode;
        list = new LinkedList<RedNodeEntry>();
        App.ConsolePrint(pre + "INITIALIZED ");
    }
    
    public RedNodeTable(BlueNodeEntry bluenode, LinkedList<RedNodeEntry> builtList) {
    	this.bluenode = bluenode;
        list = builtList;
        App.ConsolePrint(pre + "INITIALIZED ");
    }

    public synchronized RedNodeEntry getRedNodeEntryByHn(String hostname) {
    	Iterator<RedNodeEntry> iterator = list.listIterator();
        while (iterator.hasNext()) {
        	RedNodeEntry element = iterator.next();
            if (hostname.equals(element.getHostname())) {
                return element;
            }
        }    	
        return null;
    }
    
    public synchronized RedNodeEntry getRedNodeEntryByVAddr(String vaddress) {
    	Iterator<RedNodeEntry> iterator = list.listIterator();
        while (iterator.hasNext()) {
        	RedNodeEntry element = iterator.next();
        	if (vaddress.equals(element.getVaddress())) {
                return element;
            }
        }    	
        return null;
    }

    public synchronized int getSize() {
        return list.size();
    }
    
    public synchronized LinkedList<RedNodeEntry> getList() {
        return list;
    }
    
    public synchronized LinkedList<String> getLeasedRedNodeHostnameList() {
    	LinkedList<String> fetched = new LinkedList<>();
    	Iterator<RedNodeEntry> iterator = list.listIterator();
        while (iterator.hasNext()) {
        	RedNodeEntry element = iterator.next();
        	fetched.add(element.getHostname());
        }
        return fetched;
    }

    public synchronized void lease(String hostname, String vAddress) throws Exception {
    	if (	
    			hostname.length() > 0 && 
    			hostname.length() <= App.max_str_len_small_size && 
    			vAddress.length() > 0 && 
    			vAddress.length() <= App.max_str_addr_len) {
	    	
    		Iterator<RedNodeEntry> iterator = list.listIterator();
	        while (iterator.hasNext()) {
	        	RedNodeEntry element = iterator.next();
	        	if (element.getHostname().equals(hostname) || element.getVaddress().equals(vAddress)) {
	        		throw new Exception("Attempted to lease a non unique rednode entry.");
	        	}
	        }
	        
	    	RedNodeEntry rn = new RedNodeEntry(bluenode, hostname, vAddress);
	    	list.add(rn);
	    	notifyGUI();
    	} else {
    		throw new Exception("Rednode lease bad data.");
    	}
    }
    
    public synchronized boolean checkOnlineByHn(String hostname) {
    	Iterator<RedNodeEntry> iterator = list.listIterator();
        while (iterator.hasNext()) {
        	RedNodeEntry element = iterator.next();
        	if (element.getHostname().equals(hostname)) {
        		return true;
        	}
        }
        return false;
    }
    
    public synchronized boolean checkOnlineByVaddress(String vAddress) {
    	Iterator<RedNodeEntry> iterator = list.listIterator();
        while (iterator.hasNext()) {
        	RedNodeEntry element = iterator.next();
        	if (element.getVaddress().equals(vAddress)) {
        		return true;
        	}
        }
        return false;
    }
    
    public synchronized boolean release(String hostname) {
    	Iterator<RedNodeEntry> iterator = list.listIterator();
    	while (iterator.hasNext()) {
        	RedNodeEntry element = iterator.next();
            if (hostname.equals(element.getHostname())) {
            	iterator.remove();
            	notifyGUI();
                App.ConsolePrint(pre +hostname+" RELEASED ENTRY");
                return true;
            }
        }    	    	
    	return false;
    }
    
    public synchronized boolean releaseByVAddress(String vAddress) {
    	Iterator<RedNodeEntry> iterator = list.listIterator();
    	while (iterator.hasNext()) {
        	RedNodeEntry element = iterator.next();
            if (vAddress.equals(element.getVaddress())) {
                iterator.remove();
                notifyGUI();
                return true;
            }
        }    	    	
    	return false;
    }
   
	private void notifyGUI () {
    	if (App.gui) {
    		App.window.updateRedNodeTable();
    	}
    }
}
