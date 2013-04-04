import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.logging.Level;

import log.Log;


import com.sun.istack.internal.logging.Logger;

import database.DatabaseManager;


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
		
		//disable silly sqlite4java logging
		java.util.logging.Logger.getLogger("com.almworks.sqlite4java").setLevel(Level.OFF);
		
		//check file exists
		String dbfile = configuration.getProperty("database_file");
		try {
			// normalise variables and dotdots
			dbfile = new URI(dbfile).normalize().getPath();

			DatabaseManager.openDB(dbfile);
			
			File f = new File(dbfile);
			Log.info("Using database '" + dbfile + "'");
			
			// check existence
			if (f.exists() && (!DatabaseManager.checkDB())) 
			{			
				Log.warning("Existing database was invalid. Deleting and recreating.");
				f.delete();
			}
			
			if (!f.exists())
			{
				DatabaseManager.createDB();
			}
			
			
			
			
		} catch (URISyntaxException e) {
			Log.critical("Failed to interpret database=" + dbfile);
			System.exit(-1);
		} catch (NullPointerException e) {
			Log.critical("'database_file' value not found in configuration file.");
			System.exit(-1);
		}	

		
		// INITIALISE DATABASE SINGLETON
		// check if it exists
		// clean up ? stale logs/connections
		// run connection tests
		// bail if not good		
		
		
		Log.info("Starting socket listener");
		
			for(int i=0;i<1000;i++)
			{
				Log.info(i);
			}
		
		int port = Integer.parseInt(configuration.getProperty("port"));	
		
		new Server(port);
	}

}
