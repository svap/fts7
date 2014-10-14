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

import java.sql.*;
import java.util.*;
import java.util.zip.*;
import java.util.regex.*;
import org.sqlite.*;
import java.io.*;
import java.lang.*;
import org.xml.sax.SAXException;

/**
 * This class creates a full text search index.
 * The search index is created in a SQLite database file as a 
 * single file with .db extension.
 * 
 * @see <a href="http://sqlite.org">SQLite</a>
 * @author Sergey Apollonov
 */
public class Indexer {
Connection db;
String indexFile;
PreparedStatement insertedID; 
int docsSize=500000; // предельный размер таблицы docs
/** This field manages an output to console. If == 0 then the output is disabled.
 * The default value is 1 (the console output is enabled).*/
public int output=1;

ArrayList<DocsRecord> docsR=new ArrayList<>(docsSize);


/**
 * Opens an existing index file or creates a new empty index if the indexfile is not found.
 * 
 * @param indexfile a full name of the index file.
 * @param createNew if true then always creates a new empty index file.
 * If the index file exists then deletes the existing index and creates an empty new one.
 * <br>If false then tries to open an existing index file and if the file is not found
 * then creates a new empty index.

 * 
 * @throws SQLException
 * @throws ClassNotFoundException 
 */
public Indexer(String indexfile, boolean createNew) throws SQLException, ClassNotFoundException {
indexFile=indexfile;    
File f = new File(indexfile);
if (f.exists()&&!f.isFile()) // объект indexfile найден, но это не файл
                             throw new RuntimeException(String.format("Se7Indexer %s is not a file",indexfile));

if (!f.exists()) createNew=true;
if (createNew && f.exists()) // создается новый индекс
                             f.delete();

Class.forName("org.sqlite.JDBC");
db = DriverManager.getConnection("jdbc:sqlite:"+indexfile);
Statement st = db.createStatement();
st.executeUpdate("PRAGMA synchronous = OFF");
st.executeUpdate("PRAGMA cache_size = 8000");
st.executeUpdate("PRAGMA journal_mode = PERSISTENT");
db.setAutoCommit(false);

if (createNew) // был создан пустой индекс, необходимо его проинициировать
               createIndex();

insertedID=db.prepareStatement("select last_insert_rowid()");
}

/**
 * get an ID of the last inserted record
 * @return an ID of the last inserted record
 */
int getInsertedId() throws SQLException {
ResultSet rs=insertedID.executeQuery();
rs.next();
int id=rs.getInt(1);
rs.close();
return id;
}

/**
 * Возвращает максимальное значение IDw в таблице Words или 0 если таблица пуста
 * @return 
 */

int wIDw=-1;
int getWordIDw() throws SQLException{
if (wIDw==-1) {
               Statement st=db.createStatement();
               ResultSet rs=st.executeQuery("select max(IDw) from WORDS");
               rs.next();
               wIDw=rs.getInt(1);
               rs.close();
               st.close();
              }
return ++wIDw;
}

/**
 * get a number of records in tge given table
 * @param tbl table name
 * @return a number or records in the table tbl
 * @throws SQLException 
 */
int getCount(String tbl) throws SQLException {
Statement st=db.createStatement();
ResultSet rs=st.executeQuery("select count(*) from "+tbl);
rs.next();
int cnt=rs.getInt(1);
rs.close();
st.close();
return cnt;
}

/**
 *  initiate a new empty index database by creating database objects
 */
void createIndex() throws SQLException {
Statement st = db.createStatement();
st.executeUpdate("PRAGMA page_size=4096");
st.executeUpdate("DROP table if exists FILES");
st.executeUpdate("DROP table if exists FILES_S");
st.executeUpdate("DROP table if exists WORDS");
st.executeUpdate("DROP table if exists DOCS");
st.executeUpdate("DROP table if exists SCB");

st.executeUpdate("CREATE TABLE SCB (PAR text, VAL text)");
st.executeUpdate("CREATE UNIQUE INDEX SCB_PAR on SCB(PAR)");

// файлы
st.executeUpdate("CREATE TABLE FILES (IDf INTEGER PRIMARY KEY AUTOINCREMENT, FName text, Len integer, DT DateTime, NW integer, NKW integer, MD5 blob, BODY blob)");
st.executeUpdate("CREATE INDEX FILES_FName on FILES (FName)");
st.executeUpdate("CREATE INDEX FILES_H on FILES (MD5)");

// дубликаты
st.executeUpdate("CREATE TABLE FILES_S (IDf integer, FName text, Len integer, DT DateTime)");
st.executeUpdate("CREATE UNIQUE INDEX FILES_S_I on FILES_S (IDf,FName)");
//st.executeUpdate("CREATE INDEX FILES_S_IDf on FILES_S (IDf)");
//st.executeUpdate("CREATE INDEX FILES_S_Fname on FILES_S (FName)");

// слова
st.executeUpdate("CREATE TABLE WORDS (IDw INTEGER PRIMARY KEY, NN integer, SYN integer, KWord text)");
st.executeUpdate("CREATE INDEX WORDS_K on WORDS (KWord)");

// контент, индексы создадим потом
st.executeUpdate("CREATE TABLE DOCS (IDf integer, IDw integer, P blob)");

st.executeUpdate("CREATE VIEW if not exists V as select a.FName, b.Kword, c.* from DOCS c, Words b, Files a where c.IDf=a.IDf and c.IDw=b.IDw");
st.executeUpdate("CREATE VIEW if not exists V2 as select a.IDf, a.IDw, a.p, b.FName from DOCS a, Files b where a.IDf=b.IDf");
st.close();
db.commit();
}

/**
 * find object c in index, if object was found it inserted in FILES_S table and method return TRUE.
 * Else return FALSE
 * @param c an object to find
 * @return true if object was found and object always indexed, else returns false 
 */
//PreparedStatement PS_sel_files   = db.prepareStatement("select IDf,FName from FILES where MD5=?");
//PreparedStatement PS_sel_files_ss=db.prepareStatement("select IDf from FILES_S where FName=?");
//PreparedStatement PS_ins_FILES_S = db.prepareStatement("insert into FILES_S (IDf,FName,Len,DT) values (?,?,?,?)");
                 
boolean findObject(ObjectContent c) throws SQLException {
PreparedStatement pfl = db.prepareStatement("select IDf,FName from FILES where MD5=?");
pfl.setBytes(1, c.getContentHash());
ResultSet rs=pfl.executeQuery();

if (rs.next()){ // объект с таким хэшем уже имеется в индексе
if(rs.getString(2).equals(c.getName())) // объект с таким хэшем и именем уже имеется в FILES
                                        { pfl.close(); rs.close(); return true; }

// объект с таким хэшем есть в FILES, однако имя его не такое как в FILES -
// пытаемся вставить объект в таблицу дубликатов FILES_S
//PreparedStatement sts=db.prepareStatement("select IDf from FILES_S where FName=?");
//sts.setString(1, c.getName());
//ResultSet rss=sts.executeQuery();
//if (!rss.next()){ // нет такого объекта в FILES_S, добавить 
    PreparedStatement si = db.prepareStatement("insert or ignore into FILES_S (IDf,FName,Len,DT) values (?,?,?,?)");
    si.setInt(1, rs.getInt(1)); // IDf
    si.setString(2, c.getName()); // FName
    si.setInt(3, c.getLength());
    si.setDate(4, new java.sql.Date(c.getDate().getTime()));
    si.executeUpdate();
    si.close();
//                }
pfl.close(); rs.close(); 
return true; // объект уже имеется в индексе   
}
pfl.close(); rs.close();
return false; // объекта нет в индексе
}

/**
 * Compresses a text content of the ObjectContent instance
 * @param c an ObjectContent object
 * @return a compression text content of the ObjectContent object
 */
byte[] makeObjectBody (ObjectContent c) {
ByteArrayOutputStream bos = new ByteArrayOutputStream();
Deflater d = new Deflater();
DeflaterOutputStream dout = new DeflaterOutputStream(bos, d);
try{
for(int i=0; i<c.getContent().length; i++){
    String s=(i==0?"":" ")+c.getContent()[i];
    dout.write(s.getBytes("UTF-8"));
    }
dout.close();
}
catch (IOException e) {throw new RuntimeException("makeObjectBody RuntimeException "+e.getMessage());}
return bos.toByteArray();
}

int wordsSearchTime,wordsInsertTime,fcnt;
/** A count of objects processed by {@link #addObject(ObjectContent)} method. */
public int addCount;
/**
 * Adds an {@link ObjectContent} object to an index. Increments an {@link #addCount} field.
 * This method uses {@link StopList} global object to filter stop words that are discarded
 * from indexing.
 * @param c indexing object
 * @throws SQLException
 * @see StopList
 * @see ObjectContent
 */
public void addObject(ObjectContent c) throws SQLException {
addCount++;    
if (findObject(c))// объект уже проиндексирован
                  return;

try{
// формирование списка уникальных слов контента
WordList wl=new WordList(c.getLength()/2); // создание списка с начальной длиной в половину контента
wl.addWordArray(c.getContent()); // добавить массив строк контента

// формирование записи объекта 
PreparedStatement si = db.prepareStatement("insert into FILES (FName,Len,DT,NW,NKW,MD5,BODY) values (?,?,?,?,?,?,?)");
si.setString(1, c.getName());
si.setInt(2,c.getLength()); // Len
si.setDate(3, new java.sql.Date(c.getDate().getTime()));
si.setInt(4, c.getContent().length); // NW - число слов в контенте
si.setInt(5, wl.size()); // число уникальных поисковых слов в контенте после обработки и фильтрации по стоп-листам
si.setBytes(6, c.getContentHash()); // MD5
si.setBytes(7, makeObjectBody(c));  // сжатое тело 
si.executeUpdate();
si.close();

// получение IDf вставленной записи
int idf=getInsertedId();

// сканирование массива wl , поиск каждого слова в WORDS, если слово найдено,
// то извлекается его IDw, иначе добавляется новая запись в WORDS
long t1=System.currentTimeMillis();
PreparedStatement sw = db.prepareStatement("select IDw from WORDS where KWord=?");
for (String ww: wl.keySet()){
    WordListItem w=wl.get(ww);// элемент списка
    sw.setString(1,ww);
    ResultSet rsw=sw.executeQuery();
    if (rsw.next()) // слово w найдено - извлекаем его IDw
                    w.idw=rsw.getInt(1);
    rsw.close();
    }
sw.close();
wordsSearchTime+=System.currentTimeMillis()-t1;
t1=System.currentTimeMillis();

// повторное сканирование wl, выбор слов с нулевым idw (которых нет в WORDS) и
// вставка их в таблицу
PreparedStatement wi = db.prepareStatement("insert into WORDS (IDw,KWord) values (?,?)");
for (String ww: wl.keySet()){
    WordListItem w=wl.get(ww);// элемент списка
    if (w.idw==0) { // слово не было найдено в WORDS на предыдущем цикле, необходимо его добавить
         w.idw=getWordIDw();
         wi.setInt(1, w.idw);
         wi.setString(2,ww);
         wi.addBatch();
        }
    // формирование списка docsR - это записи, гоовые для вставки в таблицу DOCS
    // оптимальнее сначала накопить их в виде списка и по заполнению некоторого количества
    // отсортировать по IDw и вставить методом flushDocs
    int ir[]=wi.executeBatch();
    DocsRecord r=new DocsRecord();
    r.idf=idf;
    r.idw=w.idw;
    r.p=w.getBytes();
    docsR.add(r);
    }
wordsInsertTime+=System.currentTimeMillis()-t1;
wi.close();
wl.clear();
if (++fcnt%100==0)
                   { fcnt=0; db.commit();}

if (docsR.size()>docsSize) // буфер docsR заполнен, сбросить в таблицу DOCS
                           flushDocs();
}
catch (OutOfMemoryError e)
                        {System.out.println("Out of memory while processin "+c.getName());}
}

// для сортировки docsR по idw
class DocsComparator implements Comparator <DocsRecord> {
    @Override
    public int compare(DocsRecord r1, DocsRecord r2) {
        return r1.idw-r2.idw;
    }
}
/**
 * сбрасывает накопленный массив docsR в таблицу DOCS
 */
int flushInsertTime;
void flushDocs() throws SQLException {
if (docsR.size()==0) // список пуст
                     return;
long t1 = System.currentTimeMillis();    
Collections.sort(docsR, new DocsComparator());
t1=System.currentTimeMillis();
PreparedStatement wi = db.prepareStatement("insert into DOCS (IDf,IDw,P) values (?,?,?)");
for (DocsRecord a:docsR){
    wi.setInt(1, a.idf);
    wi.setInt(2, a.idw);
    wi.setBytes(3, a.p);
    wi.addBatch();
    }
db.commit();
wi.executeBatch();
wi.close();
docsR.clear();
db.commit();
flushInsertTime+=System.currentTimeMillis()-t1;
}


int pathScanedCount;
int maxScanCount=Integer.MAX_VALUE; // максимальное число сканируемых файлов
int fileContentTime;

/**
 * recursive directory scanning
 * @param d directory
 * @param filter file filter
 * @throws SQLException 
 */
void DirReader(File d, WildCardFileFilter filter) throws SQLException {

if (addCount>maxScanCount) return; //###################################

File[] listf = d.listFiles(filter); // получить список файловых объектов
if (listf==null) return;
if (listf.length==0) return;

// цикл выборки файлов
FileContent fob;
for(File f:listf)
    if (f.isFile()) { // это файл
                     long t=System.currentTimeMillis(); 
                     try{
                         fob=new FileContent(f.getPath());
                         addObject(fob);
                    }
                     catch (Exception|Error e) {System.out.println(e.getMessage());}
                     
                     
                     fileContentTime+=System.currentTimeMillis()-t;
                     pathScanedCount++;
                     if (output!=0) System.out.printf("%d %s\n", addCount,f.getPath());
                    }
// цикл выборки каталогов
for(File f:listf)
    if(f.isDirectory()) // это каталог
                        DirReader(f,filter);
}


int addPathTime;
/**
 * Add files of directory {@code dir} and subdirectories to the search index.
 * <br>This method scans a directory, selects files according to a listFilter patterns,
 * creates {@link org.fts7.FileContent} object for each
 * found file and calls {@link #addObject(ObjectContent)} method to add each
 * FileContent object to an index. 
 * 
 * @param dir a directory to be indexed.
 * @param listFilter an array of included or excluded file patterns with wildcards (*?).
 * If the listFilter is null or empty then all files will be indexed.
 * If a pattern begins with '-' character then it defines exclude rule, otherwise it  is an include pattern.
 * If a file name matches include and exclude pattern simultaneously then the 
 * file will be excluded from indexing.
 * <p>For example:
 * <br>{@code listFilter={"*.doc","*.rtf"}} includes only .doc and .rtf files
 * <br>{@code listFilter={"-*.exe","-*.dll"}} includes all files except .exe,.dll 
 * <br>{@code listFilter={"*.doc","-c:\xxx\bin\*"}} includes all .doc files except
 *     any files in subdirectory c:\xxx\bin\ 

 * @throws SQLException 
 * @see StopList
 * @see #addCount
 * @see #addObject(org.fts7.ObjectContent) 
 * @see org.fts7.ObjectContent
 */
public void addPath(String dir, String[] listFilter) throws SQLException {
pathScanedCount=0; 
int cnt0=getCount("FILES");
long t1 = System.currentTimeMillis();    
File f=new File(dir);
WildCardFileFilter filter=new WildCardFileFilter(listFilter);
if (!f.exists()) throw new RuntimeException(String.format("path %s is not exists",dir));
if (!f.isDirectory()) throw new RuntimeException(String.format("path %s is not a directory",dir));
DirReader(f,filter);
flushDocs();
putPar("Added_files", getParI("Added_files")+getCount("FILES")-cnt0);
addPathTime+=System.currentTimeMillis()-t1;
}

/**
 * file filtering via include or exclude patterns with wildcards (*?).
 */
class WildCardFileFilter implements FileFilter{
String[]  filter;
boolean[] include;
int ni,ne;

public WildCardFileFilter(String[] listFilter){
filter=listFilter;
if (listFilter!=null){
    filter=new String[listFilter.length];
    include=new boolean[listFilter.length];
    for (int i=0; i<listFilter.length; i++){
        if (listFilter[i].matches("^-.+")){ // exclude pattern
                                         include[i]=false; ne++;
                                         filter[i]=listFilter[i].substring(1);
                                        }
        else  // include pattern
             { include[i]=true; ni++; filter[i]=listFilter[i]; }
        
        filter[i]=filter[i].replace(".","\\.").replace("*", ".*").replace("?", ".").toUpperCase();
        }
    }    
}

public boolean accept(File file){
if (filter==null || filter.length==0) 
                                     return true;
int j=0;
for (int i=0; i<filter.length; i++) 
    if (file.getPath().toUpperCase().matches(filter[i])){
         if (include[i]) // match to include pattern
                         j++;
         else // match to exclude pattern
              return false;
         }

if (file.isDirectory())
                        return true;

return ni>0 ? j>0 : true; 
}    
}

void sortDocsS() throws SQLException {
Statement st=db.createStatement();
st.executeUpdate("Drop table if exists t");
st.executeUpdate("create index DOCS_IDW on DOCS (IDw)");
st.executeUpdate("Create table t as select * from docs order by IDw");
st.executeUpdate("Drop table DOCS");
st.executeUpdate("Alter table t rename to DOCS");
st.close();
db.commit();
}

void sortDocsT() throws SQLException {
Statement st=db.createStatement();
ResultSet rs=st.executeQuery("select max(IDw) from WORDS");
rs.next(); int maxidw=rs.getInt(1); rs.close();

rs=st.executeQuery("select count(*) from DOCS");
rs.next(); int cnt=rs.getInt(1); rs.close();

st.executeUpdate("Drop table if exists t");
st.executeUpdate("CREATE TABLE t (IDf integer, IDw integer, P blob)");
st.executeUpdate("PRAGMA temp_store = MEMORY");


int n=1+cnt/1000000;
int d=maxidw/n; // idw step
for(int idw1=1; idw1<=maxidw; idw1+=d){
String s=String.format("insert into t (IDf,IDw,p) select IDf,IDw,p from DOCS where IDW>=%d and IDW<%d order by IDw", idw1,idw1+d);
st.executeUpdate(s);
db.commit();
}
st.executeUpdate("Drop table DOCS");
st.executeUpdate("Alter table t rename to DOCS");
st.executeUpdate("PRAGMA temp_store = DEFAULT");
st.close();
db.commit();
}

void createIndexesDocs() throws SQLException {
Statement st=db.createStatement();
long t=System.currentTimeMillis();
st.executeUpdate("CREATE INDEX if not exists DOCS_IDw on DOCS (IDw)");
putPar("Index DOCS_IDw",String.format("creation %d sec", (int)(System.currentTimeMillis()-t)/1000));
t=System.currentTimeMillis();
st.executeUpdate("CREATE INDEX if not exists DOCS_IDf on DOCS (IDf)");
putPar("Index DOCS_IDf",String.format("creation %d sec", (int)(System.currentTimeMillis()-t)/1000));
db.commit();
st.close();
}

void updateWordsNN() throws SQLException{
Statement st=db.createStatement();
long t=System.currentTimeMillis();
st.executeUpdate("create index if not exists WORDS_IDW on WORDS (IDW)");
st.executeUpdate("update Words set NN=(select count(*) from Docs where Docs.IDw=Words.IDw)");
st.executeUpdate("delete from Words where NN=0");
db.commit();
putPar("updateWordsNN",String.format("%d sec", (int)(System.currentTimeMillis()-t)/1000));
st.close();
}

/**
 *  сжимает базу данных индекса декс после перестроений
 */
void vacuum()throws SQLException {
db.commit();
long t=System.currentTimeMillis();
db.setAutoCommit(true);
Statement st=db.createStatement();
st.executeUpdate("vacuum");
db.setAutoCommit(false);
putPar("vacuum",String.format("%d sec", (int)(System.currentTimeMillis()-t)/1000));
st.close();
}

/**
 * полная сортировка таблицы DOCS
 */
void sortDocs() throws SQLException {
String par="DOCS sorting";
long t=System.currentTimeMillis();
putPar(par,"");

sortDocsT();

java.util.Date date = new java.util.Date();
putPar(par,String.format("sorting time %d sec, last sorted at %s", (System.currentTimeMillis()-t)/1000,
date.toString()));
putPar("Added_files",0);
putPar("Deleted_files",0);
createIndexesDocs();
}

/**
 * Performs a final operations to finalize indexing.
 * This method should be called after calls of {@link #addPath(String, String[])}
 * or {@link #addObject(ObjectContent)} methods.
*/
public void prepareIndex() throws SQLException {
flushDocs();
int changes=getParI("Added_files")+getParI("Deleted_files");
int fcount=getCount("FILES");
if (getParS("DOCS sorting").length()==0 ||   // сортировка DOCS не выполнялась
        (fcount>0 && changes*100/fcount>10)) // изменены более 10% файлов
                                             { sortDocs();
                                               updateWordsNN();
                                               makeSyn();
                                               vacuum();
                                             }
}
    
/**
 * Запись строкового значения val параметра par в таблицу SCB
 * @param par
 * @param val 
 */
void putPar(String par, String val) throws SQLException {
PreparedStatement si=db.prepareStatement("insert or replace into SCB (PAR,VAL) values (?,?)");
si.setString(1, par);
si.setString(2, val);
int i=si.executeUpdate();
si.close();
db.commit();
}

/**
 * Запись целого значения val параметра par в таблицу SCB
 * @param par
 * @param val 
 */
void putPar(String par, int val) throws SQLException {
putPar(par,String.valueOf(val));    
}

/**
 * получение строкового значения параметра par, если параметр пустой, возвращается
 * пустая строка
 * @param par a paramether name
 * @return a String value of the parameter par
 * @throws SQLException 
 */
String getParS(String par) throws SQLException {
Statement st=db.createStatement();
ResultSet rs=st.executeQuery(String.format("select val from SCB where PAR='%s'", par));
String res="";
if (rs.next())
              res=rs.getString(1);
rs.close();
st.close();
return res;
}

int getParI(String par) throws SQLException {
String s=getParS(par);
if (s.length()==0) return 0;
int n;
try {
      n = Integer.parseInt(s);
} catch (NumberFormatException e) { n=0; }
return n;
}

/*
void putdd(ArrayList<DocsRecord> dd) throws SQLException{
PreparedStatement st=db.prepareStatement("insert into DOCS_t (idf,idw,p) values(?,?,?)");
for(DocsRecord r:dd){
  st.setInt(1, r.idf);
  st.setInt(2, r.idw);
  st.setBytes(3, r.p);
  st.addBatch();
  }
st.executeBatch();
db.commit();
}

void test1() throws SQLException{
Statement st=db.createStatement();
st.executeUpdate("DROP table if exists DOCS_t");
st.executeUpdate("create table DOCS_t as select * from DOCS where IDF=1 and IDW=1");

ArrayList<DocsRecord> dd =new ArrayList<>();

ResultSet rs=st.executeQuery("select idf,idw,p from DOCS");

for (int i=0;rs.next(); i++){
if (i>100000) break;    
DocsRecord r=new DocsRecord();
r.idf=rs.getInt(1);
r.idw=rs.getInt(2);
r.p=rs.getBytes(3);
dd.add(r);
}

for (int i=0; i<20; i++){
                          putdd(dd);
                          System.out.printf("n%d putdd\n",i);
                        }           
}
*/

/**
 * формирование поля SYN в таблице Words (поиск синонимов)
 */
void makeSyn() throws SQLException {
long t=System.currentTimeMillis();
WordsL wl=new WordsL();
Statement st=db.createStatement();

// начальная очистка поля SYN
st.executeUpdate("update Words set SYN=null where syn is not null");
st.executeUpdate("drop index if exists WORDS_SYN");
db.commit();

ResultSet rs=st.executeQuery("select IDw,Kword,NN from Words where NN>2 order by Kword");

int cd0=0,cd1=0;
while (rs.next()){
String w=rs.getString(2); // Kword
if (w.length()>2 && !java.lang.Character.isDigit(w.codePointAt(0))){
   // строка длиннее 2 символов и начинается не с цифры                  
   if (w.codePointAt(0)!=cd0 || w.codePointAt(1)!=cd1){ // поменялись первые 2 символа строки
         cd0=w.codePointAt(0);
         cd1=w.codePointAt(1);
         wl.scan();
         }
   WordsRecord r=new WordsRecord();
   r.word=w;
   r.idw=rs.getInt(1); // IDw
   r.nn=rs.getInt(3);  // NN
   wl.add(r);
   }

}
wl.scan();
st.executeUpdate("create index WORDS_SYN on WORDS (SYN)");

rs.close();
st.close();
t=(System.currentTimeMillis()-t)/1000;
putPar("makeSyn",String.format("%d sec", (int)t));
}




/**
 * Запись таблицы DOCS
 * @author Администратор
 */
class DocsRecord{
int idf;  // Object id
int idw;  // Word id
byte[] p; // packed array of positions
}


/**
 * Запись таблицы Words;
 * @author Администратор
 */
class WordsRecord{
int idw;
int ids;
int nn;
String word;
}

class WordsL extends ArrayList <WordsRecord>{

