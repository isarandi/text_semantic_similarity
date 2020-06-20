package util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

public class IO
{
	
	public static void println(String text)
	{
		System.out.println(text);
	}

	public static Map<String,String> mapFromFile(Path filePath, String regex, int keyGroup, int valueGroup)
	{
		Map<String,String> map = new HashMap<>();
		
		Pattern pattern = Pattern.compile(regex);

		List<String> lines = lineListFromFile(filePath.toString());
				
		for (String line: lines)
		{
			Matcher matcher = pattern.matcher(line);
			if (matcher.matches())
			{
				map.put(matcher.group(keyGroup), matcher.group(valueGroup));
			}
		}
		
		return map;
	}
	
	public static Map<String,String> mapFromFile(String filePath)
	{
		return mapFromFile(Paths.get(filePath), "(.+?)\t(.+?)", 1, 2);
	}
		
	public static void mapToFile(Map<String,String> map, Path filePath)
	{
		List<String> lines = new ArrayList<>();
		List<String> keys = new ArrayList<>(map.keySet());
		Collections.sort(keys);
				
		for (String key: keys)
		{
			lines.add(key+"\t"+map.get(key));
		}
		
		linesToFile(lines, filePath.toString());
	}

	public static List<String> lineListFromFile(String filePath)
	{
		List<String> lines = null;
		try
		{
			lines = Files.readAllLines(Paths.get(filePath), Charset.forName("utf8"));
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		return lines;
	}
	
	public static Set<String> lineSetFromFile(String filePath)
	{
		return new HashSet<>(lineListFromFile(filePath));
	}
	
	
	public static void linesToFile(Set<String> lines, String fileName)
	{
		List<String> sorted = new ArrayList<>(lines);
		Collections.sort(sorted);
		linesToFile(sorted, fileName);
	}
	
	public static void linesToFile(List<String> lines, String fileName)
	{
		try
		{
			com.google.common.io.Files.write(Joiner.on("\n").join(lines), new File(fileName), Charset.forName("utf8"));
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public static SetMultimap<String,String> multimapFromFile(Path filePath, String regex, int keyGroup, int valueGroup)
	{
		SetMultimap<String,String> resultingMultiMap = HashMultimap.create();
		
		Pattern pattern = Pattern.compile(regex);
		List<String> lines = lineListFromFile(filePath.toString());
				
		for (String line: lines)
		{
			Matcher matcher = pattern.matcher(line);
			if (matcher.matches())
			{
				resultingMultiMap.put(matcher.group(keyGroup), matcher.group(valueGroup));
			}
		}
		
		return resultingMultiMap;
	}

}
