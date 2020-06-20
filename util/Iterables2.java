package util;

public class Iterables2
{
	public static double sum(Iterable<Double> iterable)
	{
		double sum = 0;
		for (double d: iterable)
		{
			sum+=d;
		}
		return sum;
	}
	
	public static double sumOfSquares(Iterable<Double> iterable)
	{
		double sum = 0;
		for (double d: iterable)
		{
			sum+=d*d;
		}
		return sum;
	}
	
	
}
