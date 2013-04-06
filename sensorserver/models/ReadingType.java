package sensorserver.models;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ReadingType implements IModel
{
	private String name;
	
	public ReadingType() {}
	
	public ReadingType(String name) {
		this.name = name;
	}


	public void bindToStatement(PreparedStatement stmt) throws SQLException {
		stmt.setString(1, name);		
	}

	public String insertStmt() {
		return "INSERT INTO reading_types (`name`) VALUES (?); ";
	}

	public String createIfNEStmt() {
		return "CREATE TABLE IF NOT EXISTS reading_types ( " + 
				"`id` INT NOT NULL AUTO_INCREMENT , " +
				"`name` VARCHAR(64) NOT NULL , " + 
				"PRIMARY KEY (`id`));";
	}

	public String dropIfEStmt() {
		return "DROP TABLE IF EXISTS reading_types;";
	}

	public String tableName() {
		return "reading_types";
	}



}
