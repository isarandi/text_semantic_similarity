package learningobjectives;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import util.IO;
import util.StringUtil;

import static util.StringPredicates.*;
import static util.StringFunctions.*;
import static com.google.common.base.Predicates.*;

public class ACLO
{
	Set<String> acloLOs;
	
	
	public ACLO(String filePath)
	{
		acloLOs = new HashSet<>();
		
		Pattern p = Pattern.compile("([^\"]+?|\".+?\"),([^\\\"]+?|\\\".+?\\\"),([^\\\"]+?|\\\".+?\\\"),([^\\\"]+?|\\\".+?\\\"),([^\\\"]+?|\\\".+?\\\"),([^\\\"]+?|\\\".+?\\\"),([^\\\"]*?|\\\".*?\\\"),([^\\\"]*?|\\\".*?\\\")");
		for (String line: IO.lineListFromFile(filePath))
		{
			Matcher m = p.matcher(line);
			if (m.matches())
			{
				String skill =  m.group(3)+" | "+m.group(5).replace("\"", " ").trim();
				acloLOs.add(skill+" erklären"); // to ensure grammatical parsability
			}
		}
	}
	
	public Set<String> getTextualLearningObjectives()
	{
		return acloLOs;
	}
	
	public Set<String> getWords()
	{
		Set<String> words = new HashSet<>();
		for (String lo : acloLOs)
		{
			lo = lo.toLowerCase();
			words.addAll(Arrays.asList(lo.split("(\\(|,|\\s| |/|\"|\\.|\\))")));
		}

		Pattern wordPattern = Pattern.compile("([a-zA-Z]|ö|ü|ä|ß|Ö|Ü|Ä|-)+");
		Pattern vowelPattern = Pattern.compile("(a|e|i|o|u|ö|ü|ä|A|E|I|O|U|Ö|Ü|Ä)");
		
		Iterables.removeIf(words, endsWith("-"));
		Iterables.removeIf(words, not(matchesPattern(wordPattern)));
		Iterables.removeIf(words, lengthLessThan(4));
		Iterables.removeIf(words, not(contains(vowelPattern)));
		words = Sets.newHashSet(Iterables.transform(words, trimFromBeginning("-")));
				
		StringUtil.splitAndFlattenSet(words, "-");
		Iterables.removeIf(words, not(matchesPattern(wordPattern)));
		
		return words;
	}
}
