import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

//code to start the client
//it takes input from user and send to server
//close connection by entering "end"
public class Client {
	public static void main(String[] args) {
		try {
			EchoClient client = new EchoClient(Integer.parseInt(args[0]));
			Scanner sc = new Scanner(System.in);
			String send;
			String echo;
			while(true){
				System.out.println("enter a sentence, type end to quit");
				send = sc.nextLine();
				echo = client.sendEcho(Utils.addSize(Utils.addHash(send)));
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
