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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import org.fts7.ObjectContent;

/**
 * This class represents url as an indexing object.
 * <br>Class implements an ObjectContent interface, therefore a class instance
 * can be added to an index by {@link org.fts7.CoreIndexer#addObject(ObjectContent)}
 * method.<br>
 * To parse url this class uses the Jsoup library.
 *
 * @see <a href="http://jsoup.org">jsoup Java HTML parser</a>
 * @author Sergey Apollonov
 */
public class UrlContent implements ObjectContent {

    private String[] content;
    private int length;
    private String urln;
    private byte[] hash;

    public ArrayList<String> links = new ArrayList<>(); // URL links;

    public UrlContent(String url) throws IOException {

        urln = url; // сохраняем url

        // шаблон для разбора контента
        Pattern p = Pattern.compile(
                "([\\s_]+|" + // whitespace символ разделитель
                "[\\W&&[^\\.:\\-\\\\/]]+|" + // не буквенно-цифровой символ за исключением .:-/
                "[\\.:\\-\\\\/](?=\\D|$)|" + // текущий символ .:-/ а за ним не цифра или конец строки
                "(?<=\\D|^)[\\.:\\-\\\\/]" + // текущий символ .:-/ а предыдущий не цифра или начало строки
                ")+", Pattern.UNICODE_CHARACTER_CLASS);
        
        try {
        Document doc = Jsoup.connect(url).get();
        content = p.split(doc.text());
        length = doc.text().length();

        //get all links 
        Elements urlLinks = doc.select("a[href]");
        for (Element link : urlLinks) {
            links.add(link.attr("abs:href")); // save all links for further using by crawler!
        }
        
        hash=util.stringArrayHash(content);
        }
        catch (Exception e) // supress any exception here
                            {}
        
    }
    
    /**
     * Get an url, which was passed to the object's constructor.
     * @return an indexed url
     */
    public String getName()
    {
        return urln;
    }

    /**
     * Get an url text content for indexing. To parse url content this method uses
     * the jsoup library
     * @return an url  text content as a String array.
     * @see <a href="http://jsoup.org">jsoup Java HTML parser</a>
     */
    public String[] getContent()
    {
        return content;
    }

    /**
     * Get an url's content length in bytes.
     * @return a content length
     */
    public int getLength()
    {
        return length;
    }

    /**
     * Get a dateTime stamp when the url was parsed.
     * @return a dateTime stamp of file last updation or creation.
     */
    public Date getDate()
    {
        return new Date();
    }

    /**
     * Get an object, linked with url
     * @return null (no object, linked).
     */
    public Object getObject()
    {
        return null;
    }

    /**
     * Get a hash of a file text content.
     * @return a file content hash as a byte array.
     */
    public byte[] getContentHash()
    {
        return hash;
    }


}
