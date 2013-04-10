package sensorserver.models;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class Reading implements IModel
{
	private Timestamp time;
	private double value;
	private int sensorId;
	private int typeId;

	public Reading() { }
	
	public Reading(Timestamp time, double value, int sensorId, int typeId) 
	{
		this.time = time;
		this.value = value;
		this.sensorId = sensorId;
		this.typeId = typeId;
	}
	
	public Reading(long millis, double value, int sensorId, int typeId) 
	{
		this.time = new Timestamp(millis);
		this.value = value;
		this.sensorId = sensorId;
		this.typeId = typeId;
	}
		
	public void bindToStatement(PreparedStatement stmt) throws SQLException
	{
		stmt.setTimestamp(1,time);
		stmt.setDouble(2, value);
		stmt.setInt(3, sensorId);
		stmt.setInt(4, typeId);
	}

	public String insertStmt() {
		return "INSERT INTO readings (`time`, `value`, `sensor_id`, `type_id`) VALUES (?,?,?,?); ";
	}

	public String createIfNEStmt() {
		return "CREATE TABLE IF NOT EXISTS readings ( " + 
				"`id` INT NOT NULL AUTO_INCREMENT ," + 
				"`time` DATETIME NOT NULL , " +
				"`value` FLOAT NOT NULL , " +
				"`sensor_id` INT NOT NULL , " +
				"`type_id` INT NOT NULL , " +
				"PRIMARY KEY (`id`));";
	}

	public String dropIfEStmt() {
		return "DROP TABLE IF EXISTS readings;";
	}

	public String tableName() {
		return "readings";
	}
}
