Fts7 ia a full-text search engine library written in Java. Builds a search index on Java objects, having a text content,
and performs a quick search via this index. 

Main features:
- you can index any Java objects having a text content. That may be files, url's or any other objects;
- builds a full text search index as a single file of SQLite database;
- supports incremental index updating. You don't need to rebuild search index from scratch everytime when you update
  some objects or need to add new objects to an index;
- prohibits of indexing objects with the same text content;
- supports a stopwords list (a list of words to be ignored from being indexed);
- quick search in less than a second;
- supports fuzzy search. Can find content that match the given words and some variation around them.
- Search results are ordered by relevance to your query.To calculate the relevance each object's text content is estimated by  frequency of search phrase,
  phrase position inside content, importance of search words, object size, age, etc.



Packages:
org.fts7 - the main package contains Java classes for indexing and full-text searching; 
org.fts7.searchRes - classes represents the result of searching; 

org.fts7.util - a package with java applications as examples of using Fts7 library. 
This package contains: 

           SimpleIndex - a simplest example of indexing.
                         This console application scans C: drive and builds search index
                         in C:\Indexes directory for all .doc and .html files 

           IndexMaker  - the console utility to build a search index from an index profile. 

           Searcher    - the console full-text search utility. 

           GSearcher   - the GUI full-text search utility, based on Swing Java GUI classes. 


API Documentation is in the javadoc subfolder


Site: http://fts7.org/

Contacts: Sergey Apollonov mailto:svap@mari-el.ru
