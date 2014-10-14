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


import java.util.*;

/**
 * This class represents a result of searching, returning by the 
 * {@link org.fts7.Search#search(String, int)} method.
 * <br>This class is an ArrayList list of the {@link SearchResultItem} objects.
 * Each search item related to the indexed ObjectContent object.
 * 
 * @see SearchResultItem
 * @author Sergey Apollonov
 */
public class SearchResult extends ArrayList <SearchResultItem> {
/** A quantity of found objects. */
public int cnt;

/** Total scanning cycles. */
public int scnt;

/** A time (milliseconds) of words searching. */
public int queryWordsTime;

/** A time (milliseconds) of core searching. */
public int queryTime;

/** A time (milliseconds) of extracting query results for page. */
public int queryResultExtractTime;

/** Total search time (milliseconds). */
public int totalSearchTime;

/** An array of searching words.
 * This array includes all words from the search phrase
 * and words with the same meaning (or nearly the same) as any word in the search
 * phrase (synonyms).
 */
public String[] words; 

/**
 * Print search result to console.
 */
public void print(){
System.out.println(String.format("rcnt=%d scnt=%d totalSearchTime=%d queryWordsTime=%d queryTime=%d queryResultExtractTime=%d",
cnt,scnt,totalSearchTime,queryWordsTime,queryTime,queryResultExtractTime));

for(SearchResultItem a:this){
System.out.printf("\n%d. IDf=%d  ", a.n,a.idf);
for(ObjectItem f:a.objects)
                       System.out.printf("%s\n", f.name);
System.out.printf("%s\n",a.getContentRelevantPiece(6,60, "\u001b[1;31m%s\u001b[1;0m"));
System.out.printf("%d\t RW1=%d RWP=%d RWH=%d RWW=%d\n",a.scanR.result,a.scanR.rw1,a.scanR.rwp,
        a.scanR.rwh,a.scanR.rww);
System.out.printf("\t dw=%d p1=%d k=%d h=%d nn=%d\n",a.scanR.dw,a.scanR.p1,a.scanR.k,a.scanR.h,a.scanR.nn);
}
}

}


