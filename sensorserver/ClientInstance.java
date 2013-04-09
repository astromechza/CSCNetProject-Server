package sensorserver;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import sensorserver.log.Log;


public class ClientInstance implements Runnable {
	
	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	private boolean close;
	
	public ClientInstance(Socket s){
		socket = s;
		
		try{
			close = false;
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
			new Thread(this).start();
		}catch(IOException e){
			Log.error("Error setting up input-output streams: " + e + " " + Utils.fmtStackTrace(e.getStackTrace()));
		}
	}

	@Override
	public void run() {	
		String received;		

		Log.info("Client (" + socket.getRemoteSocketAddress() + ":" + socket.getPort() + ") connected.");
		
		try {
			
			// read and process each line individually
			while(!close && (received = in.readLine()) != null)
			{	
				Log.debug("Received: " + received);
				JSONObject inO, outO = null;
				
				long time_start = System.currentTimeMillis();
				
				try
				{
					inO = new JSONObject(received);
					
					
					
					outO = MessageHandler.reply(inO);
				}
				catch (Exception e)
				{
					
					outO = new JSONObject();
					outO.put("result", "");
					outO.put("error", e.getMessage());
				}
				
				long time_end = System.currentTimeMillis();
				
				outO.put("elapsed", time_end-time_start);
				String outS = outO.toString();
								
				Log.debug("Sent: " + outS);
				
				out.write(outS);
				out.println();
				out.flush();
				
			}			
		} 
		catch (IOException e) 
		{
			
		} 
		finally 
		{
			try 
			{
				in.close();
				out.close();
				socket.close();
			} 
			catch (IOException e) 
			{
				// Nothing we can do here.
			}
		}
		
		Log.info("Client (" + socket.getRemoteSocketAddress() + ":" + socket.getPort() + ") disconnected.");	
	}
}
