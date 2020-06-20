package learningobjectives;

import germanlanguage.GermanLanguage;
import germanlanguage.ScoredFragment;
import germanlanguage.Stem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;

import mesh.Mesh;
import mesh.MeshHeading;
import util.Maps2;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

import static util.IO.println;

public class LOCollection
{
	Set<LearningObjective> learningObjectives;
	SetMultimap<MeshHeading, LearningObjective> headingToLOMap;
	
	public LOCollection(Set<String> textualLOs, Map<Stem,Double> stemWeights, Mesh mesh)
	{
		learningObjectives = new HashSet<>();
		headingToLOMap = HashMultimap.create();
		
		for (String textualLO: textualLOs)
		{
			Set<ScoredFragment> scoredFragments = GermanLanguage.getScoredFragmentsOfExpression(textualLO);

			Set<ScoredFragment> idfWeightedFragments = getWeightedFragments(scoredFragments, stemWeights);
			
			List<ScoredFragment> lst = new ArrayList<>(idfWeightedFragments);
			Collections.sort(lst, (ScoredFragment s1, ScoredFragment s2) -> -Double.compare(s1.getScore(), s2.getScore()));
//			for (ScoredFragment fragment: lst)
//			{
//				println("fragment: " + fragment.getStem().getVariants().iterator().next() + " weight: " + fragment.getScore());
//			}
			
			Map<MeshHeading, Double> headingRelevances = mesh.getHeadingsForWordPieces(idfWeightedFragments, 0.1);
			Map<MeshHeading, Double> collapsedHeadingRelevances = collapseHeadingsBelowLevel(headingRelevances, 4);
			
			LearningObjective learningObjective = 
					new LearningObjective(textualLO, collapsedHeadingRelevances, scoredFragments);
			learningObjectives.add(learningObjective);
			
			for (MeshHeading heading: collapsedHeadingRelevances.keySet())
			{
				headingToLOMap.put(heading, learningObjective);
			}
		}
		
		//applyIdfWeights();
	}
	
	/**
	 * Climbs up the MeSH DAG and collects each first ancestor that has minDepth <= maxMinDepth.
	 */
	private Set<MeshHeading> getFirstAncestorHeadingsWithLevelLimit(
			Map<MeshHeading, Double> targetHeadingRelevances,
			MeshHeading startingHeading, 
			int maxMinDepth)
	{
		if (startingHeading.getMinDepth() <= maxMinDepth)
		{
			return Sets.newHashSet(startingHeading);
		} else
		{
			Set<MeshHeading> result = Sets.newHashSet();
			for (MeshHeading parent: startingHeading.getParents())
			{
				result.addAll(getFirstAncestorHeadingsWithLevelLimit(targetHeadingRelevances, parent, maxMinDepth));
			}
			return result;
		}
	}
	
	/**
	 * Creates a new relevance map. Relevances below the minDepth of "level" is disallowed. Their weights
	 * are added to their first ancestors above this level (towards the top of the tree).
	 */
	public Map<MeshHeading, Double> collapseHeadingsBelowLevel(Map<MeshHeading, Double> sourceRelevances, int level)
	{
		Map<MeshHeading, Double> results = new HashMap<MeshHeading, Double>();
		
		for (MeshHeading h: sourceRelevances.keySet())
		{
			Set<MeshHeading> collapsedHeadings = 
					getFirstAncestorHeadingsWithLevelLimit(results, h, level);
			
			Maps2.incrementAllBy(results, collapsedHeadings, sourceRelevances.get(h));
		}
		
		return results;
	}
	
	public void applyIdfWeights()
	{
		Map<MeshHeading,Double> idfWeights = new HashMap<>();
		int nHeadings = headingToLOMap.keySet().size();
		
		for (MeshHeading h: headingToLOMap.keySet())
		{
			double idf = Math.log(nHeadings/headingToLOMap.get(h).size());
			idfWeights.put(h, idf);
		}
		
		for (LearningObjective lo: learningObjectives)
		{
			lo.multiplyHeadingRelevances(idfWeights);
		}
	}
	
	/**
	 * Returns a map expressing the similarity of each LO in this collection w.r.t. the query LO
	 */
	public Map<LearningObjective,Double> getSimilarities(LearningObjective query)
	{
		// For efficiency, first find the 
		Set<LearningObjective> ourLOsSharingSomeHeadingsWithQuery = new HashSet<>();
		for (MeshHeading queryHeading: query.headingRelevances.keySet())
		{
			ourLOsSharingSomeHeadingsWithQuery.addAll(headingToLOMap.get(queryHeading));
		}
		
		Map<LearningObjective,Double> similarities = new HashMap<>();
		for (LearningObjective candidate : ourLOsSharingSomeHeadingsWithQuery)
		{
			double similarity = query.getCosineSimilarity(candidate);
			similarities.put(candidate, similarity);
		}
		
		return similarities;
	}
	
	public Set<LearningObjective> getLearningObjectiveSet()
	{
		return learningObjectives;
	}
	
	private static Set<ScoredFragment> getWeightedFragments(
			Set<ScoredFragment> scoredFragments,
			Map<Stem, Double> weights)
	{
		Set<ScoredFragment> result = new HashSet<>();
		for (ScoredFragment f: scoredFragments)
		{
			double score = f.calculateImportanceAmong(scoredFragments, weights);
			result.add(f.changedScore(weights.get(f.getStem())*score));
		}
		return result;
	}
}