    // для сортировки списка по возрастанию длины
    class WordsComparatorNN implements Comparator <WordsRecord> {
    @Override
    public int compare(WordsRecord r1, WordsRecord r2) {
        //return r2.nn-r1.nn;
        return r2.word.length()-r1.word.length();
    }
}
    // запись пары слов и процента их совпадения
    class Words2 {
    WordsRecord w1,w2;
    int psyn;
    }

    // для сортировки списка Words2 по psyn
    class Words2Comparator implements Comparator <Words2> {
    @Override
    public int compare(Words2 r1, Words2 r2) {
        return r2.psyn-r1.psyn;
        }
    }
    
// сортировка списка по убыванию nn    
void sortNN(){
Collections.sort(this, new WordsComparatorNN());    
}

// возвращет истину, если строки являются синонимами
boolean isSyn(String s1, String s2){
int ls1=s1.length();
int ls2=s2.length();
int maxl=Math.max(ls1, ls2);
int minl=Math.min(ls1, ls2);

if (maxl>0 && (maxl-minl)*100/maxl>33)
                                       return false;

int n=((ls1+ls2)*75/100)/2; // минимальное число совпадающих символов для 75% совпадения строк
if (n>ls1 || n>ls2)
                    return false;
return s1.substring(0, n).equals(s2.substring(0, n));
}

/**
 * возвращает процент совпадения строк s1 и s2
 * @param s1
 * @param s2
 * @return 
 */
int pSyn(String s1, String s2){
int n=Math.min(s1.length(), s2.length());
int j=0;    
for (int i=0; (i<n) && (s1.charAt(i)==s2.charAt(i)); i++)
                                                          j++; // число совпадающих символов
int len=s1.length()+s2.length();
return len>0 ? (100*j*2) / len : 0;
}

// обновление поля SYN таблицы Words
void updateSyn() throws SQLException {
PreparedStatement st=db.prepareStatement("update Words set SYN=? where IDW=?");
int n=0;
for(WordsRecord r:this)
    if (r.ids!=0) {
         st.setInt(1, r.ids);
         st.setInt(2, r.idw);
         st.addBatch();
         n++;
        }
if (n>0) st.executeBatch();
db.commit();
st.close();
}

// формирование таблицы синонимов
void scan() throws SQLException {
sortNN();
for(int i=0; i<size(); i++){
WordsRecord wi=get(i);
if (wi.ids==0)
  for (int j=i+1; j<size(); j++){
    WordsRecord wj=get(j);
    if(wj.ids==0 && isSyn(wi.word,wj.word))
                               { wj.ids=wi.idw; wi.ids=wi.idw; }
    }
}
updateSyn(); // обновление поля SYN таблицы Words
clear();     // очистить список
}

void scan_() throws SQLException {
ArrayList<Words2> w2=new ArrayList<>(10000);
for (int i=0; i<size(); i++){
    WordsRecord wi=get(i);
    for (int j=0; j<size(); j++)
      if (i!=j){
      WordsRecord wj=get(j);
      int ps=pSyn(wi.word,wj.word);
      if (ps>=75){ // это вероятный синоним, совадение более 75% запоминаем пару слов
                   Words2 r=new Words2();
                   r.psyn=ps; r.w1=wi; r.w2=wj;
                   w2.add(r);
                 }
      }
}
Collections.sort(w2, new Words2Comparator()); // сортировка по убыванию psyn
for(Words2 ww:w2)
    if (ww.w1.ids==0 && ww.w2.ids==0) {
                                ww.w1.ids=ww.w1.idw;
                                ww.w2.ids=ww.w1.ids;
                                }
for(Words2 ww:w2){
    if (ww.w1.ids==0 && ww.w2.ids!=0) 
                                     ww.w1.ids=ww.w2.ids;
    if (ww.w2.ids==0 && ww.w1.ids!=0) 
                                     ww.w2.ids=ww.w1.ids;
    
    }
updateSyn();
clear();
w2.clear();
}

}

}