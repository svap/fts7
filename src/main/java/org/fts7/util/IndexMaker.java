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
import java.util.*;
import java.io.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

/**
 * This utility builts search index file from an index profile.
 * The index profile is an XML file with .ip extension, contains all settings to build search index.
 * You can found the example of profile in the Index profiles subdirectory of the fts7.zip file.
 * <p>
 * Usage: java -cp fts7.jar org.fts7.util.IndexMaker index_profile
 * @author Sergey Apollonov
 */
public class IndexMaker
{

    public static void main(String[] args)
    {
        if (args.length==0)   // run without arguments
        {
            System.out.println("\nThis utility builts search index file from index profile"+
                               "\nUsage: java -cp fts7.jar org.fts7.util.IndexMaker <profile> [-cre]\n"+
                               "\n      <profile> is a profile file to build index"+
                               "\n      -cre force to build an index from scratch"
                              );
            return;
        }

        boolean create= false;
        String profile="";
        for (String s:args)  // arguments processing
        {
            if (s.toUpperCase().matches("^-CRE"))
            {
                create=true;
                continue;
            }
            profile=s; // an index profile to be executed
        }

        if (profile.length()==0)
        {
            System.out.println("An indexing profile is undefined");
            return;
        }

        IndexProfile p=new IndexProfile(profile);
        p.exec(create); // execute profle (create|update index)

    }
}


/**
 * This class describes a single path to be indexed
 */
class PathItem
{
    /** Full path to be indexed */
    String path;

    /** an array of included or excluded file patterns with wildcards */
    String[] filter;

    /** if true then path will be indexed, else path discards from indexing */
    boolean enable;
}

/**
 * Index profile class
 */
class IndexProfile extends ArrayList <PathItem>
{

    /** index file */
    String indexFile;

    /** index title */
    String title;

    /** profile version */
    String version;

    /** profile file */
    String filename;


    /**
     *
     * @param profile an xml file with index profile
     */
    public IndexProfile(String profile)
    {
        try
        {
            File prof = new File(profile);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(prof);

            filename=profile;
            if (doc.getDocumentElement().getNodeName().equals("Configuration"))
            {
                //7sDoc compatible index profile
                version="7sDoc";
                Element index=(Element) doc.getDocumentElement().getElementsByTagName("Index").item(0);
                indexFile=index.getElementsByTagName("ifile").item(0).getTextContent();
                title=index.getElementsByTagName("title").item(0).getTextContent();
                //System.out.printf("%s\nindex file: %s\n",title,ifile);

                Element items=(Element) index.getElementsByTagName("items").item(0);
                String scount=items.getElementsByTagName("Count").item(0).getTextContent();
                int count=Integer.parseInt(scount); // a count of paths for indexing

                for(int i=0; i<count; i++)
                {
                    Element itemn=(Element)items.getElementsByTagName("Item"+String.valueOf(i)).item(0);
                    PathItem p=new PathItem();
                    p.enable=itemn.getElementsByTagName("en").item(0).getTextContent().equals("TRUE");
                    p.path=itemn.getElementsByTagName("path").item(0).getTextContent();
                    ArrayList<String> ff=new ArrayList<>(); // filters
                    String incl=itemn.getElementsByTagName("incl").item(0).getTextContent();
                    String[] ai=incl.split(" ");
                    for (String s:ai)
                        if (s.length()>0) ff.add(s);

                    String excl=itemn.getElementsByTagName("excl").item(0).getTextContent();
                    String[] ae=excl.split(" ");
                    for (String s:ae)
                        if (s.length()>0) ff.add("-"+s);
                    p.filter=new String[ff.size()];
                    p.filter=ff.toArray(p.filter);
                    add(p);
                }
            } // Configuration

            if (doc.getDocumentElement().getNodeName().equals("index"))
            {
                // new index profile format
                version="7s";
                Element index=doc.getDocumentElement();
                indexFile=index.getElementsByTagName("ifile").item(0).getTextContent();
                title=index.getElementsByTagName("title").item(0).getTextContent();
                Element items=(Element)index.getElementsByTagName("items").item(0);
                NodeList ni=items.getElementsByTagName("item");
                for (int i=0; i<ni.getLength(); i++)
                {
                    PathItem p=new PathItem();
                    Element item=(Element)ni.item(i);
                    String enable=item.getAttribute("enable");
                    p.enable=enable.length()==0 || !enable.equals("0");
                    p.path=item.getElementsByTagName("path").item(0).getTextContent();
                    NodeList incl=item.getElementsByTagName("incl");
                    p.filter=new String[incl.getLength()];
                    for (int j=0; j<incl.getLength(); j++)
                        p.filter[j]=incl.item(j).getTextContent();
                    add(p);
                }

            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    /**
     * run index profile, create index
     */
    void exec(boolean create)
    {
        System.out.printf("\n%s profile %s\n%s\n",version,filename,title);
        System.out.printf((create?"Creating":"Updating")+" index file: %s\n",indexFile);
// Indexing paths
        try
        {
            long t1 = System.currentTimeMillis();
            org.fts7.Indexer si=new org.fts7.Indexer(indexFile,create);
            for(PathItem p:this)
                if (p.enable)
                {
                    System.out.printf("\nAdding path: %s\n",p.path);
                    si.addPath(p.path, p.filter);
                }

            System.out.println("\nPreparing index ...");
            si.prepareIndex();
            System.out.printf("\nData processing ended, total %d files processed in %d minutes\n",si.addCount,
                              (System.currentTimeMillis()-t1)/(1000*60));
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
}

