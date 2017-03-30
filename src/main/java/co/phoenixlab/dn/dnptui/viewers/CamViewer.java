/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Vincent Zhang/PhoenixLAB
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

import co.phoenixlab.dn.subfile.cam.Cam;
import co.phoenixlab.dn.subfile.cam.CamReader;
import javafx.application.Platform;

import java.nio.ByteBuffer;

public class CamViewer extends TextViewer {

    @Override
    public void parse(ByteBuffer byteBuffer) {
        CamReader reader = new CamReader();
        Cam cam = reader.read(byteBuffer);
        StringBuilder builder = new StringBuilder();
        builder.append(cam.getMagicNumber()).append('\n')
            .append("Version ").append(cam.getVersion()).append('\n')
            .append(cam.getNumFrames()).append(" frames\n")
            .append("Type ").append(cam.getType()).append('\n')
            .append(cam.getNumFovKeyFrames()).append(" FOV KeyFrames\n")
            .append(cam.getNumTranslateKeyframes()).append(" Translation KeyFrames\n")
            .append(cam.getNumRotationKeyframes()).append(" Rotation KeyFrames\n")
            .append("Init FOV: ").append(cam.getStartFov()).append('\n')
            .append("Init Pos: ").append(String.format("<%-4.4f, %-4.4f, %-4.4f>",
                cam.getStartX(), cam.getStartY(), cam.getStartZ())).append('\n')
            .append("Init Rot: ").append(vec4(cam.getStartRotation(), 0)).append('\n')
            .append("Camera: ").append(cam.getCameraName()).append('\n');
        {
            builder.append("== FOV Frame Data ==\n");
            int[] fovKeyFrameNumbers = cam.getFovKeyFrameNumbers();
            float[] fovKeyFrames = cam.getFovKeyFrames();
            for (int i = 0; i < cam.getNumFovKeyFrames(); i++) {
                builder.append(String.format("\t%,-4d\t", fovKeyFrameNumbers[i])).append(fovKeyFrames[i])
                        .append('\n');
            }
        }
        {
            builder.append("== Translation Frame Data ==\n");
            int[] translateKeyFrameNumbers = cam.getTranslateKeyFrameNumbers();
            float[] translateKeyFrames = cam.getTranslateKeyFrames();
            for (int i = 0; i < cam.getNumTranslateKeyframes(); i++) {
                builder.append(String.format("\t%,-4d\t", translateKeyFrameNumbers[i]))
                        .append(vec3(translateKeyFrames, i))
                        .append('\n');
            }
        }
        {
            builder.append("== Rotation Frame Data ==\n");
            int[] rotationKeyFrameNumbers = cam.getRotationKeyFrameNumbers();
            float[] rotationKeyFrames = cam.getRotationKeyFrames();
            for (int i = 0; i < cam.getNumRotationKeyframes(); i++) {
                builder.append(String.format("\t%,-4d\t", rotationKeyFrameNumbers[i]))
                        .append(vec4(rotationKeyFrames, i))
                        .append('\n');
            }
        }

        final String content = builder.toString();
        Platform.runLater(() -> textArea.setText(content));
    }

    private String vec3(float[] data, int index) {
        int i3 = index * 3;
        return String.format("<%-4.4f, %-4.4f, %-4.4f>",
                data[i3], data[i3 + 1], data[i3 + 2]);
    }

    private String vec4(float[] data, int index) {
        int i4 = index * 4;
        return String.format("<%-4.4f, %-4.4f, %-4.4f, %-4.4f>",
                data[i4], data[i4 + 1], data[i4 + 2], data[i4 + 3]);
    }
}
