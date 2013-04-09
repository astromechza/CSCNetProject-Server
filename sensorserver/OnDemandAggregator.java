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
	
	private enum SQLAFUNC {AVG, COUNT, STDDEV, MAX, MIN};
	private static String[] sqlFuncs = {"AVG","COUNT","STDDEV","MAX","MIN"};
	
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
				return SQLAggregationFunction(SQLAFUNC.COUNT, typeId, aggre_group_id, timeFrom, timeTo);
			case "mean":
			case "average":
			case "avg":
				return SQLAggregationFunction(SQLAFUNC.AVG, typeId, aggre_group_id, timeFrom, timeTo);
			case "min":
			case "minimum":
				return SQLAggregationFunction(SQLAFUNC.MIN, typeId, aggre_group_id, timeFrom, timeTo);
			case "max":
			case "maximum":
				return SQLAggregationFunction(SQLAFUNC.MAX, typeId, aggre_group_id, timeFrom, timeTo);
			case "std":
			case "stddev":
			case "stddeviation":
			case "standarddeviation":
				return SQLAggregationFunction(SQLAFUNC.STDDEV, typeId, aggre_group_id, timeFrom, timeTo);
			case "mode":
				return aggregateMode(typeId, aggre_group_id, timeFrom, timeTo);			
			case "median":
				return aggregateMedian(typeId, aggre_group_id, timeFrom, timeTo);		
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
	private static JSONObject SQLAggregationFunction(SQLAFUNC function, int typeId, int sensor_id, Timestamp timeFrom, Timestamp timeTo) 
	{
		// generate sql
		String sql = "SELECT " + sqlFuncs[function.ordinal()] +"(value) FROM readings WHERE type_id = " + typeId + " ";			
		if (sensor_id > -1) sql += " AND sensor_id = " + sensor_id + " ";		
		if (timeFrom != null) sql += " AND time >= '" + timeFrom.toString() + "' ";		
		if (timeTo != null) sql += " AND time <= '" + timeTo.toString() + "' ";
		
		sql += ";";
		
		try 
		{
			Statement s = Database.getInstance().getConnection().createStatement();
			
			ResultSet rs = s.executeQuery(sql);
			if(rs.first())
			{				
				Object r = rs.getObject(1);
				if (r != null)
				{
					return new JSONObject().put("result", rs.getDouble(1));
				}
			}
			return new JSONObject().put("error", "0 Records matched the filter");
			
			
		} 
		catch (SQLException e) 
		{
			Log.error(e + " " + Utils.fmtStackTrace(e.getStackTrace()));
			return MessageHandler.makeErrorJson(e);
		}
		
	}
	
	private static JSONObject aggregateMode(int typeId, int sensor_id, Timestamp timeFrom, Timestamp timeTo)
	{
		// generate sql
		String sql = "SELECT value, COUNT(value) as mode FROM readings WHERE type_id = " + typeId + " ";				
		if (sensor_id > -1) sql += " AND sensor_id = " + sensor_id + " ";		
		if (timeFrom != null) sql += " AND time >= '" + timeFrom.toString() + "' ";		
		if (timeTo != null) sql += " AND time <= '" + timeTo.toString() + "' ";
		
		sql += "GROUP BY value ORDER BY mode DESC LIMIT 1;";
		
		try 
		{
			Statement s = Database.getInstance().getConnection().createStatement();
			
			ResultSet rs = s.executeQuery(sql);
			
			if(rs.first())
			{
				
				double value = rs.getDouble(1);
				int count = rs.getInt(2);
				
				return new JSONObject()
					.put("result", new JSONObject()
							.put("mode", value)
							.put("count", count));
			}
			else
			{
				return new JSONObject().put("error", "0 Records matched the filter");
			}
		} 
		catch (SQLException e) 
		{
			Log.error(e + " " + Utils.fmtStackTrace(e.getStackTrace()));
			return MessageHandler.makeErrorJson(e);
		}
	}
	
	private static JSONObject aggregateMedian(int typeId, int sensor_id, Timestamp timeFrom, Timestamp timeTo)
	{
		// generate sql
		
		String where = "WHERE d.type_id = " + typeId + " ";	
		if (sensor_id > -1) where += " AND d.sensor_id = " + sensor_id + " ";		
		if (timeFrom != null) where += " AND d.time >= '" + timeFrom.toString() + "' ";		
		if (timeTo != null) where += " AND d.time <= '" + timeTo.toString() + "' ";
		
		

		String sql = "SELECT t1.value as median_val FROM (" +
						"SELECT @rownum:=@rownum+1 as `row_number`, d.value " +
						"FROM readings d,  (SELECT @rownum:=0) r " +
						where +
						"ORDER BY d.value " +
					 ") as t1,  " +
					 "( " +
					 	"SELECT count(*) as total_rows " +
					 	"FROM readings d " +
					 	where +
					 ") as t2 " +
					 "WHERE 1 AND t1.row_number=floor(total_rows/2)+1;";	
				
		try 
		{
			Statement s = Database.getInstance().getConnection().createStatement();
			
			ResultSet rs = s.executeQuery(sql);
			
			if(rs.first())
			{
				
				double value = rs.getDouble(1);
				
				return new JSONObject()
					.put("result", value);
			}
			else
			{
				return new JSONObject().put("error", "0 Records matched the filter");
			}
		} 
		catch (SQLException e) 
		{
			Log.error(e + " " + Utils.fmtStackTrace(e.getStackTrace()));
			return MessageHandler.makeErrorJson(e);
		}
	}
	
}
