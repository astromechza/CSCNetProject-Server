package sensorserver.tests;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URLDecoder;

import org.json.JSONArray;
import org.json.JSONObject;

public class TestLastLogLines {
	
	
	
	public static void requestSummary()
	{
		Socket s=null;
		try {
			s = new Socket("197.85.191.195", 3000);
			BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));			
			PrintWriter out = new PrintWriter(s.getOutputStream(), true);	
			
			JSONObject o = new JSONObject();
			o.put("method", "last_log_lines");
			o.put("group_id", 4);
			
			System.out.println(o.toString());
			
			out.write(o.toString());
			out.println();
			out.flush();
			
			JSONObject result = new JSONObject(in.readLine());
			
			JSONArray lines = result.getJSONArray("result");
			
			System.out.println("The last x lines from the running log are: ");
			
			for (int i=0;i<lines.length();i++)
			{
				System.out.println(URLDecoder.decode(lines.getString(i)));
			}
			
			
			
			s.close();
		} catch (IOException e) {
			System.out.println(e);
		}
				
	}
	
	
	public static void main(String[] args)
	{
		requestSummary();
	}
	
}
