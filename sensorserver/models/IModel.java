package sensorserver.models;

import java.sql.PreparedStatement;
import java.sql.SQLException;


public interface IModel {
	
	public String tableName();
	public String insertStmt();
	public String createIfNEStmt();
	public String dropIfEStmt();
	
	public void bindToStatement(PreparedStatement stmt) throws SQLException;
	
}
