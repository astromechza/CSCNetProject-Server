import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class ArgParser 
{
	private class Flag
	{
		String shorts, longs;
		String description;
		public Flag(String s, String l, String d)
		{
			shorts = s;
			longs = l;
			description = d;
		}		
	}
	
	private class Option extends Flag
	{
		String defaultValue;
		
		public Option(String s, String l, String d, String def)
		{
			super(s, l, d);
			defaultValue = def;
		}
	}
	
	public List<Flag> possibleFlags = new ArrayList<Flag>();
	public List<Option> possibleOptions = new ArrayList<Option>();
	
	public Set<String> actualFlags = new HashSet<String>();
	public Map<String, String> actualOptions = new HashMap<String, String>();
	
	public ArgParser()
	{
		
	}
	
	public void AddFlag(String name, String description)
	{
		if (name.startsWith("--")) name = name.substring(2);		
		possibleFlags.add(new Flag("-" + name.charAt(0), "--" + name, description));
	}
	
	public void AddOption(String name, String description, String defaultValue)
	{
		if (name.startsWith("--")) name = name.substring(2);		
		possibleOptions.add(new Option("-" + name.charAt(0), "--" + name, description, defaultValue));
	}
	
	public void printUsage()
	{
		System.out.println("Arguments: ");
		for (Option o : possibleOptions)
		{
			System.out.printf(" %-15s : %s \t\t default: '%s' \n", o.longs + "/" + o.shorts, o.description, o.defaultValue.toString());
		}
		
		for (Flag f : possibleFlags)
		{
			System.out.printf(" %-15s : %s \n", f.longs + "/" + f.shorts, f.description);
		}
		System.out.println();
	}
	
	public void parse(String [] args) throws Exception
	{
		
		for (int i=0;i<args.length;i++)
		{
			String trimmed = args[i].trim();
			if (trimmed.length() == 0) continue;
			
			
			
			// It could be an arg/opt
			if(trimmed.charAt(0) == '-')
			{
				boolean done = false;
				for (Flag f : possibleFlags)
				{
					if (f.longs.equalsIgnoreCase(trimmed) || f.shorts.equalsIgnoreCase(trimmed))
					{
						actualFlags.add(f.longs);
						done = true;
						break;
					}
				}
				if (done) continue;
				
				for (Option o : possibleOptions)
				{
					if (o.longs.equalsIgnoreCase(trimmed) || o.shorts.equalsIgnoreCase(trimmed))
					{
						i++;
						try
						{
							String next = args[i];
							actualOptions.put(o.longs, next);	
						}
						catch (ArrayIndexOutOfBoundsException e)
						{
							throw new Exception("'" + o.longs + "' option expected an argument!");
						}
						break;
					} 
				}
				
				
				
			}
			
		}
	}

	public boolean hasFlag(String name)
	{
		return(actualFlags.contains(name));
		
	}
	
	public String getOption(String name)
	{
		if (!name.startsWith("--")) name = "--" + name;
		if (actualOptions.containsKey(name)) return actualOptions.get(name);
		
		for (Option o : possibleOptions)
		{
			if (o.longs.equalsIgnoreCase(name))
			{
				return o.defaultValue;
			} 
		}
		
		return "";
	}
}
