/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Vincent Zhang/PhoenixLAB
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package co.phoenixlab.dn.dnptui.viewers.stageini.struct;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.StringJoiner;

public class PropInfo {

    private int unknownA;
    private int numEntries;
    private Prop[] entries;

    public PropInfo(ByteBuffer byteBuffer) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        unknownA = byteBuffer.getInt();
        numEntries = byteBuffer.getInt();
        entries = new Prop[numEntries];
        for (int i = 0; i < numEntries; i++) {
            entries[i] = new Prop(byteBuffer);
        }
        Arrays.sort(entries);
    }

    public int getUnknownA() {
        return unknownA;
    }

    public int getNumEntries() {
        return numEntries;
    }

    public Prop[] getEntries() {
        return entries;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner("\n");
        for (int i = 0, entriesLength = entries.length; i < entriesLength; i++) {
            Prop entry = entries[i];
            joiner.add(String.format("%03d: ", entry.getObjectId()) + entry.toString());
        }
        return "Prop{" +
                "unknownA=" + unknownA +
                ", numEntries=" + numEntries +
                ", entries=\n" + joiner.toString() +
                '}';
    }
}
