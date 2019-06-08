package org.kostiskag.unitynetwork.tracker.rundata.table;

import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.locks.Lock;
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
public class RedNodeTable extends NodeTable<RedNodeEntry> {

    private static final String pre = "^RNTABLE ";
    private final BlueNodeEntry bluenode;

    public RedNodeTable(BlueNodeEntry bluenode) {
        this.bluenode = bluenode;
    }

    private Optional<RedNodeEntry> getOptionalRedNodeEntryByVAddr(Lock lock, String vaddress) throws UnknownHostException, InterruptedException {
        validateLock(lock);
        return getOptionalRedNodeEntryByVAddr(lock, VirtualAddress.valueOf(vaddress));
    }

    private Optional<RedNodeEntry> getOptionalRedNodeEntryByVAddr(Lock lock, VirtualAddress vaddress) throws InterruptedException {
        validateLock(lock);
        return list.stream()
                .filter(element -> element.getAddress().equals(vaddress))
                .findFirst();
    }

    //this is not safe!
    public List<RedNodeEntry> getList(Lock lock) throws InterruptedException {
        validateLock(lock);
        return list;
    }

    /*
        These are all questions to the table
        The concept is that a caller should ask first whether the object exists
        with these and then!!!!
        perform a get! if he performs get whithout prior calling get and the entry is
        absent he will get a thrown exception!
    */

    public boolean isOnlineByVaddress(Lock lock, String vAddress) throws UnknownHostException, InterruptedException {
        return isOnlineByVaddress(lock, VirtualAddress.valueOf(vAddress));
    }

    public boolean isOnlineByVaddress(Lock lock, VirtualAddress vAddress) throws InterruptedException {
        validateLock(lock);
        return getOptionalRedNodeEntryByVAddr(lock, vAddress).isPresent();
    }

    public List<String> getLeasedRedNodeHostnameList(Lock lock) throws InterruptedException {
        validateLock(lock);
        return list.stream()
                .map(rnentry -> rnentry.getHostname())
                .collect(Collectors.toList());
    }
    //end of questions

    public RedNodeEntry getRedNodeEntryByVAddr(Lock lock, String vaddress) throws UnknownHostException, RedNodeTableException, InterruptedException {
        return getRedNodeEntryByVAddr(lock, VirtualAddress.valueOf(vaddress));
    }

    public RedNodeEntry getRedNodeEntryByVAddr(Lock lock, VirtualAddress vaddress) throws RedNodeTableException, InterruptedException {
        Optional<RedNodeEntry> r = getOptionalRedNodeEntryByVAddr(lock, vaddress);
        if (r.isPresent()){
            return r.get();
        }
        throw new RedNodeTableException("the given rn was not found on table "+vaddress);
    }
    //end of retrievers


    /*
        All lease related methods
     */
    public void lease(Lock lock, RedNodeEntry rn) throws IllegalAccessException, InterruptedException {
        validateLock(lock);
        if (!rn.getParentBlueNode().equals(this.bluenode)) {
            throw new IllegalAccessException("This instance does not belong to the table's BlueNode!");
        }
        if(list.contains(rn)) {
            throw new IllegalAccessException("Attempted to lease a non unique rednode entry. "+rn);
        }

        list.add(rn);
        AppLogger.getLogger().consolePrint(pre +" LEASED ENTRY of "+rn);
        notifyGUI();
    }

    public void lease(Lock lock, String hostname, VirtualAddress vAddress) throws IllegalAccessException, InterruptedException {
        validateLock(lock);
    	if (hostname.length() > 0 && hostname.length() <= App.MAX_STR_LEN_SMALL_SIZE) {

            RedNodeEntry rn = new RedNodeEntry(bluenode, hostname, vAddress);
            if(list.contains(rn)) {
                throw new IllegalAccessException("Attempted to lease a non unique rednode entry. "+rn);
            }

	    	list.add(rn);
            AppLogger.getLogger().consolePrint(pre +" LEASED ENTRY of "+rn);
            notifyGUI();
    	} else {
    		throw new IllegalAccessException("Rednode lease tried to lease a rn with a malformed hostname. "+vAddress);
    	}
    }

    public void lease(Lock lock, String hostname, String vAddress) throws UnknownHostException, IllegalAccessException, InterruptedException {
        lease(lock, hostname, VirtualAddress.valueOf(vAddress));
    }
    //end of lease

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
        list.remove(entryToBeReleased);
        AppLogger.getLogger().consolePrint(pre +" RELEASED ENTRY of "+entryToBeReleased);
        notifyGUI();
    }
    //end of release related methods

    //cleaners
    public void clearAndRebuildList(List redNodeList) {
        list.clear();
        list.addAll(redNodeList);
        //there is no duplicate control
        AppLogger.getLogger().consolePrint(pre +" List was rebuild for "+bluenode);
        notifyGUI();
    }

    public void clearList() {
        clearAndRebuildList(Arrays.asList());
    }

    public Stream<RedNodeEntry> stream() {
        return list.stream();
    }

    //system
    private void notifyGUI () {
    	if (MainWindow.isInstance()) {
    		MainWindow.getInstance().updateRedNodeTable();
    	}
    }
}
