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

/**
 * A class for filtering strings via WildCard patterns.
 * Constructor accepts a String array (listFilter) of include and exclude wildcards (*?) patterns.
 * If a pattern begins with '-' character then it defines an exclude rule, 
 * otherwise it is an include pattern.
 * If a test string matches include and exclude pattern simultaneously then the
 * string does not pass filtering.
 * 
 * @author Sergey Apollonov
 */
public class WildCardFilter {

    String[] filter;
    boolean[] include;
    int ni, ne;
    
    /**
     * 
     * @param listFilter an array with include or exclude wildcards patterns.
     */
    public WildCardFilter(String[] listFilter) {
        filter = listFilter;
        if (listFilter != null) {
            filter = new String[listFilter.length];
            include = new boolean[listFilter.length];
            for (int i = 0; i < listFilter.length; i++) {
                if (listFilter[i].matches("^-.+")) // exclude pattern
                {
                    include[i] = false;
                    ne++;
                    filter[i] = listFilter[i].substring(1);
                } else // include pattern
                {
                    include[i] = true;
                    ni++;
                    filter[i] = listFilter[i];
                }
                filter[i] = filter[i].replace(".", "\\.").replace("*", ".*").replace("?", ".").toUpperCase();
            }
        }
    }

    /**
     * Test a string for filtering. 
     * @param s a string for filtering
     * @return true if a string passed filtering.
     */
    public boolean passed(String s) {
        if (filter == null || filter.length == 0) // always passed if listfilter is empty
        {
            return true;
        }

        int j = 0;
        String su = s.toUpperCase();
        for (int i = 0; i < filter.length; i++) {
            if (su.matches(filter[i])) {
                if (include[i]) // match to include pattern
                {
                    j++;
                } else // match to exclude pattern
                {
                    return false;
                }
            }
        }

        return ni > 0 ? j > 0 : true;
    }
}

