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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

/**
 * A result returning by method scan
 * @author Администратор
 */
public class ScanResult implements Serializable {
    /** total search object weight */
    public int result; // итоговый вес запроса
    
    public byte nn;    // число уникальных синонимов запроса без повторений
    public short rww;  // фактор количества найденных слов
    public int p1;   // позиция начала лучшей цепочки
    public int dw;   // интервал позиций лучшей цепочки
    public byte k;     // число слов (синонимов) в лучшей цепочке
    public byte h;     // суммарный вес всех слов в цепочке
    public short rw1;  // фактор плотности цепочки
    public short rwp;  // фактор начальной позиции цепочки
    public short rwh;  // фактор суммарного веса слов

    /**
     * @return a byte array of the serialized object
     * @throws IOException
     */
    byte[] toByteArray() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeInt(result);
        dos.writeByte(nn);
        dos.writeShort(rww);
        dos.writeShort(p1);
        dos.writeShort(dw);
        dos.writeByte(k);
        dos.writeByte(h);
        dos.writeShort(rw1);
        dos.writeShort(rwp);
        dos.writeShort(rwh);
        // чтобы избежать рефлексии ...
        //ObjectOutputStream oos = new ObjectOutputStream(bos);
        //oos.writeObject(this);
        return bos.toByteArray();
    }

    // конструктор по умлчанию
    public ScanResult() {
    }

    /**
     * 
     * @param b a byte array of the serialized object
     */
    public ScanResult(byte[] b) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bis);
        result = dis.readInt();
        nn = dis.readByte();
        rww = dis.readShort();
        p1 = dis.readShort();
        dw = dis.readShort();
        k = dis.readByte();
        h = dis.readByte();
        rw1 = dis.readShort();
        rwp = dis.readShort();
        rwh = dis.readShort();
    }
    /**
     * Десериализация объекта из байтового массива
     * @param b байтовый массив сериализованного объекта
     * @return
     */
    /*
    public static ScanResult fromByteArray(byte[] b) throws IOException {
    ByteArrayInputStream bis = new ByteArrayInputStream(b);
    ObjectInputStream ois = new ObjectInputStream(bis);
    return (ScanResult)ois.readObject();
    }*/
}
