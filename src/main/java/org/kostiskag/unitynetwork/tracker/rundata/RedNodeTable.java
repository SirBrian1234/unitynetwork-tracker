package org.kostiskag.unitynetwork.tracker.rundata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.kostiskag.unitynetwork.tracker.App;
import org.kostiskag.unitynetwork.tracker.AppLogger;
import org.kostiskag.unitynetwork.tracker.gui.MainWindow;


/**
 * Each BlueNodeEntry owns an object of a RedNodeTable.
 * In other words an object of a RedNodeTable represents all the connected rns over a bn
 * 
 * @author Konstantinos Kagiampakis
 */
public class RedNodeTable {

    private final static String pre = "^RNTABLE ";
    private final BlueNodeEntry bluenode;
    private final List<RedNodeEntry> list;
    
    public RedNodeTable(BlueNodeEntry bluenode) {
        this.bluenode = bluenode;
        this.list = new ArrayList<>();
        AppLogger.getLogger().consolePrint(pre + "INITIALIZED ");
    }
    
    public RedNodeTable(BlueNodeEntry bluenode, List<RedNodeEntry> builtList) {
        this.bluenode = bluenode;
        this.list = builtList;
        AppLogger.getLogger().consolePrint(pre + "INITIALIZED ");
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
    
    public synchronized List<RedNodeEntry> getList() {
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
    			hostname.length() <= App.MAX_STR_LEN_SMALL_SIZE &&
    			vAddress.length() > 0 && 
    			vAddress.length() <= App.MAX_STR_ADDR_LEN) {
	    	
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
                AppLogger.getLogger().consolePrint(pre +hostname+" RELEASED ENTRY");
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
    	if (MainWindow.isInstance()) {
    		MainWindow.getInstance().updateRedNodeTable();
    	}
    }
}
