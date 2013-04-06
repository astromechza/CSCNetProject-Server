package sensorserver.models;


public interface IModel {
	
	public String tableName();
	public String insertStmt();
	public String createIfNEStmt();
	public String dropIfEStmt();
	
	
}
