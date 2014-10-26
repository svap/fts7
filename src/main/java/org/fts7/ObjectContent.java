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
import java.lang.*;

/**
 * This interface must be implemented by any indexable object
 * @author Sergey Apollonov
 */
public interface ObjectContent
{

    /**
     * @return a name of indexing object
     */
    String getName();

    /**
     *
     * @return a length of indexing object
     */
    int getLength();

    /**
     * @return a dateTime stamp of indexing object
     */
    Date getDate();

    /**
     * @return a text content of indexing object as a String array
     */
    String[] getContent();

    /**
     * @return an object linked with indexing object
     */
    Object getObject();

    /**
     * @return an object content hash
     */
    byte[] getContentHash();

}
