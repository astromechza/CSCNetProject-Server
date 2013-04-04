package database;

import java.util.List;

import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;

public interface Storable {
	
	public String getTableName();
	public String getColumnDefs();
	public String insertStmt();
	public void bindToStmt(SQLiteStatement s) throws SQLiteException;
	
	
	
}
