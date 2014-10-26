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

import java.util.ArrayList;
import java.io.*;
import java.sql.*;
import org.fts7.indexer.*;

/**
 * A multithread URL crawler. This utility crawls the URL, add the URL's content
 * to an index, extracts all hyperlinks from url page and pushs them to the list
 * of URL's to visit. Next the program pops the URL's from the list one by one
 * until it is empty and crawls them.<br>
 * Only the hyperlinks beginning with given URL are processed.
 * <p>
 * Usage: java -cp fts7.jar org.fts7.util.WebCrawler &lt;URL&gt; &lt;IndexFile&gt; [-cre] [-fi&lt;wildcard filter&gt;] [-tN] [-limN]
 *  <ul>
 *   <li>&lt;URL&gt; - an URL to be crawled for indexing (must begin with http://);
 *   <li>&lt;IndexFile&gt; - an index file;
 *   <li>-cre - forces to create an index file from scratch;
 *   <li>-fi&lt;wildcard filter&gt; - an include|exclude wildcards patterns to filter url;
 *   <li>-tN - a number of threads (N) to be launched simultaneously for indexing;
 *   <li>-limN - a limit of url's (N) to be processed.
 *</ul>
 * @author Sergey Apollonov
 */
public class WebCrawler {

    public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException {

        String help = "\nThis multithread URL crawler builds a search index on a given URL"
                + "\nUsage: java -cp fts7.jar org.fts7.util.WebCrawler <URL> <IndexFile> [-cre] [-fi<wildcard filter>] [-tN] [-limN]\n"
                + "\n<URL> - an URL to be crawled for indexing (must begin with http://)"
                + "\n<IndexFile> - an index file"
                + "\n-cre - forces to create an index file from scratch"
                + "\n-fi<wildcard filter> - an include|exclude wildcards patterns to filter url"
                + "\n-tN - a number of threads (N) to be launched simultaneously for indexing"
                + "\n-limN - a limit of url's (N) to be processed";

        if (args.length == 0)   // run without arguments
        {
            System.out.println(help);
            return;
        }
        String url = null;
        String indexFile = null;
        String filts = "";
        boolean createNew = false;
        int nt = 4;
        int lim = 0;

        for (String p : args)
        {
            if (p.toUpperCase().startsWith("HTTP://"))
            {   // URL
                url = p;
                continue;
            }

            if (!p.startsWith("-"))
            { // an indexfile
                indexFile = p;
                continue;
            }

            if (p.equals("-cre"))
                createNew = true;

            if (p.startsWith("-t")) // a number of parallel threads
                nt = Integer.parseInt(p.substring(2));

            if (p.startsWith("-lim")) // limit of url's to e crawled
                lim = Integer.parseInt(p.substring(4));

            if (p.startsWith("-fi")) // url filter
                filts += " " + p.substring(3);
        }

        if (indexFile == null)
        {
            System.out.println("\nAn indexFile parameter is missing\n" + help);
            return;
        }

        if (url == null)
        {
            System.out.println("\nAn URL parameter is missing\n" + help);
            return;
        }

        filts = filts.trim(); // trim first space
        System.out.printf(
                "\nindexed URL    %s"
                + "\nindexFile      %s"
                + "\nlimit of url's %d"
                + "\nurl filter     %s"
                + "\nthreads        %d\n\n", url, indexFile, lim, filts, nt);

        String[] filter=null;
        if (filts.length()>0) filter = filts.split(" ");

        long t1 = System.currentTimeMillis();
        UrlIndexer u = new UrlIndexer(indexFile, createNew);

        u.maxScanCount = lim;      // setup limit of url's to be indexed
        u.addUrl(url, filter, nt); // start multithread indexing an url with nt threads
        u.prepareIndex();          // finish indexing

        int t = (int) (System.currentTimeMillis() - t1) / 1000;
        System.out.printf("\nIndexing finished, total URL's crawled %d  processing time %d (sec)\n", u.addCount, t);
    }
}
