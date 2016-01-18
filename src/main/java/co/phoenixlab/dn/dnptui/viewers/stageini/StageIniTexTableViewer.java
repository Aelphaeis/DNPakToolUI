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

public class StageIniTexTableViewer extends TextViewer {

    @Override
    public void parse(ByteBuffer byteBuffer) {
        /*
        Basic structure
        INT32           Number of entries (n)
        INT32[n]        UnknownArrayA
        TEXGROUP[n]     Texture groups
        TEXGROUPPROP[n] UnknownTexGroupPropertiesA
        TEXGROUPPROP[n] OPTIONAL UnknownTexGroupPropertiesB

        TEXGROUP Structure
        INT32           Number of entries (n)
        LPNTSTR[n]      Array of texture names (INT32 len + CHAR[len])

        TEXGROUPPROP Structure
        INT32           Number of entries (n)
        INT32[n]        Array of entry values
         */
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        int numEntries = byteBuffer.getInt();
        final StringBuilder builder = new StringBuilder(String.format("%d entries\n", numEntries));
        int[] unknownArrayA = new int[numEntries];
        byteBuffer.asIntBuffer().get(unknownArrayA);
        byteBuffer.position(byteBuffer.position() + numEntries * Integer.BYTES);
        int entriesPerLine = 16;
        builder.append("unknownArrayA:\n");
        for (int i = 0; i < (numEntries + entriesPerLine - 1)/entriesPerLine; i++) {
            builder.append(String.format("\t0x%04X: ", i * entriesPerLine));
            for (int j = 0; j < entriesPerLine; j++) {
                int pos = i * entriesPerLine + j;
                if (pos >= numEntries) {
                    break;
                }
                builder.append(String.format("%d ", unknownArrayA[pos]));
            }
            builder.append("\n");
        }
        builder.append("TextureGroups:\n");
        byte[] nameBuf = new byte[128];
        for (int i = 0; i < numEntries; i++) {
            int numTex = byteBuffer.getInt();
            builder.append(String.format("\t0x%04X:\n", i));
            for (int j = 0; j < numTex; j++) {
                int nameSize = byteBuffer.getInt();
                if (nameBuf.length < nameSize) {
                    nameBuf = new byte[nameSize];
                }
                byteBuffer.get(nameBuf, 0, nameSize);
                builder.append("\t\t").append(j).append(": \"").
                        append(new String(nameBuf, 0, nameSize - 1, StandardCharsets.UTF_8)).append("\"\n");
            }
        }
        builder.append("UnknownTextureGroupsPropertiesA:\n");
        for (int i = 0; i < numEntries; i++) {
            int numTex = byteBuffer.getInt();
            builder.append(String.format("\t0x%04X:\n", i));
            for (int j = 0; j < numTex; j++) {
                int value = byteBuffer.getInt();
                builder.append(String.format("\t\t%1$d: 0x%2$08X %3$.2f",
                        j, value, Float.intBitsToFloat(value))).append("\n");
            }
        }
        builder.append("UnknownTextureGroupsPropertiesB:\n");
        if (byteBuffer.remaining() > 0) {

            for (int i = 0; i < numEntries; i++) {
                int numTex = byteBuffer.getInt();
                builder.append(String.format("\t0x%04X:\n", i));
                for (int j = 0; j < numTex; j++) {
                    int value = byteBuffer.getInt();
                    builder.append(String.format("\t\t%d: %8X %.2f\n",
                            j, value, Float.intBitsToFloat(value)));
                }
                builder.append("\n");
            }
        } else {
            builder.append("\tNONE\n");
        }
        Platform.runLater(() -> {
            textArea.setText(builder.toString());
        });

    }
}
