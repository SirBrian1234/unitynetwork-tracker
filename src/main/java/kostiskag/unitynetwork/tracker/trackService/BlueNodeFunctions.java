package kostiskag.unitynetwork.tracker.trackService;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.logging.Level;
import java.util.logging.Logger;
import kostiskag.unitynetwork.tracker.App;
import kostiskag.unitynetwork.tracker.database.Database;
import kostiskag.unitynetwork.tracker.database.Queries;
import kostiskag.unitynetwork.tracker.functions.HashFunctions;
import kostiskag.unitynetwork.tracker.functions.SocketFunctions;
import kostiskag.unitynetwork.tracker.functions.VAddressFunctions;
import kostiskag.unitynetwork.tracker.runData.BlueNodeEntry;

/**
 *
 * @author kostis
 * 
 *         Bluenode queries:
 *
 *         LEASE BN 
 *         LEASE RN [HOSTNAME] 
 *         RELEASE BN 
 *         RELEASE RN [HOSTNAME] 
 *         GETPH 
 *         CHECKRN [HOSTNAME]
 *         CHECKRNA [VADDRESS]
 *         
 */
public class BlueNodeFunctions {

	/*
	 * lease a bluenode on the network
	 */
	public static void BlueLease(String bluenodeHostname, String givenPort, PrintWriter writer, Socket socket) {

		String data = null;
		Queries q = null;
		ResultSet getResults = null;

		try {
			q = new Queries();
			getResults = q.selectNameFromBluenodes();
			
			boolean found = false;
			while (getResults.next() && !found) {
				if (getResults.getString("name").equals(bluenodeHostname)) {
					found = true;
					String address = socket.getInetAddress().getHostAddress();
					int port = Integer.parseInt(givenPort);
					if (!App.BNtable.checkOnlineByHn(bluenodeHostname)) {
						// normal connect for a non associated BN
						App.BNtable.leaseBn(bluenodeHostname, address, port, new Time(System.currentTimeMillis()));
						data = "LEASED " + address;
					}
				}
			}
			if (!found) {
				data = "LEASE_FAILED";
			}
			q.closeQueries();
		} catch (SQLException ex) {
			Logger.getLogger(TrackService.class.getName()).log(Level.SEVERE, null, ex);
			data = "SYSTEM_ERROR";
			try {
				q.closeQueries();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		SocketFunctions.sendFinalData(data, writer);
	}

	/*
	 * lease a rednode on the network over a bluenode
	 */
	public static void RedLease(String bluenodeName, String givenHostname, String username, String password,
			PrintWriter writer) {
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
					getResults = q.selectHostnameFromHostnamesWithUserid(userauth);
	
					if (getResults == null) {
						data = "SYSTEM_ERROR";
					} else {
						while (getResults.next() && !found) {
							String hostname = getResults.getString("hostname");
							if (hostname.equals(givenHostname)) {
								found = true;
								if (!App.BNtable.checkOnlineRnByHn(hostname)) {
									//the id from hostnames is the hostname's virtual address
									int vAddress = getResults.getInt("id");
									int inuserid = getResults.getInt("userid");
									if (userauth == inuserid) {
										bn.rednodes.lease(hostname, VAddressFunctions.numberTo10ipAddr("" + vAddress), new Time(System.currentTimeMillis()));
										data = "LEASED " + vAddress;									
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
					SocketFunctions.sendFinalData(data, writer);
					q.closeQueries();
				} catch (SQLException ex) {
					Logger.getLogger(BlueNodeFunctions.class.getName()).log(Level.SEVERE, null, ex);
					SocketFunctions.sendFinalData("SYSTEM_ERROR", writer);
					try {
						q.closeQueries();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			} else {
				SocketFunctions.sendFinalData("AUTH_FAILED", writer);
			}
		} else {
			SocketFunctions.sendFinalData("AUTH_FAILED", writer);
		}
	}

	/*
	 * releases a bluenode from the network
	 */
	public static void BlueRel(String hostname, PrintWriter writer) {
		String data = null;
		if (App.BNtable.checkOnlineByHn(hostname)) {
			App.BNtable.releaseBnByHn(hostname);			
			data = "RELEASED";
		} else {
			data = "RELEASE_FAILED";
		}
		SocketFunctions.sendFinalData(data, writer);
	}

	/*
	 * releases a rednode from a bluenode
	 */
	public static void RedRel(String bluenodeName, String hostname, PrintWriter writer) {
		String data = null;
		boolean found = false;

		BlueNodeEntry bn = App.BNtable.getBlueNodeEntryByHn(bluenodeName);
		if (bn != null) {
			if (bn.rednodes.checkOnlineByHn(hostname)) {
				bn.rednodes.releaseByHn(hostname);
				data = "RELEASED";
			} else {
				data = "NOT_AUTHORIZED";
			}
		} else {
			data = "NOT_AUTHORIZED";
		}
		
		SocketFunctions.sendFinalData(data, writer);
	}

	/* 
	 * provides the physical address and port of a known bluenode
	 */
	public static void GetPh(String BNTargetHostname, PrintWriter writer) {
		String data;
		BlueNodeEntry bn = App.BNtable.getBlueNodeEntryByHn(BNTargetHostname);
		if (bn != null) {			
			data = bn.getPhaddress()+" "+ bn.getPort();
		} else {
			data = "NOT_FOUND";
		}
		SocketFunctions.sendFinalData(data, writer);
	}

	/*
	 *  checks whether a RN is ONLINE and from which BN is connected
	 */
	public static void CheckRn(String hostname, PrintWriter writer) {
		String data;
		BlueNodeEntry bn = App.BNtable.reverseLookupBnBasedOnRn(hostname);
		if (bn != null) {
			data = "ONLINE " + bn.getName();
		} else {
			data = "OFFLINE";
		}
		SocketFunctions.sendFinalData(data, writer);
	}
	
	/*
	 * Retrieves a bluenode hostname based on a given vaddress
	 */
	public static void CheckRnAddr(String vaddress, PrintWriter writer) {
		Queries q = null;
		String data = null;
		String hostname = null;
		int vaddressNum = VAddressFunctions._10ipAddrToNumber(vaddress);
		
		try {
			q = new Queries();
			ResultSet r = q.selectAllFromHostnamesWhereAddress(vaddressNum);
			while(r.next()) {
				hostname = r.getString("hostname");			
			}
			
			if (hostname != null) {
				data = "ONLINE " + App.BNtable.checkOnlineRnByHn(hostname);
			} else {
				data = "OFFLINE";
			}
			SocketFunctions.sendFinalData(data, writer);
		} catch (SQLException e) {
			e.printStackTrace();
		}			
	}
	
	/*
	 * authorizes a bluenode to join the network
	 */
	public static int authBluenode(String BlueNodeHostname) {
		Queries q = null;
		ResultSet getResults;
		try {
			q = new Queries();
			getResults = q.selectNameFromBluenodes();

			if (getResults == null) {
				return -2;
			}

			while (getResults.next()) {
				if (getResults.getString("name").equals(BlueNodeHostname)) {
					if (App.BNtable.checkOnlineByHn(BlueNodeHostname)) {
						q.closeQueries();
						return 1;
					} else {
						q.closeQueries();
						return 0;
					}
				}
			}
			q.closeQueries();
			return -1;

		} catch (SQLException e) {
			e.printStackTrace();
			try {
				q.closeQueries();
			} catch (SQLException e1) {
				e1.printStackTrace();				
			}
			return -2;
		}
	}

	/*
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
					data = getResults.getString("username")+App.salt+HashFunctions.MD5(getResults.getString("password"));
					data = HashFunctions.MD5(data);
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
}
