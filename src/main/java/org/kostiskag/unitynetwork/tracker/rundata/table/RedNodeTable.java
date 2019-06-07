package org.kostiskag.unitynetwork.tracker.rundata.table;

import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.kostiskag.unitynetwork.tracker.App;
import org.kostiskag.unitynetwork.tracker.AppLogger;
import org.kostiskag.unitynetwork.tracker.rundata.address.VirtualAddress;
import org.kostiskag.unitynetwork.tracker.gui.MainWindow;
import org.kostiskag.unitynetwork.tracker.rundata.entry.BlueNodeEntry;
import org.kostiskag.unitynetwork.tracker.rundata.entry.RedNodeEntry;

/**
 * Each BlueNodeEntry owns an object of a RedNodeTable.
 * In other words an object of a RedNodeTable represents all the connected rns over a bn
 *
 * There a throw UnknownHostException penalty when calling methods with input argument a String vAddress
 * and that is to calculate whether the given address is valid
 * If you create a new VirtualAddress obj and pass it instead there is no throw
 * exception to catch! (or... you know... when you use hostname there is also no host exception)
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
        AppLogger.getLogger().consolePrint(pre + "INITIALIZED");
    }

    /*
        Sadly optionals cannot become public as by their nature they can be consumed anytime
        therefore our synchronized limitation to preserve data is of no use, consequently
        these have to be private!
    */
    private Optional<RedNodeEntry> getOptionalRedNodeEntry(RedNodeEntry toBeChecked) {
        return list.stream()
                .filter(rn -> rn.equals(toBeChecked))
                .findFirst();
    }

    private Optional<RedNodeEntry> getOptionalRedNodeEntryByHn(String hostname) {
        return list.stream()
                .filter(element -> hostname.equals(element.getHostname()))
                .findFirst();
    }

    private Optional<RedNodeEntry> getOptionalRedNodeEntryByVAddr(String vaddress) throws UnknownHostException {
        return getOptionalRedNodeEntryByVAddr(VirtualAddress.valueOf(vaddress));
    }

    private Optional<RedNodeEntry> getOptionalRedNodeEntryByVAddr(VirtualAddress vaddress) {
        return list.stream()
                .filter(element -> element.getAddress().equals(vaddress))
                .findFirst();
    }
    //end of optionals

    //accessors
    public synchronized int getSize() {
        return list.size();
    }

    //this is not safe!
    public synchronized List<RedNodeEntry> getList() {
        return list;
    }

    /*
        These are all questions to the table
        The concept is that a caller should ask first whether the object exists
        with these and then!!!!
        perform a get! if he performs get whithout prior calling get and the entry is
        absent he will get a thrown exception!
    */
    public synchronized boolean isOnline(RedNodeEntry toBeChecked) {
        return getOptionalRedNodeEntry(toBeChecked).isPresent();
    }

    public synchronized boolean isOnline(String hostname) {
        return getOptionalRedNodeEntryByHn(hostname).isPresent();
    }

    public synchronized boolean isOnlineByVaddress(String vAddress) throws UnknownHostException {
        return isOnlineByVaddress(VirtualAddress.valueOf(vAddress));
    }

    public synchronized boolean isOnlineByVaddress(VirtualAddress vAddress) {
        return getOptionalRedNodeEntryByVAddr(vAddress).isPresent();
    }

    public synchronized List<String> getLeasedRedNodeHostnameList() {
        return list.stream()
                .map(rnentry -> rnentry.getHostname())
                .collect(Collectors.toList());
    }
    //end of questions

    /*
        These methods are about retrieving an element from the table
        there is a throws RedNodeTableException penalty if a non existing element is
        looked up!
    */
    public synchronized RedNodeEntry getRedNodeEntry(RedNodeEntry toBeChecked) throws RedNodeTableException {
        Optional<RedNodeEntry> e = getOptionalRedNodeEntry(toBeChecked);
        if (e.isPresent()) {
            return e.get();
        }
        throw new RedNodeTableException("the given rn was not found on table "+toBeChecked);
    }

    public synchronized RedNodeEntry getRedNodeEntry(String hostname) throws RedNodeTableException {
        Optional<RedNodeEntry> e = getOptionalRedNodeEntryByHn(hostname);
        if (e.isPresent()) {
            return e.get();
        }
        throw new RedNodeTableException("the given rn with hostname was not found on table "+hostname);
    }

    public synchronized RedNodeEntry getRedNodeEntryByVAddr(String vaddress) throws UnknownHostException, RedNodeTableException {
        return getRedNodeEntryByVAddr(VirtualAddress.valueOf(vaddress));
    }

    public synchronized RedNodeEntry getRedNodeEntryByVAddr(VirtualAddress vaddress) throws RedNodeTableException {
        Optional<RedNodeEntry> r = getOptionalRedNodeEntryByVAddr(vaddress);
        if (r.isPresent()){
            return r.get();
        }
        throw new RedNodeTableException("the given rn was not found on table "+vaddress);
    }
    //end of retrievers


    /*
        All lease related methods
     */
    public synchronized void lease(RedNodeEntry rn) throws RedNodeTableException {
        if (!rn.getParentBlueNode().equals(this.bluenode)) {
            throw new RedNodeTableException("This instance does not belong to the table's BlueNode!");
        }
        if(list.contains(rn)) {
            throw new RedNodeTableException("Attempted to lease a non unique rednode entry. "+rn);
        }

        list.add(rn);
        AppLogger.getLogger().consolePrint(pre +" LEASED ENTRY of "+rn);
        notifyGUI();
    }

    public synchronized void lease(String hostname, VirtualAddress vAddress) throws RedNodeTableException, IllegalAccessException {
    	if (hostname.length() > 0 && hostname.length() <= App.MAX_STR_LEN_SMALL_SIZE) {

            RedNodeEntry rn = new RedNodeEntry(bluenode, hostname, vAddress);
            if(list.contains(rn)) {
                throw new RedNodeTableException("Attempted to lease a non unique rednode entry. "+rn);
            }

	    	list.add(rn);
            AppLogger.getLogger().consolePrint(pre +" LEASED ENTRY of "+rn);
            notifyGUI();
    	} else {
    		throw new RedNodeTableException("Rednode lease tried to lease a rn with a malformed hostname. "+vAddress);
    	}
    }

    public synchronized void lease(String hostname, String vAddress) throws UnknownHostException, RedNodeTableException, IllegalAccessException {
        lease(hostname, VirtualAddress.valueOf(vAddress));
    }
    //end of lease

    /*
        These are all release related mehtods
     */
    public synchronized void release(RedNodeEntry toBeChecked) throws RedNodeTableException {
        Optional<RedNodeEntry> r = getOptionalRedNodeEntry(toBeChecked);
        if (!r.isPresent())
            throw new RedNodeTableException("rn was not found on table "+r.get());
        else {
            releaseInner(r.get());
        }
    }

    public synchronized void release(String hostname) throws RedNodeTableException {
        Optional<RedNodeEntry> r = getOptionalRedNodeEntryByHn(hostname);
        if (!r.isPresent())
            throw new RedNodeTableException("element with given hostname "+hostname+" was not found on table");
        else {
            releaseInner(r.get());
        }
    }

    public synchronized void releaseByVAddress(String vAddress) throws UnknownHostException, RedNodeTableException {
        releaseByVAddress(VirtualAddress.valueOf(vAddress));
    }

    public synchronized void releaseByVAddress(VirtualAddress vAddress) throws RedNodeTableException {
        Optional<RedNodeEntry> r = getOptionalRedNodeEntryByVAddr(vAddress);
        if (!r.isPresent()) {
            throw new RedNodeTableException("Given entry to be released was not on table "+ r.get());
        } else {
            releaseInner(r.get());
        }
    }

    /**
     * This method is private there is no control here
     * this is called by
     * releaseByVAddress(VirtualAddress vAddress)
     * releaseByVAddress(String vAddress)
     * release(String hostname)
     *
     * @param entryToBeReleased
     */
    private void releaseInner(RedNodeEntry entryToBeReleased) {
        list.remove(entryToBeReleased);
        AppLogger.getLogger().consolePrint(pre +" RELEASED ENTRY of "+entryToBeReleased);
        notifyGUI();
    }
    //end of release related methods

    //cleaners
    public synchronized void clearAndRebuildList(List redNodeList) {
        list.clear();
        list.addAll(redNodeList);
        //there is no duplicate control
        AppLogger.getLogger().consolePrint(pre +" List was rebuild for "+bluenode);
        notifyGUI();
    }

    public synchronized void clearList() {
        clearAndRebuildList(Arrays.asList());
    }

    public synchronized Stream<RedNodeEntry> stream() {
        //this is not right there should be an obj to indicate synchronization
        return list.stream();
    }

    //system
    private void notifyGUI () {
    	if (MainWindow.isInstance()) {
    		MainWindow.getInstance().updateRedNodeTable();
    	}
    }
}
