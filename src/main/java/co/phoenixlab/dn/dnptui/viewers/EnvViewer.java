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

import co.phoenixlab.dn.dnptui.viewers.util.DNStringUtils;
import javafx.application.Platform;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class EnvViewer extends TextViewer {

    @Override
    public void parse(ByteBuffer byteBuffer) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        final String head = DNStringUtils.readFixedLengthNTString(byteBuffer, 32);
        if (!"EnvInfo File Header".equals(head)) {
            Platform.runLater(() -> textArea.setText("Not a valid ENV file: Header mismatch: " + head));
            return;
        }
        int majorVersion = byteBuffer.getInt();
        int minorVersion = byteBuffer.getInt();
        StringBuilder builder = new StringBuilder();
        builder.append("EnvInfo v").append(majorVersion).append('.').append(minorVersion).append('\n');

        String sknName = readLPNTString(byteBuffer, majorVersion, minorVersion);
        builder.append("SKYDOME\n");
        builder.append("Skn: ").append(sknName).append('\n');
        int skyDomeUnkA = byteBuffer.getInt();
        int skyDomeUnkB = byteBuffer.getInt();
        int skyDomeUnkC = byteBuffer.getInt();
        int skyDomeUnkD = byteBuffer.getInt();
        builder.append("OriginX: ").append(unknownIntToString(skyDomeUnkA)).append('\n');
        builder.append("OriginZ: ").append(unknownIntToString(skyDomeUnkB)).append('\n');
        builder.append("UnkC: ").append(unknownIntToString(skyDomeUnkC)).append('\n');
        builder.append("UnkD: ").append(unknownIntToString(skyDomeUnkD)).append('\n');

        String envTexName = readLPNTString(byteBuffer, majorVersion, minorVersion);
        builder.append("\nENVIRONMENT MAP\n");
        builder.append(envTexName).append('\n');



        final String content = builder.toString();
        Platform.runLater(() -> textArea.setText(content));
    }

    private String unknownIntToString(int i) {
        return String.format("0x%08X; %,14d; %,14.2f", i, i, Float.intBitsToFloat(i));
    }

    private String readLPNTString(ByteBuffer buffer, int majorV, int minorV) {
        if (majorV == 1) {
            if (minorV < 2) {
                return DNStringUtils.readLPString(buffer);
            }
            return DNStringUtils.readLPNTString(buffer);
        }
        throw new IllegalArgumentException("Version " + majorV + "." + minorV + " not supported");
    }
}
