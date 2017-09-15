import java.net.SocketException;


public class Server {

	public static void main(String[] args) {
		try {
			new ServerThread(Integer.parseInt(args[0])).start();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e){
			System.out.println("expected args: portnr");
			System.exit(1);
		}

	}

}
