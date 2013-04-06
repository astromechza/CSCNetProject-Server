package sensorserver.models;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class Log implements IModel {
	
	int sensorId;
	String action;
	long time;
	
	public Log() {} 
	
	public Log(int sensorId, String action, long time) {
		this.sensorId = sensorId;
		this.action = action;
		this.time = time;
	}
	
	public void bindToStatement(PreparedStatement stmt) throws SQLException {

		stmt.setInt(1, sensorId);
		stmt.setString(2, action);
		stmt.setTimestamp(3, new Timestamp(time));
		
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


}
