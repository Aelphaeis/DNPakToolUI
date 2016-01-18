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

package co.phoenixlab.dn.dnptui.viewers.stageini;

import co.phoenixlab.dn.dnptui.viewers.TextViewer;
import javafx.application.Platform;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class StageIniGridInfoViewer extends TextViewer {

    @Override
    public void parse(ByteBuffer byteBuffer) {
        /*
        Basic Structure
        CHAR[64]        Parent world name (?)
        INT32           UnknownA (seems to always be 1)
        INT32           UnknownB (seems to always be 1)
        INT32           StageLength (varies, usually multiple of 50)
        INT32           StageWidth (usually same value as UnknownC)
        INT32           UnknownE (seems to always be 50)
         */
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byte[] parentWorld = new byte[64];
        byteBuffer.get(parentWorld);
        String parentWorldStr = new String(parentWorld, StandardCharsets.UTF_8).trim();
        int unknownA = byteBuffer.getInt();
        int unknownB = byteBuffer.getInt();
        int unknownC = byteBuffer.getInt();
        int unknownD = byteBuffer.getInt();
        int unknownE = byteBuffer.getInt();
        final String content = String.format("ParentWorld: \"%s\"\n" +
                "unknownA: %d\n" +
                "unknownB: %d\n" +
                "StageLength: %d\n" +
                "StageWidth: %d\n" +
                "unknownE: %d",
                parentWorldStr,  unknownA, unknownB, unknownC, unknownD, unknownE);
        Platform.runLater(() -> {
            textArea.setText(content);
        });
    }
}
