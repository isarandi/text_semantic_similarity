package util;

import com.google.common.collect.Range;

public class Ranges
{
	public static <C extends Comparable<C>> boolean intersects(Range<C> range1, Range<C> range2)
	{
		try {
			range1.intersection(range2);
			return true;
		} catch (IllegalArgumentException e)
		{
			return false;
		}
	}
}
