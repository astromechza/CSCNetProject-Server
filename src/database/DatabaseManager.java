package database;

import java.io.File;

import log.Log;
import log.Log.LogLevel;


import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;


public class DatabaseManager {

	private static Database currentDB;
		
	public static void openDB(String fn) 
	{
		currentDB = new Database(new File(fn));
	}
	
	public static boolean checkDB() 
	{
		
		
		
		return true;
		
	}


	public static void createDB()
	{
		//currentDB.createTable(new LogMessage());		
	}
	
	
	
	
	
	
	
	
	
	
}
