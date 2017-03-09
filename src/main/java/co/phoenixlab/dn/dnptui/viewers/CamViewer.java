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
            .append(cam.getNumTypeAKeyFrames()).append(" Type A KeyFrames\n")
            .append(cam.getNumTypeBKeyFrames()).append(" Type B KeyFrames\n")
            .append(cam.getNumTranslateKeyframes()).append(" Translation KeyFrames\n")
            .append(cam.getNumRotationKeyframes()).append(" Rotation KeyFrames\n");



        final String content = builder.toString();
        Platform.runLater(() -> textArea.setText(content));
    }

}
