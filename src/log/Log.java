package log;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class Log {
	
	
	// LOG LEVEL ENUM
	public enum LogLevel { DEBUG, INFO, WARNING, ERROR, CRITICAL, NONE };
	
	// VARIABLES
	
	// LEVEL at which to log to System.out
	private static LogLevel sysOutLevel = LogLevel.DEBUG;
	
	// LEVEL at which to send to the file
	private static LogLevel fileLevel = LogLevel.DEBUG;
	private static File currentlogfile = null;
	private static Date nextrotatetime = null;
	private static SimpleDateFormat filenameformat = new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat logdateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
	// INITIALISE the log file
	// Log files are rotated daily 
	public static void init() 
	{
		rotate();		
	}
	
	// Start a new log file
	public static void rotate()
	{
		Date now = new Date();
		
		// create new filename
		String filename = filenameformat.format(now) + ".log";
		File f = new File(filename);
		currentlogfile = f;
		if (f.exists())
		{
			// do nothing? we shouldn't get this far as files should only rotate once the new date is reached.
		}
		else
		{
			try
			{
				BufferedWriter bw = new BufferedWriter(new FileWriter(f));
				
				bw.write("#Software: SensorServer\n");
				bw.write("#Date: " + now + "\n");
				bw.write("#Fields: time level message\n");				
				
				bw.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.add(Calendar.DATE, 1);		

		nextrotatetime = c.getTime();
		
		
	}
	
	// THE ALL IMPORTANT METHOD
	public static void log(LogLevel lvl, Object message)
	{
		if (lvl.ordinal() >= sysOutLevel.ordinal())
		{
			slog(lvl, message);
			
		}
		if (currentlogfile != null && lvl.ordinal() >= fileLevel.ordinal())
		{
			flog(lvl, message);
		}
	}
	
	private static void slog(LogLevel lvl, Object message)
	{
		Date now = new Date();		
		System.out.println(logdateformat.format(now) + " : " + lvl + " : " + message.toString());
	}
	
	private static void flog(LogLevel lvl, Object message)
	{
		Date now = new Date();
		if (now.after(nextrotatetime)) rotate();
		
		try
		{
			BufferedWriter bw = new BufferedWriter(new FileWriter(currentlogfile, true));
			
			bw.write(logdateformat.format(now) + " : " + lvl + " : " + message.toString() + "\n");
			
			bw.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
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
