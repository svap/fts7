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

/**
 * Список слов файла
 * @author Администратор
 */
class WordList extends HashMap <String, WordListItem> {
int nWords=0;    // число добавленных слов в список
int minLength=2; // минимальный размер слова, добавляемый в индекс

public WordList(int cnt){
super(cnt);
}

/**
 * пополняет массив строк, которые не будут добавляться в список
 * @param noIndex 
 */
/*
void addStopList(String[] noIndex){
for(String s:noIndex)
                     noIndexSet.add(s.toUpperCase());
}
*/

/**
 * Возвращает истину, если слово нужно добавлять в список (не содержится в noIndexSet)
 * @param w
 * @return 
 */
boolean isAddable (String w){
return !StopList.getInstance().isStopWord(w); 
}

/**
 * Добавляет слово w в список. Добавляются только не пустые слова и только те, которые не содержатся
 * в списке noIndexSet
 * @param w 
 */
void addWord(String w){
if (nWords>0xffff) // максимально добавляемое число слов в индекс
                   return; 
nWords++;
String uw=w.toUpperCase();
if(w.length()>=minLength && isAddable(uw)){
            WordListItem a;
            a=get(uw); // получить элемент списка по ключу
            if (a==null){ // если элемент не найден, то создать и добавить в список
                         a=new WordListItem();
                         put(uw,a);
                        }
            a.p.add(nWords); // позиция слова
            }
}

/**
 * Добавляет массив строк w в список
 * @param w 
 */
void addWordArray(String[] w){
for(String s:w)
               addWord(s);
}
}

/**
 * Элемент списка слов
 * @author Администратор
 */
class WordListItem {
int idw; // идентификатор слова
public ArrayList<Integer> p=new ArrayList<>(); // массив позиций слова

/**
 * Возвращает упакованный байтовый массив массива позиций p
 * @return 
 */
byte[] getBytes() {
int[] pp = new int[p.size()];
for(int i=0; i<pp.length; i++)
                              pp[i]=p.get(i);
return PCompressor.compress(pp);
}
}