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

package org.fts7;

import java.util.*;
import java.io.*;
import java.security.*;
import java.util.regex.Pattern;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.txt.*;
import org.apache.tika.sax.WriteOutContentHandler;
import org.xml.sax.SAXException;


/**
 * This class represents a file with a text content as an indexing object.
 * <br>Class implements an ObjectContent interface, therefore a class instance
 * can be added to an index by {@link Indexer#addObject(ObjectContent)}
 * method.<br>
 * To extract text from a file this class uses the Apache Tika™ toolkit.
 *
 * @see <a href="http://tika.apache.org">Apache Tika toolkit</a>
 * @author Sergey Apollonov
 */
public class FileContent implements ObjectContent
{

    private String fileN;
    private String[] content;
    private File fl;
    private byte[] hash;

    // шаблон для разбора тела файла
    private static Pattern p=Pattern.compile(
//"(\\s+|"+ // whitespace символ разделитель
//"(?<=[\\s^])\\W+|"+ // текущая цепочка не буквенно-цифровая и предыдущий символ разделитель (whitespace)
//"(?<=\\w)[\\W&&[^\\.-]]+|"+ // текущая цепочка не буквенно-цифровая за исключением [.:-]  и предыдущий символ буквенно-цифровой
//"(?<=\\D)[\\.:-]+)+"  // текущий символ [.:-] а предыдущий буква

        "([\\s_]+|"+ // whitespace символ разделитель
        "[\\W&&[^\\.:\\-\\\\/]]+|"+ // не буквенно-цифровой символ за исключением .:-/
        "[\\.:\\-\\\\/](?=\\D|$)|"+  // текущий символ .:-/ а за ним не цифра ли конец строки
        "(?<=\\D|^)[\\.:\\-\\\\/]"+  // текущий символ .:-/ а предыдущий не цифра или начало строки
        ")+"
        ,Pattern.UNICODE_CHARACTER_CLASS);

    // шаблон для разбора имени файла
    private static Pattern w=Pattern.compile("[\\s_\\W]+",Pattern.UNICODE_CHARACTER_CLASS);


    // ращепление токенайзером
    String[] split(String body)
    {
        StringTokenizer stk = new StringTokenizer(body," \n\t\f\r"); // разбор поисковой фразы
        ArrayList<String> ww=new ArrayList<>();
        while (stk.hasMoreTokens())
        {
            String s=stk.nextToken();
            String[] xx=p.split(s); //   w.split(s);
            for (String xi:xx)
                if (xi.length()>0 && xi.length()<=15) ww.add(xi);
            if (ww.size()>=0xFFFF)
                break;
        }
        String [] ra=ww.toArray(new String[ww.size()]);
        ww.clear();
        return ra;
    }

    /**
     * Creates an instance for file fileName.
     * @param fileName a full name of an indexing file
     */
    public FileContent(String fileName)
    throws SAXException,TikaException,FileNotFoundException,IOException,OutOfMemoryError
    {

        FileInputStream fis=null,fis1=null;

        try
        {
            fileN=fileName;
            fl=new File(fileName);
            String fn=fl.getName(); // имя файла+extension

            String fn1= fn.lastIndexOf('.')!=-1 ? fn.substring(0, fn.lastIndexOf('.')) : fn; // cut extension from fileName;
            String[] nameContent=w.split(fn1); // расщепленное имя файла, будем добавлять в контент после формирования хэша


            Parser parser = new AutoDetectParser();
            Metadata metadata = new Metadata();
            StringWriter writer = new StringWriter();

            fl=new File(fileName);
            fis=new FileInputStream(fl);
            parser.parse(fis,
                         new WriteOutContentHandler(writer),
                         metadata,
                         new ParseContext());

            String cont = writer.toString().trim();
            if (cont.length()==0)
            {
                fis1=new FileInputStream(fl);
                TXTParser tp=new TXTParser();
                tp.parse(fis1,
                         new WriteOutContentHandler(writer),
                         metadata,
                         new ParseContext());
                cont=writer.toString();
            }

            String[] bodyContent=split(cont);

            try
            {
                MessageDigest md = MessageDigest.getInstance("MD5");
                for(String a:bodyContent)
                    md.update(a.toUpperCase().getBytes("UTF-8"));
                hash=md.digest();
            }
            catch (NoSuchAlgorithmException|UnsupportedEncodingException e)
            {
                throw new RuntimeException ("FileContent exception - "+e.getMessage());
            }


            content = new String[nameContent.length+bodyContent.length];
            System.arraycopy(nameContent, 0, content, 0, nameContent.length);
            System.arraycopy(bodyContent, 0, content, nameContent.length, bodyContent.length);
        }
        finally
        {
            if (fis!=null ) fis.close();
            if (fis1!=null) fis1.close();
        }
    }

    /**
     * Get a full file name, which was passed to the object's constructor.
     * @return a name of indexing file
     */
    public String getName()
    {
        return fileN;
    }

    /**
     * Get a file text content. To extract text from a file this method uses
     * the Apache Tika™ toolkit
     * @return a file text content as a String array.
     * @see <a href="http://tika.apache.org">Apache Tika toolkit</a>
     */
    public String[] getContent()
    {
        return content;
    }

    /**
     * Get a file length in bytes.
     * @return a file length
     */
    public int getLength()
    {
        return (int)fl.length();
    }

    /**
     * Get a file dateTime stamp.
     * @return a dateTime stamp of file last updation or creation.
     */
    public Date getDate()
    {
        return new Date(fl.lastModified());
    }

    /**
     * Get an object, linked with file
     * @return null (no object, linked with file).
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
