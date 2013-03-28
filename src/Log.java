import java.util.Date;


public class Log {
	
	
	// LOG LEVEL ENUM
	public enum LogLevel { DEBUG, INFO, WARNING, ERROR, CRITICAL, NONE };
	
	// VARIABLES
	
	// LEVEL at which to log to System.out
	private static LogLevel sysOutLevel = LogLevel.DEBUG;
	
	// LEVEL at which to send to the database
	//  control is passd over to the database manager because date time may need to be 
	//  formatted correctly and table names might be specific.
	private static LogLevel dbLevel = LogLevel.NONE;
	
	
	
	
	// THE ALL IMPORTANT METHOD
	public static void log(LogLevel lvl, Object message)
	{
		Date now = new Date();
		if (lvl.ordinal() >= sysOutLevel.ordinal())
		{
			// TODO format time properly
			System.out.println(now + " : " + lvl + " : " + message.toString());
		}
		if (lvl.ordinal() >= dbLevel.ordinal())
		{
			// TODO insert into database
			// DB.log(now, lvl, message)
		}
	}
	
	
	
	// MORE STATIC METHODS
	public static void debug(Object message)
	{
		log(LogLevel.DEBUG, message);
	}
	
	public static void info(Object message)
	{
		log(LogLevel.INFO, message);
	}
	
	public static void warning(Object message)
	{
		log(LogLevel.WARNING, message);
	}
	
	public static void error(Object message)
	{
		log(LogLevel.ERROR, message);
	}
	
	public static void critical(Object message)
	{
		log(LogLevel.CRITICAL, message);
	}
}
