package util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Predicate;

public class StringPredicates
{
	public static Predicate<String> lengthLessThan(final int lengthLessThanThis)
	{
		return new Predicate<String>()
		{

			@Override
			public boolean apply(String str)
			{
				return str.length() < lengthLessThanThis;
			}
		};
	}
	
	public static Predicate<String> endsWith(final String ending)
	{
		return new Predicate<String>()
		{

			@Override
			public boolean apply(String str)
			{
				return str.endsWith(ending);
			}
		};
	}
	
	public static Predicate<String> startsWith(final String ending)
	{
		return new Predicate<String>()
		{
			@Override
			public boolean apply(String str)
			{
				return str.startsWith(ending);
			}
		};
	}
	
	public static Predicate<String> matchesPattern(final Pattern pattern)
	{
		return new Predicate<String>()
		{
			@Override
			public boolean apply(String str)
			{
				Matcher ma = pattern.matcher(str);
				return ma.matches();
			}
		};
	}
	
	public static Predicate<String> matchesPattern(final String stringPattern)
	{
		final Pattern pattern = Pattern.compile(stringPattern);
		return new Predicate<String>()
		{
			
			@Override
			public boolean apply(String str)
			{
				Matcher ma = pattern.matcher(str);
				return ma.matches();
			}
		};
	}
	
	
}
