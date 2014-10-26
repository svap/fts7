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

import org.fts7.Zip;

/**
 *
 * @author Администратор
 */
public class TestZip {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    
    StringBuilder sb=new StringBuilder();
    for(int i=0; i<25000; i++) sb.append(" "+String.valueOf(i));
    
    String s=sb.toString();
    byte[] b=Zip.compress(s);
    String s1=Zip.decompress(b);
    if (s.equals(s1)) System.out.println("Success!!!!!");
    }
    
    
}
