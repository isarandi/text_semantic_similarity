package util;

import com.google.common.base.Function;

public class StringFunctions
{
	public static Function<String,String> trimFromBeginning(final String beginningToBeTrimmed)
	{
		final int beginningLength = beginningToBeTrimmed.length();
		return new Function<String, String>()
		{
			@Override
			public String apply(String s)
			{
				if (s.startsWith(beginningToBeTrimmed))
				{
					return s.substring(beginningLength);
				} else {
					return s;
				}
			}
		};
	}
	
	public static Function<String,String> toLowerCase()
	{
		return new Function<String, String>()
		{
			@Override
			public String apply(String s)
			{
				return s.toLowerCase();
			}
		};
	}
}
