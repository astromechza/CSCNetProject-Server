package sensorserver.tests;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Timestamp;

import org.json.JSONArray;
import org.json.JSONObject;

public class TestQueryLogs {
	
	
	
	public static void main(String[] args)
	{
		Socket s=null;
		try {
			//s = new Socket("197.85.191.195", 3000);
			s = new Socket("localhost", 3000);
			BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));			
			PrintWriter out = new PrintWriter(s.getOutputStream(), true);	
			
			JSONObject o = new JSONObject();
			o.put("method", "query_logs");
			o.put("group_id", 1);
			
			JSONObject params = new JSONObject();
			
			JSONArray groupIds = new JSONArray();
			groupIds.put(1);	
			groupIds.put(4);
			params.put("group_ids", groupIds);		
						
			// Last 2 days
			params.put("time_from", new Timestamp(System.currentTimeMillis()-(1000*60*60*24*1)).toString());
			
			o.put("params", params);
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
	
}