import germanlanguage.GermanLanguage;
import germanlanguage.NounPhraseExtractor;
import germanlanguage.ScoredFragment;
import germanlanguage.Stem;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import learningobjectives.ACLO;
import learningobjectives.LOCollection;
import learningobjectives.LearningObjective;
import learningobjectives.NKLM;
import mesh.Mesh;
import mesh.MeshHeading;
import util.IO;
import util.Maps2;

import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import de.danielnaber.jwordsplitter.GermanWordSplitter;

public class Main {

	public static void other() {
		// Set<String> words = new HashSet<>();
		// for (String line:
		// IO.readLines("split_compounds_from_GermaNet8.0_modified-2013-06-12.txt"))
		// {
		// String s[] = line.split("\t");
		// words.add(s[1]);
		// words.add(s[2]);
		//
		// }
		//
		// Set<String> words2 = new HashSet<>();
		// for (String word: words)
		// {
		// String s[] = word.split("( |\\|)");
		// for (String st: s)
		// {
		// if (st.length()>=4 && containsVowel(st))
		// {
		// words2.add(st.toLowerCase());
		// }
		// }
		//
		// }
		//
		// IO.writeLines(words2, "germanet.txt");

		// GermanWordSplitter splitter = new GermanWordSplitter(true,
		// IO.readLinesSet("germanet.txt"));
		// splitter.setMinimumWordLength(3);
		// splitter.setStrictMode(true);
		//
		// Set<String> splitWords = splitter.getWordList();
		// Set<String> splitWordsCopy = new HashSet<>(splitWords);
		//
		// for (int i=0; i<3; ++i)
		// {
		// for (String word: splitWordsCopy)
		// {
		// if (!splitWords.contains(word))
		// continue;
		//
		// splitWords.remove(word);
		//
		// List<String> parts = new ArrayList<>(splitter.splitWord(word));
		//
		//
		// if (parts.size()==1)
		// {
		// splitWords.add(word);
		// }
		// }
		// }
		//
		// IO.writeLines(splitWords, "germanet2.txt");

		// for (String line:
		// IO.readLines("split_compounds_from_GermaNet8.0_modified-2013-06-12.txt"))
		// {
		// String s[] = line.split("\t");
		// words.add(s[1]);
		// words.add(s[2]);
		//
		// }

	}

	public static Set<String> cleanWords(Set<String> input) {
		// split
		Set<String> words = new HashSet<>();
		for (String s : input) {
			s = s.toLowerCase();
			words.addAll(Arrays.asList(s.split("(\\(|,|\\s| |\\n|\\||\"|\\))")));
		}

		Set<String> nonHyphen = new HashSet<>();
		Pattern p = Pattern.compile("([a-zA-Z]|ö|ü|ä|ß|Ö|Ü|Ä|-)+");

		for (String word : words) {
			if (!word.endsWith("-")) {
				Matcher ma = p.matcher(word);
				if (!ma.matches())
					continue;

				if (word.startsWith("-"))
					word = word.substring(1);

				if (word.length() > 3 && GermanLanguage.containsVowel(word))
					nonHyphen.addAll(Arrays.asList(word.split("-")));
			}
		}
		words.clear();
		for (String word : nonHyphen) {
			Matcher ma = p.matcher(word);
			if (ma.matches() && word.length() > 3 && GermanLanguage.containsVowel(word))
				words.add(word);
		}

		Set<String> wordsCopy = new HashSet<>(words);
		for (String word : wordsCopy) {
			if (word.length() > 5
					&& ((word.endsWith("es") && words.contains(word.substring(
							0, word.length() - 2))) || (word.endsWith("s") && words
							.contains(word.substring(0, word.length() - 1)))))
				words.remove(word);
			else {
				words.add(word.replace("ü", "ue").replace("ä", "ae")
						.replace("ö", "oe").replace("ß", "ss"));
			}
		}

		return words;
	}

	public static Set<String> makeDerivatives(Set<String> words) {
		Set<String> newWords = new HashSet<>(words);
		Set<String> derivedWords = new HashSet<>();

		for (String w : words) {
			if (w.endsWith("ischem") || w.endsWith("ischen")
					|| w.endsWith("ischer") || w.endsWith("ische")
					|| w.endsWith("isches")) {
				String stem = w.substring(0, w.indexOf("isch"));
				if (stem.length() >= 3) {
					derivedWords.add(stem + "isch");
				}
			} else if (w.endsWith("lich") || w.endsWith("lichen")
					|| w.endsWith("licher") || w.endsWith("liche")
					|| w.endsWith("liches")) {
				String stem = w.substring(0, w.indexOf("lich"));
				if (stem.length() >= 3) {
					derivedWords.add(stem + "lich");
				}
			}
		}
		newWords.addAll(derivedWords);
		derivedWords.clear();

		for (String w : words) {
			if (w.endsWith("lich")) {
				derivedWords.add(w + "keit");
			}
		}
		newWords.addAll(derivedWords);
		derivedWords.clear();

		for (String w : words) {

			if (w.endsWith("ieren") && w.length() >= 8) {
				derivedWords.add(w.substring(0, w.length() - 2) + "ung");
			} else if (w.endsWith("ierung") && w.length() >= 10) {
				derivedWords.add(w.substring(0, w.length() - 3) + "en");
			}
		}
		newWords.addAll(derivedWords);

		return newWords;
	}

