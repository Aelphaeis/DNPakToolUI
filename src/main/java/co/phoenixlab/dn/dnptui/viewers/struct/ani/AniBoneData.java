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

package co.phoenixlab.dn.dnptui.viewers.struct.ani;

import co.phoenixlab.dn.dnptui.viewers.util.DNStringUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class AniBoneData {

    private String boneName;
    private String parentBone;
    private AniBoneDataAniData[] aniData;

    public AniBoneData(ByteBuffer byteBuffer, int numAnim, int version) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        boneName = DNStringUtils.readFixedLengthString(byteBuffer, 256);
        parentBone = DNStringUtils.readFixedLengthString(byteBuffer, 768);
        aniData = new AniBoneDataAniData[numAnim];
        for (int i = 0; i < numAnim; i++) {
            aniData[i] = new AniBoneDataAniData(byteBuffer, version);
        }
    }

}
