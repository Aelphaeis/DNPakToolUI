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

import co.phoenixlab.dn.dnptui.viewers.struct.msh.Bone;
import co.phoenixlab.dn.dnptui.viewers.struct.msh.Mesh;
import co.phoenixlab.dn.dnptui.viewers.struct.msh.Msh;
import javafx.application.Platform;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MshViewer extends TextViewer {

    @Override
    public void init() {
        super.init();

    }

    @Override
    public void parse(ByteBuffer byteBuffer) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        Msh msh = new Msh(byteBuffer);

        StringBuilder builder = new StringBuilder("Eyedentity Mesh File\n\n");

        builder.append("Msh Header\n");
        builder.append(msh.getMshHeader().toString()).append("\n\n");

        Bone[] bones = msh.getBoneData();
        builder.append("======================================================\n\t");
        builder.append(bones.length).append(" Bones\n");
        for (int i = 0; i < bones.length; i++) {
            builder.append(String.format("%3d", i)).append(": ").append(bones[i].toString()).append('\n');
        }

        Mesh[] meshes = msh.getMeshData();
        builder.append("======================================================\n\t");
        builder.append(meshes.length).append(" Meshes\n");
        for (int i = 0; i < meshes.length; i++) {
            builder.append(String.format("%3d", i)).append(": ").append(meshes[i].toString()).append('\n');
        }

        final String content = builder.toString();
        Platform.runLater(() -> textArea.setText(content));
    }

    @Override
    public void reset() {
        displayPane.setCenter(textArea);
    }
}
