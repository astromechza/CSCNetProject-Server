package sensorserver.tests;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import org.json.JSONArray;
import org.json.JSONObject;

public class TestClient {
	public static void main(String[] args){
		new TestClient();
	}
	
	public TestClient(){
		try {
			Socket s = new Socket("localhost", 3000);			
			BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));			
			PrintWriter out = new PrintWriter(s.getOutputStream(), true);			
			
			JSONObject o = new JSONObject();
			o.put("action", "new_readings");
			o.put("group_id", 1);
			
			JSONObject params = new JSONObject();
			JSONArray readings = new JSONArray();
			JSONObject reading = new JSONObject();
			reading.put("type", "temperature");
			reading.put("value", 56.07);
			
			readings.put(reading);
			params.put("readings", readings);
			o.put("params", params);
			
			
			System.out.println("Connected");
			out.write(o.toString());
			out.println();
			out.flush();		
			
			int one = 1;			
			while(one==1){
				System.out.println(in.readLine());
			}
			s.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
