package kostiskag.unitynetwork.tracker.runData;

import java.sql.Time;
import java.util.Iterator;
import java.util.LinkedList;

import com.mysql.jdbc.UpdatableResultSet;

import kostiskag.unitynetwork.tracker.App;

/**
 *
 * @author kostis
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
    
    public synchronized RedNodeEntry getRedNodeEntryById(int id) {
    	if (id >= 0 && id < list.size()) {
    	    return list.get(id);
        } else {
            App.ConsolePrint(pre + "NO ENTRY " + id + " IN TABLE");
            return null;
        }
    }

    public synchronized RedNodeEntry getRedNodeEntryByHn(String hostname) {
    	Iterator<RedNodeEntry> iterator = list.descendingIterator();
        while (iterator.hasNext()) {
        	RedNodeEntry element = iterator.next();
            if (hostname.equals(element.getHostname())) {
                return element;
            }
        }    	
        return null;
    }
    
    //many rednodes may have the same addres (under NAT etc.)
    //this returns a list
    public synchronized LinkedList<RedNodeEntry> getRedNodeEntriesByAddr(String vaddress) {
    	LinkedList<RedNodeEntry> fetched = new LinkedList<RedNodeEntry>();
    	Iterator<RedNodeEntry> iterator = list.descendingIterator();
        while (iterator.hasNext()) {
        	RedNodeEntry element = iterator.next();
            if (vaddress.equals(element.getVaddress())) {
                fetched.add(element);
            }
        }    	
        return fetched;
    }

    public synchronized int getSize() {
        return list.size();
    }
    
    public synchronized LinkedList<RedNodeEntry> getList() {
        return list;
    }
    
    public synchronized LinkedList<String> getLeasedHostnameList() {
    	LinkedList<String> fetched = new LinkedList<>();
    	Iterator<RedNodeEntry> iterator = list.descendingIterator();
        while (iterator.hasNext()) {
        	RedNodeEntry element = iterator.next();
        	fetched.add(element.getHostname());
        }
        return fetched;
    }

    public synchronized int lease(String hostname, String vAddress, Time timestamp) {
    	RedNodeEntry rn = new RedNodeEntry(hostname, vAddress, timestamp);
    	list.add(rn);
    	notifyGUI();
    	return list.size();
    }

    public synchronized int lease(String hostname, String vAddress) {
    	RedNodeEntry rn = new RedNodeEntry(hostname, vAddress);
    	list.add(rn);
    	notifyGUI();
    	return list.size();
    }
    
    public synchronized boolean checkOnlineByHn(String hostname) {
    	Iterator<RedNodeEntry> iterator = list.descendingIterator();
        while (iterator.hasNext()) {
        	RedNodeEntry element = iterator.next();
        	element.getHostname().equals(hostname);
        	return true;
        }
        return false;
    }
    
    public synchronized int releaseByHn(String hostname) {
    	Iterator<RedNodeEntry> iterator = list.descendingIterator();
        int i = 0;
    	while (iterator.hasNext()) {
        	RedNodeEntry element = iterator.next();
            if (hostname.equals(element.getHostname())) {
                list.remove(i);
                notifyGUI();
                break;
            }
            i++;
        }    	    	
    	return list.size();
    }
    
    public synchronized int releaseById(int id) {
    	if (id >= 0 && id  < list.size()) {
    		list.remove(id);
    		notifyGUI();
    	}    	
    	return list.size();	    	
    }

    public synchronized void flushTable() {
        list.clear();
        App.ConsolePrint(pre + "INITIALIZED ");
        notifyGUI();
    }
	
	private void notifyGUI () {
    	if (App.gui) {
    		App.window.updateRedNodeTable();
    	}
    }
}
