package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import log.Log;


public class Database 
{
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
			Log.error(e.getMessage());
			System.exit(-1);
		}
		return con;
	}
	
	
}
