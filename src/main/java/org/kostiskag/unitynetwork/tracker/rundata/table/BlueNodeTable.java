package org.kostiskag.unitynetwork.tracker.rundata.table;

import java.security.GeneralSecurityException;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.util.concurrent.locks.Lock;
import java.security.PublicKey;
import java.security.NoSuchAlgorithmException;
import java.net.UnknownHostException;
import java.io.IOException;

import org.kostiskag.unitynetwork.tracker.AppLogger;
import org.kostiskag.unitynetwork.tracker.gui.MainWindow;
import org.kostiskag.unitynetwork.tracker.rundata.entry.BlueNodeEntry;
import org.kostiskag.unitynetwork.tracker.rundata.entry.RedNodeEntry;
import org.kostiskag.unitynetwork.tracker.service.sonar.BlueNodeClient;

/**
 * A bluenode table holds the entries representing all the connected bluenodes on the network.
 * Each BlueNode entry holds a RedNode list with all its connected
 * rednodes.
 *
 * THIS IS A SINGLETON!!!!!
 * to use it:
 * BlueNodeTable.getInstance();
 *
 * @author Konstantinos Kagiampakis
 */
public class BlueNodeTable extends NodeTable<BlueNodeEntry> {
	private static final String pre = "^BNTABLE ";
	private static BlueNodeTable BN_TABLE_INSTANCE;
	private final int bncap;

	private BlueNodeTable() {
		this(0);
	}

	private BlueNodeTable(int bncap) {
        super();
		this.bncap = bncap;
    }

	public static BlueNodeTable newInstance() {
		return BlueNodeTable.newInstance(0);
	}

	public static BlueNodeTable newInstance(int capacity) {
		if (BN_TABLE_INSTANCE == null) {
			BN_TABLE_INSTANCE = new BlueNodeTable(capacity);
		}
		return BN_TABLE_INSTANCE;
	}

	public static BlueNodeTable getInstance() {
		return BN_TABLE_INSTANCE;
	}

	public Optional<BlueNodeEntry> getOptionalBlueNodeEntryByPhAddrPort(Lock lock, String phAddress, int port) throws InterruptedException {
		validateLock(lock);
		return list.stream()
				.filter(bn -> bn.getAddress().asString().equals(phAddress))
				.filter(bn -> bn.getPort() == port)
				.findFirst();
	}

	//the combination of ph address and port is unique
    public BlueNodeEntry getBlueNodeEntryByPhAddrPort(Lock lock, String phAddress, int port) throws InterruptedException {
    	Optional<BlueNodeEntry> obn = getOptionalBlueNodeEntryByPhAddrPort(lock, phAddress, port);
    	return obn.orElse(null);
    }
    
    public BlueNodeEntry getBlueNodeEntryByLowestLoad(Lock lock) throws InterruptedException {
		validateLock(lock);
    	return list.stream()
				.min(Comparator.comparingInt(bn -> bn.getLoad()))
				.get();

    }

    public BlueNodeEntry reverseLookupBnBasedOnRn(Lock lock, String hostname) throws InterruptedException {
		validateLock(lock);
        Optional<RedNodeEntry> orn = list.stream()
				.flatMap(bn -> bn.getRedNodes().stream())
				.filter(rn -> rn.getHostname().equals(hostname))
				.findFirst();
		return orn.map(RedNodeEntry::getParentBlueNode).orElse(null);
	}
    
    public BlueNodeEntry reverseLookupBnBasedOnRnVaddr(Lock lock, String vAddress) throws InterruptedException {
		validateLock(lock);
		Optional<RedNodeEntry> orn = list.stream()
				.flatMap(bn -> bn.getRedNodes().stream())
				.filter(rn -> rn.getAddress().asString().equals(vAddress))
				.findFirst();
		return orn.map(RedNodeEntry::getParentBlueNode).orElse(null);
	}
    
    public List<String> getLeasedRedNodeHostnameList(Lock lock) throws InterruptedException {
		validateLock(lock);
    	return list.stream()
				.flatMap(bn -> bn.getRedNodes().stream())
				.map(rn -> rn.getHostname())
				.collect(Collectors.toList());
    }

	public Boolean isOnlineByAddrPort(Lock lock, String phAddress, int port) throws InterruptedException {
		return getOptionalBlueNodeEntryByPhAddrPort(lock, phAddress, port).isPresent();
	}

