import static javax.xml.bind.DatatypeConverter.printHexBinary;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.crypto.KeyAgreement;

//functions for the client
public class EchoClient {
	private DatagramSocket socket;
    private InetAddress address;
    private int port;
 
    public EchoClient(int serverport) throws SocketException, UnknownHostException {
        socket = new DatagramSocket();
        address = InetAddress.getByName("localhost");
        port = serverport;
    }
 
    public byte[] handshake() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException{
    	/*//start a handshake by sending hi
    	byte[] init = Utils.addSize("hi".getBytes());
    	
    	 socket.send(packet);*/
    	byte[] buf = new byte[Utils.packetSize]; 
    	DatagramPacket packet 
        = new DatagramPacket(buf, buf.length, address, port);
    	
    	//generate our keys
    	KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
	    kpg.initialize(256);
	    KeyPair kp = kpg.generateKeyPair();
	    byte[] ourPk = kp.getPublic().getEncoded();
    	
	    //send public key
    	byte[] sendPk = Utils.addSize(ourPk);
    	packet = new DatagramPacket(sendPk,sendPk.length,address,port);
    	socket.send(packet);
    	
    	//get the servers public key
    	byte[] recieve = new byte[Utils.packetSize];
    	packet = new DatagramPacket(recieve, recieve.length);
    	socket.receive(packet);
    	byte[] otherPk = Utils.read(packet.getData());    	
    	
    //	System.out.println("recieved: " + new String(otherPk));
    	
    	KeyFactory kf = KeyFactory.getInstance("EC");
 	    X509EncodedKeySpec pkSpec = new X509EncodedKeySpec(otherPk);
 	    PublicKey otherPublicKey = kf.generatePublic(pkSpec);

 	    // Perform key agreement
 	    KeyAgreement ka = KeyAgreement.getInstance("ECDH");
 	    ka.init(kp.getPrivate());
 	    ka.doPhase(otherPublicKey, true);

 	    // Read shared secret
 	    byte[] sharedSecret = ka.generateSecret();
 	    System.out.println("shared secret:" + printHexBinary(sharedSecret));
 	    
 	   // Derive a key from the shared secret and both public keys
	    MessageDigest hash = MessageDigest.getInstance("SHA-256");
	    hash.update(sharedSecret);
	    // Simple deterministic ordering
	    List<ByteBuffer> keys = Arrays.asList(ByteBuffer.wrap(ourPk), ByteBuffer.wrap(otherPk));
	    Collections.sort(keys);
	    hash.update(keys.get(0));
	    hash.update(keys.get(1));

	    byte[] derivedKey = hash.digest();
	  //  System.out.println("Final key:" + printHexBinary(derivedKey));
	    System.out.println("connected");
	    return derivedKey;
 	    
       
    }
    
    public String sendEcho(byte[] buf,byte[] dk) throws IOException, NoSuchAlgorithmException {
        DatagramPacket packet 
          = new DatagramPacket(buf, buf.length, address, port);
        //System.out.println("send packet: " + new String(buf));
        socket.send(packet);
        byte[] recieve = new byte[Utils.packetSize];
        packet = new DatagramPacket(recieve, recieve.length);
        socket.receive(packet);
        
    	byte[] orig = packet.getData();
    	//System.out.println("orig: " + Utils.getByteStream(orig));
    	byte[] enc = Utils.read(orig);
    	//System.out.println("dropped size: " + Utils.getByteStream(enc));
    	byte[] dec = Utils.decrypt(enc, dk);
    	//System.out.println("decrypted: " + Utils.getByteStream(dec));
    	String received = Utils.checkHash(dec);
    	//System.out.println("final msg: " + received);
        
      //  String received = Utils.checkHash(Utils.decrypt(Utils.read(packet.getData()),dk));
        return received;
    }
 
    public void close() {
        socket.close();
    }
    
    
    


}
