

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientInstance implements Runnable {
	
	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	private boolean close;
	
	public ClientInstance(Socket s){
		socket = s;
		setup();
	}
	
	private void setup(){
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
				// work here
				// the line that was received is in 'received'
				Server.debug("Received: " + received);
				
				
				// for now just echo it back to the client (pipe it back)
				write(received);
			}			
			
			
			
		} catch (IOException e) {
		} finally {
			try {
				in.close();
				out.close();
				socket.close();
			} catch (IOException e) {
				// Nothing we can do here.
			}
		}
		
		Server.debug("Client disconnected.");		
	}
	
	public void write(String str){
		out.write(str);
		out.println();
		out.flush();
	}
}
