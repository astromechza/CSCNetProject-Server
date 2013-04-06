package sensorserver.tests;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;

import sensorserver.database.Database;

public class TestLogTable 
{
	public static void main(String [] args) throws SQLException
	{
		Database.initInstance("jdbc:mysql://localhost:3306/sensordb", "root", "mafikeng");
		
		Connection c = Database.getInstance().getConnection();
		
		PreparedStatement ps = c.prepareStatement("INSERT INTO logs (`time`, `level`, `message`) VALUES (?,?,?);");
		
		long t1 = System.currentTimeMillis();
		int number = 1000;
		for(int i=0;i<number;i++)
		{
			ps.setTimestamp(1, new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis()));
			ps.setInt(2, 3);
			ps.setString(3, "" + i);
			ps.execute();
			System.out.println(i);
		}
		long t2 = System.currentTimeMillis();
		
		System.out.println("Inserted " + number + " logs in " + (t2-t1));
		
		c.close();
		
		
		
		
		
	}
}