	public Stream<RedNodeEntry> getAllRedNodesStream(Lock lock) throws InterruptedException {
		validateLock(lock);
		return list.stream().flatMap(bn -> bn.getRedNodes().stream());
	}

	public Optional<RedNodeEntry> isOptionalOnlineRnByHn(Lock lock, String hostname) throws InterruptedException {
		return getAllRedNodesStream(lock)
				.filter(rn -> rn.getHostname().equals(hostname))
				.findFirst();
	}

	public Optional<RedNodeEntry> isOptionalOnlineRnByVaddr(Lock lock, String vaddress) throws InterruptedException {
		return getAllRedNodesStream(lock)
				.filter(rn -> rn.getAddress().asString().equals(vaddress))
				.findFirst();
	}

	public boolean isOnlineRnByHostname(Lock lock, String hostname) throws InterruptedException {
    	return isOptionalOnlineRnByHn(lock, hostname).isPresent();
    }

	public boolean isOnlineRnByVaddress(Lock lock, String vaddress) throws InterruptedException {
    	return isOptionalOnlineRnByVaddr(lock, vaddress).isPresent();
	}

	/**
	 *  WARNING!!! Physical address is not expected to be unique, only the combination (phaddress, port) is!!!
	 * 	as from one ip may come multiple hosts with a different port
	 *  therefore this method returns all the instances with the same phaddress
	 *
	 * @param Phaddress
	 * @return
	 */
    public List<BlueNodeEntry> getBlueNodeEntriesByPhAddr(Lock lock, String Phaddress) throws InterruptedException {
    	validateLock(lock);
    	return list.stream()
				.filter(bn -> bn.getAddress().equals(Phaddress))
				.collect(Collectors.toList());

    }

    public void lease(Lock lock, String name, PublicKey pub, String phAddress, int port) throws IllegalAccessException, InterruptedException, UnknownHostException {
    	//this validation has to be moved inside the BlueNodeEntry constructor
		validateLock(lock);
		if (this.bncap == 0 || this.bncap > list.size()) {
			if (!getOptionalNodeEntry(lock, name).isPresent() && !getOptionalBlueNodeEntryByPhAddrPort(lock, phAddress, port).isPresent()) {
				BlueNodeEntry bn = new BlueNodeEntry(name, pub, phAddress, port);
				list.add(bn);
				AppLogger.getLogger().consolePrint(pre + " LEASED " + bn);
				notifyGUI();
			} else {
				throw new IllegalAccessException(pre + "Found a duplicate bn! "+name);
			}
		} else {
			throw new IllegalAccessException(pre + "Maximum Blue Node upper limit reached.");
		}
	}
    
    public void leaseRednode(Lock lock, String bluenodeName, String hostname, String vAddress) throws InterruptedException, IllegalAccessException, UnknownHostException {
    	Optional<BlueNodeEntry> obn = getOptionalNodeEntry(lock, bluenodeName);
    	if (!obn.isPresent()) {
    		throw new IllegalAccessException("Attempted to lease over a non existing bluenode.");
    	}

    	if (isOnlineRnByHostname(lock, hostname)) {
			throw new IllegalAccessException("Attempted to lease a non unique rednode entry.");
		}
		if (isOnlineRnByVaddress(lock, vAddress)) {
			throw new IllegalAccessException("Attempted to lease a non unique rednode entry.");
		}

		RedNodeTable rns = obn.get().getRedNodes();
		try {
			Lock rl = rns.aquireLock();
			obn.get().getRedNodes().lease(rl, hostname, vAddress);
		} finally {
			rns.releaseLock();
		}
    }

	public void release(Lock lock, BlueNodeEntry tobereleased) throws InterruptedException, IllegalAccessException {
        validateLock(lock);
    	boolean valid = list.remove(tobereleased);
    	if (!valid) {
			throw new IllegalAccessException("NO BLUENODE ENTRY FOR " + tobereleased + " IN TABLE");
		}
		AppLogger.getLogger().consolePrint(pre +" RELEASED ENTRY of "+tobereleased);
		notifyGUI();
	}

