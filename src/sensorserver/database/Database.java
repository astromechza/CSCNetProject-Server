package sensorserver.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import sensorserver.Utils;
import sensorserver.log.Log;
import sensorserver.models.IModel;
import sensorserver.models.Reading;
import sensorserver.models.ReadingType;

public class Database 
{
	
	private static Database instance;	
	
	private String URL;
	private String USER;
	private String PASSWORD;
	private String DRIVER = "com.mysql.jdbc.Driver";
	private Connection activeConnection;
	private List<IModel> storableTypes;
	
	   
	public Database(String url, String user, String password) throws SQLException
	{
		URL = url;
		USER = user;
		PASSWORD = password;
		
		try
		{
			Class.forName(DRIVER);
			activeConnection = DriverManager.getConnection(URL, USER, PASSWORD);
		}
		catch (ClassNotFoundException e)
		{
			Log.critical(e.getMessage());
			Log.critical(Utils.fmtStackTrace(e.getStackTrace()));
			System.exit(-1);
		}
		
		storableTypes = new ArrayList<IModel>();
		storableTypes.add(new Reading());
		storableTypes.add(new ReadingType());
		storableTypes.add(new sensorserver.models.Log());
		
		
	}
	
	// == singleton code ==
	public static void initInstance(String url, String user, String pass) throws SQLException
	{
		instance = new Database(url, user, pass);
	}

	public static Database getInstance() 
	{ 
		return instance; 
	}
	// == end of singleton code ==
	
	/**
	 * Get a connection to the current database. This exit(-1)'s if the 
	 * driver class is not found as this is a critical issue.
	 * 
	 * @return an open connection.
	 * @throws SQLException if the connection fails.
	 */
	public Connection getConnection() throws SQLException
	{			
		return activeConnection;		
	}

	/**
	 * Log some details about the connected database. This includes
	 * the url of the database, the tables, and the number of readings
	 * present in the database.
	 * 
	 * @throws SQLException if the connection failed or if any of the 
	 * sql statements failed.
	 */
	public void details() throws SQLException 
	{
		Connection con = getConnection();
		Log.info("Connected to database: " + con.getMetaData().getURL());
		
		Statement s = con.createStatement();
		ResultSet rs = s.executeQuery("SHOW TABLES;");
		String tables = "Tables: ";
		while(rs.next())
		{
			tables += "'" + rs.getString(1) + "' ";
		}
		Log.info(tables);
		
		rs = s.executeQuery("SELECT * FROM readings;");
		
		int count = 0;
		while(rs.next()) count++;
		
		Log.info("Number of Readings: " + count);		
	}
	
	/**
	 * Drop and recreate all the tables. This will detroy all information stored in the tables.
	 */
	public void recreate()
	{
		try
		{
			Statement s = activeConnection.createStatement();
			
			// for each known model, destroy its table
			for (IModel m : storableTypes)
			{
				Log.debug("Dropping `" + m.tableName() + "` table.");
				s.execute(m.dropIfEStmt());
				Log.debug("Creating `" + m.tableName() + "` table.");
				s.execute(m.createIfNEStmt());
			}
		}catch(SQLException e){
			Log.error("SQL error when dropping tables.");
			Log.error(e + " " + Utils.fmtStackTrace(e.getStackTrace()));
		}	
		
	}

	/**
	 * Check to see whether all tables exist. 
	 * TODO change this to hasCorrectSchema and use DESCRIBE() to check column defns.
	 * @return whether all tables did exist.
	 */
	public boolean hasCorrectTables() 
	{
		Statement s = null;
		// Separate try/catch for statement creation. We want createStatement() issues to be caught and exited on.
		// but we don't want the SQLExceptions from the SELECT statements to be shown.
		try
		{
			s = activeConnection.createStatement();					
		}
		catch(SQLException e)
		{
			Log.critical(e + " " + Utils.fmtStackTrace(e.getStackTrace()));
			System.exit(-1);
		}
		
	
		try
		{
			// Attempt to list the data in all the tables.
			for (IModel m : storableTypes)
			{
				s.execute("SELECT * FROM " + m.tableName() + ";");
			}
			return true;
			
		} catch(SQLException e) { Log.debug(e); }
		
		return false;
	}

	
	public int insertReading(int groupId, String type, double value) throws SQLException{
		PreparedStatement stmt = activeConnection.prepareStatement("INSERT INTO `readings` (group_id, reading_type, reading_value, created_at) VALUES (?, ?, ?, ?);");

		stmt.setInt(1, groupId);
		stmt.setString(2, type);
		stmt.setDouble(3, value);
		stmt.setTimestamp(4, new Timestamp(Calendar.getInstance().getTimeInMillis()));
		int newRows = stmt.executeUpdate();
		
		// Log this upload.
		insertLog(groupId, "new_readings");
		
		return newRows;
	}
	
	public Integer insertLog(int groupId, String action){
		try{
			PreparedStatement stmt = activeConnection.prepareStatement("INSERT INTO `logs` (group_id, action, created_at) VALUES (?, ?, ?);");
	
			stmt.setInt(1, groupId);
			stmt.setString(2, action);
			stmt.setTimestamp(3, new Timestamp(Calendar.getInstance().getTimeInMillis()));
			int newRows = stmt.executeUpdate();
			return newRows;		
		}catch(SQLException e){
			Log.error("SQL error in Database.insertLog. Failed to log action to database.");
			Log.error(e + " " + Utils.fmtStackTrace(e.getStackTrace()));
		}
		
		return null;
	}

	

	
}
