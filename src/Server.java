

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import org.json.*;// import all, not sure what exactly is needed for what

import log.Log;

public class Server {
	ServerSocket ss;
		
	public Server(int port){
		
		while(ss == null)
		{
			try {
				ss = new ServerSocket(port);
				Log.info("Listening on port " + port);
			} catch (BindException e) {
				Log.error("Failed to start server on port "+port+": "+e.toString());
				Log.info("Attempting to use port " + (++port) + " instead");
			} catch (IOException e) {
				Log.critical("Failed to start server on port "+port+": "+e.toString());
				return;
			}
		}	
		
		run();
	}
	
	private void run(){
		while(true){
			try {
				Socket s = ss.accept();
				Log.debug("Client connected.");
				new ClientInstance(s);
				
			} catch (IOException e) {
				System.err.println("Failed to accept client connection.");
				e.printStackTrace();
			}			
		}
	}
	
	/*
	 * Methods called by client: 
	 */
	
	public JSONObject update ()
	{
		JSONObject jo =  new JSONObject ();
		jo.put("Result", "update called");
		jo.put("Error", "no errors now");
		jo.put("ID", "ID goes here");
		return jo;
	}
	
	public JSONObject ping ()
	{
		JSONObject jo =  new JSONObject ();
		jo.put("Result", "ping called");
		jo.put("Error", "no errors now");
		jo.put("ID", "ID goes here");
		return jo;
	}
	
	public JSONObject hello ()
	{
		JSONObject jo =  new JSONObject ();
		jo.put("Result", "hello called");
		jo.put("Error", "no errors now");
		jo.put("ID", "ID goes here");
		return jo;
	}
	
}
