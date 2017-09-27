import java.io.IOException;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.crypto.KeyAgreement;

//accepts connections from clients
//replies to messages by echoing reply: <original message>
//closes connection when receiving "end"
public class ServerThread extends Thread{
	 private DatagramSocket socket;
	    private boolean running;
	    private byte[] buf = new byte[Utils.packetSize];
	    private HashMap<InetAddress,byte[]> keymap;
	    public ServerThread(int port) throws SocketException {
	        socket = new DatagramSocket(port);
	        keymap = new HashMap<InetAddress,byte[]>();
	    }
	 
	    public void run() {
	        running = true;
	 
	        while (running) {
	            DatagramPacket packet 
	              = new DatagramPacket(buf, buf.length);
	            try {
					socket.receive(packet);
		            InetAddress address = packet.getAddress();
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

		        	    byte[] derivedKey = hash.digest();
		        	//    System.out.println("Final key:" + printHexBinary(derivedKey));
		        	    //store the key
		            	keymap.put(address, derivedKey);
		            	
		            	
		            }
		            else{
		            	byte[] orig = packet.getData();
		            	
		           // 	System.out.println("orig: " + Utils.getByteStream(orig));
		            	byte[] enc = Utils.read(orig);
		            //	System.out.println("dropped size: " + Utils.getByteStream(enc));
		            	byte[] dec = Utils.decrypt(enc, keymap.get(address));
		            	//System.out.println("decrypted: " + Utils.getByteStream(dec));
		            	String received = Utils.checkHash(dec);
		            	System.out.println("final msg: " + received);
		            	
		            	
			       //     String received = Utils.checkHash(Utils.decrypt(Utils.read(packet.getData()),keymap.get(address)));//Utils.checkHash(packet.getData());
			            if (received.equals("end")) {
			            	System.out.println("close connection");
			            	String temp = "reply: " + received;
			            	buf = Utils.addSize(Utils.encrypt(Utils.addSize(Utils.addHash(temp)),keymap.get(address)));
							
			            	packet = new DatagramPacket(buf, buf.length, address, port);
			            	keymap.remove(address);
							socket.send(packet);
			                running = false;
			                continue;
			            }
		            	String temp = "reply: " + received;
		            	
		            	//System.out.println("send this msg: " + temp);
						byte[] hash = Utils.addHash(temp);
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


