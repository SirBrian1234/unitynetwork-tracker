package kostiskag.unitynetwork.tracker.functions;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 
 * @author Konstantinos Kagiampakis
 */
public class HashFunctions { 
 
    public static String bytesToHexStr(byte[] data) { 
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) { 
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do { 
                if ((0 <= halfbyte) && (halfbyte <= 9)) 
                    buf.append((char) ('0' + halfbyte));
                else 
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while(two_halfs++ < 1);
        } 
        return buf.toString();
    } 
    
    public static byte[] hexStrToBytes(String hexStr) {
        int len = hexStr.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexStr.charAt(i), 16) << 4)
                                 + Character.digit(hexStr.charAt(i+1), 16));
        }
        return data;
    }
    
    public static int bytesToUnsignedInt(byte[] data) {
    	return new BigInteger(1, data).intValue(); 
    }
    
    public static byte[] UnsignedIntTo4Bytes(int num) {
    	byte[] bytes = null;
    	if (num >= 0 && num < Math.pow(2, 24)) {
    		bytes =  new byte[] { 
    		        (byte)(num >> 24),
    		        (byte)(num >> 16),
    		        (byte)(num >> 8),
    		        (byte)num };    		
    	}
    	return bytes;
    }
    
    public static byte[] UnsignedIntTo2Bytes(int num) {
    	byte[] bytes = null;
    	if (num >= 0 && num < Math.pow(2, 16)) {
    		bytes =  new byte[] { 
    		        (byte)(num >> 8),
    		        (byte)num };    		
    	}
    	return bytes;
    }
    
    public static byte[] UnsignedIntToByteArray(int num) {
    	byte[] bytes = null;
    	if (num >= 0 && num < Math.pow(2, 8)) {
    		bytes =  new byte[] {(byte)num };    		
    	}
    	return bytes;
    }
    
    public static byte UnsignedIntTo1Byte(int num) {
    	byte b = (byte) 0x00;
    	if (num >= 0 && num < Math.pow(2, 8)) {
    		b =  (byte)num ;    		
    	}
    	return b;
    }
    
    public static byte buildByteFromBits(String flags) {
    	flags = flags.replaceAll(" ", "");
    	byte b = (byte) 0x00;
    	if (flags.length() == 8) {
    		b = (byte) Integer.parseInt(flags, 2);
    	}
    	return b;
    }
 
    public static String MD5(String text) 
    throws NoSuchAlgorithmException, UnsupportedEncodingException  { 
        MessageDigest md;
        md = MessageDigest.getInstance("MD5");
        byte[] hash = new byte[32];
        md.update(text.getBytes("iso-8859-1"), 0, text.length());
        hash = md.digest();
        return bytesToHexStr(hash);
    }
    
    public static String SHA256(String text) 
    	    throws NoSuchAlgorithmException, UnsupportedEncodingException  { 
        MessageDigest md;
        md = MessageDigest.getInstance("SHA-256");
        byte[] hash = new byte[32];
        md.update(text.getBytes("iso-8859-1"), 0, text.length());
        hash = md.digest();
        return bytesToHexStr(hash);
    }
} 