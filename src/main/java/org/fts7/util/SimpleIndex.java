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

import org.fts7.Indexer;
import java.io.*;

/**
 * A simplest example of indexing.
 * <br>This utility scans C: drive and 
 * builds search index in C:\Indexes directory for all .doc and .html files.
 * <p>
 * Usage: java -cp fts7.jar org.fts7.util.SimpleIndex
 * @author Sergey Apollonov
 */
public class SimpleIndex {
    
public static void main(String[] args) {

System.out.println(
"\nA simplest example of indexing. This utility scans C: drive and "+
"\nbuilds search index in C:/Indexes directory for all .doc and .html files of the C: drive.\n\n"
);

// create C:\Indexes directory if it does not exists
File dir = new File("C:/Indexes");
if (!dir.exists()) // the directory C:/Indexes is not exists, create 
                   dir.mkdir();

try {
// create a new indexer instance, C:/Indexes/c.db is an index file,
// the index will be created from scratch (true)    
Indexer in=new Indexer("C:/Indexes/c.db",true);

String[] filter = {"*.doc","*.html"}; // an array of file types to be indexed
in.addPath("C:/", filter); // add *.doc,*.html files of C: drive to an index
in.prepareIndex(); // this method must be called after a series of addPath calls

System.out.printf("\n Indexing finished, total files scaned %d\n", in.addCount);
}
catch (Exception e) {e.printStackTrace();}

}
}