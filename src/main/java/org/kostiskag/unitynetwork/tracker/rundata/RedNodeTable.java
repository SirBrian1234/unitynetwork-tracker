package org.kostiskag.unitynetwork.tracker.rundata;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.kostiskag.unitynetwork.tracker.App;
import org.kostiskag.unitynetwork.tracker.AppLogger;
import org.kostiskag.unitynetwork.tracker.gui.MainWindow;

import static java.util.Arrays.asList;


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
        this(bluenode, new ArrayList<>());
    }
    
    public RedNodeTable(BlueNodeEntry bluenode, List<RedNodeEntry> builtList) {
        this.bluenode = bluenode;
        this.list = builtList;
        AppLogger.getLogger().consolePrint(pre + "INITIALIZED");
    }

    public synchronized Optional<RedNodeEntry> getOptionalRedNodeEntryByHn(String hostname) {
        return list.stream()
                .filter(element -> hostname.equals(element.getHostname()))
                .findFirst();
    }

    public synchronized RedNodeEntry getRedNodeEntryByHn(String hostname) {
        Optional<RedNodeEntry> e = getOptionalRedNodeEntryByHn(hostname);
        if (e.isPresent()) {
            return e.get();
        }
        return null;
    }

    public synchronized Optional<RedNodeEntry> getOptionalRedNodeEntryByVAddr(String vaddress) {
        return list.stream()
                .filter(element -> element.getVaddress().equals(vaddress))
                .findFirst();
    }

    public synchronized RedNodeEntry getRedNodeEntryByVAddr(String vaddress) {
    	Optional<RedNodeEntry> r= getOptionalRedNodeEntryByVAddr(vaddress);
    	if (r.isPresent()){
    	    return r.get();
        }
    	return null;
    }

    public synchronized int getSize() {
        return list.size();
    }
    
    public synchronized List<RedNodeEntry> getList() {
        return list;
    }
    
    public synchronized List<String> getLeasedRedNodeHostnameList() {
    	return list.stream()
                .map(rnentry -> rnentry.getHostname())
                .collect(Collectors.toList());
    }

    public synchronized void lease(String hostname, String vAddress) throws Exception {
    	if (	
    			hostname.length() > 0 && 
    			hostname.length() <= App.MAX_STR_LEN_SMALL_SIZE &&
    			vAddress.length() > 0 && 
    			vAddress.length() <= App.MAX_STR_ADDR_LEN) {

            RedNodeEntry rn = new RedNodeEntry(bluenode, hostname, vAddress);

            if(list.contains(rn)) {
                throw new Exception("Attempted to lease a non unique rednode entry. "+rn);
            }

	    	list.add(rn);
            AppLogger.getLogger().consolePrint(pre +" LEASED ENTRY of "+rn);
            notifyGUI();
    	} else {
    		throw new Exception("Rednode lease bad data.");
    	}
    }
    
    public synchronized boolean checkOnlineByHn(String hostname) {
    	return getOptionalRedNodeEntryByHn(hostname).isPresent();
    }
    
    public synchronized boolean checkOnlineByVaddress(String vAddress) {
        return getOptionalRedNodeEntryByVAddr(vAddress).isPresent();
    }

    public synchronized boolean release(RedNodeEntry entryToBeReleased) {
        Optional<RedNodeEntry> r = list.stream().filter(e -> e.equals(entryToBeReleased)).findFirst();
        if (!r.isPresent())
            return false;
        else {
            list.remove(r.get());
            AppLogger.getLogger().consolePrint(pre +" RELEASED ENTRY of "+r);
            notifyGUI();
            return true;
        }
    }

    public synchronized boolean release(String hostname) {
    	Optional<RedNodeEntry> r = getOptionalRedNodeEntryByHn(hostname);
    	if (!r.isPresent())
    	    return false;
    	else {
    	    list.remove(r.get());
            AppLogger.getLogger().consolePrint(pre +" RELEASED ENTRY of "+r);
            notifyGUI();
            return true;
        }
    }
    
    public synchronized boolean releaseByVAddress(String vAddress) {
    	Optional<RedNodeEntry> r = getOptionalRedNodeEntryByVAddr(vAddress);
    	if (!r.isPresent()) {
    	    return false;
        } else {
    	    list.remove(r.get());
            AppLogger.getLogger().consolePrint(pre +" RELEASED ENTRY of "+r);
            notifyGUI();
            return true;
        }
    }

    public synchronized void clearAndRebuildList(List redNodeList) {
        list.clear();
        list.addAll(redNodeList);
        AppLogger.getLogger().consolePrint(pre +" List was rebuild for "+bluenode);
        notifyGUI();
    }

    public synchronized void clearList() {
        clearAndRebuildList(Arrays.asList());
    }

    public synchronized Stream<RedNodeEntry> stream() {
        return list.stream();
    }

    private void notifyGUI () {
    	if (MainWindow.isInstance()) {
    		MainWindow.getInstance().updateRedNodeTable();
    	}
    }
}
