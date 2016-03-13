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

package co.phoenixlab.dn.dnptui.viewers.struct.skn;

import co.phoenixlab.dn.dnptui.viewers.util.DNStringUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.StringJoiner;

public class Skn {

    private String header;
    private String mshFileName;
    private int unknownA;
    private int numEntries;
    private byte[] unknownB;
    private SknEntry[] entries;

    public Skn() {
    }

    public Skn(ByteBuffer byteBuffer) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        header = DNStringUtils.readFixedLengthNTString(byteBuffer, 256);
        mshFileName = DNStringUtils.readFixedLengthNTString(byteBuffer, 256);
        unknownA = byteBuffer.getInt();
        numEntries = byteBuffer.getInt();
        unknownB = new byte[512 - 8];
        byteBuffer.get(unknownB);
        entries = new SknEntry[numEntries];
        for (int i = 0; i < numEntries; i++) {
            entries[i] = new SknEntry(byteBuffer);
        }
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getMshFileName() {
        return mshFileName;
    }

    public void setMshFileName(String mshFileName) {
        this.mshFileName = mshFileName;
    }

    public int getUnknownA() {
        return unknownA;
    }

    public void setUnknownA(int unknownA) {
        this.unknownA = unknownA;
    }

    public int getNumEntries() {
        return numEntries;
    }

    public void setNumEntries(int numEntries) {
        this.numEntries = numEntries;
    }

    public SknEntry[] getEntries() {
        return entries;
    }

    public void setEntries(SknEntry[] entries) {
        this.entries = entries;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(",\n\t");
        for (SknEntry entry : entries) {
            joiner.add(entry.toString());
        }
        return "Skn{" +
                "header='" + header + '\'' +
                ",\nmshFileName='" + mshFileName + '\'' +
                ",\nunknownA=" + unknownA +
                ",\nentries=[\n" + joiner.toString() +
                "\n]}";
    }
}
