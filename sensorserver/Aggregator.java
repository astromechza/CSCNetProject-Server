package sensorserver;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import org.json.JSONArray;
import org.json.JSONObject;

import sensorserver.database.Database;
import sensorserver.log.Log;

public class Aggregator {
	
	private static long lastGenerationTime = 0;
	private static JSONArray dataSummary;

	public static JSONArray getDataSummary(){
		
		// Only generate new summary if there have been new readings inserted.
		if(requireNewGeneration()){
			Log.debug("Creating new data summary");
			generateSummary();
			lastGenerationTime = System.currentTimeMillis();
		}else{
			Log.debug("Existing data summary is still valid.");
		}
		
		return dataSummary;		
	}
	
	// Returns true if there has been a new batch of readings uploaded
	// since we last generated the data summary.
	private static boolean requireNewGeneration(){	
		Statement s = null;
		ResultSet rs = null;
		try {
			s = Database.getInstance().getConnection().createStatement();		
			// Get the time the last set of readings received.
			rs = s.executeQuery("SELECT time FROM logs WHERE action = 'new_readings' ORDER BY id DESC LIMIT 1");
			
			// Ensure there is at least one log entry.
			if (rs.first())	{
				Timestamp time = rs.getTimestamp("time");				
				return lastGenerationTime < time.getTime();
			}else{
				return true;
			}
		}catch (SQLException e) {
			sensorserver.log.Log.error(e);
		}finally{
			try{
				if(rs != null)
					rs.close();
				if(s != null)
					s.close();
			}catch(Exception e){}			
		}
		
		return true;
	}
	
	private static void generateSummary(){
		dataSummary = new JSONArray();
		
		Log.info("Generating new data summary.");
		
		Statement s = null;
		Statement s2 = null;
		ResultSet rs = null;
		try {
			s = Database.getInstance().getConnection().createStatement();
			s2 = Database.getInstance().getConnection().createStatement();
			rs = s.executeQuery("SELECT type_id, AVG(value) AS avg, MIN(value) AS min, MAX(value) AS max, STDDEV(value) AS stddev FROM readings GROUP BY type_id");
			
			while(rs.next()){
				JSONObject set = new JSONObject();
				int type_id = rs.getInt("type_id");
				String type = Database.getInstance().getTypeFromId(type_id);
				set.put("type", type);
				set.put("type_id", type_id);
				set.put("mean", rs.getDouble("avg"));
				set.put("min", rs.getDouble("min"));
				set.put("max", rs.getDouble("max"));
				set.put("stddev", rs.getDouble("stddev"));
				
				// Modes and medians need to be calculated differently
				// ...mode
				ResultSet modeSet = s2.executeQuery("SELECT value, COUNT(value) AS count FROM readings " +
						"WHERE type_id = "+ type_id +" " +
						"GROUP BY value HAVING count > 1 AND count = " +
							"(SELECT COUNT(value) AS c FROM readings " +
							"WHERE type_id = "+ type_id +" " +
							"GROUP BY value " +
							"ORDER BY c DESC LIMIT 1) ");
				
				JSONArray modes = new JSONArray();
				while(modeSet.next()){
					modes.put(modeSet.getDouble("value"));
				}

				set.put("mode", modes);				
				modeSet.close();
				
				// ...median
				ResultSet count = s2.executeQuery("SELECT COUNT(id) AS count FROM readings WHERE type_id = "+ type_id);
				count.first();
				int numReadings = count.getInt("count");
				count.close();
				
				// Median is different if there is an even or odd number of rows.
				ResultSet medianQuery;
				if(numReadings % 2 == 0){
					int offset = Math.max((numReadings/2) - 1, 0);
					medianQuery = s2.executeQuery("SELECT AVG(value) AS median FROM (SELECT value FROM readings WHERE type_id = "+type_id+" ORDER BY value ASC LIMIT "+offset+", 2) AS a");
				}else{
					int offset = numReadings/2;
					medianQuery = s2.executeQuery("SELECT value AS median FROM readings WHERE type_id = "+type_id+" ORDER BY value ASC LIMIT "+offset+", 1");
				}				
				medianQuery.first();
				
				double median = medianQuery.getDouble("median");
				set.put("median", median);
				medianQuery.close();
				
				dataSummary.put(set);
			}			
		} catch (SQLException e) {
			sensorserver.log.Log.error(e + " "  + Utils.fmtStackTrace(e.getStackTrace()));
		}finally{
			try{
				if(rs != null)
					rs.close();
				if(s != null)
					s.close();
				if(s2 != null)
					s2.close();
			}catch(Exception e){}			
		}
	}
}
