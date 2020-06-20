package germanlanguage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import util.Ranges;

import com.google.common.collect.Range;

public class ScoredFragment
{
	private Stem stem;
	private Range<Integer> pieceRangeInPhrase;
	private double score;
	
	public ScoredFragment(Stem stem, Range<Integer> pieceRangeInPhrase, double score)
	{
		this.stem = stem;
		this.pieceRangeInPhrase = pieceRangeInPhrase;
		this.score = score;
	}
	
	public double getScore()
	{
		return score;
	}
	
	public ScoredFragment getWeighted(double weight)
	{
		return new ScoredFragment(stem, pieceRangeInPhrase, score*weight);
	}
	
	public ScoredFragment changedScore(double newScore)
	{
		return new ScoredFragment(stem, pieceRangeInPhrase, newScore);
	}
	
	public Stem getStem()
	{
		return stem;
	}
	
	public boolean hasOverlap(ScoredFragment other)
	{
		return Ranges.intersects(this.pieceRangeInPhrase, other.pieceRangeInPhrase);
	}
	
	public boolean isPartOf(ScoredFragment other)
	{
		return other.pieceRangeInPhrase.encloses(this.pieceRangeInPhrase);
	}
	
	public final Range<Integer> getPieceRangeInPhrase()
	{
		return pieceRangeInPhrase;
	}
	
	public int getRangeLength()
	{
		return pieceRangeInPhrase.upperEndpoint()-pieceRangeInPhrase.lowerEndpoint()+1;
	}
	
	public boolean encloses(ScoredFragment other)
	{
		return this.pieceRangeInPhrase.encloses(other.pieceRangeInPhrase);
	}
	

	public static boolean hasMaximalRangeOutside(ScoredFragment query, ScoredFragment excludedFragment, Set<ScoredFragment> fragments)
	{
		for (ScoredFragment sf: fragments)
		{
			if (!excludedFragment.hasOverlap(sf) && sf.encloses(query))
			{
				return false;
			}
		}
		return true;
	}
	
	public double calculateImportanceAmong(Set<ScoredFragment> fragments, Map<Stem, Double> stemImportances)
	{
		double sumScoreForNonOverlappingFragments = 0;
		double sumScoreForIncludedFragments = 0;
													
		for (ScoredFragment otherFragment : fragments)
		{
			if (!this.hasOverlap(otherFragment) && hasMaximalRangeOutside(otherFragment, this, fragments))
			{
				sumScoreForNonOverlappingFragments += stemImportances.get(otherFragment.getStem());
			} else if (otherFragment.equals(this))
			{
				sumScoreForIncludedFragments += stemImportances.get(otherFragment.getStem());	
			}
		}
		
		double totalScore = sumScoreForNonOverlappingFragments+sumScoreForIncludedFragments;
		double relativeRelevance = sumScoreForIncludedFragments/totalScore;
		return relativeRelevance;
	}
	
	public double calculateImportanceAmong2(Set<ScoredFragment> fragments, Map<Stem, Double> stemImportances)
	{
		double sumScoreForNonOverlappingFragments = 0;
		double sumScoreForIncludedFragments = 0;
								
		for (ScoredFragment otherFragment : fragments)
		{
			if (!this.hasOverlap(otherFragment))
			{
				sumScoreForNonOverlappingFragments += stemImportances.get(otherFragment.getStem());
			} else if (otherFragment.isPartOf(this))
			{
				sumScoreForIncludedFragments += stemImportances.get(otherFragment.getStem());	
			}
		}			
		
		double totalScore = sumScoreForNonOverlappingFragments+sumScoreForIncludedFragments;
		double relativeRelevance = sumScoreForIncludedFragments/totalScore;
		return relativeRelevance;
	}

	@Override
	public String toString()
	{
		return "[stem=" + stem + ", range="
				+ pieceRangeInPhrase + ", score=" + score + "]";
	}
	

	
	
}
