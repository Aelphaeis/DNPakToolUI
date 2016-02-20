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
import java.nio.FloatBuffer;
import java.util.Arrays;

public class MeshData {

    private final boolean hasBones;
    private short[] faceIndex;
    private float[] vertexData;
    private float[] normalData;
    private float[] uvData;
    private short[] boneIndex;
    private float[] boneWeight;
    private int boneCount = -1;
    private String[] boneNames;

    public MeshData(ByteBuffer byteBuffer, Mesh parent, boolean hasBones, int version) {
        try {
            faceIndex = new short[parent.getNumIndex()];
            byteBuffer.asShortBuffer().get(faceIndex);
            BufferUtils.skip(byteBuffer, faceIndex.length * Short.BYTES);

            FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();

            int numFloatsForVerts = parent.getNumVertex() * 3;

            vertexData = new float[numFloatsForVerts];
            floatBuffer.get(vertexData);
            BufferUtils.skip(byteBuffer, vertexData.length * Float.BYTES);

            normalData = new float[numFloatsForVerts];
            floatBuffer.get(normalData);
            BufferUtils.skip(byteBuffer, normalData.length * Float.BYTES);

            uvData = new float[parent.getNumVertex() * 2];
            floatBuffer.get(uvData);
            BufferUtils.skip(byteBuffer, uvData.length * Float.BYTES);

            this.hasBones = hasBones;
            if (this.hasBones) {
                boneIndex = new short[parent.getNumVertex() * 4];
                byteBuffer.asShortBuffer().get(boneIndex);
                BufferUtils.skip(byteBuffer, boneIndex.length * Short.BYTES);

                floatBuffer = byteBuffer.asFloatBuffer();
                boneWeight = new float[parent.getNumVertex() * 4];
                floatBuffer.get(boneWeight);
                BufferUtils.skip(byteBuffer, boneWeight.length * Float.BYTES);
                boneCount = byteBuffer.getInt();
                boneNames = new String[boneCount];
                for (int i = 0; i < boneNames.length; i++) {
                    boneNames[i] = DNStringUtils.readFixedLengthNTString(byteBuffer, 256);
                }
            } else {
                boneIndex = new short[0];
                boneWeight = new float[0];
                boneNames = new String[0];
            }
        } catch (Exception e) {
            System.out.printf("pos %s rem %s, parent %s%n",
                    byteBuffer.position(), byteBuffer.remaining(),
                    parent.toString());
            throw new RuntimeException(e);
        }
    }

    public boolean isHasBones() {
        return hasBones;
    }

    public short[] getFaceIndex() {
        return faceIndex;
    }

    public float[] getVertexData() {
        return vertexData;
    }

    public float[] getNormalData() {
        return normalData;
    }

    public float[] getUvData() {
        return uvData;
    }

    public short[] getBoneIndex() {
        return boneIndex;
    }

    public float[] getBoneWeight() {
        return boneWeight;
    }

    public int getBoneCount() {
        return boneCount;
    }

    public String[] getBoneNames() {
        return boneNames;
    }

    @Override
    public String toString() {
        return "MeshData{" +
                "hasBones=" + hasBones +
                ", uvData=" + uvData.length +
                ", boneIndex=" + boneIndex.length +
                ", boneWeight=" + boneWeight.length +
                ", boneCount=" + boneCount +
                ", boneNames=" + Arrays.toString(boneNames) +
                ", faceIndex=" + faceIndex.length +
                ", vertexData=" + vertexData.length +
                ", normalData=" + normalData.length +
                '}';
    }
}
