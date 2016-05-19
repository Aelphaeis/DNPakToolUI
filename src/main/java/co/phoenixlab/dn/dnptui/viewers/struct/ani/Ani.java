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
import co.phoenixlab.dn.dnptui.viewers.util.DNStringUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class Ani {

    private boolean compound;
    private String[] childAnis;
    private String magicString;
    private AniHeader aniHeader;
    private AniAnimation[] animations;
    private AniBoneData[] animationData;

    public Ani(ByteBuffer byteBuffer) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        //  peek at the first 10 bytes, if its not Eyedentity then we have a compound Ani
        byte[] temp = new byte[24];
        byteBuffer.get(temp);
        BufferUtils.skip(byteBuffer, -24);
        if (!new String(temp, StandardCharsets.UTF_8).startsWith("Eternity Engine Ani File")){
            compound = true;
            temp = new byte[byteBuffer.remaining()];
            byteBuffer.get(temp);
            String s = new String(temp, StandardCharsets.UTF_8);
            childAnis = s.split("\\r?\\n");
            return;
        }
        magicString = DNStringUtils.readFixedLengthString(byteBuffer,  256);
        aniHeader = new AniHeader(byteBuffer.duplicate());
        BufferUtils.skip(byteBuffer, 768);
        animations = new AniAnimation[aniHeader.getNumAnimations()];
        animationData = new AniBoneData[aniHeader.getNumBones()];
        for (int i = 0; i < animations.length; i++) {
            animations[i] = new AniAnimation(DNStringUtils.readFixedLengthString(byteBuffer, 256));
        }
        for (int i = 0; i < animations.length; i++) {
            animations[i].setNumFrames(byteBuffer.getInt());
        }
        for (int i = 0; i < aniHeader.getNumBones(); i++) {
            animationData[i] = new AniBoneData(byteBuffer, aniHeader.getNumAnimations(), aniHeader.getVersion());
        }
    }

    public AniHeader getAniHeader() {
        return aniHeader;
    }

    public AniBoneData[] getAnimationData() {
        return animationData;
    }

    public AniAnimation[] getAnimations() {
        return animations;
    }

    public String getMagicString() {
        return magicString;
    }

    public String[] getChildAnis() {
        return childAnis;
    }

    public boolean isCompound() {
        return compound;
    }
}
