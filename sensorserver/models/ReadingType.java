package sensorserver.models;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReadingType implements IModel
{
	private int id;
	private String name;
	
	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public ReadingType() {}
	
	public ReadingType(String name) {
		this.name = name;
	}

	public ReadingType(ResultSet row) throws SQLException
	{
		this.id = row.getInt(1);
		this.name = row.getString(2);
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
