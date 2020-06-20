package util;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class StringUtil
{
	public static List<String> splitAndFlattenList(Iterable<String> iterable, String splitPattern)
	{
		List<String> parts = Lists.newArrayList();
		for (String s: iterable)
		{
			parts.addAll(Arrays.asList(s.split(splitPattern)));
		}
		return parts;
	}
	
	public static List<String> splitAndFlattenListKeepingGlue(Iterable<String> iterable, String splitString)
	{
		List<String> parts = Lists.newArrayList();
		for (String s: iterable)
		{
			List<String> partsInElement = Arrays.asList(s.split(splitString));
			
			for (String part: partsInElement)
			{
				parts.add(part);
				parts.add(splitString);
			}
			parts.remove(parts.size()-1);
		}
		return parts;
	}
	
	public static Set<String> splitAndFlattenSet(Iterable<String> iterable, String splitPattern)
	{
		Set<String> parts = Sets.newHashSet();
		for (String s: iterable)
		{
			parts.addAll(Arrays.asList(s.split(splitPattern)));
		}
		return parts;
	}
	
}
