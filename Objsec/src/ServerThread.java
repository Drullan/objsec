import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;

//accepts connections from clients
//replies to messages by echoing reply: <original message>
//closes connection when receiving "end"
public class ServerThread extends Thread{
	 private DatagramSocket socket;
	    private boolean running;
	    private byte[] buf = new byte[Utils.packetSize];
	 
	    public ServerThread(int port) throws SocketException {
	        socket = new DatagramSocket(port);
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
		            String received = Utils.checkHash(packet.getData());
		            if (received.equals("end")) {
		            	System.out.println("close connection");
		            	String temp = "reply: " + received;
		            	buf = Utils.addSize(Utils.addHash(temp));
		            	packet = new DatagramPacket(buf, buf.length, address, port);
						socket.send(packet);
		                running = false;
		                continue;
		            }
		            
	            	String temp = "reply: " + received;
	            	System.out.println("recieved: " + received);
	            	buf = Utils.addSize(Utils.addHash(temp));
	            	packet = new DatagramPacket(buf, buf.length, address, port);
					socket.send(packet);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}     
	        }
	        socket.close();
	    }
}
