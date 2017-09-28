import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

//util functions
public class Utils {
	public static int packetSize=256;//modify this value to modify packetsize throughout the 
	//entire project
	
	//decode the message from the given byte array and check if hashes match
	//check that the nonce matches with the given nonce
    public static String validateMsg(byte[] data, int nonce) throws NoSuchAlgorithmException{
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
    		size = Arrays.copyOfRange(data, 4, 8);
    		int n=(size[0]<<24)&0xff000000|
     		       (size[1]<<16)&0x00ff0000|
     		       (size[2]<< 8)&0x0000ff00|
     		       (size[3]<< 0)&0x000000ff;
    		if(n!=nonce){
    			return "wrong nonce";
    		}
    		//byte[] hash = Arrays.copyOfRange(data, 4, length-28);
	    	byte[] msg = Arrays.copyOfRange(data, 8, length-28);//the bare msg
	    	byte[] hash1 = Arrays.copyOfRange(data, length-28, length+4);//only the hash
	    	//System.out.println("fetched hash: " + getByteStream(hash1));
	    	String value = new String(msg,0,msg.length);
	    	MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash2 = digest.digest(Arrays.copyOfRange(data, 4, length-28));
			if(Arrays.equals(hash1,hash2)){
				return value;
			}else{
				System.out.println("hash1: ");
				for(int i=0; i<hash1.length; i++){
					System.out.print(hash1[i]);
				}
				 System.out.println("hash2: ");
				 for(int i=0; i<hash2.length; i++){
						System.out.print(hash2[i]);
					} 
				return "this string has been tampered with: " + value;
			}
		}
    }
    
    //only used for debugging, prints the byte array
    public static String getByteStream(byte[] data){
    	String s="";
    	if(data==null){
    		return s;
    	}
    	for(int i=0; i<data.length; i++){
    		s+=data[i];
    	}
    	return s;
    }
    
    //read data without checking integrity, used to read the encrypted stream and for the handshake
    public static byte[] read(byte[] data){
    	byte[] size = Arrays.copyOfRange(data,0,4);
    	int length=(size[0]<<24)&0xff000000|
    		       (size[1]<<16)&0x00ff0000|
    		       (size[2]<< 8)&0x0000ff00|
    		       (size[3]<< 0)&0x000000ff;
    	
    	byte[] msg = Arrays.copyOfRange(data, 4, length+4);
    	return msg;
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
    
    //add a nonce to the byte stream
    public static byte[] addNonce(String s,int nonce){
    	byte[] data = s.getBytes(StandardCharsets.UTF_8);
    	byte[] values = new byte[data.length+4];
    	byte[] length = new byte[]{
    			(byte)(nonce >>> 24),
    			(byte)(nonce >>> 16),
    			(byte)(nonce >>> 8),
    			(byte)(nonce),
    	};
    	
    	System.arraycopy(length,0,values,0,length.length);
    	System.arraycopy(data, 0, values, 4, data.length);
    	return values;
    }
    
    
    //add a hash to the given string
	public static byte[] addHash(byte[] data) throws NoSuchAlgorithmException{
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] encodedhash = digest.digest(data);
	//	System.out.println("added hash: " + getByteStream(encodedhash));
		//byte[] msg=s.getBytes();
		byte[] returnvalue = new byte[encodedhash.length+data.length];
		System.arraycopy(data, 0, returnvalue, 0, data.length);
		System.arraycopy(encodedhash,0,returnvalue,data.length,encodedhash.length);
		return returnvalue;
	}
	
	//helper function for encrypt/decrypt
	private static SecretKeySpec setKey(byte[] key) throws NoSuchAlgorithmException{
		MessageDigest sha = null;
        sha = MessageDigest.getInstance("SHA-1");
        key = sha.digest(key);
        key = Arrays.copyOf(key, 16);
        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
        return secretKey;
	}
	
	//encrypt a byte sequence
	public static byte[] encrypt(byte[] bytesToEncrypt, byte[] secret){
		try
        {
			SecretKeySpec secretKey=setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            //Base64.getEncoder().encode(src)
            return Base64.getEncoder().encode(cipher.doFinal(bytesToEncrypt));
        }
        catch (Exception e)
        {
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
	}
	
	//decrypt a byte sequence with a given key
	public static byte[] decrypt(byte[] bytesToDecrypt, byte[] secret){
        try{
        //	String strToDecrypt = new String(bytesToDecrypt,"UTF-8");
        	SecretKeySpec secretKey=setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
	        cipher.init(Cipher.DECRYPT_MODE, secretKey);
	    
	        return cipher.doFinal(Base64.getDecoder().decode(bytesToDecrypt));
	    }
	    catch (Exception e)
	    {
	        System.out.println("Error while decrypting: " + e.toString());
	    }
	    return null;
	}
}
