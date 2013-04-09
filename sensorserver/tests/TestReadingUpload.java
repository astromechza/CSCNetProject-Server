package sensorserver.tests;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Timestamp;

import org.json.JSONArray;
import org.json.JSONObject;

import sensorserver.ArgParser;

public class TestReadingUpload {
	
	
	static String[] readingTypes  = new String[]{"temperature", "humidity", "light"};
	static int[] readingMaxes = new int[]{40,100,100};
	static int[] readingMins = new int[]{-20, 0,0};
	
	static String host = "localhost";
	static int port = 3000;
	static int id = 1;
	
	public static JSONObject randomReading()
	{
		int type = (int)Math.floor(Math.random()*readingTypes.length);
		double value = (Math.random()*(readingMaxes[type]-readingMins[type]) + readingMins[type]);
		long time = System.currentTimeMillis();
		
		Timestamp ts = new Timestamp(time);
		
		JSONObject o = new JSONObject();
		o.put("type", readingTypes[type]);
		o.put("value", value);
		o.put("time", ts.toString());
		
		return o;
		
	}
	
	public static void sendReadings(JSONArray batch)
	{
		Socket s=null;
		try {
			s = new Socket(host, port);
			BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));			
			PrintWriter out = new PrintWriter(s.getOutputStream(), true);	
			
			JSONObject o = new JSONObject();
			o.put("method", "new_readings");
			o.put("group_id", id);
			
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
	
	public static void main(String[] argsarray)
	{
		ArgParser args = new ArgParser();
		args.AddOption("host", "The host to connect to", "localhost");
		args.AddOption("port", "The remote port", "3000");
		args.AddOption("id", "The group ID to use","1");	
		
		try {
			args.parse(argsarray);
			
			host = args.getOption("host");
			port = Integer.parseInt(args.getOption("port"));
			id = Integer.parseInt(args.getOption("id"));			
			
		} catch (Exception e1) {
			e1.printStackTrace();
			System.exit(-1);
		}

		JSONArray currentbatch = new JSONArray();
		try 
		{
			while (true)
			{
				Thread.sleep(500);
				JSONObject o = randomReading();
				
				currentbatch.put(o);
				System.out.println("Took reading: " + o.toString());
				
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