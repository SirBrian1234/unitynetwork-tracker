package org.kostiskag.unitynetwork.tracker.rundata;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.security.PublicKey;

import org.kostiskag.unitynetwork.tracker.App;
import org.kostiskag.unitynetwork.tracker.AppLogger;
import org.kostiskag.unitynetwork.tracker.gui.MainWindow;
import org.kostiskag.unitynetwork.tracker.service.sonar.BlueNodeClient;

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
    private final int bncap;
	private final List<BlueNodeEntry> list;

	public BlueNodeTable() {
		this(0);
	}

	public BlueNodeTable(int bncap) {
        this.bncap = bncap;
        list = new ArrayList<BlueNodeEntry>();
        AppLogger.getLogger().consolePrint(pre + "INITIALIZED ");
    }

	public synchronized Optional<BlueNodeEntry> getOptionalBlueNodeEntryByHn(String name) {
		return list.stream()
			.filter(bn -> bn.getName().equals(name))
			.findFirst();
	}

	public synchronized BlueNodeEntry getBlueNodeEntryByHn(String name) {
    	Optional<BlueNodeEntry> bn = getOptionalBlueNodeEntryByHn(name);
    	if (bn.isPresent()) {
			return bn.get();
		}
    	return null;
    }

	public synchronized Optional<BlueNodeEntry> getOptionalBlueNodeEntryByPhAddrPort(String Phaddress, int port) {
		return list.stream()
				.filter(bn -> bn.getPhaddress().equals(Phaddress))
				.filter(bn -> bn.getPort() == port)
				.findFirst();
	}
		//the combination of ph address and port is unique
    public synchronized BlueNodeEntry getBlueNodeEntryByPhAddrPort(String Phaddress, int port) {
    	Optional<BlueNodeEntry> obn = getOptionalBlueNodeEntryByPhAddrPort(Phaddress,port);
    	if (obn.isPresent()) {
    		return obn.get();
		}
    	return null;
    }
    
    public synchronized BlueNodeEntry getBlueNodeEntryByLowestLoad() {
    	return list.stream()
				.min(Comparator.comparingInt(bn -> bn.getLoad()))
				.get();

    }


    public synchronized BlueNodeEntry reverseLookupBnBasedOnRn(String hostname) {
        Optional<RedNodeEntry> orn = list.stream()
				.flatMap(bn -> bn.getRedNodes().stream())
				.filter(rn -> rn.getHostname().equals(hostname))
				.findFirst();
        if (orn.isPresent()) {
        	return orn.get().getParentBlueNode();
		}
        return null;
    }
    
    public synchronized BlueNodeEntry reverseLookupBnBasedOnRnVaddr(String vAddress) {
		Optional<RedNodeEntry> orn = list.stream()
				.flatMap(bn -> bn.getRedNodes().stream())
				.filter(rn -> rn.getVaddress().equals(vAddress))
				.findFirst();
		if (orn.isPresent()) {
			return orn.get().getParentBlueNode();
		}
		return null;
    }
    
    public synchronized List<String> getLeasedRedNodeHostnameList() {
    	return list.stream()
				.flatMap(bn -> bn.getRedNodes().stream())
				.map(rn -> rn.getHostname())
				.collect(Collectors.toList());
    }
    
    public synchronized Boolean checkOnlineByName(String name) {
    	return getOptionalBlueNodeEntryByHn(name).isPresent();
    }

	public synchronized Boolean checkOnlineByAddrPort(String phaddress, int port) {
		return getOptionalBlueNodeEntryByPhAddrPort(phaddress,port).isPresent();
	}

	public synchronized Stream<RedNodeEntry> getAllRedNodesStream() {
		return list.stream().flatMap(bn -> bn.getRedNodes().stream());
	}

	public synchronized Optional<RedNodeEntry> checkOptionalOnlineRnByHn(String hostname) {
		return getAllRedNodesStream()
				.filter(rn -> rn.getHostname().equals(hostname))
				.findFirst();
	}

	public synchronized Optional<RedNodeEntry> checkOptionalOnlineRnByVaddr(String vaddress) {
		return getAllRedNodesStream()
				.filter(rn -> rn.getVaddress().equals(vaddress))
				.findFirst();
	}

	public synchronized boolean checkOnlineRnByHn(String hostname) {
    	return checkOptionalOnlineRnByHn(hostname).isPresent();
    }

	public synchronized boolean checkOnlineRnByVaddr(String vaddress) {
    	return checkOptionalOnlineRnByVaddr(vaddress).isPresent();
	}

	/**
	 *  WARNING!!! Physical address is not expected to be unique, only the combination (phaddress, port) is!!!
	 * 	as from one ip may come multiple hosts with a different port
	 *  therefore this method returns all the instances with the same phaddress
	 *
	 * @param Phaddress
	 * @return
	 */
    public synchronized List<BlueNodeEntry> getBlueNodeEntriesByPhAddr(String Phaddress) {
    	return list.stream()
				.filter(bn -> bn.getPhaddress().equals(Phaddress))
				.collect(Collectors.toList());

    }
    
    public synchronized int getSize() {
        return list.size();
    }

    public synchronized void lease(String name, PublicKey pub, String phAddress, int port) throws Exception {     
    	if (
				!name.isEmpty() && 
				name.length() <= App.MAX_STR_LEN_SMALL_SIZE &&
				!phAddress.isEmpty() && 
				phAddress.length() <= App.MAX_STR_ADDR_LEN &&
				port > 0 && port <= App.MAX_ALLOWED_PORT_NUM
    		) {
	    	
    		if (this.bncap == 0 || this.bncap > list.size()) {
    			if (!getOptionalBlueNodeEntryByHn(name).isPresent() && !getOptionalBlueNodeEntryByPhAddrPort(phAddress, port).isPresent()) {
					BlueNodeEntry bn = new BlueNodeEntry(name, pub, phAddress, port);
					list.add(bn);
					AppLogger.getLogger().consolePrint(pre + " LEASED " + bn);
					notifyGUI();
				} else {
					throw new Exception(pre + "Found a duplicate bn! "+name);
				}
	    	} else {
	    		throw new Exception(pre + "Maximum Blue Node upper limit reached.");
	    	}
    	} else {
    		throw new Exception(pre + "Bad input data.");
    	}
    }
    
    public synchronized void leaseRednode(String bluenodeName, String hostname, String vAddress) throws Exception {
    	Optional<BlueNodeEntry> obn = getOptionalBlueNodeEntryByHn(bluenodeName);
    	if (!obn.isPresent()) {
    		throw new Exception("Attempted to lease over a non existing bluenode.");
    	}

    	if (checkOnlineRnByHn(hostname)) {
			throw new Exception("Attempted to lease a non unique rednode entry.");
		}
		if (checkOnlineRnByVaddr(vAddress)) {
			throw new Exception("Attempted to lease a non unique rednode entry.");
		}

		obn.get().getRedNodes().lease(hostname, vAddress);
    }

	public synchronized void release(BlueNodeEntry tobereleased) throws Exception {
        list.remove(tobereleased);
		AppLogger.getLogger().consolePrint(pre +" RELEASED ENTRY of "+tobereleased);
		notifyGUI();
	}

    public synchronized void release(String name) throws Exception {
        Optional<BlueNodeEntry> obn = getOptionalBlueNodeEntryByHn(name);
        if (!obn.isPresent()) {
			throw new Exception("NO BLUENODE ENTRY FOR " + name + " IN TABLE");
		}

        list.remove(obn.get());
		AppLogger.getLogger().consolePrint(pre +name+" RELEASED ENTRY");
		notifyGUI();
    }
    
    public synchronized void releaseRednode(String hostname) throws Exception {
        Optional<RedNodeEntry> orn = checkOptionalOnlineRnByHn(hostname);
		if(orn.isPresent()) {
			RedNodeEntry rn = orn.get();
			rn.getParentBlueNode().getRedNodes().release(rn);
		} else {
			throw new Exception("NO REDNODE ENTRY FOR " + hostname + " IN TABLE");
		}
    }
    
    public synchronized void rebuildTableViaAuthClient() {
    	list.stream().forEach(bn -> {
			 try {
				 BlueNodeClient cl = new BlueNodeClient(bn);
				 if (cl.checkBnOnline()) {
					 System.out.println(pre+"Fetching RNs from BN "+bn.getName());
					 bn.updateTimestamp();
					 cl = new BlueNodeClient(bn);
					 List<RedNodeEntry> rns = cl.getRedNodes();
					 bn.getRedNodes().clearAndRebuildList(rns);
				 } else {
					 release(bn);
				 }
			 } catch (Exception e) {
				AppLogger.getLogger().consolePrint(pre+"network error when getting rednodes");
				bn.getRedNodes().clearList();
			 }
    	});

    	System.out.println(pre+" BN Table rebuilt");
    	notifyGUI();    	
    }
    
    public synchronized void sendKillSigsAndClearTable() {
    	list.stream().forEach(bn -> {
			try {
				BlueNodeClient cl = new BlueNodeClient(bn);
				cl.sendkillsig();
			} catch (Exception e) {

			}
		});

    	list.clear();
    	System.out.println(pre+" BN Table cleared");
    	notifyGUI(); 
    }

    //these will build the objects required for gui appearance
    public synchronized String[][] buildStringInstanceObject() {
    	String obj[][] = null;
    	return list.stream()
				.map(element -> new String[] {element.getName(), element.getPhaddress(), ""+element.getPort(), ""+element.getLoad(), element.getTimestamp().toString()})
    		.collect(Collectors.toList()).toArray(obj);

    }
    
    public synchronized String[][] buildRednodeStringInstanceObject() {        
    	String obj[][] = null;
    	return getAllRedNodesStream()
				.map(e -> new String[]{e.getHostname(), e.getVaddress(), e.getParentBlueNode().getName(), e.getTimestamp().toString()} )
    			.collect(Collectors.toList()).toArray(obj);
    }
    
    private void notifyGUI () {
    	if (MainWindow.isInstance()) {
    		MainWindow.getInstance().updateBlueNodeTable();
		}
    }
}
