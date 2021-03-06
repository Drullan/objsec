
import java.io.IOException;
import java.math.BigInteger;
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
import java.util.Random;

import javax.crypto.KeyAgreement;

//functions for the client
public class EchoClient {
	private DatagramSocket socket;
    private InetAddress address;
    private int port;
    private BigInteger d;
    private BigInteger n;
    private PubKey pubk;//servers public key
 
    public EchoClient(int serverport) throws SocketException, UnknownHostException {
        socket = new DatagramSocket();
        address = InetAddress.getByName("localhost");
        port = serverport;
        d = new BigInteger("526696509393763751665436556937193200647473011804811651");
        n = new BigInteger("965610267221900211386633689709680083946428922042684753");
        pubk = new PubKey(new BigInteger("903568581836942277823087494889323859446530238712611721"),19);
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
 	 //   System.out.println("shared secret:" + printHexBinary(sharedSecret));
 	    
 	   // Derive a key from the shared secret and both public keys
	    MessageDigest hash = MessageDigest.getInstance("SHA-256");
	    hash.update(sharedSecret);
	    // Simple deterministic ordering
	    List<ByteBuffer> keys = Arrays.asList(ByteBuffer.wrap(ourPk), ByteBuffer.wrap(otherPk));
	    Collections.sort(keys);
	    hash.update(keys.get(0));
	    hash.update(keys.get(1));

	    //recieve a challange to sign
	    recieve = new byte[Utils.packetSize];
    	packet = new DatagramPacket(recieve, recieve.length);
	    socket.receive(packet);
	    byte[] challangebytes = Utils.read(packet.getData());
	    BigInteger challange = new BigInteger(challangebytes);
	    RSA rsa = new RSA();
	    BigInteger signed = rsa.sign(challange, d, n);

	    byte[] signedBytes=Utils.addSize(signed.toByteArray());
	    packet = new DatagramPacket(signedBytes,signedBytes.length,address,port);
	    socket.send(packet);
	    
	    
	    //generate random value to be signed
	    Random rand = new Random();
	    int r= rand.nextInt(6000);
	    BigInteger msg = Utils.hash(""+r);
	    byte[] cert = Utils.addSize(msg.toByteArray());
	    packet = new DatagramPacket(cert,cert.length,address,port);
	    socket.send(packet);
	    
	    packet = new DatagramPacket(buf, buf.length);
	    socket.receive(packet);
	    signedBytes = Utils.read(packet.getData());
	    signed = new BigInteger(signedBytes);
	   // RSA rsa = new RSA();
	    if(!rsa.verifysign(signed, pubk.e, pubk.n).equals(msg)){
	    	System.out.println("couldn't verify this server, exiting...");
	    	return null;
	    }
	    
	    byte[] derivedKey = hash.digest();
	  //  System.out.println("Final key:" + printHexBinary(derivedKey));
	    System.out.println("connected");
	    return derivedKey;
 	    
       
    }
    
    public String sendEcho(byte[] buf,byte[] dk,int nonce) throws IOException, NoSuchAlgorithmException {
        DatagramPacket packet 
          = new DatagramPacket(buf, buf.length, address, port);
        //System.out.println("send packet: " + new String(buf));
     //   System.out.println("send packet: " + Utils.getByteStream(buf));
        socket.send(packet);
        nonce++;
        byte[] recieve = new byte[Utils.packetSize];
        packet = new DatagramPacket(recieve, recieve.length);
        socket.receive(packet);
        
    	byte[] orig = packet.getData();
    	//System.out.println("receive packet: " + Utils.getByteStream(orig));
    	byte[] enc = Utils.read(orig);
    	//System.out.println("dropped size: " + Utils.getByteStream(enc));
    	byte[] dec = Utils.decrypt(enc, dk);
    	//System.out.println("decrypted: " + Utils.getByteStream(dec));
    	String received = Utils.validateMsg(dec,nonce);
    	//System.out.println("final msg: " + received);
        
      //  String received = Utils.checkHash(Utils.decrypt(Utils.read(packet.getData()),dk));
        return received;
    }
 
    public void close() {
        socket.close();
    }
    
    
    


}
