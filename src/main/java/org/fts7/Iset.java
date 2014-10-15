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
 *
 * @author Администратор
 */
class Iset
{
    public int a[]=new int[100];
    int ind=-1;

    /**
     * очистка списка
     */
    void clear()
    {
        ind=-1;
    }

    int count()
    {
        return ind+1;
    }

    /**
     * возвращает индекс элемента val в списке целых или -1, если элемент val не найден
     * @param val искомый элемент
     * @return индекс val или -1, если элемент не найден
     */
    int find(int val)
    {
        for (int i=0; i<=ind; i++)
            if (a[i]==val)
                return i;
        return -1;
    }

    /**
     *
     * @param val добавляет элемент val в список, если его там нет
     * @return индекс добавленного элемента, либо -1, если элемент уже имеется в списке
     */
    int add(int val)
    {
        if(find(val)<0)
        {
            a[++ind]=val;
            return ind;
        }
        else return -1;
    }

}

