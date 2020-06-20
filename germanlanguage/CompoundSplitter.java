package germanlanguage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import util.IO;
import util.StringPredicates;
import util.StringUtil;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import de.danielnaber.jwordsplitter.GermanWordSplitter;
import edu.stanford.nlp.util.StringUtils;

public class CompoundSplitter
{
	private GermanWordSplitter wrappedSplitter;
	private Stemmer stemmer;
	
	public CompoundSplitter()
	{
		createWrappedSplitter();
        stemmer = new Stemmer();
	}

	private void createWrappedSplitter()
	{
		wrappedSplitter = new GermanWordSplitter(false, loadSplitterWords());
        wrappedSplitter.setMinimumWordLength(3);
        wrappedSplitter.setStrictMode(true);
	}
	
	private Set<String> loadSplitterWords()
	{
		Set<String> shaken = IO.lineSetFromFile("shakenWords.txt");
		Set<String> toRemove = IO.lineSetFromFile("nonsplit.txt");
		shaken.removeAll(toRemove);
		return shaken;
	}
		
	public SplitResult splitAndStem(String word)
	{
		SplitResult result = new SplitResult();
		
		boolean isWordCapitalized = StringUtils.isCapitalized(word);
		word = stemmer.getStem(word, isWordCapitalized);
		
		List<String> parts = Lists.newArrayList(wrappedSplitter.splitWord(word));
				
		parts = StringUtil.splitAndFlattenListKeepingGlue(parts, "-");
		
		result.unstemmedPiecesWithGlue = new ArrayList<>();
		for (String part: parts)
		{
			
			if (!result.unstemmedPiecesWithGlue.isEmpty() && (part.equals("s") || part.equals("-")))
			{
				int lastIndex = result.unstemmedPiecesWithGlue.size()-1;
				String lastPiece = result.unstemmedPiecesWithGlue.get(lastIndex);
				result.unstemmedPiecesWithGlue.set(lastIndex, lastPiece+part);
			} else {
				result.unstemmedPiecesWithGlue.add(part);
			}
		}
		
		Iterables.removeIf(parts, StringPredicates.matchesPattern("(\\-|s)"));

    	for (int i=0; i<parts.size(); ++i)
    	{
   		    String stem = stemmer.getStem(parts.get(i), isWordCapitalized);	
    		parts.set(i, stem);
    	}
    	
    	result.stemmedPieces = parts;
    	return result;
	}

	
	public static class SplitResult
	{
		public List<String> stemmedPieces;
		public List<String> unstemmedPiecesWithGlue;
	}


}
