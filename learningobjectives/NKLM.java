package learningobjectives;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.IO;
import util.StringUtil;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import static util.StringPredicates.*;
import static util.StringFunctions.*;
import static com.google.common.base.Predicates.*;

public class NKLM
{
	Set<String> nklmLOs;
	
	public NKLM(String filePath)
	{
		nklmLOs = new HashSet<>();
		
    	Pattern linePattern = Pattern.compile("([^\\\"]*|\\\".*\\\"),([^\\\"]*|\\\".*\\\"),([^\\\"]*|\\\".*\\\"),([^\\\"]*|\\\".*\\\"),([^\\\"]*|\\\".*\\\")");
		
		for (String line: IO.lineListFromFile(filePath))
		{
			Matcher m = linePattern.matcher(line);
			if (m.matches())
			{
				String loDescription = m.group(2).replace("\"", " ").trim();
				
				int colonIndex = loDescription.indexOf(":");
				if (colonIndex != -1)
				{
					if (!isInsideParentheses(loDescription, colonIndex))
					{
						loDescription = loDescription.substring(colonIndex+1, loDescription.length());
					}
				}
				
				if (loDescription.length()>2)
				{
					nklmLOs.add(loDescription);
				}
			}
		}
	}
	
	public Set<String> getTextualLearningObjectives()
	{
		return nklmLOs;
	}
	
	public Set<String> getWords()
	{
		Set<String> words = new HashSet<>();
		for (String lo : nklmLOs)
		{
			lo = lo.toLowerCase();
			words.addAll(Arrays.asList(lo.split("(\\(|,|\\s| |/|\"|\\))+")));
		}

		Pattern wordPattern = Pattern.compile("([a-zA-Z]|ö|ü|ä|ß|Ö|Ü|Ä|-|\\.)+");
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
	
	private static boolean isInsideParentheses(String string, int pos)
	{
		int nUnclosedParentheses = 0;
		
		for (int i=0; i<string.length(); ++i)
		{
			if (i==pos)
			{
				return nUnclosedParentheses > 0;
			}
			
			if (string.charAt(i) == '(')
			{
				++nUnclosedParentheses;
			} else if (string.charAt(i) == ')')
			{
				--nUnclosedParentheses;
			}
		}
		
		return false;
	}
}
