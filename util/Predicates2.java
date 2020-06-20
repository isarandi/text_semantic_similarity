package util;

import com.google.common.base.Predicate;

public class Predicates2
{
	public static Predicate<Double> lessThan(final double limit)
	{
		return new Predicate<Double>()
		{
			@Override
			public boolean apply(Double value)
			{
				return value < limit;
			}
		};
	}
	
	public static Predicate<Double> greaterThan(final double limit)
	{
		return new Predicate<Double>()
		{
			@Override
			public boolean apply(Double value)
			{
				return value > limit;
			}
		};
	}
	
	public static Predicate<Double> lessThanOrEqual(final double limit)
	{
		return new Predicate<Double>()
		{
			@Override
			public boolean apply(Double value)
			{
				return value <= limit;
			}
		};
	}
	
	public static Predicate<Double> greaterThanOrEqual(final double limit)
	{
		return new Predicate<Double>()
		{
			@Override
			public boolean apply(Double value)
			{
				return value >= limit;
			}
		};
	}
}
