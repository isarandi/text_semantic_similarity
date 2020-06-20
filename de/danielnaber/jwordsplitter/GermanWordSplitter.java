/**
 * Copyright 2012 Daniel Naber (www.danielnaber.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.danielnaber.jwordsplitter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import de.danielnaber.jwordsplitter.tools.FileTools;

/**
 * Split German compound words. Based on an embedded dictionary, or on an
 * external plain text dictionary.
 */
public class GermanWordSplitter extends AbstractWordSplitter {

    private static final String DICT = "splitWords4.txt";   // dict inside the JAR
    private static final String EXCEPTION_DICT = "exceptionsGerman.txt";   // dict inside the JAR
    /** Interfixes = Fugenelemente */
    private static final Collection<String> INTERFIXES = Arrays.asList(
            "s-",  // combination of the characters below
            "s",
            "-");

    // Add some exceptions so we can easily add terms without re-building the binary dictionary:
    // TODO: remove once we keep the
    private static final Set<String> IGNORED_PARTS = new HashSet<String>();
    static {
        IGNORED_PARTS.add("richten");
    }
    private static final Set<String> ADDED_PARTS = new HashSet<String>();
    static {
        ADDED_PARTS.add("sozial");
    }

    private GermanInterfixDisambiguator disambiguator;

    public GermanWordSplitter(boolean hideInterfixCharacters) {
        super(hideInterfixCharacters);
        init();
    }

    public GermanWordSplitter(boolean hideInterfixCharacters, InputStream plainTextDict) throws IOException {
        super(hideInterfixCharacters, plainTextDict);
        init();
    }

    public GermanWordSplitter(boolean hideInterfixCharacters, File plainTextDict) throws IOException {
        super(hideInterfixCharacters, plainTextDict);
        init();
    }
    
    public GermanWordSplitter(boolean hideInterfixCharacters, Set<String> words)  {
        super(hideInterfixCharacters, words);
        init();
    }

    private void init() {
        disambiguator = new GermanInterfixDisambiguator(getWordList());
        setExceptionFile(EXCEPTION_DICT);
    }

    @Override
    protected Set<String> getWordList(InputStream stream) throws IOException {
        return FileTools.loadFileToSet(stream, "utf-8");
    }
    

    @Override
    public Set<String> getWordList() {
        if (words == null) {
            try
			{
				words = new HashSet<>(Files.readAllLines(Paths.get(DICT), Charset.forName("utf8")));
			} catch (IOException e)
			{
				throw new RuntimeException(e);
			}
        }
        words.addAll(ADDED_PARTS);
        words.removeAll(IGNORED_PARTS);
        return words;
    }

    @Override
    protected GermanInterfixDisambiguator getDisambiguator() {
        return disambiguator;
    }

    @Override
    protected int getDefaultMinimumWordLength() {
        return 4;
    }

    @Override
    protected Collection<String> getInterfixCharacters() {
        return INTERFIXES;
    }

}
