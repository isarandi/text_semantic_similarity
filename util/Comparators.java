package util;

import java.util.Comparator;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.base.Functions;

public class Comparators
{
	public static <A, B extends Comparable<B>> Comparator<A> forFunction(final Function<A,B> function)
	{
		return new Comparator<A>()
		{
			@Override
			public int compare(A o1, A o2)
			{
				return function.apply(o1).compareTo(function.apply(o2));
			}
		};
	}
	
	public static <A, B extends Comparable<B>> Comparator<A> forMap(final Map<A,B> map)
	{
		return Comparators.forFunction(Functions.forMap(map));
	}
}
