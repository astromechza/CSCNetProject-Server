package sensorserver;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import sensorserver.database.Database;
import sensorserver.log.Log;

/*
 * This class receives the raw string sent by a client, parses it and determines what to
 * do with it based on the 'action' key supplied in the message. 
 * 
 * All messages must follow this structure:
 * 
 * {
 * 		'group_id' => (int),
 * 		'action' => (string),
 * 		'params' => { 'any' => 'additional, 'parameters => 'go here' }
 * }
 * 
 */
public class MessageHandler {
	public static void consume(ClientInstance client, String message){
		try{
			JSONObject messageObj = new JSONObject(message);
			String action = messageObj.getString("action");
			
			switch(action){
				case "new_readings":
					handleNewReadings(client, messageObj);
					break;
				case "query_readings":
					handleQueryReadings(client, messageObj);
					break;
				case "query_logs":
					handleQueryLogs(client, messageObj);
					break;
			}
			
		}catch(JSONException e){
			Log.error("JSON error for message: "+message);
			e.printStackTrace();
		}
	}
	
	/*
	 * Called when a client sends new reading data in the following message format:
	 * 
	 * {
	 * 		'group_id' 	=> 	(int) group_id,
	 * 		'action' 	=>	'new_readings',
	 * 		'params'	=>	{
	 * 							'readings' =>	[ 
	 * 												{ 'type' => 'humidity', 'value' => 53.632 }
	 * 												{ 'type' => 'humidity', 'value' => 34.32 }
	 * 												{ 'type' => 'temperature', value => 26 }
	 * 												{ ... }
	 * 											]
	 * 						}
	 *  }	
	 */
	private static void handleNewReadings(ClientInstance client, JSONObject messageObj){
		try{
			int groupId = messageObj.getInt("group_id");
			JSONArray readings = messageObj.getJSONObject("params").getJSONArray("readings");
			
			int totalNewRows = 0;
			for(int i = 0; i < readings.length(); i++){
				JSONObject reading = readings.getJSONObject(i);
				String type = reading.getString("type");
				double value = reading.getDouble("value");
				
				Database d = Database.getInstance();
				totalNewRows += d.insertReading(groupId, type, value);
			}
			
			client.write(buildSuccessMessage(totalNewRows+" records logged."));
		
		}catch(JSONException e){
			Log.error("JSON Error in MessageHandler.handleNewReadings.");
			e.printStackTrace();
			client.write(buildErrorMessage("Malformed JSON request."));
		}catch(SQLException e){
			Log.error("SQL Error in MessageHandler.handleNewReadings.");
			e.printStackTrace();
			client.write(buildErrorMessage("SQL Error."));
		}
	}
	
	private static void handleQueryReadings(ClientInstance client, JSONObject messageObj){
		
	}
	
	private static void handleQueryLogs(ClientInstance client, JSONObject messageObj){
		
	}	
	
	private static JSONObject buildSuccessMessage(String message){
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("result", message);
		return buildServerResponse(m);
	}
	
	private static JSONObject buildErrorMessage(String message){
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("error", message);
		return buildServerResponse(m);
	}
	
	private static JSONObject buildServerResponse(Map<String, Object> keyvals){
		return new JSONObject(keyvals);
	}
}
