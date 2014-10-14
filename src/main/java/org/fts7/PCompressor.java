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

import java.io.*;

/**
 * компрессия-декомпрессия отсортированного массива позиций
 * @author Администратор
 */
class PCompressor {

static byte BYTE_OFFSET=1; // байтовый интервал
static byte BYTE_0=2;      // байтовый 0 элемент
/**
 * компрессия входного массива позиций. Массив должен быть обязательно отсортирован
 * по возрастанию позиций
 * @param p
 * @return 
 */    
static byte[] compress(int[] p) {

byte head=(byte)((p[0]<=255 ? BYTE_0 : 0)|BYTE_OFFSET);
for (int i=1; i<p.length; i++) // определяем смещение между элементами
                if ((p[i]-p[i-1])>255){ // интервал будет short
                                        head -=BYTE_OFFSET; break;
                                      }
ByteArrayOutputStream bos = new ByteArrayOutputStream();
DataOutputStream dos = new DataOutputStream(bos);

try {
dos.write(head); // заголовок
if((head & BYTE_0)!=0) // 0-ой элемент байтовый
                       dos.write(p[0]);
else  // 0-ой элемент short
      dos.writeShort((short)p[0]);

int j;
for (int i=1; i<p.length; i++){
              j=p[i]-p[i-1];
              if (j<0) throw new RuntimeException("Unsorted input integer array in PCompressor.compress");
              if ((head&BYTE_OFFSET)!=0) // байтовое смещение
                                         dos.write(j);
              else // смещение short
                   dos.writeShort(j);
              }
}
catch (IOException e) {System.out.println(e.getMessage());}
return bos.toByteArray();
}

/**
 * декомпрессия массива позиций p
 * @param b
 * @return 
 */
static int[] decompress(byte[] b){
ByteArrayInputStream bis = new ByteArrayInputStream(b);
DataInputStream dis=new DataInputStream(bis);    
int[] p=null; // это выходной массив
try{
int head=dis.read(); // читаем байтовый заголовок
boolean BP0=(head&BYTE_0)!=0; // истина, если нулевой элемент 1-байтный, иначе-short
boolean BOFS=(head&BYTE_OFFSET)!=0; // истина, если остальные элементы массива 1-байтные

int l0=BP0 ? 1 : 2; // длина нулевого элемента
if (BOFS) // байтовый массив смещений
          p=new int[b.length-1-l0+1]; // создаем выходгой массив
    else // short массив смещений
          p=new int[(b.length-1-l0)/2+1];
    
if (BP0) // если 0 элемент 1-байтный, читать байт из потока
         p[0]=dis.read(); 
    else // 0 элемент типа short, читать short со сбросом знакового бита
         { p[0]=dis.readShort(); p[0]&=0xffff;}

int d;
if (BOFS) // цикл для байтового массива
          for(int i=1; i<p.length; i++)
                                       { d=dis.read(); p[i]=p[i-1]+d;}
else      // цикл для short массива
          for(int i=1; i<p.length; i++) //читаем short смещения
                                       { d=dis.readShort(); d&=0xffff; p[i]=p[i-1]+d;}
}
catch (IOException e) {System.out.println(e.getMessage());}
return p;
}
}
