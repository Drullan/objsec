import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
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
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.crypto.KeyAgreement;

//accepts connections from clients
//replies to messages by echoing reply: <original message>
//closes connection when receiving "end"
public class ServerThread extends Thread{
	 private DatagramSocket socket;
	    private boolean running;
	    private byte[] buf = new byte[Utils.packetSize];
	    private HashMap<InetAddress,byte[]> keymap;
	    private HashMap<InetAddress,Integer> noncemap;
	    private HashMap<InetAddress,PubKey> publickeymap;
	    
	    public ServerThread(int port) throws SocketException {
	        socket = new DatagramSocket(port);
	        keymap = new HashMap<InetAddress,byte[]>();
	        noncemap = new HashMap<InetAddress,Integer>();
	        publickeymap = new HashMap<InetAddress,PubKey>();
	    }
	 
	    public void run() {
	        running = true;
	 
	        while (running) {
	            DatagramPacket packet 
	              = new DatagramPacket(buf, buf.length);
	            try {
					socket.receive(packet);
		            InetAddress address = packet.getAddress();
		            publickeymap.put(address, new PubKey(new BigInteger("965610267221900211386633689709680083946428922042684753"),11));
		            int port = packet.getPort();
		            if(!keymap.containsKey(address)){
		            	//fetch client key
		            	byte[] otherPk = Utils.read(packet.getData());   
		            	
		            	//generate our keys
		            	KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
		        	    kpg.initialize(256);
		        	    KeyPair kp = kpg.generateKeyPair();
		        	    byte[] ourPk = kp.getPublic().getEncoded();
		            	
		        	    //send public key
		            	byte[] sendPk = Utils.addSize(ourPk);
		            	packet = new DatagramPacket(sendPk,sendPk.length,address,port);
		            	socket.send(packet);
		            	
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

		        	    
		        	    //generate random value to be signed
		        	    Random rand = new Random();
		        	    int r= rand.nextInt(6000);
		        	    BigInteger msg = Utils.hash(""+r);
		        	    byte[] cert = Utils.addSize(msg.toByteArray());
		        	    packet = new DatagramPacket(cert,cert.length,address,port);
		        	    socket.send(packet);
		        	    
		        	    packet = new DatagramPacket(buf, buf.length);
		        	    socket.receive(packet);
		        	    byte[] signedBytes = Utils.read(packet.getData());
		        	    BigInteger signed = new BigInteger(signedBytes);
		        	    RSA rsa = new RSA();
		        	    if(!rsa.verifysign(signed, publickeymap.get(address).e, publickeymap.get(address).n).equals(msg)){
		        	    	System.out.println("couldn't verify this user, exiting...");
		        	    	break;
		        	    }
		        	    
		        	    byte[] derivedKey = hash.digest();
		        	//    System.out.println("Final key:" + printHexBinary(derivedKey));
		        	    //store the key
		            	keymap.put(address, derivedKey);
		            	noncemap.put(address, 0);
		    	        
		            	
		            }
		            else{
		            	byte[] orig = packet.getData();
		            	
		            	//System.out.println("orig: " +  Base64.getEncoder().encodeToString(orig));//used to test reply atacks
		            	byte[] enc = Utils.read(orig);
		            	//System.out.println("dropped size: " + Utils.getByteStream(enc));
		            	byte[] dec = Utils.decrypt(enc, keymap.get(address));
		            //	System.out.println("decrypted: " + Utils.getByteStream(dec));
		            	String received = Utils.validateMsg(dec,noncemap.get(address));
		            	if(!received.equals("wrong nonce")){
		            		noncemap.put(address, noncemap.get(address)+1);
		            	
			            	System.out.println("final msg: " + received);
			            	
			            	
				       //     String received = Utils.checkHash(Utils.decrypt(Utils.read(packet.getData()),keymap.get(address)));//Utils.checkHash(packet.getData());
				            if (received.equals("end")) {
				            	System.out.println("close connection");
				            	String temp = "reply: " + received;
				            	buf = Utils.addSize(Utils.encrypt(Utils.addSize(Utils.addHash(Utils.addNonce(temp, noncemap.get(address).intValue()))),keymap.get(address)));
								
				            	packet = new DatagramPacket(buf, buf.length, address, port);
				            	keymap.remove(address);
				            	noncemap.remove(address);
								socket.send(packet);
				                running = false;
				                continue;
				            }
				            
			            	String temp = "reply: " + received;
			            	
			            	//System.out.println("send this msg: " + temp);
			            	byte[] nonce = Utils.addNonce(temp, noncemap.get(address).intValue());
							byte[] hash = Utils.addHash(nonce);
							//System.out.println("added hash: " + Utils.getByteStream(hash));
							byte[] hashns = Utils.addSize(hash);
							//System.out.println("added size: " + Utils.getByteStream(hashns));
							byte[] encrypt = Utils.encrypt(hashns, keymap.get(address));
							//System.out.println("added encryption: " + Utils.getByteStream(encrypt));
							byte[] encryptns = Utils.addSize(encrypt);
							//System.out.println("added final size: " + Utils.getByteStream(encryptns));
			            	
			            	System.out.println("recieved: " + received);
			            //	buf = Utils.addSize(Utils.encrypt(Utils.addSize(Utils.addHash(temp)),keymap.get(address)));
			            //	packet = new DatagramPacket(buf, buf.length, address, port);
			            	packet = new DatagramPacket(encryptns,encryptns.length,address,port);
							socket.send(packet);
		            	}else{
		            		System.out.println("stop reply atacking me!");
		            	}
		            }
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvalidKeySpecException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvalidKeyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}     
	        }
	        socket.close();
	    }
	    
	    
}


