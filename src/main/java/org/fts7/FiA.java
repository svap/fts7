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
import java.io.*;

/**
 *
 * @author Sergey Apollonov
 */
class FiA extends ArrayList <Fi>
{

    HashMap <Integer,ScanResult> hsr = new HashMap <>(20000);


    /**
     * Формирование списка позиций слова
     * @param idw идентификатор слова
     * @param p упакованный массив позиций слова
     */
    void addFiA(int idw, byte[] p)
    {
        int [] pp=PCompressor.decompress(p); // распаковка
        for(int pn:pp)
        {
            Fi a=new Fi();
            a.idw=idw;
            a.p=pn;
            add(a);
        }
    }


    class FiComparator implements Comparator <Fi>
    {
        @Override
        public int compare(Fi r1, Fi r2)
        {
            return r1.p-r2.p;
        }
    }
    /**
     * сортировка элементов списка по возрастнию позиции (.p )
     */
    void sort()
    {
        Collections.sort(this, new FiComparator());
    }

    static short AWW[]=new short[] {1257,2422,3440,4296,5000,5577,6051};
    static double CMINT=10000;
    public int scnt; // число циклов сканирования

    ScanResult scan(TwSynArray kws)
    {
        ScanResult p=new ScanResult();
        Iset synSet=new Iset(); // set уникальных синонимов, составляющих запрос
        int hh=0;   // сумма весов всех слов
        int idw=-1;
        int rmax=1; // максимальный вес слова
        TwSyn syn=new TwSyn();

        for (Fi a:this)
        {
            if (idw!=a.idw)  // список отсортирован по idw, поэтому так оптимальнее
            {
                idw=a.idw; // чтобы не повтоять на след. шаге
                syn=kws.synFindIdw(a.idw);
                synSet.add(syn.ids); // накапливаем set синонимов запроса без повторений
            }
            a.ids=syn.ids;
            a.r=syn.r;
            hh+=syn.r; // суммарный вес слов
            if (a.r>rmax) rmax=a.r; // определяем максимальный вес слова
        }

        p.nn=(byte)synSet.count(); // число синонимов запроса без повторений

        sort(); // сортируем список по возрастанию позиций (.p)

        // подсчет фактора количества найденных слов (p.rww)
        int lsize=size(); // размер списка
        if (lsize<AWW.length) p.rww=AWW[lsize];
        else p.rww=(short)Math.round(CMINT*Math.atan((double)lsize/5)/(0.5*Math.PI));

        int pmin,pmax,h,n,dw1,w1,wh,wp,w,x,k;
        Fi a,b;
        double d;
        //======================================
        for (int i=x=0; i<lsize; i++)
        {
            // поиск всех возможных цепочек
            a=get(i); // i-ый элемент
            pmin=a.p;  // начальная позиция цепочки
            pmax=pmin; // позиция конца цепочки (сначала цепочка пустая)
            h=0; // сумарный вес всех слов
            synSet.clear(); // очистка списка уникальных синонимов

            for (int j=i; j<lsize; j++)
            {
                b=get(j);
                if (synSet.add(b.ids)<0) // обнаружен повтор синонима, это конец цепочки
                    break;
                h+=b.r;   // накапливаем суммарный вес слов в цепочке
                pmax=b.p; // позиция конца цепочки
            }

            k=synSet.count();
            dw1=pmax-pmin; //интервал позиций цепочки

            // фактор плотности цепочки
            w1=0; // для однословной фразы плотность не учитываем
            if (dw1>0)   // многословная цепочка
            {
                d=dw1/(k-1); // среднее расстояние в цепочке
                w1=(int)(Math.round(CMINT*Math.exp(-d*d/100)));
            }

            // фактор суммарного веса слов в цепочке
            wh=(int)((CMINT*h)/(2*k));

            // фактор начальной позиции цепочки
            wp=0;
            if (pmin<800)
            {
                d=pmin-1;
                wp=(int)Math.round(CMINT*Math.exp(-d*d/100000));
            }

            scnt++;
            //==========================================================
            w=(2*w1 + wp + wh + p.rww)*k*k;  // весовая функция
            //==========================================================

            if (w>=x)   // найдена цепочка слов с наилучшим весом, запомнить ее параметры
            {
                x=w;
                p.result=w;      // весовая функция
                p.p1=pmin;       // начальная позиция лучшей цепочки
                p.dw=dw1;        // интервал позиций лучшей цепочки
                p.k=(byte)k;     // число синонимов
                p.h=(byte)h;     // суммарный вес слов
                p.rw1=(short)w1; // фактор плотности цепочки
                p.rwp=(short)wp; // фактор начальной позиции
                p.rwh=(short)wh; // фактор суммарного веса слов
            }

            if (p.nn==1 && a.r==rmax) // если файл содержит только 1 поисковое слово и это слово
                // найдено с максимальным весом в файле, то далее сканировать
                // смысла нет, так как весовая функция расти не будет
                break;

        } // for i..

        return p;
    }
}

