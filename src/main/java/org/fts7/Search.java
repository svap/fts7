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

import org.fts7.searchRes.SearchResultItem;
import org.fts7.searchRes.SearchResult;
import org.fts7.searchRes.ObjectItem;
import org.fts7.searchRes.ContentItem;
import java.sql.*;
import java.util.*;
import java.util.zip.*;
import org.sqlite.*;
import java.io.*;

/**
 * This class performs search using the index created by {@link Indexer}.
 * @author Sergey Apolonov
 */
public class Search {

Connection db;
TwSynArray kws;
FiA        fi; 
int        nq;  // число слов в поисковой фразе

/** A default quantity (20) of {@link org.fts7.searchRes.SearchResultItem} objects 
 * in {@link org.fts7.searchRes.SearchResult} returned by {@link #search(java.lang.String, int)} method per one call 
 */ 
public int pagelen=20;

/**
 * Creates a class instance for searching via given index.
 * @param indexfile the index file for searching. This file
 * must be created by {@link Indexer}.
 * @throws SQLException
 * @throws ClassNotFoundException 
 */
public Search (String indexfile) throws SQLException, ClassNotFoundException {

Class.forName("org.sqlite.JDBC");
db = DriverManager.getConnection("jdbc:sqlite:"+indexfile);
kws=new TwSynArray();
fi=new FiA();

C1x fu1=new C1x();
Function.create(db, "C1X", fu1);
}

/**
 * Поиск слова w и лучших nsyn синонимов в таблице WORDS
 * @param w     поисковое слово
 * @param nsyn  число лучших синонимов слова w, помещаемых в массив kws
 */
void searchWords(String word, int nsyn) throws SQLException {
Statement stmt = db.createStatement();
ResultSet rs = stmt.executeQuery( "select IDw,SYN,NN from WORDS where KWord='"+word+"'" );
while ( rs.next() ) {
      TwSyn w=new TwSyn();
      w.idw=rs.getInt(1);
      w.nn=rs.getInt(3);
      w.ids=rs.getInt(2);
      if (rs.wasNull()) // поле SYN пустое, нет синонимов
                        { w.ids=w.idw; nsyn=0;}
      w.r=2; // это вес основного слова
      w.word=word;
      kws.add(w); // добавить слово в список kws
      
      if (nsyn>0) // есть сининимы для word и нужно подобрать nsyn синонимов
          {
          String s=String.format("select IDw,Kword,NN from Words where Syn=%d and IDw<>%d order by NN desc",w.ids,w.idw);
          Statement st = db.createStatement();
          ResultSet rw = st.executeQuery(s);
          for (int k=0; rw.next() && k<nsyn; k++) {
              TwSyn ws=new TwSyn();
              ws.ids=w.ids;
              ws.idw=rw.getInt(1);
              ws.nn=rw.getInt(3);
              ws.r=1; // а это вес для синонима
              ws.word=rw.getString(2);
              kws.add(ws); // добавить синоним в список kws
              }
          }
      }
}

// данные, совместно используемые агрегатными функциями
int idf;    // идентификатор файла
int rcnt=0; // счетчик файлов
int gcnt=0; // счетчик записей в текущей агрегации
ScanResult scanR=new ScanResult(); // результат сканирования

/**
 * Класс реализует агрегатную функцию подсчета весов для каждого файла
 */
class C1x extends Function.Aggregate {

/**
 * Агрегатная шаговая функция, вызывемая для каждого файла на шаге агрегации,
 * принимает 3 пареметра:
 * int  idf - идентификатор файла (0)
 * int  idw - идентификатор слова (1)
 * blob p   - массив позиций слова в файле (2)
 * Функция накапливает данные в списке fi
 */
public void xStep() throws java.sql.SQLException {
idf=value_int(0);
int idw=value_int(1);
byte[] p=value_blob(2);
fi.addFiA(idw, p);
gcnt++;
}

/**
 * Финальная функция агрегации, реализует подсчет веса для файла, хранит результат
 * расчета в объекте r, передает int результат весовой функции
 * @throws java.sql.SQLException 
 */
public void xFinal() throws java.sql.SQLException {
//int idf=value_int(0);
scanR=fi.scan(kws);
rcnt++; // счетчик файлов 
gcnt=0; // сброс счетчика записей в аггрегации
fi.clear(); // очистка списка позиций
fi.hsr.put(idf, scanR);// запоминаем результаты в hashmap для извлечения при обработке результата
result(scanR.result);  // передаем результат сканирования контекста файла
}

}

/**
 *  Класс реализует агрегатную функцию, которая передает объект ScanResult 
 *  (результат  сканирования контента файла), формируемый классом C1x в виде
 *  массива байт в среду SQLite
 */
class C2x extends Function.Aggregate {

// на этапе Step ничего не делает
public void xStep() throws SQLException {
}

/**
 * Вотвращает в среду SQLite сериализованный объект SearchResult в виде массива байт
 * для дальнейшего извлечения объекта по запросу select
 * @throws java.sql.SQLException
 * @throws IOException 
 */
public void xFinal() throws SQLException {
try {   
    result(scanR.toByteArray());
}
catch (IOException e) {
    System.err.println("C2x xFinal IOException: " + e.getMessage());
}
}


}


/**
 * Performs a full-text search in the current index.
 * The returning result is grouped by pages, each page includes {@link #pagelen} objects. 
 * Method returns a result for a single given page. The page numbering begins with 1.
 * 
 * @param s search phrase
 * @param npage required page number of the search result.
 * 
 * @return {@link org.fts7.searchRes.SearchResult} object which consists of {@link #pagelen}
 * {@link org.fts7.searchRes.SearchResultItem} list for page № {@code npage}.
 */
public SearchResult search (String s, int npage) throws SQLException,IOException,DataFormatException {
    
SearchResult r=new SearchResult(); // это результат поиска
kws=new TwSynArray();
fi =new FiA();
rcnt=0; // счетчик файлов
long t1 = System.currentTimeMillis();
long t0 = t1;
StringTokenizer stk = new StringTokenizer(s.toUpperCase()," "); // разбор поисковой фразы
for (nq=0; stk.hasMoreTokens(); nq++) 
                                      searchWords(stk.nextToken(),4);

r.queryWordsTime=(int)(System.currentTimeMillis()-t1);

r.words=kws.wordsArray();
kws.sort();

//--C1x fu1=new C1x();
//C2x fu2=new C2x();

//--Function.create(db, "C1X", fu1);
//Function.create(db, "C2X", fu2);

String sidw="";
for(int i=0; i<kws.alist.size(); i++ ){
             sidw += (i==0 ? "" : ",") +String.valueOf(kws.alist.get(i).idw);
             }

String sl=String.format("limit %d",pagelen);
int nn=(npage-1)*pagelen;
if (npage>1) sl+=String.format(" offset %d",nn);

//String sqs=String.format("select IDf,C1X(IDf,IDw,P) as XX, C2X() as RR from DOCS where IDw in (%s) and IDF=9759 group by IDf order by XX desc %s",sidw,sl);
//String sqs=String.format("select IDf,C1X(IDf,IDw,P) as XX, C2X() as RR from DOCS where IDw in (%s) group by IDf order by XX desc %s",sidw,sl);
String sqs=String.format("select IDf,C1X(IDf,IDw,P) as XX from DOCS where IDw in (%s) group by IDf order by XX desc %s",sidw,sl);
Statement stmt = db.createStatement();
t1 = System.currentTimeMillis(); 
ResultSet rs = stmt.executeQuery(sqs);
r.queryTime=(int)(System.currentTimeMillis()-t1);
r.scnt=fi.scnt; // суммарное число циклов сканирования функцией FiA.scan
r.cnt=rcnt; // счетчик нйденных записей


// извлечение результата запроса
t1 = System.currentTimeMillis();
PreparedStatement pfl = db.prepareStatement("select IDf,Fname,BODY,Len,DT from Files where IDf=? union all select IDf,Fname,Null as BODY,Len,DT from Files_S where IDf=?");
while (rs.next()){
            SearchResultItem a=new SearchResultItem();
       a.n=++nn;
       a.idf=rs.getInt(1); // IDf
       //a.scanR=new ScanResult(rs.getBytes(3)); // RR
       a.scanR=fi.hsr.get(a.idf); // из хэша достанем результаты сканирования для файла
       
       // извлечение файлов 
       pfl.setInt(1, a.idf);
       pfl.setInt(2, a.idf);
       ResultSet rsf=pfl.executeQuery();
       while (rsf.next()){ // цикл извлечения файлов
             ObjectItem ff=new ObjectItem();
             ff.name=rsf.getString(2); //Fname
             ff.len=rsf.getInt(4);
             ff.d=rsf.getDate(5);
             
             byte[] zbody=rsf.getBytes(3); // BODY Zlib compressed!
             if(!rsf.wasNull()) { // извлекли непустое тело
                Inflater decomp = new Inflater();
                decomp.setInput(zbody);
                byte[] ubody=new byte[ff.len]; // буфер для декомпрессии заведомо большой по длине файла
                int ulen=decomp.inflate(ubody); // декомпрессия в буфер, в ulen длина полученной строки
                //byte[] ub=Arrays.copyOf(ubody,ulen);
                String sb=new String(Arrays.copyOf(ubody,ulen),"UTF-8");
                
                // передача контента в пункт результата
                String[] cc=sb.split(" ");
                int j=1; // позиция в контенте
                for (String w:cc){
                    ContentItem ci=new ContentItem();
                    ci.word=w;
                    if(j>=a.scanR.p1 && j<=a.scanR.p1+a.scanR.dw) {
                        TwSyn ws=kws.synFindWord(w.toUpperCase());
                        if (ws!=null)
                                   ci.flag = ws.r==2 ? ContentItem.SEARCH_WORD : ContentItem.SEARCH_SYN;
                        }
                    a.content.add(ci);
                    j++;
                    }
                a.p1=a.scanR.p1-1;
                a.dw=a.scanR.dw;
                }
             a.objects.add(ff);
             }
       r.add(a);
      }
r.queryResultExtractTime=(int)(System.currentTimeMillis()-t1);
r.totalSearchTime=(int)(System.currentTimeMillis()-t0);
return r;
}

}
