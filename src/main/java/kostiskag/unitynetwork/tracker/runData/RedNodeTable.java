package kostiskag.unitynetwork.tracker.runData;

import java.sql.Time;
import java.util.Iterator;
import java.util.LinkedList;

import com.mysql.jdbc.UpdatableResultSet;

import kostiskag.unitynetwork.tracker.App;

/**
 * Each BlueNodeEntry owns an object of a RedNodeTable.
 * In other words an object of a RedNodeTable represents all the connected rns over a bn
 * 
 * @author Konstantinos Kagiampakis
 */
public class RedNodeTable {

    private static String pre = "^RNTABLE ";
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
    	Iterator<RedNodeEntry> iterator = list.listIterator();
        while (iterator.hasNext()) {
        	RedNodeEntry element = iterator.next();
        	if (element.getHostname().equals(hostname) || element.getVaddress().equals(vAddress)) {
        		throw new Exception("Attempted to lease a non unique rednode entry.");
        	}
        }
        
    	RedNodeEntry rn = new RedNodeEntry(hostname, vAddress);
    	list.add(rn);
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
        int i = 0;
    	while (iterator.hasNext()) {
        	RedNodeEntry element = iterator.next();
            if (hostname.equals(element.getHostname())) {
                list.remove(i);
                notifyGUI();
                App.ConsolePrint(pre +hostname+" RELEASED ENTRY");
                return true;
            }
            i++;
        }    	    	
    	return false;
    }
    
    public synchronized boolean releaseByVAddress(String vAddress) {
    	Iterator<RedNodeEntry> iterator = list.listIterator();
        int i = 0;
    	while (iterator.hasNext()) {
        	RedNodeEntry element = iterator.next();
            if (vAddress.equals(element.getVaddress())) {
                list.remove(i);
                notifyGUI();
                return true;
            }
            i++;
        }    	    	
    	return false;
    }
   
    public synchronized void flushTable() {
        list.clear();
        notifyGUI();
    }
	
	private void notifyGUI () {
    	if (App.gui) {
    		App.window.updateRedNodeTable();
    	}
    }
}
