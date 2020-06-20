package vector;

import java.util.HashMap;
import java.util.Map;

public class Vectorizer<T>
{
	Map<T,Integer> coordinateCorrespondence = new HashMap<>();
	
	public Vector vectorize(Map<T,Double> weights)
	{
		Vector v = new SparseVector();
		
		for (Map.Entry<T, Double> entry : weights.entrySet())
		{
			T key = entry.getKey();
			if (!coordinateCorrespondence.containsKey(key))
			{
				coordinateCorrespondence.put(key, coordinateCorrespondence.size());
			}
			
			int index = coordinateCorrespondence.get(key);
			v.set(index, entry.getValue());
		}
		
		return v;
	}
}
