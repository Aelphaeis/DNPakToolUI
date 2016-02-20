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

package co.phoenixlab.dn.dnptui.viewers.struct.msh;

import co.phoenixlab.dn.dnptui.viewers.util.BufferUtils;
import co.phoenixlab.dn.dnptui.viewers.util.DNStringUtils;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class MshHeader {

    public static int MSH_HEADER_SIZE = 1024;

    private String name;
    private int version;
    private int numMesh;
    private int unknownA;
    private int unknownB;
    private float[] boundingBox;
    private int numBones;
    private int unknownC;
    private int numOther;

    public MshHeader(ByteBuffer byteBuffer) {
        int startPos = byteBuffer.position();
        name = DNStringUtils.readFixedLengthNTString(byteBuffer, 256);
        version = byteBuffer.getInt();
        numMesh = byteBuffer.getInt();
        unknownA = byteBuffer.getInt();
        unknownB = byteBuffer.getInt();
        boundingBox = new float[6];
        byteBuffer.asFloatBuffer().get(boundingBox);
        BufferUtils.skip(byteBuffer, boundingBox.length * Float.BYTES);
        numBones = byteBuffer.getInt();
        unknownC = byteBuffer.getInt();
        numOther = byteBuffer.getInt();
        byteBuffer.position(startPos + MSH_HEADER_SIZE);
    }

    public String getName() {
        return name;
    }

    public int getVersion() {
        return version;
    }

    public int getNumMesh() {
        return numMesh;
    }

    public int getUnknownA() {
        return unknownA;
    }

    public int getUnknownB() {
        return unknownB;
    }

    public float[] getBoundingBox() {
        return boundingBox;
    }

    public int getNumBones() {
        return numBones;
    }

    public int getUnknownC() {
        return unknownC;
    }

    public int getNumOther() {
        return numOther;
    }

    @Override
    public String toString() {
        return "{" +
                "name='" + name + '\'' +
                ", version=" + version +
                ", numMesh=" + numMesh +
                ", unknownA=" + unknownA +
                ", unknownB=" + unknownB +
                ", numBones=" + numBones +
                ", unknownC=" + unknownC +
                ", numOther=" + numOther +
                ", boundingBox=" + Arrays.toString(boundingBox) +
                '}';
    }
}
