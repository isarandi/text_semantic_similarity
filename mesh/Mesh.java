package mesh;

import germanlanguage.ScoredFragment;
import germanlanguage.GermanLanguage;
import germanlanguage.Stem;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import util.IO;
import util.Iterables2;
import util.Maps2;
import util.Multimaps2;
import util.Predicates2;
import util.StringFunctions;
import util.StringUtil;

import com.google.common.base.Predicates;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

public class Mesh
{
	private static final Path folderPath = Paths.get("../Data/MeSH essential/");
	
	private Set<MeshHeading> headings = new HashSet<>();
	
	private Map<String, MeshHeading> idToHeadingMap = new HashMap<>();
	private Map<String, MeshHeading> nameToHeadingMap = new HashMap<>();
	
	private Map<MeshHeading, Double> headingToWordSpaceVectorAbs = new HashMap<>();
	
	private Map<Stem, Double> stemToImportanceMap = new HashMap<>();
	private SetMultimap<Stem, MeshHeading> stemToHeadingMap = HashMultimap.create();
	
	public Mesh()
	{
		createHeadings();
	    createStemsForHeadings(false);
	    calculateStemImportance();
	    createStemsForHeadings(true);

	    calculateHeadingToWordSpaceVectorAbs();
		createTreePositionMappings();
	}
	
	public Set<MeshHeading> getHeadings()
	{
		return ImmutableSet.copyOf(headings);
	}
	
	public MeshHeading getHeading(String name)
	{
		return nameToHeadingMap.get(name);
	}
	
	public Map<MeshHeading, Double> getHeadingsForWordPieces(Set<ScoredFragment> scoredFragments, double minScoreRelativeToBest)
	{
		Map<MeshHeading, Double> sumsOfMulWeights = new HashMap<>();
		
		double sumOfQueryWeightsSquared = 0;
		
		for (ScoredFragment scoredFragment: scoredFragments)
		{
			Stem stem = scoredFragment.getStem();
			double weightOfFragmentForQuery = scoredFragment.getScore();
			sumOfQueryWeightsSquared += weightOfFragmentForQuery*weightOfFragmentForQuery;
			
			double weightOfFragmentForMeSH = stemToImportanceMap.containsKey(stem) ? stemToImportanceMap.get(stem) : 0.0;
			
			for (MeshHeading heading: stemToHeadingMap.get(stem))
			{
				double weightOfFragmentForHeading = heading.wordSpaceWeights.get(stem);
				double weightOfFragment = weightOfFragmentForQuery * weightOfFragmentForHeading;
				Maps2.incrementBy(sumsOfMulWeights, heading, weightOfFragment);
			}
		}
		
		if (sumsOfMulWeights.isEmpty())
		{
			return Collections.emptyMap();
		}
		
		Map<MeshHeading, Double> scores = new HashMap<>();
		for (MeshHeading heading: sumsOfMulWeights.keySet())
		{
			double cosineSimilarity =
					sumsOfMulWeights.get(heading)/
					(Math.sqrt(sumOfQueryWeightsSquared)*headingToWordSpaceVectorAbs.get(heading));
			
			scores.put(heading, cosineSimilarity);
		}
		
		double maxScore = Collections.max(scores.values());
		
		double limit = maxScore*minScoreRelativeToBest;
		Map<MeshHeading, Double> filteredMap = Maps.filterValues(scores, Predicates2.greaterThanOrEqual(limit));
		return Maps2.topNByValue(filteredMap, 10);
	}
	
	public static Set<String> getWords()
	{
		Map<String, String> nameToIdMap = IO.mapFromFile(folderPath.resolve("MH.TXT"), "\\|(.+?)\\|;\\|(.+?)\\|;\\|(.+?)\\|;\\|(.+?)\\|", 2, 1);
		Map<String, String> entryToIdMap = IO.mapFromFile(folderPath.resolve("ETD.TXT"), "\\|(.+?)\\|;\\|(.+?)\\|;", 1, 2);
		entryToIdMap.putAll(nameToIdMap);
				
		Set<String> lowercaseWords = Sets.newHashSet(
				Iterables.transform(entryToIdMap.keySet(), StringFunctions.toLowerCase()));
		
		lowercaseWords = StringUtil.splitAndFlattenSet(lowercaseWords, "(\\(|,|\\s| |\"|\\))");
		
		Pattern p = Pattern.compile("([a-zA-Z]|ö|ü|ä|ß|Ö|Ü|Ä|-)+");

		for (Iterator<String> iter = lowercaseWords.iterator(); iter.hasNext(); )
		{
			String word = iter.next();
			if (word.endsWith("-") || !p.matcher(word).matches())
			{
				iter.remove();
			}
		}
		
		Set<String> splitWords = Sets.newHashSet();
		for (String word: lowercaseWords)
		{
			if (word.startsWith("-"))
				word = word.substring(1);
			
			if (word.length()>3 && Predicates.containsPattern("(a|e|i|o|ö|u|ü|ä)").apply(word))
				splitWords.addAll(Arrays.asList(word.split("-")));
		}
		
		for (Iterator<String> iter = splitWords.iterator(); iter.hasNext(); )
		{
			String word = iter.next();
			if (!p.matcher(word).matches())
			{
				iter.remove();
			}
		}
		
		return splitWords;
	}
	
