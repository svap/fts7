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

package org.fts7.util;

import org.fts7.searchRes.SearchResultItem;
import org.fts7.searchRes.SearchResult;
import org.fts7.searchRes.ObjectItem;
import org.fts7.Search;
import java.sql.*;
import java.io.*;
import java.util.*;

/**
 * This is a simplest example of using full-text searching.
 * Searcher is a console utility, it accepts an index file name as single parameter on startup.
 * Searcher reads a search phrase from console and searches objects within the index.
 * Search results are printed to console.
 * <p>
 * Usage: java -cp fts7.jar org.fts7.util.Searcher IndexFile
 * @author Sergey Apollonov
 */
public class Searcher
{

    public static void main(String[] args)
    {
        if (args.length==0)   // run without arguments
        {
            System.out.println("\nThis utility search in index file"+
                               "\nUsage: java -cp fts7.jar org.fts7.util.Searcher <IndexFile>\n"
                              );
            return;
        }
        try
        {
            Search ss=new Search(args[0]); // create Search object instance for IndexFile
            Scanner in = new Scanner(System.in);
            while (true) // console input loop
            {
                System.out.print("\nEnter search phrase (- to break):");
                String s=in.nextLine();
                if (s.length()>0)
                {
                    if (s.equals("-")) break;
                    SearchResult res=ss.search(s, 1); // search, get a result for page 1

                    // print search result to console
                    for(SearchResultItem a:res)
                    {
                        System.out.printf("\n%d.", a.n); // line number
                        for(ObjectItem f:a.objects) // print all found file names
                            System.out.printf("%s\n", f.name);
                        // print the best relevant piece of a file content
                        System.out.printf("%s\n",a.getContentRelevantPiece(6,60, "[%s]"));
                    }
                    System.out.printf("\nTotal found %d  totalSearchTime=%d printed first "+
                                      "20 items (page 1), found keywords are surrounded by [] brackets\n",
                                      res.cnt,res.totalSearchTime);
                }
            } // while
        } //try
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }

}
