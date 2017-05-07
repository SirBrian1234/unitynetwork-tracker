package kostiskag.unitynetwork.tracker.functions;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import kostiskag.unitynetwork.tracker.App;

/**
 *
 * @author Konstantinos Kagiampakis
 */
public class VAddressFunctions {
    
	/**
	 * If a database's firs id is one there is no need to sum/substarct one to get ahead of the network address
	 * 
	 * @param vaddress
	 * @return
	 */
    public static String numberTo10ipAddr(int num_addr) {
        byte[] networkpart = new byte[]{0x0a};
        int hostnum = num_addr + App.systemReservedAddressNumber;

        byte[] hostpart = new byte[]{
            (byte) ((hostnum) >>> 16),
            (byte) ((hostnum) >>> 8),
            (byte) (hostnum)};

        byte[] address = new byte[4];
        System.arraycopy(networkpart, 0, address, 0, networkpart.length);
        System.arraycopy(hostpart, 0, address, 1, hostpart.length);
        try {
            return InetAddress.getByAddress(address).getHostAddress();
        } catch (UnknownHostException ex) {
            Logger.getLogger(VAddressFunctions.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public static int _10ipAddrToNumber(String vaddress) {
        InetAddress addr = null;
        try {
            addr = InetAddress.getByName(vaddress);
        } catch (UnknownHostException ex) {
            Logger.getLogger(VAddressFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }
        byte[] address = addr.getAddress();
        byte[] hostpart = new byte[3];
        System.arraycopy(address, 1, hostpart, 0, 3);
        int hostnum = 0;
        for (int i = 0; i < hostpart.length; i++) {
            hostnum = (hostnum << 8) + (hostpart[i] & 0xff);
        }
        hostnum = hostnum - App.systemReservedAddressNumber;
        return hostnum;
    }
}
