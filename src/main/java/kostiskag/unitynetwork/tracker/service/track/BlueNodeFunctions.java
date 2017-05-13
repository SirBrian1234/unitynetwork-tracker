package kostiskag.unitynetwork.tracker.service.track;

import java.io.DataOutputStream;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;

import kostiskag.unitynetwork.tracker.App;
import kostiskag.unitynetwork.tracker.database.Queries;
import kostiskag.unitynetwork.tracker.functions.CryptoMethods;
import kostiskag.unitynetwork.tracker.functions.HashFunctions;
import kostiskag.unitynetwork.tracker.functions.SocketFunctions;
import kostiskag.unitynetwork.tracker.functions.VAddressFunctions;
import kostiskag.unitynetwork.tracker.runData.BlueNodeEntry;

/**
 *  Bluenode queries:
 *
 *  LEASE BN 
 *  LEASE RN [HOSTNAME] [USERNAME] [PASSWORD]
 *  RELEASE BN 
 *  RELEASE RN [HOSTNAME] 
 *  GETPH [BLUENODE_NAME]
 *  CHECKRN [HOSTNAME]
 *  CHECKRNA [VADDRESS]
 *  
 *  @author Konstantinos Kagiampakis
 */
public class BlueNodeFunctions {

	/**
	 * lease a bluenode on the network
	 * @throws Exception 
	 */
	public static void BlueLease(String bluenodeHostname, String givenPort, DataOutputStream writer, Socket socket) throws Exception {

		String data = null;
		Queries q = null;
		ResultSet getResults = null;

		try {
			q = new Queries();
			getResults = q.selectNameFromBluenodes();
			
			boolean found = false;
			while (getResults.next() && !found) {
				if (getResults.getString("name").equals(bluenodeHostname)) {
					String address = socket.getInetAddress().getHostAddress();
					int port = Integer.parseInt(givenPort);
					if (!App.BNtable.checkOnlineByName(bluenodeHostname)) {
						// normal connect for a non associated BN
						try {
							App.BNtable.lease(bluenodeHostname, address, port);
							data = "LEASED " + address;
							found = true;
							break;
						} catch (Exception e) {
							e.printStackTrace();
							data = "LEASE_FAILED";
							break;
						}						
					} 
				}
			}
			if (!found) {
				data = "LEASE_FAILED";
			}
			q.closeQueries();
		} catch (SQLException ex) {
			data = "SYSTEM_ERROR";
			try {
				q.closeQueries();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		SocketFunctions.sendStringlData(data, writer);
	}

	/**
	 * lease a rednode on the network over a bluenode
	 * on a successful lease a full ip is returned 
	 * @throws Exception 
	 */
	public static void RedLease(String bluenodeName, String givenHostname, String username, String password,
			DataOutputStream writer) throws Exception {
		int userauth = checkUser(password);

		BlueNodeEntry bn = App.BNtable.getBlueNodeEntryByHn(bluenodeName);
		if (bn != null) {				
			String data = null;
			Queries q = null;
			ResultSet getResults = null;
	
			if (userauth > 0) {
				boolean found = false;
				try {
					q = new Queries();
					getResults = q.selectAllFromHostnamesWhereUserid(userauth);
	
					if (getResults == null) {
						data = "SYSTEM_ERROR";
					} else {
						while (getResults.next() && !found) {
							String hostname = getResults.getString("hostname");
							if (hostname.equals(givenHostname)) {
								found = true;
								if (!App.BNtable.checkOnlineRnByHn(hostname)) {
									//the id from hostnames is the hostname's virtual address
									int num_addr = getResults.getInt("address");
									int inuserid = getResults.getInt("userid");
									if (userauth == inuserid) {
										try {
											String vaddress = VAddressFunctions.numberTo10ipAddr(num_addr);
											bn.rednodes.lease(hostname, vaddress);
											data = "LEASED " + vaddress;
										} catch (Exception e) {
											e.printStackTrace();
											data = "ALLREADY_LEASED";											
										}																			
									} else {
										//a user tried to lease another user's hostname
										data = "USER_HOSTNAME_MISSMATCH";
									}
								} else {
									data = "ALLREADY_LEASED";
								}
							}
						}
	
						if (!found) {
							data = "LEASE_FAILED";
						}
					}
					q.closeQueries();
					SocketFunctions.sendStringlData(data, writer);					
				} catch (SQLException ex) {					
					try {
						q.closeQueries();
					} catch (SQLException e) {
						e.printStackTrace();
					}
					SocketFunctions.sendStringlData("SYSTEM_ERROR", writer);
				}
			} else {
				SocketFunctions.sendStringlData("AUTH_FAILED", writer);
			}
		} else {
			SocketFunctions.sendStringlData("AUTH_FAILED", writer);
		}
	}

	/**
	 * releases a bluenode from the network
	 * @throws Exception 
	 */
	public static void BlueRel(String hostname, DataOutputStream writer) throws Exception {
		String data = null;
		if (App.BNtable.checkOnlineByName(hostname)) {
			try {
				App.BNtable.release(hostname);
			} catch (Exception e) {
				e.printStackTrace();
				data = "RELEASE_FAILED";
			}			
			data = "RELEASED";
		} else {
			data = "RELEASE_FAILED";
		}
		SocketFunctions.sendStringlData(data, writer);
	}

	/**
	 * releases a rednode from a bluenode
	 * @throws Exception 
	 */
	public static void RedRel(String bluenodeName, String hostname, DataOutputStream writer) throws Exception {
		String data = null;
		boolean found = false;

		BlueNodeEntry bn = App.BNtable.getBlueNodeEntryByHn(bluenodeName);
		if (bn != null) {
			if (bn.rednodes.checkOnlineByHn(hostname)) {
				bn.rednodes.release(hostname);
				data = "RELEASED";
			} else {
				data = "NOT_AUTHORIZED";
			}
		} else {
			data = "NOT_AUTHORIZED";
		}
		
		SocketFunctions.sendStringlData(data, writer);
	}

	/** 
	 * provides the physical address and port of a known bluenode
	 * @throws Exception 
	 */
	public static void GetPh(String BNTargetHostname, DataOutputStream writer) throws Exception {
		String data;
		BlueNodeEntry bn = App.BNtable.getBlueNodeEntryByHn(BNTargetHostname);
		if (bn != null) {			
			data = bn.getPhaddress()+" "+ bn.getPort();
		} else {
			data = "OFFLINE";
		}
		SocketFunctions.sendStringlData(data, writer);
	}

	/**
	 *  checks whether a RN is ONLINE and from which BN is connected
	 * @throws Exception 
	 */
	public static void CheckRn(String hostname, DataOutputStream writer) throws Exception {
		String data;
		BlueNodeEntry bn = App.BNtable.reverseLookupBnBasedOnRn(hostname);
		if (bn != null) {
			data = "ONLINE " +bn.getName()+" "+bn.getPhaddress()+" "+bn.getPort();
		} else {
			data = "OFFLINE";
		}
		SocketFunctions.sendStringlData(data, writer);
	}
	
	/**
	 * checks whether a RN based on its virtual address is ONLINE and from which BN is connected
	 * @throws Exception 
	 */
	public static void CheckRnAddr(String vaddress, DataOutputStream writer) throws Exception {
		Queries q = null;
		String data = null;
		String hostname = null;
		
		BlueNodeEntry bn = App.BNtable.reverseLookupBnBasedOnRnVaddr(vaddress);
		if (bn!=null) {
			data = "ONLINE "+bn.getName()+" "+bn.getPhaddress()+" "+bn.getPort();
		} else {
			data = "OFFLINE";
		}						
		SocketFunctions.sendStringlData(data, writer);				
	}
	
	

	/**
	 * validates a network user to a bluenode 
	 */
	public static int checkUser(String outhash) {
		String data = null;
		Queries q = null;
		ResultSet getResults;

		try {
			q = new Queries();
			getResults = q.selectIdUsernamePasswordFromUsers();
		
			if (getResults == null) {
				return -1;
			}

			int i = 0;
			while (getResults.next()) {				
				try {
					data = HashFunctions.SHA256(App.SALT) +  HashFunctions.SHA256(getResults.getString("username")) + getResults.getString("password");
					data = HashFunctions.SHA256(data);
				} catch (Exception ex) {
					ex.printStackTrace();					
				}
				if (outhash.equals(data)) {
					return getResults.getInt("id");
				}
			}
			q.closeQueries();
			return 0;			
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				q.closeQueries();
			} catch (SQLException e1) {
				e1.printStackTrace();
				return -1;
			}
			return -1;
		}
	}

	public static void LookupByHn(String hostname, DataOutputStream writer) {
		Queries q = null;
		String vaddress = null;
		String retrievedHostname = null;
		try {
			q = new Queries();
			ResultSet r = q.selectAllFromHostnames();
			while (r.next()) {
				retrievedHostname = r.getString("hostname");
				if (retrievedHostname.equals(hostname)) {
					//found!!!
					int num_addr = r.getInt("address");
					vaddress = VAddressFunctions.numberTo10ipAddr(num_addr);
					
					q.closeQueries();
					try {
						SocketFunctions.sendStringlData(vaddress, writer);
					} catch (Exception e) {
						e.printStackTrace();
					}							
					return;		
				}
			}
			q.closeQueries();
			try {
				SocketFunctions.sendStringlData("NOT_FOUND", writer);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				q.closeQueries();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			try {
				SocketFunctions.sendStringlData("NOT_FOUND", writer);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}		
	}

	public static void LookupByAddr(String vaddress, DataOutputStream writer) {
		Queries q = null;
		String hostname = null;
		int addr_num  = VAddressFunctions._10ipAddrToNumber(vaddress);
		int retrieved_addr_num = -1;
		
		try {
			q = new Queries();
			ResultSet r = q.selectAllFromHostnames();
			while (r.next()) {
				retrieved_addr_num = r.getInt("address");
				if (retrieved_addr_num == addr_num) {
					//found!!!
					hostname = r.getString("hostname");
					
					q.closeQueries();
					try {
						SocketFunctions.sendStringlData(hostname, writer);
					} catch (Exception e) {
						e.printStackTrace();
					}							
					return;		
				}
			}
			q.closeQueries();
			try {
				SocketFunctions.sendStringlData("NOT_FOUND", writer);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				q.closeQueries();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			try {
				SocketFunctions.sendStringlData("NOT_FOUND", writer);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}		
	}

	public static void offerPublicKey(String blueNodeHostname, String ticket, String publicKey, DataOutputStream writer) {
		Queries q = null;
		try {
			q = new Queries();
			ResultSet r = q.selectAllFromBluenodesWhereName(blueNodeHostname);
			if (r.next()) {
				String storedKey = r.getString("public");
				String args[] = storedKey.split("\\s+");
				if (args[0].equals("NOT_SET") && args[1].equals(ticket)) {
					q.updateEntryBluenodesPublicWithName(blueNodeHostname, "KEY_SET"+" "+publicKey);
					try {
						SocketFunctions.sendStringlData("KEY_SET", writer);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else if (args[0].equals("KEY_SET")) {
					try {
						SocketFunctions.sendStringlData("KEY_IS_SET", writer);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			q.closeQueries();
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				q.closeQueries();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		try {
			SocketFunctions.sendStringlData("NOT_SET", writer);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void revokePublicKey(String blueNodeHostname, DataOutputStream writer) {
		String key = "NOT_SET "+CryptoMethods.generateQuestion();
		Queries q = null;
		try {
			q = new Queries();
			q.updateEntryBluenodesPublicWithName(blueNodeHostname, key);
			q.closeQueries();
			try {
				SocketFunctions.sendStringlData("KEY_REVOKED", writer);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				q.closeQueries();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		try {
			SocketFunctions.sendStringlData("NOT_SET", writer);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
