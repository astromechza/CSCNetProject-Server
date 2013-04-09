package sensorserver;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import org.json.JSONObject;

import sensorserver.database.Database;
import sensorserver.log.Log;

/**
 * Class to support on demand aggregation of specific ranges and types of readings
 */
public class OnDemandAggregator 
{
	
	/**
	 * 
	 * @param parameters
	 * A JSONObject like:
	 * {
	 *     "method" : "aggregate",
	 *     "group_id : X,
	 *     "params" : {
	 *         "aggregation" : "mean"
	 *         "type" : "temperature"
	 *         "group_id" : X
	 *         "time_from" : "ISO time" (optional)
	 *         "time_to" : "ISO time" (optional)
	 *     }
	 * }
	 * 
	 * @return the result of the aggregation
	 */
	public static JSONObject aggregate(JSONObject in)
	{
		JSONObject parameters = in.getJSONObject("params");
		
		// process type argument
		String readingType = parameters.getString("type");		
		int typeId = Database.getInstance().getTypeIdFromStr(readingType, false);
		if (typeId == -1)
		{
			return new JSONObject().put("error", "Unknown reading type '" + readingType + "'");
		}
		
				
		// process times
		Timestamp timeFrom = null;
		Timestamp timeTo = null;
		
		if(parameters.has("time_from"))
		{
			timeFrom = Utils.readJSONTimefield(parameters, "time_from");
		}
		
		if(parameters.has("time_to"))
		{
			timeTo = Utils.readJSONTimefield(parameters, "time_to");
		}
		
		//process group/sensor id
		int aggre_group_id = -1;
		if(parameters.has("group_id"))
		{
			aggre_group_id = parameters.getInt("group_id");
		}
		
		
		// process method argument
		String aggregationMethod = parameters.getString("aggregation");
				
		switch(aggregationMethod)
		{
			case "count":
				return aggregateCount(typeId, aggre_group_id, timeFrom, timeTo);
			default:
				return new JSONObject().put("error", "Unsupported aggregation method '" + aggregationMethod + "'");
		}
	}

	/**
	 * 
	 * @param typeId			The type of reading
	 * @param sensor_id			The sensor id that took the readings(optional, ignore = -1)
	 * @param timeFrom			From time							(optional, ignore = null)
	 * @param timeTo			To time								(optional, ignore = null)
	 * @return
	 */
	private static JSONObject aggregateCount(int typeId, int sensor_id, Timestamp timeFrom, Timestamp timeTo) 
	{
		// generate sql
		String sql = "SELECT COUNT(*) FROM readings ";
		
		if ((sensor_id > -1) || (timeFrom != null) || (timeTo != null)) sql += "WHERE ";
		
		if (sensor_id > -1) sql += "sensor_id = " + sensor_id + " ";
		
		if (timeFrom != null) sql += "time >= '" + timeFrom.toString() + "' ";
		
		if (timeTo != null) sql += "time <= '" + timeTo.toString() + "' ";
		
		sql += ";";
		
		try 
		{
			Statement s = Database.getInstance().getConnection().createStatement();
			
			ResultSet rs = s.executeQuery(sql);
			rs.first();
			
			int count = rs.getInt(1);
			return new JSONObject().put("result", count);
			
		} 
		catch (SQLException e) 
		{
			Log.error(e + " " + Utils.fmtStackTrace(e.getStackTrace()));
			return MessageHandler.makeErrorJson(e);
		}
		
	}
	
	
	
}
