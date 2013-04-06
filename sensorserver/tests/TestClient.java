package sensorserver.tests;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.json.JSONArray;
import org.json.JSONObject;

public class TestClient {
	
	
	static String[] readingTypes  = new String[]{"temperature", "humidity", "light"};
	static int[] readingMaxes = new int[]{40,100,100};
	static int[] readingMins = new int[]{-20, 0,0};
	
	public static JSONObject randomReading()
	{
		int type = (int)Math.floor(Math.random()*readingTypes.length);
		double value = (Math.random()*(readingMaxes[type]-readingMins[type]) + readingMins[type]);
		long time = System.currentTimeMillis();
		
		JSONObject o = new JSONObject();
		o.put("type", readingTypes[type]);
		o.put("value", value);
		o.put("time", time);
		
		return o;
		
	}
	
	public static void sendReadings(JSONArray batch)
	{
		Socket s=null;
		try {
			s = new Socket("localhost", 3000);
			BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));			
			PrintWriter out = new PrintWriter(s.getOutputStream(), true);	
			
			JSONObject o = new JSONObject();
			o.put("action", "new_readings");
			o.put("group_id", 1);
			
			JSONObject params = new JSONObject();
			
			params.put("readings", batch);
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
	
	
	public static void main(String[] args)
	{
		JSONArray currentbatch = new JSONArray();
		try 
		{
			while (true)
			{
				Thread.sleep(1000);
				currentbatch.put(randomReading());
				if(currentbatch.length() >= 20)
				{
					sendReadings(currentbatch);
					currentbatch = new JSONArray();
				}
			}			
		} 
		catch (InterruptedException e) 
		{
			System.out.println(e);
		}
	}
	
}
