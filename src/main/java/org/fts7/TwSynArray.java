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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 *
 * @author Sergey Apollonov
 */
class TwSynArray {

ArrayList <TwSyn> alist;


public TwSynArray(){
alist=new ArrayList <TwSyn> ()    ;
}

/**
 * 
 * @param e объект типа TwSyn, который добавляется к списку
 */
void add(TwSyn e){
alist.add(e);
}

/**
 * очистка списка
 */
void clear() {
alist.clear();
}

/**
 * 
 * @param idw word id to find in the array
 * @return TwSyn Object with idw or null if not found
 */    
TwSyn synFindIdw (int idw) {
for (TwSyn a :alist) // linear search
                     if (a.idw==idw) return(a);
return (null);
}  

/**
 * 
 * @param w word to find in the array
 * @return TwSyn Object with word w or null if not found
 */    
TwSyn synFindWord (String w) {
for (TwSyn a :alist) // linear search
                     if (a.word.equals(w)) return(a);
return (null);
} 

class ComparatorIdw implements Comparator <TwSyn> {
    @Override
    public int compare(TwSyn r1, TwSyn r2) {
        return r1.idw-r2.idw;
    }
}

/**
 *  Сортировка элементов списка по возрастанию idw
 */
void sort(){
Collections.sort(alist, new ComparatorIdw());
}

/**
 * возвращает массив слов
 * @return 
 */
String[] wordsArray(){
String[] r=new String[alist.size()];
int i=0;
for (TwSyn a:alist)
                    r[i++]=a.word;
return r;
}

}
