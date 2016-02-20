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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Msh {


    private MshHeader mshHeader;
    private Bone[] boneData;
    private Mesh[] meshData;

    public Msh(ByteBuffer byteBuffer) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        mshHeader = new MshHeader(byteBuffer);
        boneData = new Bone[mshHeader.getNumBones()];
        for (int i = 0; i < boneData.length; i++) {
            boneData[i] = new Bone(byteBuffer);
        }
        boolean hasBones = boneData.length > 0;
        int version = mshHeader.getVersion();
        meshData = new Mesh[mshHeader.getNumMesh()];
        for (int i = 0; i < meshData.length; i++) {
            meshData[i] = new Mesh(byteBuffer, hasBones, version);
        }
    }

    public MshHeader getMshHeader() {
        return mshHeader;
    }

    public Bone[] getBoneData() {
        return boneData;
    }

    public Mesh[] getMeshData() {
        return meshData;
    }
}
