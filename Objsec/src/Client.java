import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Scanner;

//code to start the client
//it takes input from user and send to server
//close connection by entering "end"
public class Client {
	public static void main(String[] args) throws InvalidKeyException, InvalidKeySpecException {
		try {
			EchoClient client = new EchoClient(Integer.parseInt(args[0]));
			Scanner sc = new Scanner(System.in);
			String send;
			String echo;
			byte[] dk = client.handshake();
			int nonce=0;
			while(true){
				System.out.println("enter a sentence, type end to quit");
				send = sc.nextLine();
		//		System.out.println("send this msg: " + send);
				byte[] non = Utils.addNonce(send, nonce);
				byte[] hash = Utils.addHash(non);
				//System.out.println("added hash: " + Utils.getByteStream(hash));
				byte[] hashns = Utils.addSize(hash);
				//System.out.println("added size: " + Utils.getByteStream(hashns));
				byte[] encrypt = Utils.encrypt(hashns, dk);
				//System.out.println("added encryption: " + Utils.getByteStream(encrypt));
				byte[] encryptns = Utils.addSize(encrypt);
				//System.out.println("added final size: " + Utils.getByteStream(encryptns));
				//echo = client.sendEcho(Utils.addSize(Utils.encrypt(Utils.addSize(Utils.addHash(send)),dk)),dk);
				echo = client.sendEcho(encryptns, dk,nonce);
				nonce++;
				System.out.println(echo);
				if(send.equals("end")){
					client.close();
					break;
				}
				
			}	
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		} catch (NumberFormatException e){
			System.out.println("expected args: portnr");
			System.exit(1);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	


}
