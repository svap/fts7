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

package org.fts7.searchRes;

import java.util.Date;

/**
 * This class represents an object data,  storing in a search index.
 * This is a base class for a {@link SearchResultItem#objects} list - the list of
 * objects having the same text content which are linked together with the search result item.
 * @author Sergey Apollonov
 */
public class ObjectItem {
    /** Object name.     */
    public String name;
    /** Object length.   */
    public int len;
    /** Object date-time.*/
    public Date d;
    /** Any object data. */
    public Object data;
    
}
