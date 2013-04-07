package sensorserver;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Properties;

import sensorserver.database.Database;
import sensorserver.log.Log;



public class RunServer {
	
	public static void main(String[] argarray) {
		
		Log.init();
		
		// create the argument parser
		ArgParser args = new ArgParser();
		args.AddFlag("debug", "Force the server into debug mode.");
		args.AddFlag("rebuild", "Drop and recreate the database tables.");
		args.AddFlag("help", "Print this usage message.");
		args.AddOption("file", "Path to the file containing server properties. View example.server.properties for more information", "server.properties");
				
		try {
			// Attempt to parse the command line arguments. Bail if any exceptions occur
			args.parse(argarray);
		} catch (Exception e) {
			// Argument errors are critical
			System.err.println(e.getMessage());
			args.printUsage();							// print help string just in case
			System.exit(-1);
		}
				
		// handle --help option
		if (args.hasFlag("--help")) {
			args.printUsage();
			System.exit(0);
		}
		
		Log.info("--------------------------");
		Log.info(" Starting SensorServer ");
		Log.info("--------------------------");
		
		Log.info("Reading configuration file");
		
		Properties configuration = new Properties();
		
		try {
			
			String path = args.getOption("file");
			path = new URI(path).normalize().getPath();
			Log.debug("configuration file path = " + path);			
			configuration.load(new FileInputStream(path));
			
		} catch (IOException | URISyntaxException e) {
			Log.critical("Aborting: " + e +  " " + Utils.fmtStackTrace(e.getStackTrace()));
			System.exit(-1);
		}

		Log.debug("configuration: " + configuration);		
		
		// Test connection stuff
		try 
		{
			Database.initInstance(
					configuration.getProperty("database_url"),
					configuration.getProperty("database_user"),
					configuration.getProperty("database_password"));
			
			Database.getInstance().getConnection();
		} 
		catch (Exception e) 
		{			
			Log.critical(e + " " + Utils.fmtStackTrace(e.getStackTrace()));
			System.exit(-1);
		}			
		
		if( (!Database.getInstance().hasCorrectTables()) || args.hasFlag("rebuild")) 
		{
			Log.info("Rebuilding database:");
			Database.getInstance().recreate();
		}
		else
		{
			Database.getInstance().preloadTypes();
		}
		
		int port = Integer.parseInt(configuration.getProperty("preferred_port"));			

		Log.info("Starting socket listener.");	
		
		new Server(port);
		
		Log.info("Shutting down..");
		
		// This is never reached since the server does not shutdown nicely
		// TODO: shutdown nicely
		try 
		{
			Database.getInstance().getConnection().close();
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
			Log.error(e + " " + Utils.fmtStackTrace(e.getStackTrace()));
		}	
		
	}

}
