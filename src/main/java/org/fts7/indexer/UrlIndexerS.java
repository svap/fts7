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
package org.fts7.indexer;

import java.sql.SQLException;
import java.io.*;
import java.util.*;
import org.fts7.CoreIndexer;
import org.fts7.WildCardFilter;

/**
 * A single-thread class to create search index on URL's
 *
 * @author Sergey Apollonov
 */
public class UrlIndexerS extends CoreIndexer {

    /**
     * Opens an existing index file or creates a new empty index if the
     * indexfile is not found.
     *
     * @param indexfile a full name of the index file.
     * @param createNew if true then always creates a new empty index file. If
     * the index file exists then deletes the existing index and creates an
     * empty new one.
     * <br>If false then tries to open an existing index file and if the file is
     * not found then creates a new empty index.
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public UrlIndexerS(String indexfile, boolean createNew) throws SQLException, ClassNotFoundException {
        super(indexfile, createNew);
    }

    /**
     * Add URL to a search index.
     *
     * @param url an URL to be indexed
     * @throws IOException
     * @throws SQLException
     */

    String baseUrl;
    
    public void addUrl(String url, String[] urlFilter) throws IOException, SQLException {
        try
        {
            baseUrl = url;
            putCUrl(url); // store initial url
            addCount=0;

            String uu;
            WildCardFilter filter = new WildCardFilter(urlFilter);
            while ((uu = getCUrl()) != null)
            {
                if (maxScanCount > 0 && addCount >= maxScanCount)
                    break;
                
                if (filter.passed(uu) && uu.startsWith(baseUrl))
                {
                    //_addUrl(uu);
                    UrlContent u = new UrlContent(uu); // get URL content
                    addObject(u); // add to index
                    if (output != 0)
                        System.out.printf("%d %s\n", addCount, uu);

                    for (String link : u.links) // add links to CURL table
                    {
                        if (filter.passed(link) && link.startsWith(baseUrl))
                        {
                            String[] ss = link.split("#");
                            putCUrl(ss[0]);
                        }
                    }
                }
            }

        } finally
        {
            saveCount();   // save addedCount in database;
        }
    }
}
