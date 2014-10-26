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

import java.io.IOException;
import java.sql.SQLException;
import org.fts7.CoreIndexer;
import org.fts7.WildCardFilter;

/**
 * A multi-thread class to create search index on URL's
 * @author Sergey Apollonov
 */
public class UrlIndexer extends CoreIndexer implements Runnable {

    public UrlIndexer(String indexfile, boolean createNew) throws SQLException, ClassNotFoundException {
        super(indexfile, createNew);
    }

    
    String baseUrl;
    WildCardFilter filter;
    int ntr;
    
    /**
     * Add an URL to an index.
     * This method crawls the URL, add the URL's content to an index, extracts
     * all hyperlinks from the page and pushs them to the list of URLs to visit. 
     * Next the method pops the URL's from the list one by one until it is empty and crawls them.
     * Only the hyperlinks beginning with {@code url} are pushed  to a crawling list.
     *
     * @param url an URL to be indexed
     * @param urlf an include|exclude url's wildcards patterns to filter url's 
     * on indexing;
     * @param nt a number of parallel threads to be launched on url processing
     * @throws IOException
     * @throws SQLException
     */
    public void addUrl(String url, String[] urlf, int nt) throws IOException, SQLException {
        Thread[] tt = new Thread[nt]; // array of threads objects to be launched
        baseUrl = url;
        ntr = nt;
        filter = new WildCardFilter(urlf);

        putCUrl(url); // store initial url
        addCount=0;

        for (int i = 0; i < nt; i++) // порождение  и запуск nt параллельных потоков 
        {
            tt[i] = new Thread(this);
            tt[i].start();
        }

        for (Thread t : tt) // ждать завершения всех потоков
            try
            {
                t.join();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        saveCount();   // save addedCount in database;
    }

    static final Object synGET = new Object();
    static final Object synADD = new Object();

    @Override
    public void run() {
        String uu;
        int nn = 0;
        try
        {
            while (true)
            {
                //synchronized (synGET)
                {
                    uu = getCUrl();
                }
                if (uu == null)
                {
                    if (++nn > 10 || ntr == 1)
                        // число холостых циклов неполучения url методом getCUrl()
                        // более 10, есть основания полагать, что данных более не будет
                        //(все проиндексировали) и параллельные потоки ничего
                        // более не добавят ...                        
                        break;
                    // ждем 10 сек перед следующей попыткой
                    Thread.sleep(10000);
                    continue;
                }
                nn = 0; // url получен - сбрасываем
                if (maxScanCount > 0 && addCount >= maxScanCount)
                    break;

                if (filter.passed(uu) && uu.startsWith(baseUrl))
                {
                    UrlContent u = new UrlContent(uu); // get URL content
                    
                    synchronized (synADD)
                    {
                        addObject(u); // add to index
                        if (output != 0)
                           System.out.printf("%d %s\n", addCount, uu);
                    }

                    //synchronized (synGET)
                    {
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
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }

    }

}
