package util;

import com.google.common.collect.Multimap;

public class Multimaps2
{
	public static <K,V> void putAll(Multimap<K,V> multimap, Iterable<K> keys, V value)
	{
		for (K key: keys)
		{
			multimap.put(key, value);
		}
	}
}
