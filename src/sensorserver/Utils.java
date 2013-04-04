package sensorserver;

public class Utils 
{
	public static String fmtStackTrace(StackTraceElement[] st)
	{
		String out = "";
		for(int i =0;i<st.length;i++)
		{
			if(i>0) out += "\n";
			out+=st[i];
		}
		return out;
	}
}
