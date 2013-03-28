import sun.tools.jar.CommandLine;


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
		
		Log.debug("Parsing arguments");
				
		
		// Argument parsing
		// 1) get configuration file
		// 2) check for --debug
		
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
		
//		ConnectionServer cs = new ConnectionServer(9876);
//		cs.run();
	}

}
