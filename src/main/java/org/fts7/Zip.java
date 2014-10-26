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

import java.util.zip.*;
import java.io.*;

/**
 * Compress & decompress methods
 * @author Sergey Apollonov
 */
public class Zip {

    public static byte[] compress(String text) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try
        {
            OutputStream out = new DeflaterOutputStream(baos);
            out.write(text.getBytes("UTF-8"));
            out.close();
        } catch (IOException e)
        {
            throw new AssertionError(e);
        }
        return baos.toByteArray();
    }

    public static String decompress(byte[] bytes) {
        InputStream in = new InflaterInputStream(new ByteArrayInputStream(bytes));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try
        {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) > 0)
            {
                baos.write(buffer, 0, len);
            }
            return new String(baos.toByteArray(), "UTF-8");
        } catch (IOException e)
        {
            throw new AssertionError(e);
        }
    }
    
    /**
     * Compress an array of Strings via deflater.
     * @param ss an array of strings to be compressed. Before the compression
     * an array converted to a single string with space delimiter
     * @return a byte array of compressed string
     */
    public static byte[] zipA(String[] ss){
        StringBuilder sb=new StringBuilder();
        for (int i=0; i<ss.length; i++)
            sb.append(i==0? ss[i]: " "+ss[i]);
        return compress(sb.toString());
    }
    
    /**
     * Decompress a byte array to an array of strings.
     * @param b a byte array returned by zipA to decompress
     * @return an array of Strings
     */
    public static String[] unzipA(byte[] b){
        String ss=decompress(b);
        return ss.split(" ");
    }
}
