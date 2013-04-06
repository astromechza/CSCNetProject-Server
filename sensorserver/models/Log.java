package sensorserver.models;

public class Log implements IModel {

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
