package sensorserver;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import sensorserver.database.Database;
import sensorserver.log.Log;
import sensorserver.models.ReadingType;

/*
 * This class receives the raw string sent by a client, parses it and determines what to
 * do with it based on the 'method' key supplied in the message. 
 * 
 * All messages must follow this structure:
 * 
 * {
 * 		'group_id' => (int),
 * 		'method' => (string),
 * 		'params' => { 'any' => 'additional, 'parameters => 'go here' }
 * }
 * 
 */
public class MessageHandler 
{
	/**
	 * Generate a reply for the given JSON
	 */
	public static JSONObject reply(JSONObject in) throws Exception
	{		
		
		// Reject messages that do not say who they are from or what they want.
		if(!in.has("group_id")){
			return makeErrorJson(new Exception("No group ID supplied."));
		}
		
		if(!in.has("method")){
			return makeErrorJson(new Exception("No method supplied."));
		}
		
		String method = in.getString("method");		
		int groupId = in.getInt("group_id");
		
		Log.info("Processing `" + method +"` call from group " + groupId);
				
		switch(method){
			case "ping":
				Database.getInstance().insertLog(groupId, "ping");
				return handlePing(in);
			case "info":
				return handleInfo(in);
			case "new_readings":
				Database.getInstance().insertLog(groupId, "new_readings");
				return handleNewReadings(in);
			case "query_readings":
				Database.getInstance().insertLog(groupId, "query_readings");
				return handleQueryReadings(in);
			case "query_logs":
				Database.getInstance().insertLog(groupId, "query_logs");
				return Database.getInstance().queryLogTable(in);
			case "data_summary":
				Database.getInstance().insertLog(groupId, "data_summary");
				return handleDataSummaryRequest(in);
			case "last_log_lines":
				Database.getInstance().insertLog(groupId, "last_log_lines");
				return handleGetLastLinesFromCurrentLog();
			case "aggregate":
				Database.getInstance().insertLog(groupId, "aggregation");
				return OnDemandAggregator.aggregate(in);
		}
		
		return makeErrorJson(new Exception("Unknown method '"+method+"'"));
	}


	/**
	 * Handle a 'ping' command. Just reply with pong as soon as possible.
	 * in = {"group_id":X,"method":"ping"}
	 * out = {"result":"pong"}
	 */
	private static JSONObject handlePing(JSONObject in) 
	{
		JSONObject reply = new JSONObject();
		reply.put("result", "pong");
		return reply;
	}

	/**
	 * Return info about the reading types:
	 * for each type of reading:
	 *  - type name
	 *  - earliest date
	 *  - latest date
	 *  - count
	 */
	private static JSONObject handleInfo(JSONObject in) throws Exception
	{
		Statement s = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try
		{
			JSONObject reply = new JSONObject();
			JSONArray result = new JSONArray();
			
			// Get Reading Types
			String sql = "SELECT id, name FROM reading_types;";			
			s = Database.getInstance().getConnection().createStatement();
			
			rs = s.executeQuery(sql);		
			List<ReadingType> readingTypes = new ArrayList<ReadingType>();
			while(rs.next())
			{
				ReadingType rt = new ReadingType(rs);
				readingTypes.add(rt);
			}
			
			// Get ranges and things
			sql = "SELECT MAX(time), MIN(time), COUNT(*) FROM readings WHERE type_id = ?;";
			ps = Database.getInstance().getConnection().prepareStatement(sql);
			
			
			for (ReadingType rt : readingTypes)
			{				
				ps.setInt(1, rt.getId());
				
				rs = ps.executeQuery();
				rs.first();
				
				Timestamp t1 = rs.getTimestamp(2);
				Timestamp t2 = rs.getTimestamp(1);
				
				result.put(
						new JSONObject()
						     .put("name", rt.getName())
						     .put("count", rs.getInt(3))
						     .put("time_from", (t1==null)?null:t1.toString())
						     .put("time_to", (t2==null)?null:t2.toString())
				);
				
			}
			

			reply.put("result", result);
			return reply;
		} 
		catch (SQLException e)
		{
			Log.error(e + " " + Utils.fmtStackTrace(e.getStackTrace()));
			return new JSONObject().put("error", "SQLException on server.");
		}finally{
			try{
				if(rs != null)
					rs.close();
				if(s != null)
					s.close();
				if(ps != null)
					ps.close();
			}catch(Exception e){}			
		}		
	}
	
