package sensorserver.models;

public class Reading implements IModel
{

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
