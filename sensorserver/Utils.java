package sensorserver;

import java.sql.Timestamp;

import org.json.JSONException;
import org.json.JSONObject;

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
	
	public static Timestamp readJSONTimefield(JSONObject o, String fieldname) throws JSONException
	{
		Object tobj = o.get(fieldname);
		if (tobj instanceof String)
		{
			return Timestamp.valueOf((String) tobj);
		}
		else
		{
			return new Timestamp((long)tobj);
		}
	}
}
