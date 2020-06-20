package germanlanguage;

import germanlanguage.CompoundSplitter.SplitResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import util.IO;
import util.Maps2;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;


public class GermanLanguage
{
	private static CompoundSplitter splitter = new CompoundSplitter();
	private static Pattern sentenceSplitPattern = Pattern.compile("( |,|\\/|\\.|-|\\(|\\)|'|\")+");
	private static Set<String> stopWords = IO.lineSetFromFile("grammarWords.txt");
	
	private static Map<String, Stem> stemMap = new HashMap<>();
	
	public static Stem getStem(String word)
	{
		word = word.toLowerCase();
			
		if (stemMap.containsKey(word))
		{
			return stemMap.get(word);
		}
		else
		{
			Set<String> derivatives = getDerivatives(word);

			Stem stem = null;
			for (String derivative : derivatives)
			{
				if (stemMap.containsKey(derivative))
				{
					stem = stemMap.get(derivative);
					break;
				}
			}
			
			if (stem==null)
			{
				stem = new Stem();
			}
			
			
			Maps2.putAll(stemMap, derivatives, stem);
			stemMap.put(word, stem);
			stem.addVariants(derivatives);
			stem.addVariant(word);
			return stem;
		}
	}
			
	public static Set<String> getDerivatives(String word)
	{
		Set<String> newWords = new HashSet<>();
		if (word.length()>=6)
		{
			if (word.endsWith("er") || word.endsWith("em") || word.endsWith("es") || word.endsWith("en"))
			{
				newWords.add(word.substring(0,word.length()-2));
			} else if (word.endsWith("e") || word.endsWith("n") || word.endsWith("s"))
			{
				newWords.add(word.substring(0,word.length()-1));	
			}			
			newWords.add(word+"n");
			newWords.add(word+"en");
			newWords.add(word+"em");
			newWords.add(word+"er");
			newWords.add(word+"es");
			newWords.add(word+"s");
			newWords.add(word+"e");
		}
		return newWords;
	}
	
	private static double linearReposition(double value, double src1, double src2, double dst1, double dst2)
	{
		return dst1 + (value-src1)*(dst2-dst1)/(src2-src1);
	}
	
	public static Set<ScoredFragment> getScoredFragmentsOfExpression(String sentence)
	{
		Set<ScoredFragment> result = new HashSet<>();
				
		int pieceOffset = 0;
		for (String word: sentenceSplitPattern.split(sentence))
		{
			if (word.equals("") || stopWords.contains(word.toLowerCase()))
				continue;

			SplitResult splitResult = splitter.splitAndStem(word);
			int nPieces = splitResult.stemmedPieces.size();
			
			for (int nPiecesInCompound = 1; nPiecesInCompound <= nPieces; ++nPiecesInCompound)
			{
				double score = (nPieces == 1 ? 15 : linearReposition(nPiecesInCompound, 1, nPieces, 5, 20));
				
				for (int iFirstPiece = 0; iFirstPiece <= nPieces-nPiecesInCompound; ++iFirstPiece)
				{
					int lastIndexOfCompound = iFirstPiece+nPiecesInCompound-1;
					
					List<String> unstemmedPieces =
							splitResult.unstemmedPiecesWithGlue.subList(iFirstPiece, lastIndexOfCompound);
					String compound =
							Joiner.on("").join(unstemmedPieces) + 
							splitResult.stemmedPieces.get(lastIndexOfCompound);
										
					Range<Integer> compoundRange =
							Range.closed(pieceOffset+iFirstPiece, pieceOffset+lastIndexOfCompound);
					
					result.add(new ScoredFragment(getStem(compound), compoundRange, score));
				}
			}
			
			pieceOffset+= nPieces;
		}
		return result;
	}
	
	public static boolean containsVowel(String s) {
		return s.contains("a") || s.contains("e") || s.contains("i")
				|| s.contains("o") || s.contains("ö") || s.contains("u")
				|| s.contains("ü") || s.contains("ä");
	}
}
