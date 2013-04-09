package sensorserver.models;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.json.JSONObject;

public class Log implements IModel {
	
	int sensorId;
	String action;
	Timestamp time;
	
	public Log() {} 
	
	public Log(int sensorId, String action, Timestamp time) {
		this.sensorId = sensorId;
		this.action = action;
		this.time = time;
	}
	
	public Log(ResultSet rs) throws SQLException
	{
		this.sensorId = rs.getInt(2);
		this.action = rs.getString(3);
		this.time = rs.getTimestamp(4);
	}
	
	public void bindToStatement(PreparedStatement stmt) throws SQLException {

		stmt.setInt(1, sensorId);
		stmt.setString(2, action);
		stmt.setTimestamp(3, time);
		
	}
	
	

	public String insertStmt() {
		return "INSERT INTO `logs` (sensor_id, action, time) VALUES (?, ?, ?);";
	}

	public String createIfNEStmt() {
		return "CREATE TABLE IF NOT EXISTS logs ( " + 
				"`id` INT NOT NULL AUTO_INCREMENT , " +
				"`sensor_id` INT NOT NULL , " +
				"`action` VARCHAR(255) NOT NULL , " + 
				"`time` DATETIME NOT NULL , " +
				"PRIMARY KEY (`id`));";
	}

	public String dropIfEStmt() {
		return "DROP TABLE IF EXISTS logs;";
	}

	public String tableName() {
		return "logs";
	}
	
	public JSONObject toJSON()
	{
		return new JSONObject()
			.put("time",time.toString())
			.put("group_id",sensorId)
			.put("action", action);
	}


}
