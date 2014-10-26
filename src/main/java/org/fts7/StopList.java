/*
 * Copyright 2014 Sergey Apollonov.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fts7;

import java.lang.*;
import java.io.*;
import java.util.HashSet;
import java.util.Scanner;
import java.util.regex.*;
import java.util.ArrayList;

/**
 * This class manages a list of words  which should be ignored by
 * {@link CoreIndexer#addObject(ObjectContent)} method.
 * <br>This class has a single instance which must be get by {@link #getInstance()} method.
 * <p>
 * The package org.fts7.stopW contains a text file StopW.txt with a list of stop words.
 * This words will be excluded from indexing. File StopW.txt is loaded by StopList class
 * on class initialization.
 *
 * @author Sergey Apollonov
 */
public class StopList
{

    private static StopList instance;
    HashSet<String> noIndex=new HashSet<>();
    ArrayList<Pattern> patterns=new ArrayList<>();


    /**
     * Get a single instance of a StopList object.
     * @return an instance of the StopList object
     */
    public static StopList getInstance()
    {
        if (instance == null)
        {
            instance = new StopList();
        }
        return instance;
    }

    /**
     * Clears a StopList.
     */
    public void clear()
    {
        noIndex.clear();
        patterns.clear();
    }

    /**
     * Adds an array of {@code words} to a stop list.
     */
    public void addWords(String[] words)
    {
        for (String s:words)
            addWord(s);
    }

    /**
     * Adds a {@code word} to a stop list. The {@code word} may be a regex pattern.
     */
    public void addWord(String word)
    {
        if (word.length()==0)
            return;
        if (word.matches(".*[\\\\\\.\\*\\+\\[\\]].*")) // this is a pattern
            patterns.add(Pattern.compile(word));
        else
            noIndex.add(word.toUpperCase());
    }

    /**
     * Loads a text resource with stop words
     * @param rname resource name to load a StopList from
     */
    void loadResource (String rname)
    {
        try
        {
            InputStream is=getClass().getResourceAsStream(rname);
            Scanner sc=new Scanner(is,"UTF-8");

            sc.useDelimiter("\\s+|#.*(\\x0D\\x0A)+"); // cut comments, begins with #
            while (sc.hasNext())
            {
                String s=sc.next();
                addWord(s);
            }
            is.close();
            sc.close();
        }
        catch (Exception e)
        {
            System.out.printf("failed to load resource %s %s",rname,e.getMessage());
        }
    }

    /** private not accessible constructor  */
    private StopList()
    {
        loadResource("Stopw/StopW.txt");
    }

    /**
     * Checks a {@code word} for matching to a StopList. This method is used by
     * {@link org.fts7.CoreIndexer#addObject(org.fts7.ObjectContent) } method
     * to exclude a word from being indexed.
     *
     * @return true if the {@code word} is in the StopList.
     */
    public boolean isStopWord(String word)
    {
        if (noIndex.contains(word.toUpperCase()))
            return true;
        else
            for (Pattern p:patterns)
            {
                Matcher m=p.matcher(word);
                if (m.matches())
                    return true;
            }
        return false;
    }
}
