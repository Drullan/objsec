import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

//util functions
public class Utils {
	public static int packetSize=256;//modify this value to modify packetsize throughout the 
	//entire project
	
	//decode the message from the given byte array and check if hashes match
    public static String checkHash(byte[] data) throws NoSuchAlgorithmException{
    	byte[] size = Arrays.copyOfRange(data,0,4);
    	int length=(size[0]<<24)&0xff000000|
    		       (size[1]<<16)&0x00ff0000|
    		       (size[2]<< 8)&0x0000ff00|
    		       (size[3]<< 0)&0x000000ff;
    	if(length>packetSize){
    		//do something
    		//might become a problem if the message is over multiple udp packets
    		return new String(data,0,data.length);
    	}else{
	    	byte[] msg = Arrays.copyOfRange(data, 4, length-28);
	    	byte[] hash1 = Arrays.copyOfRange(data, length-28, length+4);
	    	String value = new String(msg,0,msg.length);
	    	MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash2 = digest.digest(msg);
			if(Arrays.equals(hash1,hash2)){
				return value;
			}else{
				return "this string has been tampered with: " + value;
			}
		}
    }
    
    //add size to the given byte array
    public static byte[] addSize(byte[] data){
    	byte[] values = new byte[data.length+4];
    	byte[] length = new byte[]{
    			(byte)(data.length >>> 24),
    			(byte)(data.length >>> 16),
    			(byte)(data.length >>> 8),
    			(byte)(data.length),
    	};
    	
    	System.arraycopy(length,0,values,0,length.length);
    	System.arraycopy(data, 0, values, 4, data.length);
    	return values;
    }
    
    //add a hash to the given string
	public static byte[] addHash(String s) throws NoSuchAlgorithmException{
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] encodedhash = digest.digest(s.getBytes(StandardCharsets.UTF_8));
		byte[] msg=s.getBytes();
		byte[] returnvalue = new byte[encodedhash.length+msg.length];
		System.arraycopy(msg, 0, returnvalue, 0, msg.length);
		System.arraycopy(encodedhash,0,returnvalue,msg.length,encodedhash.length);
		return returnvalue;
	}
}