    public void release(Lock lock, String name) throws InterruptedException, IllegalAccessException  {
        Optional<BlueNodeEntry> obn = getOptionalNodeEntry(lock, name);
        if (!obn.isPresent()) {
			throw new IllegalAccessException("NO BLUENODE ENTRY FOR " + name + " IN TABLE");
		}

        list.remove(obn.get());
		AppLogger.getLogger().consolePrint(pre +name+" RELEASED ENTRY");
		notifyGUI();
    }
    
    public void releaseRednode(Lock lock, String hostname) throws IllegalAccessException, InterruptedException {
        Optional<RedNodeEntry> orn = isOptionalOnlineRnByHn(lock, hostname);
		if(orn.isPresent()) {
			RedNodeEntry rn = orn.get();
			RedNodeTable t = rn.getParentBlueNode().getRedNodes();
			try {
				Lock rnlock = t.aquireLock();
				t.release(rnlock, rn);
			} finally {
				t.releaseLock();
			}
		} else {
			throw new IllegalAccessException("NO REDNODE ENTRY FOR " + hostname + " IN TABLE");
		}
    }
    
    public void rebuildTableViaAuthClient(Lock lock) throws InterruptedException {
    	validateLock(lock);
    	list.stream().forEach(bn -> {
			boolean validConn = false;

			//is bn online?
			if (bn.getClient().testBnOnline()) {
				validConn = true;
			} else {
				//can we recover connection?
				try {
					new BlueNodeClient(bn);
					validConn = true;
				} catch (GeneralSecurityException | IOException e) {
					AppLogger.getLogger().consolePrint(pre+"network error when connecting to bn");
					try {
						release(lock, bn);
					} catch (IllegalAccessException | InterruptedException ex) {
						AppLogger.getLogger().consolePrint(pre+"attempted to release bn "+bn);
					}
					validConn = false;
				}
			}

			if (validConn) {
				boolean validList = true;
				System.out.println(pre+"Fetching RNs from BN "+bn.getHostname());
				List<RedNodeEntry> rns = null;

				try {
					rns = bn.getClient().getRedNodes();
				} catch (GeneralSecurityException | IOException e) {
					AppLogger.getLogger().consolePrint(pre+"network error when connecting to bn");
					try {
						release(lock, bn);
					} catch (IllegalAccessException | InterruptedException ex) {
						AppLogger.getLogger().consolePrint(pre+"attempted to release bn "+bn);
					}
					validList = false;
				}

				if(validList) {
					bn.getRedNodes().clearAndRebuildList(rns);
					bn.updateTimestamp();
				}
			}
    	});

    	System.out.println(pre+" BN Table rebuilt");
    	notifyGUI();    	
    }
    
    public void sendKillSigsAndClearTable(Lock lock) throws InterruptedException {
    	validateLock(lock);
    	list.stream().forEach(bn -> {
			boolean validConn = false;

			if (bn.getClient().testBnOnline()) {
				//is bn online?
				validConn = true;
			} else {
				//can we recover connection?
				try {
					new BlueNodeClient(bn);
					validConn = true;
				} catch (GeneralSecurityException | IOException e) {
					AppLogger.getLogger().consolePrint(pre+"network error when connecting to bn");
					validConn = false;
				}
			}

			if (validConn) {
				bn.getClient().sendkillsig();
			}
		});

    	list.clear();
    	System.out.println(pre+" BN Table cleared");
    	notifyGUI(); 
    }

    //these will build the objects required for gui appearance
    public String[][] buildStringInstanceObject(Lock lock) throws InterruptedException {
    	validateLock(lock);
    	String obj[][] = new String[list.size()][];
    	return list.stream()
				.map(element -> new String[] {element.getHostname(), element.getAddress().asString(), ""+element.getPort(), ""+element.getLoad(), element.getTimestamp().toString()})
    		.collect(Collectors.toList()).toArray(obj);

    }
    
    public String[][] buildRednodeStringInstanceObject(Lock lock) throws InterruptedException {
    	String obj[][] = new String[(int) getAllRedNodesStream(lock).count()][];
    	return getAllRedNodesStream(lock)
				.map(e -> new String[]{e.getHostname(), e.getAddress().asString(), e.getParentBlueNode().getHostname(), e.getTimestamp().toString()} )
    			.collect(Collectors.toList()).toArray(obj);
    }
    
    private void notifyGUI () {
    	if (MainWindow.isInstance()) {
    		MainWindow.getInstance().updateBlueNodeTable();
		}
    }
}
