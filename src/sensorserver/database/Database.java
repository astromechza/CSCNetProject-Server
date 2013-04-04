package sensorserver.database;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import sensorserver.Utils;
import sensorserver.log.Log;



public class Database 
{
	
	private static Database instance;	
	
	private String URL;
	private String USER;
	private String PASSWORD;
	private String DRIVER = "com.mysql.jdbc.Driver";
	   
	public Database(String url, String user, String password)
	{
		URL = url;
		USER = user;
		PASSWORD = password;
	}
	
	// == singleton code ==
	public static void initInstance(String url, String user, String pass)
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
		Connection con = null;
		try
		{
			Class.forName(DRIVER);
			con = DriverManager.getConnection(URL, USER, PASSWORD);
		}
		catch (ClassNotFoundException e)
		{
			Log.critical(e.getMessage());
			Log.critical(Utils.fmtStackTrace(e.getStackTrace()));
			System.exit(-1);
		}
		return con;
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
	
	
}
