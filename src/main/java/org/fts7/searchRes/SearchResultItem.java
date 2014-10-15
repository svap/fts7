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

package org.fts7.searchRes;

import org.fts7.ScanResult;
import java.util.ArrayList;
import java.lang.Math.*;

/**
 * A single item of a search result.
 * <br>This class is a base element of a {@link SearchResult} list.
 * The SearchResultItem is a search item related to the indexed ObjectContent object.
 *
 * @author Sergey Apollonov
 */
public class SearchResultItem
{
    /** A sequential number of a search result item */
    public int n;

    /** An object id in the search index */
    public int idf;

    /** An index of the beginning of the best relevant content piece (0-based) */
    public int p1;

    /** The length of a best relevant piece of text content
     * (a quantity of content list elements) */
    public int dw;

    /** An object's text content represented as a list of {@link ContentItem} objects. */
    public ArrayList<ContentItem> content = new ArrayList<>();

    /** A list of {@link ObjectItem} objects having the same text content which are linked
     *  with this search result item. */
    public ArrayList<ObjectItem> objects = new ArrayList<>();

    /** A searching parameters of this item */
    public ScanResult scanR;

    /**
     * Get a best relevant content piece.
     * <p>
     * An example of usage:<p>
     * {@code
     * getContentRelevantPiece(10, 20, "<b>%s</b>");
     * }
     * <p> This call returns a string with a piece of 20 words
     * from the 10-th position behind the beginning of a best relevant phrase.
     * Each word contained in a search phrase will be marked in result as a bold text.
     *
     * @param dp A position offset behind the beginning of the best relevant piece of object content.
     * @param nwords A length in words of the returning content piece.
     * @param formatSword A format string for formatting a content word which is in a search phrase via String.format method.
     * @return a best relevant content piece of {@code nwords} length from position {@link #p1} - dp.
     * @see java.lang.String#format(java.lang.String, java.lang.Object...)
     */
    public String getContentRelevantPiece(int dp, int nwords, String formatSword)
    {
        int i1=Math.max(0,p1-dp);
        int i2=Math.min(content.size(), i1+nwords);
        String s="";
        for (int i=i1; i<i2; i++)
        {
            if (i!=i1) s+=" ";
            ContentItem ci=content.get(i);
            s+= formatSword.length()>0 && ci.flag!=0 ? String.format(formatSword, ci.word) : ci.word;
        }
        return s;
    }

}
