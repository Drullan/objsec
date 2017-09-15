import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;

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
 
    public String sendEcho(byte[] buf) throws IOException, NoSuchAlgorithmException {
        DatagramPacket packet 
          = new DatagramPacket(buf, buf.length, address, port);
        socket.send(packet);
        byte[] recieve = new byte[Utils.packetSize];
        packet = new DatagramPacket(recieve, recieve.length);
        socket.receive(packet);
        String received = Utils.checkHash(packet.getData());
        return received;
    }
 
    public void close() {
        socket.close();
    }
    


}
