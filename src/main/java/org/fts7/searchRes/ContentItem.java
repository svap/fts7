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

/**
 * An element of an object's text content (a single word) .
 * This is a base class for a {@link SearchResultItem#content} list which
 * represents a full text content of an indexing object.
 * @author Sergey Apollonov
 */
public class ContentItem {
/** This flag will be set when the {@link #word} is in a search phrase */     
public static byte SEARCH_WORD=1;

/** This flag will be set if the {@link #word} is a word with the same meaning
 * (or nearly the same) as any word in a search phrase (synonym). */
public static byte SEARCH_SYN =2;

/** A single word of text content */    
public String word;

/** flag of the {@link #word} ({@link #SEARCH_WORD} or {@link #SEARCH_SYN}) */
public byte flag;
}
