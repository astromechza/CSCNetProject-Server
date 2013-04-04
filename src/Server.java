

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import log.Log;

public class Server {
	ServerSocket ss;
	
	public final static boolean DEBUG = true;
	
	public Server(int port){
		
		while(ss == null)
		{
			try {
				ss = new ServerSocket(port);
				Log.info("Listening on port " + port);
			} catch (IOException e) {
				Log.error("Failed to start server on port "+port+": "+e.toString());
				Log.info("Attempting to use port " + (++port) + " instead");
			}
		}	
		
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
