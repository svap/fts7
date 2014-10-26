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

import java.io.File;
import java.io.FileFilter;
import java.sql.SQLException;
import org.fts7.*;

/**
 * This class creates a full text search index on files, having text content.
 * The search index is created in a SQLite database file as a
 * single file with .db extension.
 *
 * @see <a href="http://sqlite.org">SQLite</a>
 * @author Sergey Apollonov
 
 */
public class FileIndexer extends CoreIndexer {

    public FileIndexer(String indexfile, boolean createNew) throws SQLException, ClassNotFoundException {
        super(indexfile, createNew);
    }

    
    int pathScanedCount;
    int fileContentTime;

    /**
     * recursive directory scanning
     * @param d directory
     * @param filter file filter
     * @throws SQLException
     */
    void DirReader(File d, WildCardFileFilter filter) throws SQLException
    {

        if (maxScanCount>0 && addCount>maxScanCount) return; //###################################

        File[] listf = d.listFiles(filter); // получить список файловых объектов
        if (listf==null) return;
        if (listf.length==0) return;

// цикл выборки файлов
        FileContent fob;
        for(File f:listf)
            if (f.isFile())   // это файл
            {
                long t=System.currentTimeMillis();
                try
                {
                    fob=new FileContent(f.getPath());
                    addObject(fob);
                }
                catch (Exception|Error e)
                {
                    System.out.println(e.getMessage());
                }


                fileContentTime+=System.currentTimeMillis()-t;
                pathScanedCount++;
                if (output!=0) System.out.printf("%d %s\n", addCount,f.getPath());
            }
// цикл выборки каталогов
        for(File f:listf)
            if(f.isDirectory()) // это каталог
                DirReader(f,filter);
    }


    int addPathTime;
    /**
     * Add files of directory {@code dir} and subdirectories to the search index.
     * <br>This method scans a directory, selects files according to a listFilter patterns,
     * creates {@link org.fts7.indexer.FileContent} object for each
     * found file and calls {@link #addObject(ObjectContent)} method to add each
     * FileContent object to an index.
     *
     * @param dir a directory to be indexed.
     * @param listFilter an array of included or excluded file patterns with wildcards (*?).
     * If the listFilter is null or empty then all files will be indexed.
     * If a pattern begins with '-' character then it defines exclude rule, otherwise it  is an include pattern.
     * If a file name matches include and exclude pattern simultaneously then the
     * file will be excluded from indexing.
     * <p>For example:
     * <br>{@code listFilter={"*.doc","*.rtf"}} includes only .doc and .rtf files
     * <br>{@code listFilter={"-*.exe","-*.dll"}} includes all files except .exe,.dll
     * <br>{@code listFilter={"*.doc","-c:\xxx\bin\*"}} includes all .doc files except
     *     any files in subdirectory c:\xxx\bin\

     * @throws SQLException
     * @see StopList
     * @see #addCount
     * @see #addObject(org.fts7.ObjectContent)
     * @see org.fts7.ObjectContent
     */
    public void addPath(String dir, String[] listFilter) throws SQLException
    {
        pathScanedCount=0;
        addCount=0;
        long t1 = System.currentTimeMillis();
        File f=new File(dir);
        WildCardFileFilter filter=new WildCardFileFilter(listFilter);
        if (!f.exists()) throw new RuntimeException(String.format("path %s is not exists",dir));
        if (!f.isDirectory()) throw new RuntimeException(String.format("path %s is not a directory",dir));
        DirReader(f,filter);
        flushDocs();
        saveCount(); // save quantity of processed files in database index
        addPathTime+=System.currentTimeMillis()-t1;
    }

    /**
     * file filtering via include or exclude patterns with wildcards (*?).
     */
    class WildCardFileFilter implements FileFilter
    {
        String[]  filter;
        boolean[] include;
        int ni,ne;

        public WildCardFileFilter(String[] listFilter)
        {
            filter=listFilter;
            if (listFilter!=null)
            {
                filter=new String[listFilter.length];
                include=new boolean[listFilter.length];
                for (int i=0; i<listFilter.length; i++)
                {
                    if (listFilter[i].matches("^-.+"))  // exclude pattern
                    {
                        include[i]=false;
                        ne++;
                        filter[i]=listFilter[i].substring(1);
                    }
                    else  // include pattern
                    {
                        include[i]=true;
                        ni++;
                        filter[i]=listFilter[i];
                    }

                    filter[i]=filter[i].replace(".","\\.").replace("*", ".*").replace("?", ".").toUpperCase();
                }
            }
        }

        public boolean accept(File file)
        {
            if (filter==null || filter.length==0)
                return true;
            int j=0;
            for (int i=0; i<filter.length; i++)
                if (file.getPath().toUpperCase().matches(filter[i]))
                {
                    if (include[i]) // match to include pattern
                        j++;
                    else // match to exclude pattern
                        return false;
                }

            if (file.isDirectory())
                return true;

            return ni>0 ? j>0 : true;
        }
    }

}
