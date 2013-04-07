package sensorserver.tests;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.json.JSONObject;

public class TestDataSummary {
	
	
	
	public static void requestSummary()
	{
		Socket s=null;
		try {
			s = new Socket("localhost", 3000);
			BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));			
			PrintWriter out = new PrintWriter(s.getOutputStream(), true);	
			
			JSONObject o = new JSONObject();
			o.put("method", "data_summary");
			o.put("group_id", 4);
			
			System.out.println(o.toString());
			
			out.write(o.toString());
			out.println();
			out.flush();
			
			System.out.println(in.readLine());
			
			
			s.close();
		} catch (IOException e) {
			System.out.println(e);
		}
				
	}
	
	
	public static void main(String[] args)
	{
		try 
		{
			while (true)
			{
				Thread.sleep(5000);
				requestSummary();
			}			
		} 
		catch (InterruptedException e) 
		{
			System.out.println(e);
		}
	}
	
}
