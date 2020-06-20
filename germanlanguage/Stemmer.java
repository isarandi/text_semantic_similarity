package germanlanguage;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

import edu.stanford.nlp.util.StringUtils;

import util.IO;

public class Stemmer
{
	Map<String,String> inflectionRemovingMap;
	
	public Stemmer()
	{
        inflectionRemovingMap = IO.mapFromFile("newMorphy.txt");
		Map<String,String> morphyCorrections = IO.mapFromFile("morphy-corrections.txt");
		inflectionRemovingMap.putAll(morphyCorrections);
		
		for (String src: inflectionRemovingMap.keySet())
		{
			if (src.toLowerCase().contains("führen"))
			{
				String dst = inflectionRemovingMap.get(src);
				inflectionRemovingMap.put(src, dst.replace("fahren", "führen").replace("Fahren", "Führen"));
			}
		}
		
		Set<String> words = Sets.newHashSet(inflectionRemovingMap.keySet());
		for (String word: words)
		{
			if (word.contains("ö") || word.contains("ü") || word.contains("ä")) {
				String replaced = word.replace("ö", "oe").replace("ü", "ue").replace("ä", "ae");
				inflectionRemovingMap.put(replaced, inflectionRemovingMap.get(word));
			} else {
				String replaced = word.replace("oe", "ö").replace("ue", "ü").replace("ae", "ä");
				inflectionRemovingMap.put(replaced, inflectionRemovingMap.get(word));
			}
		}
	}
	
	public String getStem(String part, boolean isWordCapitalized)
	{
		if (!isWordCapitalized && inflectionRemovingMap.containsKey(part.toLowerCase()))
		{
			return inflectionRemovingMap.get(part.toLowerCase());
		} else if (inflectionRemovingMap.containsKey(StringUtils.capitalize(part)))
		{
			return inflectionRemovingMap.get(StringUtils.capitalize(part));
		}
		return part;
	}

}
