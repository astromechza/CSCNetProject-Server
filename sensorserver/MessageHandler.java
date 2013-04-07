package sensorserver;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import sensorserver.database.Database;
import sensorserver.log.Log;
import sensorserver.models.Reading;

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
public class MessageHandler 
{
	
	public static JSONObject reply(JSONObject in) throws JSONException
	{
		
		String action = in.getString("action");
		
		switch(action){
			case "ping":
				return handlePing(in);			
			case "new_readings":
				return handleNewReadings(in);
			case "query_readings":
				return handleQueryReadings(in);
			case "query_logs":
				return handleQueryLogs(in);
			case "data_summary":
				return handleDataSummaryRequest(in);
		}
		
		return makeErrorJson(new Exception("Unknown action '"+action+"'"));
	}

	/**
	 * Handle a 'ping' command. Just reply with pong as soon as possible.
	 * in = {"group_id":X,"action":"ping"}
	 * out = {"result":"pong"}
	 */
	private static JSONObject handlePing(JSONObject in) 
	{
		JSONObject reply = new JSONObject();
		reply.put("result", "pong");
		return reply;
	}
	
	/**
	 * Called when a client wants to upload a set of readings
	 * in = {
	 * 			'group_id' 	=> 	(int) group_id,
	 * 			'action' 	=>	'new_readings',
	 * 			'params'	=>	{
	 * 								'readings' =>	[ 
	 * 													{ 'time' => milliseconds, 'type' => 'humidity', 'value' => 53.632 }
	 * 													{ 'time' => milliseconds, 'type' => 'humidity', 'value' => 34.32 }
	 * 													{ 'time' => milliseconds, 'type' => 'temperature', value => 26 }
	 * 													{ ... }
	 * 												]
	 * 							}
	 * 		}
	 */
	private static JSONObject handleNewReadings(JSONObject in) 
	{
		JSONObject reply = new JSONObject();
		
		try
		{
			// The group id TODO change to sensor_id
			int groupId = in.getInt("group_id");
			
			JSONArray readings = in.getJSONObject("params").getJSONArray("readings");
			
			// prepare an insert statement for Readings table
			PreparedStatement stmt = Database.getInstance().getConnection().prepareStatement(new Reading().insertStmt());
			
			for(int i = 0; i < readings.length(); i++)
			{
				JSONObject reading = readings.getJSONObject(i);
				String type = reading.getString("type");
				int type_id = Database.getInstance().getType(type);
				double value = reading.getDouble("value");
				long time = reading.getLong("time");
				
				Reading r = new Reading(time, value, groupId, type_id);
				
				r.bindToStatement(stmt);
				
				//add to batch
				stmt.addBatch();														
			}
			
			// Calculate the number of inserted rows
			int totalNewRows = 0;
			int [] changes = stmt.executeBatch();
			for (int u : changes) totalNewRows += u;
			
			Database.getInstance().insertLog(groupId, "new_readings");
			
			reply.put("result", totalNewRows+" records logged.");
		
		}catch(JSONException e){
			Log.error("JSON Error in MessageHandler.handleNewReadings." + Utils.fmtStackTrace(e.getStackTrace()));
			reply = makeErrorJson(e);
		}catch(SQLException e){
			Log.error("SQL Error in MessageHandler.handleNewReadings." + Utils.fmtStackTrace(e.getStackTrace()));
			reply = makeErrorJson(e);
		}
		return reply;
	}

	
	private static JSONObject handleQueryReadings(JSONObject in)
	{
		JSONObject reply = new JSONObject();
		reply.put("result", "");
		return reply;
	}
	
	private static JSONObject handleQueryLogs(JSONObject in)
	{
		JSONObject reply = new JSONObject();
		reply.put("result", "");
		return reply;
	}
	
	private static JSONObject handleDataSummaryRequest(JSONObject in) {
		JSONObject reply = new JSONObject();
		reply.put("action", "data_summary");
		reply.put("data_summary", Aggregator.getDataSummary());
		return reply;
	}	
	

	/**
	 * Build a reply message for an exception
	 */
	public static JSONObject makeErrorJson(Exception e)
	{
		JSONObject o = new JSONObject();
		o.put("result", "");
		o.put("error", e.getMessage());
		return o;
	}
	
	
	

}
