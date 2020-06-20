package util;

public class Math2
{

	
	public static double linearReposition(double value, double src1, double src2, double dst1, double dst2, double defaultRelativePosition)
	{
		if (src1==src2)
			return dst1 + defaultRelativePosition*(dst2-dst1);
		else
			return dst1 + (value-src1)*(dst2-dst1)/(src2-src1);
	}
	
	
}
