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

import co.phoenixlab.dn.dnptui.viewers.struct.msh.*;
import javafx.application.Platform;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.StringJoiner;
import java.util.function.IntUnaryOperator;

public class MshViewer extends TextViewer {

    @Override
    public void init() {
        super.init();

    }

    @Override
    public void parse(ByteBuffer byteBuffer) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        Msh msh = new Msh(byteBuffer);

        StringBuilder builder = new StringBuilder();

        builder.append("======================================================\n");
        builder.append("HEADER\n");
        builder.append(toString(msh.getMshHeader())).append("\n");

        Bone[] bones = msh.getBoneData();
        builder.append("\n======================================================\n");
        builder.append(bones.length).append(" Bones\n");
        for (int i = 0; i < bones.length; i++) {
            builder.append(String.format("%3d", i)).append(": ").append(indentTabs(toString(bones[i]), 1)).append('\n');
        }

        Mesh[] meshes = msh.getMeshData();
        builder.append("\n======================================================\n");
        builder.append(meshes.length).append(" Meshes\n");
        for (int i = 0; i < meshes.length; i++) {
            builder.append(String.format("%3d", i)).append(": ").append(indentTabs(toString(meshes[i]), 1)).append('\n');
        }

        final String content = builder.toString();
        Platform.runLater(() -> textArea.setText(content));
    }

    @Override
    public void reset() {
        displayPane.setCenter(textArea);
    }

    private String toString(MshHeader header) {
        return String.format("version: %s\nbones: %,d\nmeshes: %,d\nothers: %,d\nbb: %s\n" +
                "unkA: 0x%08X\nunkB: 0x%08X\nunkC: 0x%08X",
                header.getVersion(),
                header.getNumBones(),
                header.getNumMesh(),
                header.getNumOther(),
                indentTabs(boundingBoxToString(header.getBoundingBox()), 1),
                header.getUnknownA(),
                header.getUnknownB(),
                header.getUnknownC()
                );
    }

    private String boundingBoxToString(float[] f) {
        return String.format("%.2e x %.2e x %.2e\n[% .2e % .2e % .2e]\n[% .2e % .2e % .2e]",
                Math.abs(f[0] - f[3]), Math.abs(f[1] - f[4]), Math.abs(f[2] - f[5]),
                f[0], f[1], f[2], f[3], f[4], f[5]);
    }

    private String toString(Bone bone) {
        return String.format("\"%s\"\n%s",
                bone.getName(), fourByFourToString(bone.getTransformMatrix()));
    }

    private String toString(Mesh mesh) {
        return String.format("%s\nv: %,d\ni: %,d\nm: %s (%s)\nunkA: 0x%08X\nd:\n\t%s",
                mesh.getMeshName(),
                mesh.getNumVertex(),
                mesh.getNumIndex(),
                mesh.getRenderModeEnum(),
                mesh.getRenderMode(),
                mesh.getUnknownA(),
                indentTabs(meshDataToString(mesh), 1));
    }

    private String meshDataToString(Mesh mesh) {
        MeshData data = mesh.getMeshData();
        IntUnaryOperator faceCountConverter = mesh.getRenderModeEnum().getFaceCountConverter();
        if (data.isHasBones()) {
            StringJoiner boneJoiner = new StringJoiner("\n");
            for (int i = 0; i < data.getBoneCount(); i++) {
                boneJoiner.add(String.format("%.2f \"%s\"", data.getBoneWeight()[i], data.getBoneNames()[i]));
            }
            return String.format("numFaces: %,d\nbones: %,d\n\t%s",
                    faceCountConverter.applyAsInt(data.getFaceIndex().length),
                    data.getBoneCount(),
                    indentTabs(boneJoiner.toString(), 1)
                    );

        } else {
            return String.format("numFaces: %,d\nbones: 0\n",
                    faceCountConverter.applyAsInt(data.getFaceIndex().length));
        }
    }

    private String fourByFourToString(float[] f) {
        StringJoiner joiner = new StringJoiner("\n");
        for (int i = 0; i < 4; i++) {
            int j = i * 4;
            joiner.add(String.format("| % .2e % .2e % .2e % .2e |", f[j], f[j + 1], f[j + 2], f[j + 3]));
        }
        return joiner.toString();
    }
}