	/**
	 * Called when a client wants to upload a set of readings
	 * in = {
	 * 			'group_id' 	=> 	(int) group_id,
	 * 			'method' 	=>	'new_readings',
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
	private static JSONObject handleNewReadings(JSONObject in)  throws Exception
	{
		JSONObject reply = new JSONObject();
		Statement s = null;
		try
		{
			// The group id TODO change to sensor_id
			int groupId = in.getInt("group_id");
			
			JSONArray readings = in.getJSONObject("params").getJSONArray("readings");
			
			Log.info("Processing " + readings.length() + " readings." );
			
			String sql = "INSERT INTO readings (`time`, `value`, `sensor_id`, `type_id`) VALUES ";
			
			for(int i = 0; i < readings.length(); i++)
			{
				JSONObject reading = readings.getJSONObject(i);
				String type = reading.getString("type");
				int type_id = Database.getInstance().getTypeIdFromStr(type, true);
				double value = reading.getDouble("value");
				
				Object tvalue = reading.get("time");
				
				Timestamp time;
				
				if (tvalue instanceof String)
				{
					time = Timestamp.valueOf((String)tvalue);
				}
				else
				{
					time = new Timestamp((long)tvalue);
				}
				
				if (i>0)sql+=", ";
				sql += "('"+time.toString()+"', "+value+", "+groupId+", "+type_id+")";
				
			}	
			
			s = Database.getInstance().getConnection().createStatement();
			int totalNewRows = s.executeUpdate(sql + ";");
			
			reply.put("result", totalNewRows+" records logged.");
		
		}catch(JSONException e){
			Log.error("JSON Error in MessageHandler.handleNewReadings." + Utils.fmtStackTrace(e.getStackTrace()));
			reply = makeErrorJson(e);
		}catch(SQLException e){
			Log.error("SQL Error in MessageHandler.handleNewReadings." + Utils.fmtStackTrace(e.getStackTrace()));
			reply = makeErrorJson(e);
		}catch (Exception e){
			Log.error("Unexpected Error in MessageHandler.handleNewReadings." + Utils.fmtStackTrace(e.getStackTrace()));
			reply = makeErrorJson(e);
		}finally{
			try{
				if(s != null)
					s.close();
			}catch(Exception e){}			
		}
		return reply;
	}

	 /**
	 * A client message to query the readings table. All filters in the "params" hash are optional.
	 * in = {
	 * 			'group_id' 	=> 	(int) group_id,
	 * 			'method' 	=>	'query_readings',
	 * 			'params'	=>	{
	 * 								'group_ids' => [1,2,3...],
	 * 								'time_from' => "<timestamp>",
	 * 								'time_to'	=> "<timestamp>",
	 * 								'types'		=> ['light', 'temperature', 'humidity']
	 * 							}
	 * 		}
	 */	
	private static JSONObject handleQueryReadings(JSONObject in)  throws Exception
	{		
		JSONObject reply = new JSONObject();
		ArrayList<String> whereClause = new ArrayList<String>(3);
		
		// Add whatever result filters have been supplied.
		if(in.has("params")){
			JSONObject params = in.getJSONObject("params");
			
			if(params.has("group_ids")){
				JSONArray json_group_ids = params.getJSONArray("group_ids");
				if(json_group_ids.length() > 0){
					String group_ids = json_group_ids.join(",");
					whereClause.add("sensor_id IN("+group_ids+")");
				}
			}
			
			if(params.has("time_from")){
				// Convert it to ensure proper format.
				Timestamp time_from = Timestamp.valueOf(params.getString("time_from"));
				whereClause.add("time >= \""+time_from+"\"");
			}
			
			if(params.has("time_to")){
				// Convert it to ensure proper format.
				Timestamp time_to = Timestamp.valueOf(params.getString("time_to"));
				whereClause.add("time <= \""+time_to+"\"");
			}
			
			if(params.has("types")){
				JSONArray json_types = params.getJSONArray("types");
				if(json_types.length() > 0){
					String types = json_types.join(",");
					whereClause.add("reading_types.name IN("+types+")");
				}
			}
		
		}
		
		String query = "SELECT * FROM readings INNER JOIN reading_types ON reading_types.id = readings.type_id";
		Statement s = null;
		ResultSet rs = null;
		try {
			
			// Combine our WHERE clauses into a string.
			if(!whereClause.isEmpty()){
				StringBuilder whereQuery = new StringBuilder();
	
				for (String string : whereClause) {
				    if (whereQuery.length() == 0) {
				        whereQuery.append(" WHERE "+string+" ");
				    }else{
				    	whereQuery.append("AND "+string+" ");
				    }
				}
				
				query += whereQuery;
			}

			s = Database.getInstance().getConnection().createStatement();
			rs = s.executeQuery(query);
			
			Log.debug("Ran query: "+query);
			
			JSONArray readings = new JSONArray();
			
			while(rs.next()){
				JSONObject reading = new JSONObject();
				reading.put("group_id", rs.getInt("sensor_id"));
				reading.put("type", rs.getString("name"));
				reading.put("value", rs.getDouble("value"));
				reading.put("time", rs.getTimestamp("time"));
				readings.put(reading);
			}
			
			reply.put("method", "query_readings");
			reply.put("result", readings);
			
			return reply;			
		} catch (SQLException e) {
			Log.error("SQL Error: "+e.getMessage()+" in query \""+query+"\"");
			return makeErrorJson(e);
		}finally{
			try{
				if(rs != null)
					rs.close();
				if(s != null)
					s.close();
			}catch(Exception e){}			
		}
	}
	
	/**
	 * Return data summaries for all the readings
	 */
	private static JSONObject handleDataSummaryRequest(JSONObject in) throws Exception
	{
		JSONObject reply = new JSONObject();
		reply.put("result", Aggregator.getDataSummary());
		return reply;
	}	
	
	/**
     * Return the last 30 lines or so from the sys.out
     * Mostly used for debug purposes. Should be disabled in final build
	 */
	private static JSONObject handleGetLastLinesFromCurrentLog() throws Exception
	{
		JSONObject reply = new JSONObject();
		reply.put("result", Log.getLastLines());
		return reply;
	}
	

	/**
	 * Build a reply message for an exception
	 */
	public static JSONObject makeErrorJson(Exception e) throws Exception
	{
		JSONObject o = new JSONObject();
		o.put("result", "");
		o.put("error", e.getMessage());
		return o;
	}
}
