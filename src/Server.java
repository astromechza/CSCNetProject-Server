

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	ServerSocket ss;
	
	public final static boolean DEBUG = true;
	
	public Server(int port){
		try {
			ss = new ServerSocket(port);
		} catch (IOException e) {
			System.err.println("Failed to start server on port "+port+": "+e.toString());
		}
		
		debug("Server listening on port "+port+".");
		run();
	}
	
	private void run(){
		while(true){
			try {
				Socket s = ss.accept();
				debug("Client connected.");
				new ClientInstance(s);
				
			} catch (IOException e) {
				System.err.println("Failed to accept client connection.");
				e.printStackTrace();
			}			
		}
	}
	
	public static void debug(String s){
		if(DEBUG)
			System.out.println(s);
	}
}
