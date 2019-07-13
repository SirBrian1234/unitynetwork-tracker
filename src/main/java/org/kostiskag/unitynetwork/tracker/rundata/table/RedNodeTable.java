package org.kostiskag.unitynetwork.tracker.rundata.table;

import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.kostiskag.unitynetwork.common.address.VirtualAddress;
import org.kostiskag.unitynetwork.common.calculated.NumericConstraints;
import org.kostiskag.unitynetwork.common.table.NodeTable;

import org.kostiskag.unitynetwork.tracker.AppLogger;
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
public class RedNodeTable extends NodeTable<RedNodeEntry> {

    private static final String pre = "^RNTABLE ";
    private final BlueNodeEntry bluenode;

    public RedNodeTable(BlueNodeEntry bluenode) {
        this.bluenode = bluenode;
    }

    /*
        These are all queries to the table
        The concept is that a caller should ask first whether the object exists
        with these and then!!!!
        perform a get! if he performs get whithout prior calling get and the entry is
        absent he will get a thrown exception!

        If he uses the optionals he can both check whether and element is online and retrieve it
        as well!!!
    */
    public Optional<RedNodeEntry> getOptionalRedNodeEntryByVAddr(Lock lock, String vaddress) throws UnknownHostException, InterruptedException {
        validateLock(lock);
        return getOptionalRedNodeEntryByVAddr(lock, VirtualAddress.valueOf(vaddress));
    }

    public Optional<RedNodeEntry> getOptionalRedNodeEntryByVAddr(Lock lock, VirtualAddress vaddress) throws InterruptedException {
        validateLock(lock);
        return nodes.stream()
                .filter(element -> element.getAddress().equals(vaddress))
                .findFirst();
    }

    //use optionals instead!
    @Deprecated
    public boolean isOnlineByVaddress(Lock lock, String vAddress) throws UnknownHostException, InterruptedException {
        return isOnlineByVaddress(lock, VirtualAddress.valueOf(vAddress));
    }

    @Deprecated
    public boolean isOnlineByVaddress(Lock lock, VirtualAddress vAddress) throws InterruptedException {
        validateLock(lock);
        return getOptionalRedNodeEntryByVAddr(lock, vAddress).isPresent();
    }

    @Deprecated
    public RedNodeEntry getRedNodeEntryByVAddr(Lock lock, String vaddress) throws UnknownHostException, IllegalAccessException, InterruptedException {
        return getRedNodeEntryByVAddr(lock, VirtualAddress.valueOf(vaddress));
    }

    @Deprecated
    public RedNodeEntry getRedNodeEntryByVAddr(Lock lock, VirtualAddress vaddress) throws IllegalAccessException, InterruptedException {
        Optional<RedNodeEntry> r = getOptionalRedNodeEntryByVAddr(lock, vaddress);
        if (r.isPresent()){
            return r.get();
        }
        throw new IllegalAccessException("the given rn was not found on table "+vaddress);
    }

    //this is not safe!
    @Deprecated
    public List<RedNodeEntry> getList(Lock lock) throws InterruptedException {
        validateLock(lock);
        return nodes.stream().collect(Collectors.toList());
    }

    //this is safe!
    public Stream<RedNodeEntry> stream() {
        return nodes.stream();
    }

    //this is an immutable list!
    public List<String> getLeasedRedNodeHostnameList(Lock lock) throws InterruptedException {
        validateLock(lock);
        return nodes.stream()
                .map(rnentry -> rnentry.getHostname())
                .collect(Collectors.toUnmodifiableList());
    }

    /*
        All lease related methods
     */
    public void lease(Lock lock, RedNodeEntry rn) throws IllegalAccessException, InterruptedException {
        validateLock(lock);
        if (!rn.getParentBlueNode().equals(this.bluenode)) {
            throw new IllegalAccessException("This instance does not belong to the table's BlueNode!");
        }
        if(nodes.contains(rn)) {
            throw new IllegalAccessException("Attempted to lease a non unique rednode entry. "+rn);
        }

        nodes.add(rn);
        AppLogger.getLogger().consolePrint(pre +" LEASED ENTRY of "+rn);
        notifyGUI();
    }

    public boolean lease(Lock lock, String hostname, String vAddress) throws UnknownHostException, IllegalAccessException, InterruptedException {
        return lease(lock, hostname, VirtualAddress.valueOf(vAddress));
    }

    public boolean lease(Lock lock, String hostname, VirtualAddress vAddress) throws IllegalAccessException, InterruptedException {
        validateLock(lock);
    	if (hostname.length() > 0 && hostname.length() <= NumericConstraints.MAX_STR_LEN_SMALL.size()) {

            RedNodeEntry rn = new RedNodeEntry(bluenode, hostname, vAddress);
            if(nodes.contains(rn)) {
                throw new IllegalAccessException("Attempted to lease a non unique rednode entry. "+rn);
            }

	    	boolean result = nodes.add(rn);
            if (result) {
                AppLogger.getLogger().consolePrint(pre + " LEASED ENTRY of " + rn);
                notifyGUI();
                return true;
            }
        } else {
    		throw new IllegalAccessException("Rednode lease tried to lease a rn with a malformed hostname. "+vAddress);
    	}
    	return false;
    }

    /*
        These are all release related mehtods
     */
    public void release(Lock lock, RedNodeEntry toBeChecked) throws IllegalAccessException, InterruptedException {
        Optional<RedNodeEntry> r = getOptionalNodeEntry(lock, toBeChecked);
        if (!r.isPresent())
            throw new IllegalAccessException("rn was not found on table "+r.get());
        else {
            releaseInner(r.get());
        }
    }

    public void release(Lock lock, String hostname) throws IllegalAccessException, InterruptedException {
        Optional<RedNodeEntry> r = getOptionalNodeEntry(lock, hostname);
        if (!r.isPresent())
            throw new IllegalAccessException("element with given hostname "+hostname+" was not found on table");
        else {
            releaseInner(r.get());
        }
    }

    public void releaseByVAddress(Lock lock, String vAddress) throws UnknownHostException, IllegalAccessException, InterruptedException {
        releaseByVAddress(lock, VirtualAddress.valueOf(vAddress));
    }

    public void releaseByVAddress(Lock lock, VirtualAddress vAddress) throws IllegalAccessException, InterruptedException {
        Optional<RedNodeEntry> r = getOptionalRedNodeEntryByVAddr(lock, vAddress);
        if (!r.isPresent()) {
            throw new IllegalAccessException("Given entry to be released was not on table "+ r.get());
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
        nodes.remove(entryToBeReleased);
        AppLogger.getLogger().consolePrint(pre +" RELEASED ENTRY of "+entryToBeReleased);
        notifyGUI();
    }
    //end of release related methods

    //cleaners
    public void clearAndRebuildList(List redNodeList) {
        nodes.clear();
        nodes.addAll(redNodeList);
        //there is no duplicate control
        AppLogger.getLogger().consolePrint(pre +" List was rebuild for "+bluenode);
        notifyGUI();
    }

    public void clearList() {
        clearAndRebuildList(Arrays.asList());
    }

    //system
    private void notifyGUI () {
    	if (MainWindow.isInstance()) {
    		MainWindow.getInstance().updateRedNodeTable();
    	}
    }
}
