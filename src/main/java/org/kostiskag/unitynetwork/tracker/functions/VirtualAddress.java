package org.kostiskag.unitynetwork.tracker.functions;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Objects;

import org.kostiskag.unitynetwork.tracker.App;

/**
 *
 * @author Konstantinos Kagiampakis
 */
public class VirtualAddress {

    //this is "10.256.256.255" in byte[]
    public static final int MAX_INT_CAPACITY = VirtualAddress.byteTo10IpAddrNumber(new byte[]{0x00, 0x00, 0x00, 0x00})
            - App.SYSTEM_RESERVED_ADDRESS_NUMBER;

    private final String asString;
    private final int asInt;
    private final byte[] asByte;
    private final InetAddress asInet;

    public VirtualAddress(String asString) throws UnknownHostException {
        if (asString.length() < 0 ||
                asString.length() > App.MAX_STR_ADDR_LEN) {
            throw new UnknownHostException("the given ip is invalid");
        }
        this.asString = asString;
        this.asInet = VirtualAddress.vAddressToInetAddress(asString);
        this.asByte = asInet.getAddress();
        if (asByte[0] != (byte)10) {
            throw new UnknownHostException("the given ip does not start with a 10.* network part");
        }
        this.asInt = VirtualAddress.byteTo10IpAddrNumber(asByte);
    }

    public VirtualAddress(int asInt) throws UnknownHostException {
        if (asInt <= 0) {
            throw new UnknownHostException("the given ip number is invalid");
        }
        this.asInt = asInt;
        this.asByte = VirtualAddress.numberTo10IpByteAddress(asInt);
        if (asByte[0] != (byte)10) {
            throw new UnknownHostException("the given ip does not start with a 10.* network part");
        }
        this.asInet = VirtualAddress._10IpByteToInetAddress(asByte);
        this.asString = asInet.getHostAddress();
    }

    public String asString() {
        return asString;
    }

    public int asInt() {
        return asInt;
    }

    public byte[] asByte() {
        return asByte;
    }

    public InetAddress asInet() {
        return asInet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VirtualAddress)) return false;
        VirtualAddress vaddr = (VirtualAddress) o;
        return asInt == vaddr.asInt &&
                Objects.equals(asString, vaddr.asString) &&
                Arrays.equals(asByte, vaddr.asByte) &&
                Objects.equals(asInet, vaddr.asInet);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(asString, asInt, asInet);
        result = 31 * result + Arrays.hashCode(asByte);
        return result;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() +
                ": asString: '" + asString + '\'' +
                ", asInt: " + asInt +
                ", asByte: " + Arrays.toString(asByte) +
                ", asInet: " + asInet +
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
    public static InetAddress vAddressToInetAddress(String vAddress) throws UnknownHostException {
        return InetAddress.getByName(vAddress);
    }

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
        return byteTo10IpAddrNumber(vAddressToInetAddress(vaddress).getAddress());
    }
}
