package germanlanguage;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class Stem
{
	private Set<String> variants;
	
	public ImmutableSet<String> getVariants()
	{
		return ImmutableSet.copyOf(this.variants);
	}

	public Stem()
	{
		this.variants = Sets.newHashSet(); 
	}
	
	public void addVariant(String variant)
	{
		this.variants.add(variant);
	}
	
	public void addVariants(Collection<String> variants)
	{
		this.variants.addAll(variants);
	}

	@Override
	public String toString()
	{
		return "["+variants.iterator().next()+"]";
	}
	
	

}
