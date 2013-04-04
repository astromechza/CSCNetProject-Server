package sensorserver.tests;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

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
			o.put("action", "ping");
			
			
			System.out.println("Connected");
			out.write(o.toString());
			out.println();
			out.flush();		
			
			while(true){
				System.out.println(in.readLine());
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
