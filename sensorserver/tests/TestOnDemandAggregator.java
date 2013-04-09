package sensorserver.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Timestamp;

import org.json.JSONObject;

public class TestOnDemandAggregator 
{
	
	public static void sendRecv(JSONObject o)
	{
		Socket s=null;
		try {
			s = new Socket("localhost", 3000);
			BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));			
			PrintWriter out = new PrintWriter(s.getOutputStream(), true);	
			
			
			
			System.out.println("SENT: " + o.toString());
			
			out.write(o.toString());
			out.println();
			out.flush();
			
			JSONObject result = new JSONObject(in.readLine());
			

			System.out.println("RECEIVED: " + result);
			
			
			
			s.close();
		} catch (IOException e) {
			System.out.println(e);
		}
	}
	
	public static void main(String[] args)
	{
		
		
		JSONObject o = new JSONObject();
		o.put("method", "aggregate");
		o.put("group_id", 4);
		o.put("params", new JSONObject()
			.put("aggregation", "median")
			.put("type", "temperature")
			.put("group_id", "2")
		);
		
		sendRecv(o);
		
		o = new JSONObject();
		o.put("method", "aggregate");
		o.put("group_id", 4);
		o.put("params", new JSONObject()
			.put("aggregation", "mean")
			.put("type", "temperature")
			.put("group_id", "2")
		);
		
		sendRecv(o);
		
		o = new JSONObject();
		o.put("method", "aggregate");
		o.put("group_id", 4);
		o.put("params", new JSONObject()
			.put("aggregation", "mode")
			.put("type", "temperature")
			.put("group_id", "2")
		);
		
		sendRecv(o);
		
		o = new JSONObject();
		o.put("method", "aggregate");
		o.put("group_id", 4);
		o.put("params", new JSONObject()
			.put("aggregation", "mean")
			.put("type", "temperature")
			.put("time_from", new Timestamp(System.currentTimeMillis() - (int)(86_400_000*1.2)).toString())
			.put("group_id", "1")
		);
		
		sendRecv(o);
		
	}
}
