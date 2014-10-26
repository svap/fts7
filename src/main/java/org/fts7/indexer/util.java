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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Common methods to be used in Indexer's
 * @author Sergey Apollonov
 */
public class util {
    
    /**
     * Calculates an md5 hash of a string array.
     * @param cc an array of strings to be hashed
     * @return md5 hash of the cc array
     */
    public static byte[] stringArrayHash(String[] cc){
    try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            for (String a : cc) {
                md.update(a.toUpperCase().getBytes("UTF-8"));
            }
            return md.digest();
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new RuntimeException("stringArrayHash exception - " + e.getMessage());
        }    
    }
    
}
