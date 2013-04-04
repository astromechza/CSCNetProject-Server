package sensorserver.database;


public interface Storable {
	
	public String getTableName();
	public String getColumnDefs();
	public String insertStmt();
	
	
	
}
