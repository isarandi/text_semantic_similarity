package learningobjectives;

import germanlanguage.ScoredFragment;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import mesh.MeshHeading;
import util.Iterables2;
import static java.lang.Math.*;

import com.google.common.collect.Sets;

public class LearningObjective
{
	String text;
	Set<ScoredFragment> scoredFragments;
	Map<MeshHeading, Double> headingRelevances;
	
	double vectorLengthInMeshSpace;
	
	public LearningObjective(String text, 
			Map<MeshHeading, Double> headingRelevances,
			Set<ScoredFragment> scoredFragments)
	{
		this.headingRelevances = headingRelevances;
		this.scoredFragments = scoredFragments;
		this.text = text;

		vectorLengthInMeshSpace = sqrt(Iterables2.sumOfSquares(headingRelevances.values()));
	}
	
	public final String getText()
	{
		return text;
	}

	public final void setTextl(String rawSkill)
	{
		this.text = rawSkill;
	}

	public final Map<MeshHeading, Double> getHeadingRelevances()
	{
		return headingRelevances;
	}

	public final void setHeadingRelevances(Map<MeshHeading, Double> headingRelevances)
	{
		this.headingRelevances = headingRelevances;
	}

	public final Set<ScoredFragment> getScoredFragments()
	{
		return scoredFragments;
	}

	public final void setScoredFragments(Set<ScoredFragment> scoredFragments)
	{
		this.scoredFragments = scoredFragments;
	}

	public double getCosineSimilarity(LearningObjective other)
	{
		double dotProduct = 0.0;
		
		for (MeshHeading commonHeading:
			Sets.intersection(
					this.headingRelevances.keySet(),
					other.headingRelevances.keySet()))
		{
			dotProduct += this.headingRelevances.get(commonHeading) * other.headingRelevances.get(commonHeading);
		}
		
		return dotProduct/(this.vectorLengthInMeshSpace * other.vectorLengthInMeshSpace);
	}
	
	public double getEuclideanDistance(LearningObjective other)
	{
		double sumOfSquaredDiffs = 0.0;
		
		for (MeshHeading heading:
			Sets.union(
					this.headingRelevances.keySet(),
					other.headingRelevances.keySet()))
		{
			double thisRelevance = this.headingRelevances.containsKey(heading) ? this.headingRelevances.get(heading) : 0;
			double otherRelevance = other.headingRelevances.containsKey(heading) ? other.headingRelevances.get(heading) : 0;
			sumOfSquaredDiffs += pow(thisRelevance-otherRelevance, 2);
		}
		
		return sqrt(sumOfSquaredDiffs);
	}

	public void multiplyHeadingRelevances(Map<MeshHeading, Double> weights)
	{
		for (MeshHeading h: new HashSet<>(headingRelevances.keySet()))
		{
			headingRelevances.put(h, headingRelevances.get(h)*weights.get(h));
		}
		
		vectorLengthInMeshSpace = Math.sqrt(Iterables2.sumOfSquares(headingRelevances.values()));
	}
	
	
}
