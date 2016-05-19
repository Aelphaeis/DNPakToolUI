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

package co.phoenixlab.dn.dnptui.viewers;

import co.phoenixlab.dn.dnptui.viewers.struct.ani.*;
import javafx.application.Platform;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.StringJoiner;

public class AniViewer extends TextViewer {
    @Override
    public void init() {
        super.init();

    }

    @Override
    public void parse(ByteBuffer byteBuffer) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        Ani ani = new Ani(byteBuffer);
        StringBuilder builder = new StringBuilder();
        if (ani.isCompound()) {
            builder.append("Compound ANI file\n");
            for (String s : ani.getChildAnis()) {
                builder.append(s).append("\n");
            }
        } else {
            builder.append("======================================================\n");
            builder.append("HEADER\n");
            builder.append(toString(ani.getAniHeader()));

            builder.append("\n======================================================\n");
            builder.append(ani.getAniHeader().getNumAnimations());
            builder.append(" Animations\n");
            AniAnimation[] animations = ani.getAnimations();
            for (int i = 0; i < animations.length; i++) {
                builder.append(String.format("%3d\n\tname: \"%s\"\n\tframes: %d\n",
                        i,
                        animations[i].getName(),
                        animations[i].getNumFrames()));
            }

            builder.append("\n======================================================\n");
            builder.append(ani.getAniHeader().getNumBones());
            builder.append(" Bone Data Sections\n");
            AniBoneData[] animationData = ani.getAnimationData();
            for (int i = 0; i < animationData.length; i++) {
                AniBoneData aniBoneData = animationData[i];
                builder.append(String.format("%3d\n\tboneName: \"%s\"\n\tparentBone: \"%s\"\n\tdata:\n%s\n",
                        i,
                        aniBoneData.getBoneName(),
                        aniBoneData.getParentBone(),
                        indentTabs(toString(aniBoneData.getAniData(), animations), 2, true)));

            }
        }
        final String content = builder.toString();
        Platform.runLater(() -> textArea.setText(content));
    }

    @Override
    public void reset() {
        displayPane.setCenter(textArea);
    }

    private String toString(AniHeader header) {
        return String.format("version: %s\nanimations: %,d\nbones: %,d",
                header.getVersion(),
                header.getNumAnimations(),
                header.getNumBones());
    }

    private String toString(AniBoneDataAniData[] data, AniAnimation[] anims) {
        StringJoiner joiner = new StringJoiner("\n");
        for (int i = 0; i < data.length; i++) {
            AniBoneDataAniData aniBoneDataAniData = data[i];
            joiner.add(String.format("%3d (%s)\n%s",
                    i,
                    anims[i].getName(),
                    indentTabs(toString(aniBoneDataAniData), 1, true)));
        }
        return joiner.toString();
    }

    private String toString(AniBoneDataAniData data) {
        return "iPos: " + toString(data.getInitialPositionVec3f(), 3) + "\n" +
                "iRot: " + toString(data.getInitialRotationQuaternionVec4f(), 4) + "\n" +
                "iSca: " + toString(data.getInitialScalingVec3f(), 3) + "\n" +
                "posFrames: " + data.getNumTranslationTransformations() + "\n" +
                "rotFrames: " + data.getNumRotationTransformations() + "\n" +
                "scaFrames: " + data.getNumScalingTransformations();
    }

    private String toString(float[] vec, int components) {
        return toString(vec, 0, components);
    }

    private String toString(float[] vec, int offset, int components) {
        StringJoiner joiner = new StringJoiner(" ", "| ", " |");
        for (int i = offset; i < offset + components; i++) {
            joiner.add(String.format("% .2e", vec[i]));
        }
        return joiner.toString();
    }
}
