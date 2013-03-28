
public class RunServer {

	/* RUNSERVER
	 *  TODO Args:
	 *  1) which port to listen on.
	 *  2) log level (override default)
	 *  3) ...
	 *  
	 *  These could also be populated via config file
	 * 
	 */
	
	public static void main(String[] args) {
		
		// create the argument parser
		ArgParser a = new ArgParser();
		a.AddFlag("debug", "Force the server into debug mode.");
		a.AddFlag("help", "Print this usage message.");
		a.AddOption("file", "Path to the file containing server properties. View example.server.properties for more information", "server.properties");
		
		Log.debug("Parsing arguments");
		
		// Attempt to parse the command line arguments. Bail if any exceptions occur
		try {
			a.parse(args);
		} catch (Exception e) {
			// Argument errors are critical
			Log.critical(e.getMessage());
			a.printUsage();							// print help string just in case
			Log.info("Aborting");
			System.exit(-1);
		}
				
		if (a.hasFlag("--help")) a.printUsage();
		
		Log.info("Reading configuration file");
		
		// READ config file
		// 1) database filename
		// 2) database credentials
		// 3) database loglevel
		// 4) syslog level 				THIS IS OVERRIDDEN BY --debug
		
		Log.info("Initialise database monitor");
		
		// INITIALISE DATABASE SINGLETON
		// check if it exists
		// clean up ? stale logs/connections
		// run connection tests
		// bail if not good		
		
		Log.info("Initialise logging");
		
		// INITIALISE LOGGING TABLES/FILES
		// setup logging table in database dump/drop/create
		// create static LOG class
		// set logging level
		// test ( write to log, read from log )
		
		Log.info("Starting socket listener");
		
		// TODO: pass this port in a variable.
		new Server(3000);
	}

}
