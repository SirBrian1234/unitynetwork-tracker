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
    	/*
		Iterator<BlueNodeEntry> iterator = list.listIterator();
        while (iterator.hasNext()) {
        	BlueNodeEntry element = iterator.next();
            if (Hostname.equals(element.getName())) {
                return element;
            }
        }
        AppLogger.getLogger().consolePrint(pre + "NO ENTRY FOR " + Hostname + " IN TABLE");
        return null;
        */

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
    	/*
		Iterator<BlueNodeEntry> iterator = list.listIterator();
        while (iterator.hasNext()) {
        	BlueNodeEntry bn = iterator.next();
        	if (bn.getPhaddress().equals(Phaddress) && bn.getPort() == port) {
        		return bn;
        	}        	
        }
        return null;
        */

    	Optional<BlueNodeEntry> obn = getOptionalBlueNodeEntryByPhAddrPort(Phaddress,port);
    	if (obn.isPresent()) {
    		return obn.get();
		}
    	return null;
    }
    
    public synchronized BlueNodeEntry getBlueNodeEntryByLowestLoad() {
    	/*
		BlueNodeEntry retrieved = null;
    	if (list.size() > 0) {
    		int min = list.getFirst().getLoad();
    		retrieved = list.getFirst();
    		
	    	Iterator<BlueNodeEntry> iterator = list.listIterator();
	        while (iterator.hasNext()) {
	        	BlueNodeEntry element = iterator.next();
	            if (element.getLoad() <= min) {
	                min = element.getLoad();
	                retrieved = element;
	            }
	        }	      
        } 
        return retrieved;
    	*/

    	return list.stream()
				.min(Comparator.comparingInt(bn -> bn.getLoad()))
				.get();

    }


    public synchronized BlueNodeEntry reverseLookupBnBasedOnRn(String hostname) {
    	/*
		Iterator<BlueNodeEntry> iterator = list.listIterator();
        while (iterator.hasNext()) {
        	BlueNodeEntry bn = iterator.next();
        	if (bn.rednodes.checkOnlineByHn(hostname)) {
        		return bn;
        	}        	
        }        
    	return null;
    	*/

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
    	/*
		Iterator<BlueNodeEntry> iterator = list.listIterator();
        while (iterator.hasNext()) {
        	BlueNodeEntry bn = iterator.next();
        	if (bn.rednodes.checkOnlineByVaddress(vAddress)) {
        		return bn;
        	}        	
        }        
    	return null;
    	*/

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
    	/*
		LinkedList<String> fetched = new LinkedList<>();
    	Iterator<BlueNodeEntry> iterator = list.listIterator();
        while (iterator.hasNext()) {
        	BlueNodeEntry bn = iterator.next();
        	fetched.addAll(bn.getRedNodes().getLeasedRedNodeHostnameList());
        }
        return fetched;
        */

    	return list.stream()
				.flatMap(bn -> bn.getRedNodes().stream())
				.map(rn -> rn.getHostname())
				.collect(Collectors.toList());
    }
    
    public synchronized Boolean checkOnlineByName(String name) {
    	/*
		Iterator<BlueNodeEntry> iterator = list.listIterator();
    	while (iterator.hasNext()) {
        	BlueNodeEntry element = iterator.next();
            if (name.equals(element.getName())) {  
                return true;
            }
        }
        return false;
        */

    	return getOptionalBlueNodeEntryByHn(name).isPresent();
    }

	public synchronized Boolean checkOnlineByAddrPort(String phaddress, int port) {
    	/*
		Iterator<BlueNodeEntry> iterator = list.listIterator();
    	while (iterator.hasNext()) {
        	BlueNodeEntry element = iterator.next();
            if (name.equals(element.getName())) {
                return true;
            }
        }
        return false;
        */

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
    	/*
		LinkedList<String> fetched = new LinkedList<>();
    	Iterator<BlueNodeEntry> iterator = list.listIterator();
        while (iterator.hasNext()) {
        	BlueNodeEntry bn = iterator.next();
        	if (bn.rednodes.checkOnlineByHn(hostname)) {
        		return true;
        	}        	
        }
        return false;
        */

    	return checkOptionalOnlineRnByHn(hostname).isPresent();
    }

	public synchronized boolean checkOnlineRnByVaddr(String vaddress) {
    	return checkOptionalOnlineRnByVaddr(vaddress).isPresent();
	}

    //WARNING!!! Physical address is not expected to be unique
    //therefore the table may return all the instances with the same phaddress
    public synchronized List<BlueNodeEntry> getBlueNodeEntriesByPhAddr(String Phaddress) {
    	/*
		LinkedList<BlueNodeEntry> found = new LinkedList<>();
    	Iterator<BlueNodeEntry> iterator = list.listIterator();
        while (iterator.hasNext()) {
        	BlueNodeEntry element = iterator.next();
            if (Phaddress.equals(element.getPhaddress())) {
                found.add(element);
            }
        }
        AppLogger.getLogger().consolePrint(pre + "NO ENTRY FOR " + Phaddress + " IN TABLE");
        return found;
        */

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
    			/*
		    	Iterator<BlueNodeEntry> iterator = list.listIterator();
		        while (iterator.hasNext()) {
		        	BlueNodeEntry element = iterator.next();
		        	if (element.getName().equals(name) || (element.getPhaddress().equals(phAddress) && element.getPort() == port)) {
		        		throw new Exception("Attempted to insert a non unique bluenode entry.");
		        	}
		        }
		    	*/

    			if (!getOptionalBlueNodeEntryByHn(name).isPresent() && !getOptionalBlueNodeEntryByPhAddrPort(phAddress, port).isPresent()) {
					BlueNodeEntry bn = new BlueNodeEntry(name, pub, phAddress, port);
					list.add(bn);
					AppLogger.getLogger().consolePrint(pre + " LEASED " + bn);
					notifyGUI();
				} else {
					throw new Exception(pre + "Found a duplicate bn!");
				}
	    	} else {
	    		throw new Exception(pre + "Maximum Blue Node upper limit reached.");
	    	}
    	} else {
    		throw new Exception(pre + "Bad input data.");
    	}
    }
    
    public synchronized void leaseRednode(String bluenodeName, String hostname, String vAddress) throws Exception {
    	/*
		BlueNodeEntry element = null;
    	boolean found = false;
    	Iterator<BlueNodeEntry> iterator = list.listIterator();
    	while (iterator.hasNext()) {
        	element = iterator.next();
            if (bluenodeName.equals(element.getName())) {
                found = true;
            	break;                
            }
        }
        */

    	Optional<BlueNodeEntry> obn = getOptionalBlueNodeEntryByHn(bluenodeName);
    	if (!obn.isPresent()) {
    		throw new Exception("Attempted to lease over a non existing bluenode.");
    	}

    	/*
    	iterator = list.listIterator();
        while (iterator.hasNext()) {
        	BlueNodeEntry bn = iterator.next();
        	if (bn.rednodes.checkOnlineByHn(hostname)) {
        		throw new Exception("Attempted to lease a non unique rednode entry.");
        	} else if (bn.rednodes.checkOnlineByVaddress(vAddress)) {
        		throw new Exception("Attempted to lease a non unique rednode entry.");
        	}
        }
        */

		if (checkOnlineRnByHn(hostname)) {
			throw new Exception("Attempted to lease a non unique rednode entry.");
		}
		if (checkOnlineRnByVaddr(vAddress)) {
			throw new Exception("Attempted to lease a non unique rednode entry.");
		}

		obn.get().getRedNodes().lease(hostname, vAddress);
    }

	public synchronized void release(BlueNodeEntry tobereleased) throws Exception {
        /*
		Iterator<BlueNodeEntry> iterator = list.listIterator();
        while (iterator.hasNext()) {
        	BlueNodeEntry element = iterator.next();
            if (name.equals(element.getName())) {
                    iterator.remove();

                    return;
            }
        }
        */

		list.remove(tobereleased);
		AppLogger.getLogger().consolePrint(pre +" RELEASED ENTRY of "+tobereleased);
		notifyGUI();
	}

    public synchronized void release(String name) throws Exception {
        /*
		Iterator<BlueNodeEntry> iterator = list.listIterator();
        while (iterator.hasNext()) {
        	BlueNodeEntry element = iterator.next();
            if (name.equals(element.getName())) {                
                    iterator.remove();              

                    return;
            }
        }
        */

        Optional<BlueNodeEntry> obn = getOptionalBlueNodeEntryByHn(name);
        if (!obn.isPresent()) {
			throw new Exception("NO BLUENODE ENTRY FOR " + name + " IN TABLE");
		}

        list.remove(obn.get());
		AppLogger.getLogger().consolePrint(pre +name+" RELEASED ENTRY");
		notifyGUI();
    }
    
    public synchronized void releaseRednode(String hostname) throws Exception {
        /*
		Iterator<BlueNodeEntry> iterator = list.listIterator();
        while (iterator.hasNext()) {
        	BlueNodeEntry element = iterator.next();
            if (element.rednodes.checkOnlineByHn(hostname)) {                
                    element.rednodes.release(hostname);              
                    return;
            }
        }
        */

        Optional<RedNodeEntry> orn = checkOptionalOnlineRnByHn(hostname);
		if(orn.isPresent()) {
			RedNodeEntry rn = orn.get();
			rn.getParentBlueNode().getRedNodes().release(rn);
		} else {
			throw new Exception("NO REDNODE ENTRY FOR " + hostname + " IN TABLE");
		}
    }
    
    public synchronized void rebuildTableViaAuthClient() {
    	/*
		Iterator<BlueNodeEntry> iterator = list.listIterator();
    	while (iterator.hasNext()) {
        	BlueNodeEntry element = iterator.next();            
            try {
            	BlueNodeClient cl = new BlueNodeClient(element);
				if (cl.checkBnOnline()) {
					System.out.println(pre+"Fetching RNs from BN "+element.getName());
				    element.updateTimestamp();
				    cl = new BlueNodeClient(element);
				    List<RedNodeEntry> rns = cl.getRedNodes();
				    List<RedNodeEntry> in = element.rednodes.getList();
				    List<RedNodeEntry> valid = new LinkedList<RedNodeEntry>();
				    Iterator<RedNodeEntry> rnsIt = rns.iterator();
				    
				    while (rnsIt.hasNext()) {
				    	RedNodeEntry outE = rnsIt.next();				    	
			    		Iterator<RedNodeEntry> inIt = in.iterator();
			    		while(inIt.hasNext()) {
			    			RedNodeEntry inE = inIt.next();	
				    		if (inE.getHostname().equals(outE.getHostname())) {
				    			valid.add(inE);			    			
				    		}				    	
			    		}
				    }				    
				    element.getRedNodes().clearAndRebuildList(valid);
				} else {                
					iterator.remove();               
				}
			} catch (Exception e) {
				iterator.remove();  
			}
        }
        */

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
    	/*
		Iterator<BlueNodeEntry> iterator = list.listIterator();
    	while (iterator.hasNext()) {
        	BlueNodeEntry element = iterator.next();            
        	try {
        		BlueNodeClient cl = new BlueNodeClient(element);
				cl.sendkillsig();
			} catch (Exception e) {
				
			}
            iterator.remove();                           
        }*/

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

    //this will build the obj for gui
    //gui can handle itself
    public synchronized String[][] buildStringInstanceObject() {        
    	String obj[][] = null;

    	/*
    	int i = 0;
        Iterator<BlueNodeEntry> iterator = list.listIterator();
    	while (iterator.hasNext()) {
    		BlueNodeEntry element = iterator.next();
            obj[i] = new String[]{element.getName(), element.getPhaddress(), ""+element.getPort(), ""+element.getLoad(), element.getTimestamp().toString()};
            i++;
        }
		*/

		return list.stream()
				.map(element -> new String[] {element.getName(), element.getPhaddress(), ""+element.getPort(), ""+element.getLoad(), element.getTimestamp().toString()})
    		.collect(Collectors.toList()).toArray(obj);

    }
    
    public synchronized String[][] buildRednodeStringInstanceObject() {        
    	/*
		LinkedList<RedNodeEntry> li = new LinkedList<>();
        Iterator<BlueNodeEntry> iterator = list.listIterator();
    	while (iterator.hasNext()) {
    		BlueNodeEntry element = iterator.next();
    		Iterator<RedNodeEntry> redIterator = element.getRedNodes().getList().listIterator();
    		while (redIterator.hasNext()) {
        		RedNodeEntry redElement = redIterator.next();
        		li.add(redElement);
            }
        }
        */

		String obj[][] = null;
    	return getAllRedNodesStream()
				.map(e -> new String[]{e.getHostname(), e.getVaddress(), e.getParentBlueNode().getName(), e.getTimestamp().toString()} )
    			.collect(Collectors.toList()).toArray(obj);

    	/*
    	Iterator<RedNodeEntry> redIterator = li.listIterator();
    	int i=0;
    	while (redIterator.hasNext()) {
    		RedNodeEntry e = redIterator.next();
    		obj[i] = new String[]{e.getHostname(), e.getVaddress(), e.getParentBlueNode().getName(), e.getTimestamp().toString()};            
    		i++;
    	}

    	return obj;
    	*/

    }
    
    private void notifyGUI () {
    	if (MainWindow.isInstance()) {
    		MainWindow.getInstance().updateBlueNodeTable();
		}
    }
}
