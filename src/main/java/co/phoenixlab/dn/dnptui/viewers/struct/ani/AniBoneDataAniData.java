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

import co.phoenixlab.dn.dnptui.viewers.util.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class AniBoneDataAniData {

    private float[] initialPositionVec3f;
    private float[] initialRotationQuaternionVec4f;
    private float[] initialScalingVec3f;
    private int numTranslationTransformations;
    private int numRotationTransformations;
    private int numScalingTransformations;
    private float[] translationTransformations;
    private int[] translationTransformationFrameCount;
    private float[] rotationTransformations;
    private int[] rotationTransformationFrameCount;
    private float[] scalingTransformations;
    private int[] scalingTransformationFrameCount;

    public AniBoneDataAniData(ByteBuffer byteBuffer, int version) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        FloatBuffer fbuf = byteBuffer.asFloatBuffer();
        initialPositionVec3f = new float[3];
        initialRotationQuaternionVec4f = new float[4];
        initialScalingVec3f = new float[3];
        fbuf.get(initialPositionVec3f);
        fbuf.get(initialRotationQuaternionVec4f);
        fbuf.get(initialScalingVec3f);
        BufferUtils.fskip(byteBuffer, 10 /* 3 + 4 + 3 */);

        if (version == 11) {
            loadV11(byteBuffer);
        } else if (version == 10) {
            loadV10(byteBuffer);
        } else {
            throw new IllegalArgumentException("Version " + version + " not supported");
        }
    }

    private void loadV10(ByteBuffer byteBuffer) {
        numTranslationTransformations = byteBuffer.getInt();
        translationTransformations = new float[numTranslationTransformations * 3];
        translationTransformationFrameCount = new int[numTranslationTransformations];
        for (int i = 0; i < numTranslationTransformations; i++) {
            translationTransformationFrameCount[i] = byteBuffer.getInt();
            int idx = i * 3;
            translationTransformations[idx] = byteBuffer.getFloat();
            translationTransformations[idx + 1] = byteBuffer.getFloat();
            translationTransformations[idx + 2] = byteBuffer.getFloat();
        }

        numRotationTransformations = byteBuffer.getInt();
        rotationTransformations = new float[numRotationTransformations * 4];
        rotationTransformationFrameCount = new int[numRotationTransformations];
        for (int i = 0; i < numRotationTransformations; i++) {
            rotationTransformationFrameCount[i] = byteBuffer.getInt();
            int idx = i * 4;
            rotationTransformations[idx] = byteBuffer.getFloat();
            rotationTransformations[idx + 1] = byteBuffer.getFloat();
            rotationTransformations[idx + 2] = byteBuffer.getFloat();
            rotationTransformations[idx + 3] = byteBuffer.getFloat();
        }

        numScalingTransformations = byteBuffer.getInt();
        scalingTransformations = new float[numScalingTransformations * 3];
        scalingTransformationFrameCount = new int[numScalingTransformations];
        for (int i = 0; i < numScalingTransformations; i++) {
            scalingTransformationFrameCount[i] = byteBuffer.getInt();
            int idx = i * 3;
            scalingTransformations[idx] = byteBuffer.getFloat();
            scalingTransformations[idx + 1] = byteBuffer.getFloat();
            scalingTransformations[idx + 2] = byteBuffer.getFloat();
        }
    }

    private void loadV11(ByteBuffer byteBuffer) {
        numTranslationTransformations = byteBuffer.getInt();
        translationTransformations = new float[numTranslationTransformations * 3];
        translationTransformationFrameCount = new int[numTranslationTransformations];
        for (int i = 0; i < numTranslationTransformations; i++) {
            translationTransformationFrameCount[i] = Short.toUnsignedInt(byteBuffer.getShort());
            int idx = i * 3;
            translationTransformations[idx] = byteBuffer.getFloat();
            translationTransformations[idx + 1] = byteBuffer.getFloat();
            translationTransformations[idx + 2] = byteBuffer.getFloat();
        }

        numRotationTransformations = byteBuffer.getInt();
        rotationTransformations = new float[numRotationTransformations * 4];
        rotationTransformationFrameCount = new int[numRotationTransformations];
        for (int i = 0; i < numRotationTransformations; i++) {
            rotationTransformationFrameCount[i] = Short.toUnsignedInt(byteBuffer.getShort());
            int idx = i * 4;
            rotationTransformations[idx] = shortToFloat(byteBuffer.getShort());
            rotationTransformations[idx + 1] = shortToFloat(byteBuffer.getShort());
            rotationTransformations[idx + 2] = shortToFloat(byteBuffer.getShort());
            rotationTransformations[idx + 3] = shortToFloat(byteBuffer.getShort());
        }

        numScalingTransformations = byteBuffer.getInt();
        scalingTransformations = new float[numScalingTransformations * 3];
        scalingTransformationFrameCount = new int[numScalingTransformations];
        for (int i = 0; i < numScalingTransformations; i++) {
            scalingTransformationFrameCount[i] = Short.toUnsignedInt(byteBuffer.getShort());
            int idx = i * 3;
            scalingTransformations[idx] = byteBuffer.getFloat();
            scalingTransformations[idx + 1] = byteBuffer.getFloat();
            scalingTransformations[idx + 2] = byteBuffer.getFloat();
        }
    }

    private float shortToFloat(short s) {
        //  fast float16 to float32 https://gist.github.com/martinkallman/5049614
        // consider using http://stackoverflow.com/a/3542975 ?
        int v = 0xFFFF & ((int) s);
        int t1 = v & 0x7FFF;
        int t2 = v & 0x8000;
        int t3 = v & 0x7C00;
        t1 <<= 13;
        t2 <<= 16;
        t1 += 0x38000000;
        t1 = (t3 == 0 ? 0 : t1);
        t1 |= t2;
        return Float.intBitsToFloat(t1);
    }
}
