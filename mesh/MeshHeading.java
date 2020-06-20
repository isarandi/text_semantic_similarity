package mesh;

import germanlanguage.Stem;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class MeshHeading
{
	final String id;
	final String headingName;
	
	final Set<MeshHeading> parents;
	final Map<Stem, Double> wordSpaceWeights;
	
	int deepestDepth = -1;
	
	public MeshHeading(String id, String headingName)
	{
		this.id = id;
		this.headingName = headingName;
		parents = new HashSet<>();
		wordSpaceWeights = new HashMap<>();
	}

	public String getId()
	{
		return id;
	}

	public String getHeadingName()
	{
		return headingName;
	}

	public final Set<MeshHeading> getParents()
	{
		return parents;
	}

	public final Map<Stem, Double> getWordSpaceWeights()
	{
		return wordSpaceWeights;
	}
	
	public int getMinDepth()
	{
		if (deepestDepth == -1)
		{
			deepestDepth = calculateMinDepth(Collections.<MeshHeading>emptySet());
		}
		return deepestDepth;
	}
	
	public int getMinDepth(Set<MeshHeading> lowerHeadings)
	{
		if (deepestDepth == -1)
		{
			deepestDepth = calculateMinDepth(lowerHeadings);
		}
		return deepestDepth;
	}
	
	public int calculateMinDepth(Set<MeshHeading> lowerHeadings)
	{
		if (lowerHeadings.contains(this))
		{
			return Integer.MAX_VALUE/2; //needs to be incrementable without overflow, but still practically infinite
		}
		if (parents.isEmpty())
		{
			return 0;
		}
		
		//System.out.println(this.headingName+" "+this.id);
		int depth = Integer.MAX_VALUE;
		for (MeshHeading parent: parents)
		{
			depth = Math.min(depth, parent.getMinDepth(Sets.union(lowerHeadings, ImmutableSet.of(this)))+1);
		}
		
		return depth;
	}

	@Override
	public String toString()
	{
		return headingName;
	}
		
	
	
	
}