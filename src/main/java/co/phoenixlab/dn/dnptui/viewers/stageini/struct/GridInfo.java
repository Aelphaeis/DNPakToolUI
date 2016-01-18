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
import java.nio.charset.StandardCharsets;

public class GridInfo {

    /*
    Basic Structure
    CHAR[64]        Parent world name (?)
    INT32           UnknownA (seems to always be 1)
    INT32           UnknownB (seems to always be 1)
    INT32           StageLength (varies, usually multiple of 50)
    INT32           StageWidth (usually same value as UnknownC)
    INT32           UnknownE (seems to always be 50)
     */

    private String parent;
    private int unknownA;
    private int unknownB;
    private int gridLength;
    private int gridWidth;
    private int unknownE;

    public GridInfo() {
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public int getUnknownA() {
        return unknownA;
    }

    public void setUnknownA(int unknownA) {
        this.unknownA = unknownA;
    }

    public int getUnknownB() {
        return unknownB;
    }

    public void setUnknownB(int unknownB) {
        this.unknownB = unknownB;
    }

    public int getGridLength() {
        return gridLength;
    }

    public void setGridLength(int gridLength) {
        this.gridLength = gridLength;
    }

    public int getGridWidth() {
        return gridWidth;
    }

    public void setGridWidth(int gridWidth) {
        this.gridWidth = gridWidth;
    }

    public int getUnknownE() {
        return unknownE;
    }

    public void setUnknownE(int unknownE) {
        this.unknownE = unknownE;
    }

    public void read(ByteBuffer byteBuffer) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byte[] parentWorld = new byte[64];
        byteBuffer.get(parentWorld);
        parent = new String(parentWorld, StandardCharsets.UTF_8).trim();
        unknownA = byteBuffer.getInt();
        unknownB = byteBuffer.getInt();
        gridLength = byteBuffer.getInt();
        gridWidth = byteBuffer.getInt();
        unknownE = byteBuffer.getInt();
    }

    @Override
    public String toString() {
        return String.format("ParentWorld: \"%s\"\n" +
                        "unknownA: %d\n" +
                        "unknownB: %d\n" +
                        "StageLength: %d\n" +
                        "StageWidth: %d\n" +
                        "unknownE: %d",
                parent, unknownA, unknownB, gridLength, gridWidth, unknownE);
    }
}
