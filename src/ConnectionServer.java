import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class ConnectionServer {
	
	private int listenport;
	private ServerSocket serverSocket;
	
	public ConnectionServer(int port) 
	{
		listenport = port;
	}
	
	public void run() 
	{
		try 
		{
			serverSocket = new ServerSocket(listenport);
		} 
		catch (IOException e) 
		{
			System.out.printf("Could not listen on port %s!\n", listenport);
			return;
		}
		
		while(true)
		{
			try 
			{
				Socket client = serverSocket.accept();
				Thread.sleep(1);
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}
}
