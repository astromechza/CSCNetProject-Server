import java.io.File;


import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;


public class DatabaseManager {

	private static String filename;
	
	
	
	public static void setFile(String fn) 
	{
		filename = fn;
	}
	
	public static boolean checkDB() {
		
		SQLiteConnection db = new SQLiteConnection(new File(filename));
		try {
			db.openReadonly();
			SQLiteStatement st;
			st = db.prepare("SELECT * FROM log;");
			st = db.prepare("SELECT * FROM readings;");	
			
		} catch (SQLiteException e) {
			Log.debug("CheckDB Failed with " + e.getMessage());
			return false;
		} finally {
			db.dispose();
		}
		
		return true;
		
	}


	public static void createDB() 
	{
		Log.info("Creating new database");
		
		SQLiteConnection db = new SQLiteConnection(new File(filename));
		try {
			db.open(true);
			SQLiteStatement st;
			
		} catch (SQLiteException e) {
			Log.debug("CheckDB Failed with " + e.getMessage());
		} finally {
			db.dispose();
		}
		
	}
}
