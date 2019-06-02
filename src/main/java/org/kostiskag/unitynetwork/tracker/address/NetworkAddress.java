package org.kostiskag.unitynetwork.tracker.address;

import java.util.Arrays;
import java.util.Objects;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.kostiskag.unitynetwork.tracker.App;

class NetworkAddress {

    private final String asString;
    private final byte[] asByte;
    private final InetAddress asInet;

    public NetworkAddress(String address) throws UnknownHostException {
        //also should throw a regex here!
        if (address.length() > App.MAX_STR_ADDR_LEN || address.length() < App.MIN_STR_ADDR_LEN) {
            throw new UnknownHostException("the given ip is invalid");
        }
        this.asString = address;
        this.asInet = NetworkAddress.networkAddressToInetAddress(asString);
        this.asByte = asInet.getAddress();
    }

    public NetworkAddress(InetAddress asInet) {
        this.asInet = asInet;
        this.asString = asInet.getHostAddress();
        this.asByte = asInet.getAddress();
    }

    public NetworkAddress(String asString, byte[] asByte, InetAddress asInet) {
        this.asString = asString;
        this.asByte = asByte;
        this.asInet = asInet;
    }

    public String asString() {
        return asString;
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
        if (!(o instanceof NetworkAddress)) return false;
        NetworkAddress addr = (NetworkAddress) o;
        return Objects.equals(asString, addr.asString) &&
                Arrays.equals(asByte, addr.asByte) &&
                Objects.equals(asInet, addr.asInet);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(asString, asInet);
        result = 31 * result + Arrays.hashCode(asByte);
        return result;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() +
                ": asString: '" + asString + '\'' +
                ", asByte: " + Arrays.toString(asByte) +
                ", asInet: " + asInet +
                '}';
    }

    public static InetAddress networkAddressToInetAddress(String address) throws UnknownHostException {
        return InetAddress.getByName(address);
    }

}
