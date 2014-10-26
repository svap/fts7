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
package tests;

import java.io.*;
import java.sql.*;
import org.fts7.indexer.*;

/**
 *
 * @author Администратор
 */
public class TestCrawler {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {

        long t1 = System.currentTimeMillis();
        UrlIndexer u = new UrlIndexer("z:/Indexes/gazeta.db", false);
        //String s1="http://newsland.com/news/detail/id/1433047/";
        //String s1="http://newsland.com";
        String s1="http://www.gazeta.ru";
        String[] filter =
        {
            "-*user*"
        }; // an array of page filter
        
        
        u.maxScanCount = 50000;
        u.addUrl(s1, filter,10);
        u.prepareIndex();
        
        int t=(int)(System.currentTimeMillis()-t1)/1000;
        System.out.printf("\nData processing ended, crawling time %d (sec)\n", t);
        
    }
}
