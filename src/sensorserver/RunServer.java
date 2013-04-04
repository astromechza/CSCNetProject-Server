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
	
	public static void main(String[] args) {
		
		Log.init();
		
		// create the argument parser
		ArgParser a = new ArgParser();
		a.AddFlag("debug", "Force the server into debug mode.");
		a.AddFlag("help", "Print this usage message.");
		a.AddOption("file", "Path to the file containing server properties. View example.server.properties for more information", "server.properties");
				
		try {
			// Attempt to parse the command line arguments. Bail if any exceptions occur
			a.parse(args);
		} catch (Exception e) {
			// Argument errors are critical
			System.err.println(e.getMessage());
			a.printUsage();							// print help string just in case
			System.exit(-1);
		}
				
		// handle --help option
		if (a.hasFlag("--help")) {
			a.printUsage();
			System.exit(0);
		}
		

		Log.info("-------------------------");
		Log.info("Initialising SensorServer");
		Log.info("-------------------------");
		
		Log.info("Reading configuration file");
		
		Properties configuration = new Properties();
		
		try {
			
			String path = a.getOption("file");
			path = new URI(path).normalize().getPath();
			Log.debug("configuration file path = " + path);			
			configuration.load(new FileInputStream(path));
			
		} catch (IOException | URISyntaxException e) {
			Log.critical(e.getMessage());
			Log.info("Aborting");
			System.exit(-1);
		}
		
		Log.debug("configuration: " + configuration);		
		
		Log.info("Initialise database manager");		
		
		// INITIALISE DATABASE SINGLETON	
		
		
		
		// Test connection stuff
		try 
		{
			Database.initInstance(
					configuration.getProperty("database_url"),
					configuration.getProperty("database_user"),
					configuration.getProperty("database_password"));
			Database.getInstance().details();
		} 
		catch (Exception e) 
		{			
			Log.critical(e + " " + Utils.fmtStackTrace(e.getStackTrace()));
			System.exit(-1);
		}			

		int port = Integer.parseInt(configuration.getProperty("preferred_port"));	
		
			

		Log.debug("Starting socket listener");	
		
		new Server(port);
	}

}
