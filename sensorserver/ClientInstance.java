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
			System.err.println("Error setting up input-output streams for client.");
			e.printStackTrace();
		}
	}

	@Override
	public void run() {	
		String received;
		
		try {
			// read everything
			while(!close && (received = in.readLine()) != null)
			{	
				Log.debug("Received: " + received);
				JSONObject inO, outO = null;
				
				try
				{
					inO = new JSONObject(received);
					outO = MessageHandler.reply(inO);
				}
				catch (JSONException e)
				{
					
					outO = new JSONObject();
					outO.put("result", "");
					outO.put("error", e.getMessage());
				}
				
				out.write(outO.toString());
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
		
		Log.debug("Client disconnected.");		
	}
}
