package germanlanguage;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Joiner;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.Tree;

public class NounPhraseExtractor
{
	private LexicalizedParser lp = LexicalizedParser.loadModel("../Tools/stanford-parser-full-2014-01-04/germanPCFG.ser.gz");
		
	public Set<String> extract(String input)
	{
		TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
	    Tokenizer<CoreLabel> tok = tokenizerFactory.getTokenizer(new StringReader(input));
	    List<CoreLabel> rawWords = tok.tokenize();
	    Tree parse = lp.apply(rawWords);
	    
	    //parse.pennPrint();
	    
	    Set<String> results = new HashSet<>();
	    List<String> currentParts = new ArrayList<>();

	    for (TaggedWord tw: parse.taggedYield())
	    {
	    	if (tw.word().equals("-LRB-") ||tw.word().equals("-RRB-"))
	    	{
	    		continue;
	    	} 
	    	else if (tw.tag().startsWith("ADJ") || tw.tag().startsWith("ADV"))
    		{
	    		String stem = tw.word();
	    		if (tw.tag().equals("ADJA") && (stem.endsWith("es")||stem.endsWith("en")||stem.endsWith("er")||stem.endsWith("em")))
	    			stem = stem.substring(0,stem.length()-2);
	    		else if (tw.tag().equals("ADJA") && (stem.endsWith("e")))
	    			stem = stem.substring(0,stem.length()-1);
    			currentParts.add(stem);
    		}else if (tw.tag().equals("NN") || tw.tag().equals("NE") || tw.tag().startsWith("NN-") || tw.tag().startsWith("FM"))
    			
    		{
    			currentParts.add(tw.word());
    			results.add(Joiner.on(" ").join(currentParts));
    			currentParts.clear();
    		}

	    }
	    
	    if (!currentParts.isEmpty())
	    {
	    	results.add(Joiner.on(" ").join(currentParts));
	    }
	    
	    return results;
	}
}
