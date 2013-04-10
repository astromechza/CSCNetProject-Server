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

import org.json.JSONArray;
import org.json.JSONObject;

import sensorserver.MessageHandler;
import sensorserver.Utils;
import sensorserver.log.Log;
import sensorserver.models.IModel;
import sensorserver.models.Reading;
import sensorserver.models.ReadingType;
import sensorserver.struct.BiMap;

public class Database 
{
	
	private static Database instance;	
	
	private String URL;
	private String USER;
	private String PASSWORD;
	private String DRIVER = "com.mysql.jdbc.Driver";
	private Connection activeConnection;
	private List<IModel> storableTypes;

	private BiMap<Integer, String> readingTypeCache;
	
	   
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
		
		readingTypeCache = new BiMap<Integer, String>();
		
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
		
		rs.close();
		s.close();
	}
	
	/**
	 * Drop and recreate all the tables. This will detroy all information stored in the tables.
	 */
	public void recreate()
	{
		Statement s = null;
		try
		{
			s = activeConnection.createStatement();
			
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
		}finally{
			try{
				if(s != null)
					s.close();
			}catch(Exception e){}
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
			// Attempt to select something from each table.
			for (IModel m : storableTypes)
			{
				s.execute("SELECT * FROM " + m.tableName() + " LIMIT 1;");
			}
			
			return true;
			
		} catch(SQLException e) { 
			Log.debug(e); 
		}finally{
			try{
				if(s != null)
					s.close();
			}catch(Exception e){}
		}
		
		return false;
	}

	/**
	 * Preload the int-string reading_types from the database. 
	 */
	public void preloadTypes()
	{
		Statement s = null;
		ResultSet rs = null;
		try {
			s = Database.getInstance().activeConnection.createStatement();			
			rs = s.executeQuery("SELECT * FROM reading_types;");
			
			while(rs.next())
			{
				int id = rs.getInt(1);
				String name = rs.getString(2);
				readingTypeCache.put(id, name);
			}
			
			rs.close();
			s.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try{
				if(rs != null)
					rs.close();
				if(s != null)
					s.close();
			}catch(Exception e){}			
		}
		
		Log.debug(readingTypeCache);
	}
	
	/**
	 * Returns the type_id for a given type. If the type:type_id is not present in the cache, it is 
	 * inserted into the database and the cache is updated.
	 * 
	 * Synchronized to avoid 2 clients adding the same type at the same time.
	 */
	public synchronized int getTypeIdFromStr(String type, boolean createIfNotExist)
	{
		Integer i = readingTypeCache.getKeyForValue(type);
		PreparedStatement ps = null;
		Statement s = null;
		ResultSet rs = null;
		
		if (i==null && createIfNotExist)
		{
			Log.debug("Reading type '"+type+"' not in cache/database.");
			try
			{
				ReadingType rt = new ReadingType(type);
				
				ps = Database.getInstance().activeConnection.prepareStatement(rt.insertStmt());
				
				rt.bindToStatement(ps);
				
				int count = ps.executeUpdate();
				
				if (count != 1) throw new SQLException();
					
				s = Database.getInstance().activeConnection.createStatement();
				rs = s.executeQuery("SELECT LAST_INSERT_ID();");
				rs.first();
				int id = rs.getInt(1);
				
				Log.debug("Reading type '"+type+"' = " + id);
				
				readingTypeCache.put(id, type);		
				
				return id;
			} 
			catch (SQLException e)
			{
				Log.error("Could not insert new reading_type.");
			}finally{
				try{
					if(ps != null)
						ps.close();
					if(rs != null)
						rs.close();
					if(s != null)
						s.close();
				}catch(Exception e){}				
			}
			
		}
		else
		{
			return i.intValue();
		}

		return -1;
	}

	/**
	 * Returns the type of a given type_id. If the type_id is not in the cache/database, it is unknown.
	 */
	public String getTypeFromId(int type) 
	{
		String s = readingTypeCache.getValueForKey(type);
		return (s == null) ? "unknown" : s;
	}
	
	/**
	 * Insert an action line for the given group id
	 */
	public Integer insertLog(int groupId, String action){
		PreparedStatement stmt = null;
		try{
			
			sensorserver.models.Log l = new sensorserver.models.Log(groupId, action, new Timestamp(Calendar.getInstance().getTime().getTime()));
			
			stmt = activeConnection.prepareStatement(l.insertStmt());
			
			l.bindToStatement(stmt);
			
			int newRows = stmt.executeUpdate();
			return newRows;		
		}catch(SQLException e){
			Log.error("SQL error in Database.insertLog. Failed to log action to database.");
			Log.error(e + " " + Utils.fmtStackTrace(e.getStackTrace()));
		}finally{
			try{
				if(stmt != null)
					stmt.close();
			}catch(Exception e){}			
		}
		
		return null;
	}	
	
	/**
	 * 
	 * @param in
	 * @return
	 */
	public JSONObject queryLogTable(JSONObject in)   throws Exception
	{
		
		//defaults
		Timestamp time_from = null;									
		Timestamp time_to = null;		
		long limit = 20;
		List<Integer> group_ids = new ArrayList<Integer>();
		
		if(in.has("params"))
		{
			JSONObject params = in.getJSONObject("params");
			
			if (params.has("time_from"))
			{
				time_from = Timestamp.valueOf(params.getString("time_from"));
			}
			
			if (params.has("time_to"))
			{
				time_to = Timestamp.valueOf(params.getString("time_to"));
			}
			
			if (params.has("limit"))
			{
				long l = params.getLong("limit");
				if (l >= 1) limit = l; 
			}
			
			if (params.has("group_ids"))
			{
				JSONArray ids = params.getJSONArray("group_ids");
				
				for(int i=0;i<ids.length();i++)
				{
					group_ids.add(ids.getInt(i));
				}
				
			}
		}
		
		// Construct statement
		String statement = "SELECT * FROM logs ";
		
		String whereClause = "";
		if(time_from != null || time_to != null)
		{
			whereClause += "(";
			
			if(time_from != null) whereClause += "time >= '"+time_from+"' ";
			if(time_to != null) whereClause += "time <= '"+time_to+"' ";
			
			whereClause += ") AND ";
		}		
		
		if (group_ids.size()>0)
		{
			whereClause += "(";

			for (int i=0;i<group_ids.size();i++)
			{
				if (i>0) whereClause += "OR ";
				whereClause += "sensor_id = " + group_ids.get(i) + " ";			
			}
			
			whereClause += ") ";
		}
		
		if(whereClause.length()>0) statement += "WHERE " + whereClause;
		
		statement += "ORDER BY id DESC LIMIT " + limit + ";";
		
		JSONObject reply = new JSONObject();
		
		// now do actual query
		Statement sql = null;
		ResultSet rows = null;
		
		try 
		{
			
			int lineCount = 0;
			JSONArray lines = new JSONArray();
			
			sql = Database.getInstance().activeConnection.createStatement();
			rows = sql.executeQuery(statement);
			
			while(rows.next())
			{
				lineCount += 1;
				sensorserver.models.Log l = new sensorserver.models.Log(rows);
				
				lines.put(l.toJSON());
			}
			
			reply.put("result", new JSONObject().put("line_count", lineCount).put("lines", lines));
			
			
		} 
		catch (SQLException e) 
		{
			Log.error(e + " " + Utils.fmtStackTrace(e.getStackTrace())+". SQL Query: "+statement);
			return MessageHandler.makeErrorJson(e);
		}finally{
			try{
				if(rows != null)
					rows.close();
				if(sql != null)
					sql.close();
			}catch(Exception e){}			
		}
		
		return reply;
	}
}
