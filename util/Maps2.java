package util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mesh.MeshHeading;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class Maps2
{

	public static <K,V> void putAll(Map<K,V> map, Iterable<K> keys, V value)
	{
		for (K key: keys)
		{
			map.put(key, value);
		}
	}
	
	public static <K> void elementwiseAdd(Map<K,Double> toThis, Map<K,Double> addThis)
	{
		for (K key: addThis.keySet())
		{
			double prevValue = toThis.containsKey(key) ? toThis.get(key) : 0.0;
			toThis.put(key, prevValue+addThis.get(key));
		}
		
	}
	
	public static <K,V extends Comparable<V>> List<K> getKeysSortedByValue(Map<K,V> map)
	{
		List<K> keyList = Lists.newArrayList(map.keySet());
		Collections.sort(keyList, Comparators.forMap(map));
		return keyList;
	}
	
	public static <K,V extends Comparable<V>> List<K> getKeysSortedByValueDescending(Map<K,V> map)
	{
		List<K> keyList = Lists.newArrayList(map.keySet());
		Collections.sort(keyList, Collections.reverseOrder(Comparators.forMap(map)));
		return keyList;
	}
	
	public static <K> void incrementBy(Map<K,Double> map, K key, double value)
	{
		double prevValue = (map.containsKey(key) ? map.get(key) : 0.0);
		map.put(key, prevValue + value);
	}
	
	public static <K> void maxBy(Map<K,Double> map, K key, double value)
	{
		double prevValue = (map.containsKey(key) ? map.get(key) : Double.NEGATIVE_INFINITY);
		map.put(key, Math.max(value, prevValue));
	}
	
	public static <K> void maxAllBy(Map<K,Double> map, Iterable<K> keys, double value)
	{
		for (K key: keys)
		{
			maxBy(map, key, value);
		}
	}
	
	public static <K> void incrementAllBy(Map<K,Double> map, Iterable<K> keys, double value)
	{
		for (K key: keys)
		{
			incrementBy(map, key, value);
		}
	}
	
	public static <K> void elementwiseMultiply(Map<K,Double> toThis, Map<K,Double> mulByThis)
	{
		Set<K> smallerKeySet = (mulByThis.size() < toThis.size()) ? mulByThis.keySet() : toThis.keySet();
		for (K key: smallerKeySet)
		{
			if (toThis.containsKey(key))
			{
				double prevValue = toThis.get(key);
				double multiplier = mulByThis.containsKey(key) ? mulByThis.get(key) : 0.0;
				
				toThis.put(key, prevValue*multiplier);
			}
		}
	}
	
	public static <K,V extends Comparable<V>> Map<K,V> topNByValue(Map<K,V> map, int numberOfKept)
	{
		List<K> keys = Lists.newArrayList(map.keySet());
		
		Collections.sort(keys, Collections.reverseOrder(Comparators.forMap(map)));
		List<K> keptKeys = keys.subList(0, Math.min(keys.size(), numberOfKept));
		
		return Maps.filterKeys(map, Predicates.in(keptKeys));
	}


}
