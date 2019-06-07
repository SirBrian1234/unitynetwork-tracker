package org.kostiskag.unitynetwork.tracker.rundata.address;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Objects;

import org.kostiskag.unitynetwork.tracker.App;

/**
 *
 * @author Konstantinos Kagiampakis
 */
public class VirtualAddress extends NetworkAddress{

    // this is the total number of allowed hosts on network
    // from 10.255.255.254 in byte[] (aka: the last permitted host) is substracted
    // the number of system reserved addresses to find the number
    public static final int MAX_INT_CAPACITY = VirtualAddress.byteTo10IpAddrNumber(new byte[] {(byte) 0x0a, (byte) 0xff, (byte) 0xff, (byte) 0xfe})
            - App.SYSTEM_RESERVED_ADDRESS_NUMBER;

    private final int asInt;

    private VirtualAddress(String asString, byte[] asByte, InetAddress asInet, int asInt) throws UnknownHostException {
        super(asString,asByte,asInet);
        this.asInt = asInt;
    }

    public static VirtualAddress valueOf(String vAddress) throws UnknownHostException {
        if (vAddress.length() > App.MAX_STR_ADDR_LEN || vAddress.length() < App.MIN_STR_ADDR_LEN) {
            throw new UnknownHostException("the given ip is invalid");
        }
        InetAddress asInet = NetworkAddress.networkAddressToInetAddress(vAddress);
        byte[] asByte = asInet.getAddress();
        if (asByte[0] != (byte)10) {
            throw new UnknownHostException("the given ip does not start with a 10.* network part");
        }
        int asInt = VirtualAddress.byteTo10IpAddrNumber(asByte);

        return new VirtualAddress(vAddress, asByte, asInet, asInt);
    }

    public static VirtualAddress valueOf(int numericVAddress) throws UnknownHostException {
        if (numericVAddress <= 0 || numericVAddress > MAX_INT_CAPACITY) {
            throw new UnknownHostException("the given ip number is invalid");
        }
        byte[] asByte = VirtualAddress.numberTo10IpByteAddress(numericVAddress);
        if (asByte[0] != (byte)10) {
            throw new UnknownHostException("the given ip does not start with a 10.* network part");
        }
        InetAddress asInet = VirtualAddress._10IpByteToInetAddress(asByte);
        String asString = asInet.getHostAddress();

        return new VirtualAddress(asString, asByte, asInet, numericVAddress);
    }

    public int asInt() {
        return asInt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VirtualAddress)) return false;
        VirtualAddress vaddr = (VirtualAddress) o;
        return asInt == vaddr.asInt
                && super.equals(o);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(asInt);
        result = 31 * result + super.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() +
                ": asString: '" + super.asString() + '\'' +
                ", asInt: " + asInt +
                ", asByte: " + Arrays.toString(super.asByte()) +
                ", asInet: " + super.asInet() +
                '}';
    }

    public static byte[] numberTo10IpByteAddress(int numAddr) {
        byte[] networkpart = new byte[]{0x0a};
        int hostnum = numAddr + App.SYSTEM_RESERVED_ADDRESS_NUMBER;

        byte[] hostpart = new byte[]{
                (byte) ((hostnum) >>> 16),
                (byte) ((hostnum) >>> 8),
                (byte) (hostnum)};

        byte[] address = new byte[4];
        System.arraycopy(networkpart, 0, address, 0, networkpart.length);
        System.arraycopy(hostpart, 0, address, 1, hostpart.length);
        return address;
    }

    public static InetAddress _10IpByteToInetAddress(byte[] byteAddress) throws UnknownHostException {
        return InetAddress.getByAddress(byteAddress);
    }

    public static String numberTo10ipAddr(int numAddr) throws UnknownHostException {
        byte[] address = VirtualAddress.numberTo10IpByteAddress(numAddr);
        return VirtualAddress._10IpByteToInetAddress(address).getHostAddress();
    }

    //---------------reverse process----------------
    public static int byteTo10IpAddrNumber(byte[] address) {
        byte[] hostpart = new byte[3];
        System.arraycopy(address, 1, hostpart, 0, 3);
        int hostnum = 0;
        for (int i = 0; i < hostpart.length; i++) {
            hostnum = (hostnum << 8) + (hostpart[i] & 0xff);
        }
        hostnum = hostnum - App.SYSTEM_RESERVED_ADDRESS_NUMBER;
        return hostnum;
    }

    public static int _10IpAddrToNumber(String vaddress) throws UnknownHostException {
        return byteTo10IpAddrNumber(NetworkAddress.networkAddressToInetAddress(vaddress).getAddress());
    }
}