	public static Set<String> shakeCompounds(Set<String> allwords,
			Set<String> unsplittableWords, Set<String> nosplitWords) {

		GermanWordSplitter splitter = new GermanWordSplitter(true, allwords);
		splitter.setMinimumWordLength(3);
		splitter.setStrictMode(true);

		Set<String> splitWords = splitter.getWordList();
		splitWords.removeAll(nosplitWords);

		List<String> lengthOrderedWords = new ArrayList<>(allwords);
		Collections.sort(lengthOrderedWords, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.length() - o2.length();
			}
		});

		for (String word : lengthOrderedWords) {
			if (!unsplittableWords.contains(word)
					&& !nosplitWords.contains(word)) {
				splitWords.remove(word);

				List<String> parts = new ArrayList<>(splitter.splitWord(word));

				if (parts.size() == 1)
				{
					splitWords.add(word);
				}
			}
		}

		return splitWords;
	}

	public static void cleanMorphy() {
		Set<String> wiktiWords = IO.lineSetFromFile("wiktiWords.txt");
		Map<String, String> morphy = IO.mapFromFile("morphy-mapping.txt");
		Set<String> morphyKeys = new HashSet<>(morphy.keySet());

		for (String src : morphyKeys) {
			if (morphy.containsKey(morphy.get(src))) {
				morphy.put(src, morphy.get(src));
			}
		}

		for (String src : morphyKeys) {
			if (!src.equals(src.toLowerCase())
					&& morphy.containsKey(src.toLowerCase())
					&& !wiktiWords.contains(src)) {
				morphy.put(src, morphy.get(src.toLowerCase()));
			}
		}

		IO.mapToFile(morphy, Paths.get("newMorphy.txt"));
	}

	public static void printPiecesOfHeading(MeshHeading hea) {
		for (Stem stem : hea.getWordSpaceWeights().keySet()) {
			System.out.println(Joiner.on("|").join(stem.getVariants()) + " > "
					+ hea.getWordSpaceWeights().get(stem));
		}

		System.out.println("---------------------");
	}

	public static void main(String[] args) throws FileNotFoundException {

		//
		Mesh mesh = new Mesh();
	
		printPiecesOfHeading(mesh.getHeading("Therapeutische Okklusion"));
	
		///////////////////////////
		ACLO aclo = new
		ACLO("../Data/aclo_corr_quotes.csv");
		NKLM nklm = new
		NKLM("../Data/nklm_corr_quotes.csv");
		
		Map<String,String> morphy = IO.mapFromFile("morphy-mapping.txt");
		Set<String> jwords =
		IO.lineSetFromFile("germanWords_jwordsplitter.txt");
		Set<String> wiktiWords = IO.lineSetFromFile("wiktiWords.txt");
		Set<String> germaNetWords =
		IO.lineSetFromFile("split_compounds_from_GermaNet8.0_modified-2013-06-12.txt");
		
		Set<String> allWords = new HashSet<>();
		allWords.addAll(morphy.keySet());
		allWords.addAll(morphy.values());
		allWords.addAll(wiktiWords);
		allWords.addAll(jwords);
		allWords.addAll(mesh.getWords());
		allWords.addAll(aclo.getWords());
		allWords.addAll(nklm.getWords());
		allWords.addAll(germaNetWords);
		
		Set<String> cleanedWords = cleanWords(allWords);
		IO.linesToFile(cleanedWords, "cleanedWords.txt");
		
		Set<String> shakenCompounds = shakeCompounds(cleanedWords, jwords,
		IO.lineSetFromFile("nonsplit.txt"));
		
		shakenCompounds = makeDerivatives(shakenCompounds);
		IO.linesToFile(shakenCompounds, "shakenWords.txt");
		
		///////////////

		// NounPhraseExtractor npe = new NounPhraseExtractor();
		// CompoundSplitter splitter = new CompoundSplitter();

//		ACLO aclo = new ACLO(
//				"../Data/aclo_corr_quotes.csv");
//		System.out.println("aclo: " + aclo.getTextualLearningObjectives().size());
//		NKLM nklm = new NKLM(
//				"../Data/nklm_corr_quotes.csv");
//		System.out.println("aclo: " + nklm.getTextualLearningObjectives().size());

		NounPhraseExtractor npExtractor = new NounPhraseExtractor();

		Map<Stem, Double> idfWeightsACLONKLM = getStemToImportanceMap(
				Sets.union(aclo.getTextualLearningObjectives(), nklm.getTextualLearningObjectives()), npExtractor);

		List<String> list1 = new ArrayList<>(aclo.getTextualLearningObjectives());
		List<String> list2 = new ArrayList<>(nklm.getTextualLearningObjectives());

		String ex_ = "Allgemeine Anatomie der Knochen, Gelenke und Muskeln | die Baumaterialien, den Aufbau und die Funktion des Knochens erklären";
		String ex0 = "Abstoßungsreaktionen beschreiben und therapeutische Konsequenzen daraus ableiten.";
		String ex1 = "Sie erläutern den topographischen Aufbau des Körpers und leiten diesen aus der Entwicklung ab.";
		String ex2 = "Sie erklären die Prinzipien der Thermoregulation.";
		String ex3 = "ein sensibles Gespräch mit Kindern (schwerst-) kranker Sorgeberechtigten/Bezugspersonen führen.";
		String ex4 = "Sie erklären die Mechanismen und Bedeutung der Niere in der Ausscheidung harnpflichtiger Substanzen sowie der langfristigen Blutdruckregulation.";

		List<String> list3 = Lists.newArrayList(ex0, ex1, ex2, ex3, ex4);
		List<String> list4 = Lists.newArrayList(ex0);
		
		
		List<String> list = list1;

		Collections.sort(list);

		// PrintStream ps = System.out;
		PrintStream ps = new PrintStream("aclo2nklm.txt");
		
		
		LOCollection acloCollection = 
				new LOCollection(
						aclo.getTextualLearningObjectives(),//.stream().limit(1500).collect(Collectors.toSet()),
						idfWeightsACLONKLM, mesh);
		
	
		LOCollection nklmCollection =
				new LOCollection(
						nklm.getTextualLearningObjectives(),//.stream().limit(1500).collect(Collectors.toSet()),
						idfWeightsACLONKLM,
						mesh);
		
		LOCollection smallCollection =
				new LOCollection(
						list3.stream().collect(Collectors.toSet()),//.stream().limit(1500).collect(Collectors.toSet()),
						idfWeightsACLONKLM,
						mesh);
				
		List<LearningObjective> learningObjectives1 = 
				new ArrayList<LearningObjective>(nklmCollection.getLearningObjectiveSet());
				
		Collections.sort(
				learningObjectives1, 
				(LearningObjective s1, LearningObjective s2) -> s1.getText().compareTo(s2.getText()));

		double minSimilarity = 0.3; 

		for (LearningObjective lo1 : learningObjectives1) {
			
			Map<LearningObjective, Double> similarSkills = acloCollection.getSimilarities(lo1);
			
			
			if (!similarSkills.values().stream().anyMatch(x -> x > minSimilarity ))
			{
			    continue;
			}
			
			ps.println("Query: " + lo1.getText());
			int limit = 15;
			for (LearningObjective similarLO : Maps2.getKeysSortedByValueDescending(similarSkills))
			{
				if (similarSkills.get(similarLO) > minSimilarity)
				{
					ps.println("> " + similarLO.getText() + " ("
							+ similarSkills.get(similarLO) + ")");
					if (--limit == 0)
						break;
				}

			}

			ps.println();
		}

		ps.close();

		// for (String skill: list)
		// {
		// //String skillNPs = Joiner.on(", ").join(npExtractor.extract(skill));
		//
		// Set<ScoredFragment> scoredFragments =
		// GermanLanguage.getScoredFragmentsOfSentence(skill);
		//
		// Set<ScoredFragment> idfWeightedFragments =
		// getWeightedFragments2(scoredFragments, idfWeightsACLONKLM);
		//
		// Map<Heading, Double> results =
		// mesh.getHeadingsForWordPieces(idfWeightedFragments, 0.1);
		//
		// List<Heading> resultingHeadings =
		// Lists.newArrayList(results.keySet());
		// Collections.sort(resultingHeadings, Comparators.forMap(results));
		//
		// ps.println("Skill: "+skill);
		// //ps.println("NPs: "+skillNPs);
		//
		// for (Heading h: resultingHeadings)
		// {
		// ps.println(h.getHeadingName() + " (" + results.get(h) + ")");
		// }
		// ps.println();
		// }
		// ps.close();

	}

	public static Map<Stem, Double> getStemToImportanceMap(
			Set<String> sentences, NounPhraseExtractor npExtractor) {
		Map<Stem, Double> stemAbsoluteFrequencies = new HashMap<>();

		for (String sentence : sentences) {
			// String skillNPs =
			// Joiner.on(", ").join(npExtractor.extract(sentence));

			// Set<ScoredFragment> scoredFragments =
			// GermanLanguage.getScoredFragmentsOfSentence(skillNPs);
			Set<ScoredFragment> scoredFragments = GermanLanguage
					.getScoredFragmentsOfExpression(sentence);

			for (ScoredFragment scoredFragment : scoredFragments) {
				Maps2.incrementBy(stemAbsoluteFrequencies,
						scoredFragment.getStem(), 1.0);
			}
		}

		Map<Stem, Double> stemWeights = new HashMap<>();
		for (Stem stem : stemAbsoluteFrequencies.keySet()) {
			double idfWeight = Math.log(1.0 / (stemAbsoluteFrequencies
					.get(stem) / sentences.size()));
			stemWeights.put(stem, idfWeight);
		}
		return stemWeights;
	}
}
