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

import co.phoenixlab.dn.subfile.ani.Ani;
import co.phoenixlab.dn.subfile.ani.AniBoneData;
import co.phoenixlab.dn.subfile.ani.AniBoneKeyframes;
import co.phoenixlab.dn.subfile.ani.AniReader;
import co.phoenixlab.dn.util.math.Vec3;
import co.phoenixlab.dn.util.math.Vec4;
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
        AniReader reader = new AniReader();
        Ani ani = reader.read(byteBuffer);

        StringBuilder builder = new StringBuilder();
        if (ani.isCompound()) {
            builder.append("Compound ANI file\n");
            for (String s : ani.getChildAnis()) {
                builder.append(s).append("\n");
            }
        } else {
            builder.append("======================================================\n");
            builder.append("HEADER\n");
            builder.append(ani.getMagicString());
            builder.append(ani.getVersion());

            builder.append("\n======================================================\n");
            builder.append(ani.getNumAnimations());
            builder.append(" Animations\n");
            String[] animationNames = ani.getAnimationNames();
            int[] animationFrameCounts = ani.getAnimationFrameCounts();
            for (int i = 0; i < ani.getNumAnimations(); i++) {
                builder.append(String.format("%3d\n\tname: \"%s\"\n\tframes: %d\n",
                        i,
                        animationNames[i],
                        animationFrameCounts[i]));
            }

            builder.append("\n======================================================\n");
            builder.append(ani.getNumBones());
            builder.append(" Bone Data Sections\n");
            AniBoneData[] animationData = ani.getAnimationData();
            for (int i = 0; i < animationData.length; i++) {
                AniBoneData aniBoneData = animationData[i];
                builder.append(String.format("%3d\n\tboneName: \"%s\"\n\tparentBone: \"%s\"\n\tdata:\n%s\n",
                        i,
                        aniBoneData.getBoneName(),
                        aniBoneData.getParentBone(),
                        indentTabs(toString(aniBoneData.getKeyframes(), ani), 2, true)));

            }
        }
        final String content = builder.toString();
        Platform.runLater(() -> textArea.setText(content));
    }

    @Override
    public void reset() {
        displayPane.setCenter(textArea);
    }

    private String toString(AniBoneKeyframes[] data, Ani ani) {
        StringJoiner joiner = new StringJoiner("\n");
        for (int i = 0; i < data.length; i++) {
            AniBoneKeyframes aniBoneDataAniData = data[i];
            joiner.add(String.format("%3d (%s)\n%s",
                    i,
                    ani.getAnimationNames()[i],
                    indentTabs(toString(aniBoneDataAniData), 1, true)));
        }
        return joiner.toString();
    }

    private String toString(AniBoneKeyframes data) {
        return "iPos: " + toString(data.getStartPos()) + "\n" +
                "iRot: " + toString(data.getStartQuat()) + "\n" +
                "iSca: " + toString(data.getStartScale()) + "\n" +
                "posFrames: " + data.getNumPosTransform() + "\n" +
                "rotFrames: " + data.getNumRotTransform() + "\n" +
                "scaFrames: " + data.getNumScaleTransform();
    }

    private String toString(Vec3 vec3f) {
        return String.format("| % .2e % .2e % .2e |", vec3f.getX(), vec3f.getY(), vec3f.getZ());
    }

    private String toString(Vec4 vec4f) {
        return String.format("| % .2e % .2e % .2e % .2e |", vec4f.getX(), vec4f.getY(), vec4f.getZ(), vec4f.getW());
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
