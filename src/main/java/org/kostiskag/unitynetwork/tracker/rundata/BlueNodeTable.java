package org.kostiskag.unitynetwork.tracker.rundata;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.security.PublicKey;
import java.security.NoSuchAlgorithmException;
import java.net.UnknownHostException;
import java.io.IOException;

import org.kostiskag.unitynetwork.tracker.App;
import org.kostiskag.unitynetwork.tracker.AppLogger;
import org.kostiskag.unitynetwork.tracker.gui.MainWindow;
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
public class BlueNodeTable {
	private static final String pre = "^BNTABLE ";
	private static final int TIMEOUT_SECONDS = 5;
	private static BlueNodeTable BN_TABLE_INSTANCE;
	private final int bncap;
	private final List<BlueNodeEntry> list;
	private final Lock orb = new ReentrantLock(true);

	private BlueNodeTable() {
		this(0);
	}

	private BlueNodeTable(int bncap) {
        this.bncap = bncap;
        list = new ArrayList<BlueNodeEntry>();
        AppLogger.getLogger().consolePrint(pre + "INITIALIZED ");
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

    /*
		WE USED to have synchronized methods for table access but this
		approach was found to be ineffective as one calling thread was
		frequently in the need to call several methods before
		making a complete and meaningful action on the table like ex.
		1. check if bn named "pakis" exists
		2. if yes get me its instance

		with sync methods there was no guarantee that after method 1 another thread wouldn't
		have deleted "pakis" before 2 was called!

		SO NOW, the caller gets the orb, does his action by calling one or several methods
		gives the orb back when he completes his action!

		This method also lets the caller use Optionals and streams as by their nature there was no
		guarantee for which point in time the caller would decide to consume one!

		To improve matters further and to be sure the caller owns the orb before calling
		anything, he has to also pass it as argument in the calling method!

		every caller should do :
		try {
        	Lock l = aquireLock();
        	findSmth(lock, args);
        	findAnotherSmth(lock, args);
        } catch interupted {
			log("unbelivable!!!");
        } finally {
        	readLock.unlock();
    	}

    	The inner method on its turn has to validate the lock to ensure it was not called from
    	a caller without having a lock

    	public int getSmth(Lock lock, String name) throws InterruptedException {
			validateLock(lock);
			do stuff...
			return ...
		}
	*/
	public Lock aquireLock() throws InterruptedException {
		orb.tryLock(TIMEOUT_SECONDS, TimeUnit.SECONDS);
		return orb;
	}


	/**
	 * To be used inside a finally block
	 */
	public void releaseLock() {
		orb.unlock();
	}

	/**
	 * To be called by all inner methods
	 * Ensures the lock is called before attempting to call a method
	 *
	 * @param lock
	 * @throws InterruptedException
	 */
	private void validateLock(Lock lock) throws InterruptedException {
		if (lock != orb) {
			throw new InterruptedException("the given lock is not the BNtable's orb!");
		}
	}
	//////////////////////////////////////////////////////////////////////////////////////////////////

	public Optional<BlueNodeEntry> getOptionalBlueNodeEntryByHn(Lock lock, String name) throws InterruptedException {
		validateLock(lock);
		return list.stream()
			.filter(bn -> bn.getName().equals(name))
			.findFirst();
	}

	public BlueNodeEntry getBlueNodeEntryByHn(Lock lock, String name) throws InterruptedException {
		Optional<BlueNodeEntry> bn = getOptionalBlueNodeEntryByHn(lock, name);
    	if (bn.isPresent()) {
			return bn.get();
		}
    	return null;
    }

	public Optional<BlueNodeEntry> getOptionalBlueNodeEntryByPhAddrPort(Lock lock, String phAddress, int port) throws InterruptedException {
		validateLock(lock);
		return list.stream()
				.filter(bn -> bn.getPhAddress().asString().equals(phAddress))
				.filter(bn -> bn.getPort() == port)
				.findFirst();
	}
		//the combination of ph address and port is unique
    public BlueNodeEntry getBlueNodeEntryByPhAddrPort(Lock lock, String phAddress, int port) throws InterruptedException {
    	Optional<BlueNodeEntry> obn = getOptionalBlueNodeEntryByPhAddrPort(lock, phAddress, port);
    	if (obn.isPresent()) {
    		return obn.get();
		}
    	return null;
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
        if (orn.isPresent()) {
        	return orn.get().getParentBlueNode();
		}
        return null;
    }
    
    public BlueNodeEntry reverseLookupBnBasedOnRnVaddr(Lock lock, String vAddress) throws InterruptedException {
		validateLock(lock);
		Optional<RedNodeEntry> orn = list.stream()
				.flatMap(bn -> bn.getRedNodes().stream())
				.filter(rn -> rn.getVaddress().asString().equals(vAddress))
				.findFirst();
		if (orn.isPresent()) {
			return orn.get().getParentBlueNode();
		}
		return null;
    }
    
    public List<String> getLeasedRedNodeHostnameList(Lock lock) throws InterruptedException {
		validateLock(lock);
    	return list.stream()
				.flatMap(bn -> bn.getRedNodes().stream())
				.map(rn -> rn.getHostname())
				.collect(Collectors.toList());
    }
    
    public Boolean checkOnlineByName(Lock lock, String name) throws InterruptedException {
    	return getOptionalBlueNodeEntryByHn(lock, name).isPresent();
    }

	public Boolean checkOnlineByAddrPort(Lock lock, String phAddress, int port) throws InterruptedException {
		return getOptionalBlueNodeEntryByPhAddrPort(lock, phAddress,port).isPresent();
	}

	public Stream<RedNodeEntry> getAllRedNodesStream(Lock lock) throws InterruptedException {
		validateLock(lock);
		return list.stream().flatMap(bn -> bn.getRedNodes().stream());
	}

	public Optional<RedNodeEntry> checkOptionalOnlineRnByHn(Lock lock, String hostname) throws InterruptedException {
		return getAllRedNodesStream(lock)
				.filter(rn -> rn.getHostname().equals(hostname))
				.findFirst();
	}

	public Optional<RedNodeEntry> checkOptionalOnlineRnByVaddr(Lock lock, String vaddress) throws InterruptedException {
		return getAllRedNodesStream(lock)
				.filter(rn -> rn.getVaddress().asString().equals(vaddress))
				.findFirst();
	}

	public boolean checkOnlineRnByHn(Lock lock, String hostname) throws InterruptedException {
    	return checkOptionalOnlineRnByHn(lock, hostname).isPresent();
    }

	public boolean checkOnlineRnByVaddr(Lock lock, String vaddress) throws InterruptedException {
    	return checkOptionalOnlineRnByVaddr(lock, vaddress).isPresent();
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
				.filter(bn -> bn.getPhAddress().equals(Phaddress))
				.collect(Collectors.toList());

    }
    
    public int getSize(Lock lock) throws InterruptedException {
    	validateLock(lock);
        return list.size();
    }

    public void lease(Lock lock, String name, PublicKey pub, String phAddress, int port) throws IllegalAccessException, InterruptedException, UnknownHostException {
    	//this validation has to be moved inside the BlueNodeEntry constructor
		validateLock(lock);
    	if (!name.isEmpty() &&
				name.length() <= App.MAX_STR_LEN_SMALL_SIZE &&
				!phAddress.isEmpty() && 
				phAddress.length() <= App.MAX_STR_ADDR_LEN &&
				port > 0 && port <= App.MAX_ALLOWED_PORT_NUM
		) {
	    	if (this.bncap == 0 || this.bncap > list.size()) {
    			if (!getOptionalBlueNodeEntryByHn(lock, name).isPresent() && !getOptionalBlueNodeEntryByPhAddrPort(lock, phAddress, port).isPresent()) {
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
    	} else {
    		throw new IllegalAccessException(pre + "Bad input data.");
    	}
    }
    
    public synchronized void leaseRednode(Lock lock, String bluenodeName, String hostname, String vAddress) throws InterruptedException, IllegalAccessException, RedNodeTableException, UnknownHostException {
    	Optional<BlueNodeEntry> obn = getOptionalBlueNodeEntryByHn(lock, bluenodeName);
    	if (!obn.isPresent()) {
    		throw new IllegalAccessException("Attempted to lease over a non existing bluenode.");
    	}

    	if (checkOnlineRnByHn(lock, hostname)) {
			throw new IllegalAccessException("Attempted to lease a non unique rednode entry.");
		}
		if (checkOnlineRnByVaddr(lock, vAddress)) {
			throw new IllegalAccessException("Attempted to lease a non unique rednode entry.");
		}

		obn.get().getRedNodes().lease(hostname, vAddress);
    }

	public void release(Lock lock, BlueNodeEntry tobereleased) throws InterruptedException {
        validateLock(lock);
    	list.remove(tobereleased);
		AppLogger.getLogger().consolePrint(pre +" RELEASED ENTRY of "+tobereleased);
		notifyGUI();
	}

    public void release(Lock lock, String name) throws InterruptedException, IllegalAccessException  {
        Optional<BlueNodeEntry> obn = getOptionalBlueNodeEntryByHn(lock, name);
        if (!obn.isPresent()) {
			throw new IllegalAccessException("NO BLUENODE ENTRY FOR " + name + " IN TABLE");
		}

        list.remove(obn.get());
		AppLogger.getLogger().consolePrint(pre +name+" RELEASED ENTRY");
		notifyGUI();
    }
    
    public void releaseRednode(Lock lock, String hostname) throws IllegalAccessException, RedNodeTableException, InterruptedException {
        Optional<RedNodeEntry> orn = checkOptionalOnlineRnByHn(lock, hostname);
		if(orn.isPresent()) {
			RedNodeEntry rn = orn.get();
			rn.getParentBlueNode().getRedNodes().release(rn);
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
				} catch (NoSuchAlgorithmException | IOException e) {
					AppLogger.getLogger().consolePrint(pre+"network error when connecting to bn");
					try {
						release(lock, bn);
					} catch (InterruptedException ex) {
						AppLogger.getLogger().consolePrint(pre+"attempted to release bn "+bn);
					}
					validConn = false;
				}
			}

			if (validConn) {
				boolean validList = true;
				System.out.println(pre+"Fetching RNs from BN "+bn.getName());
				List<RedNodeEntry> rns = null;

				try {
					rns = bn.getClient().getRedNodes();
				} catch (IOException e) {
					AppLogger.getLogger().consolePrint(pre+"network error when connecting to bn");
					try {
						release(lock, bn);
					} catch (InterruptedException ex) {
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
				} catch (NoSuchAlgorithmException | IOException e) {
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
    	String obj[][] = null;
    	return list.stream()
				.map(element -> new String[] {element.getName(), element.getPhAddress().asString(), ""+element.getPort(), ""+element.getLoad(), element.getTimestamp().toString()})
    		.collect(Collectors.toList()).toArray(obj);

    }
    
    public String[][] buildRednodeStringInstanceObject(Lock lock) throws InterruptedException {
    	String obj[][] = null;
    	return getAllRedNodesStream(lock)
				.map(e -> new String[]{e.getHostname(), e.getVaddress().asString(), e.getParentBlueNode().getName(), e.getTimestamp().toString()} )
    			.collect(Collectors.toList()).toArray(obj);
    }
    
    private void notifyGUI () {
    	if (MainWindow.isInstance()) {
    		MainWindow.getInstance().updateBlueNodeTable();
		}
    }
}