	// Initialization methods (called only at construction time)
	private Map<String, String> createHeadings()
	{
		Map<String, String> nameToIdMap = IO.mapFromFile(folderPath.resolve("MH.TXT"), "\\|(.+?)\\|;\\|(.+?)\\|;\\|(.+?)\\|;.*?", 2, 1);
		for (String headingName: nameToIdMap.keySet())
		{
			String headingID = nameToIdMap.get(headingName);
			createNewHeading(headingID, headingName);
		}
		return nameToIdMap;
	}

	private void createTreePositionMappings()
	{
		Map<String, String> treePositionToIdMap = IO.mapFromFile(folderPath.resolve("CC.TXT"), "\\|(.+?)\\|;\\|(.+?)\\|;", 1, 2);
		for (String treePosition : treePositionToIdMap.keySet())
		{
			if (treePosition.contains("."))
			{
				String headingID = treePositionToIdMap.get(treePosition);
				
				MeshHeading heading = idToHeadingMap.get(headingID);
				
				String parentPosition = treePosition.substring(0, treePosition.lastIndexOf("."));
				String parentHeadingID = treePositionToIdMap.get(parentPosition);
				MeshHeading parentHeading = idToHeadingMap.get(parentHeadingID);
				
				heading.parents.add(parentHeading);
			}
		}
	}

	private void createStemsForHeadings(boolean determineWeights)
	{
		Map<String, String> nameToIdMap = IO.mapFromFile(folderPath.resolve("MH.TXT"), "\\|(.+?)\\|;\\|(.+?)\\|;\\|(.+?)\\|;\\|(.+?)\\|", 2, 1);
		Map<String, String> entryToIdMap = IO.mapFromFile(folderPath.resolve("ETD.TXT"), "\\|(.+?)\\|;\\|(.+?)\\|;", 1, 2);
		entryToIdMap.putAll(nameToIdMap);
		
		for (String entry: entryToIdMap.keySet())
		{
			String headingID = entryToIdMap.get(entry);
			MeshHeading heading = idToHeadingMap.get(headingID);
			
			if (heading == null)
			{
				throw new RuntimeException(headingID+" unknown.");
			}
			
			Set<ScoredFragment> scoredFragments = GermanLanguage.getScoredFragmentsOfExpression(entry);
			
			//savePiecesForHeading(heading, pieceResult.pieceToScoreMap.keySet());
			//saveFullWordForHeading(heading, entry);
			
			if (determineWeights)
			{
				determineWeightsForHeading(heading, scoredFragments);
			} else {
				for (ScoredFragment sf: scoredFragments)
				{
					stemToHeadingMap.put(sf.getStem(), heading);
				}
			}
		}
	}
	
	
	private void calculateHeadingToWordSpaceVectorAbs()
	{
		for (MeshHeading heading: headings)
		{
			double sumOfSquaredPieceWeights = Iterables2.sumOfSquares(heading.wordSpaceWeights.values());
			headingToWordSpaceVectorAbs.put(heading, Math.sqrt(sumOfSquaredPieceWeights));
		}
	}

	private void calculateStemImportance()
	{
		int nAllHeadings = stemToHeadingMap.values().size();
		for (Stem stem: stemToHeadingMap.keySet())
		{
			int absoluteFrequency = stemToHeadingMap.get(stem).size();
			double relativeFrequency = absoluteFrequency/(double)nAllHeadings;
			
			double idfWeight = Math.log(1.0/relativeFrequency);
			stemToImportanceMap.put(stem, idfWeight);
		}
	}
	
	private MeshHeading createNewHeading(String headingID, String headingName)
	{
		MeshHeading heading = new MeshHeading(headingID, headingName);
		headings.add(heading);
		idToHeadingMap.put(headingID, heading);
		nameToHeadingMap.put(headingName, heading);
		
		return heading;
	}
	
//	private void savePiecesForHeading(Heading heading, Iterable<String> piecesOfEntry)
//	{
//		piecesOfEntry = Iterables.transform(piecesOfEntry, StringFunctions.toLowerCase());
//		Multimaps2.putAll(wordPieceToHeadingMap, piecesOfEntry, heading);
//		Maps2.maxAllBy(heading.weightedWordPieces, piecesOfEntry, 1.0);		
//	}
//	
//	private void saveFullWordForHeading(Heading heading, String fullWord)
//	{
//		fullWord = fullWord.toLowerCase();
//		wordPieceToHeadingMap.put(fullWord, heading);
//		Maps2.maxBy(heading.weightedWordPieces, fullWord, 100.0);
//	}
//	
		
	private void determineWeightsForHeading(MeshHeading heading, Set<ScoredFragment> fragmentsOfEntry)
	{
		for (ScoredFragment fragmentBeingScored : fragmentsOfEntry)
		{
			double score = fragmentBeingScored.calculateImportanceAmong(fragmentsOfEntry, stemToImportanceMap);
			Maps2.maxBy(heading.wordSpaceWeights, fragmentBeingScored.getStem(), score*stemToImportanceMap.get(fragmentBeingScored.getStem()));
		}
	}

}
