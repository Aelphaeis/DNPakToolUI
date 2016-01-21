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
import co.phoenixlab.dn.dnptui.viewers.stageini.struct.TriggerDefineEntry;
import javafx.application.Platform;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;

public class StageIniTriggerDefineViewer extends TextViewer {

    @Override
    public void parse(ByteBuffer byteBuffer) {
        TriggerDefineEntry[] entries = decodeEntries(byteBuffer);
        StringJoiner joiner = new StringJoiner("\n");
        joiner.add(String.format("%,d entries", entries.length));
        for (TriggerDefineEntry entry : entries) {
            joiner.add(String.format("0x%04X:\n\t\"%s\"\n\t0x%3$08X (%3$,d)\n\t0x%4$08X (%4$,d)",
                    entry.getEntryId(), entry.getTriggerName(), entry.getUnknownA(), entry.getUnknownB()));
        }
        final String content = joiner.toString();
        Platform.runLater(() -> textArea.setText(content));
    }

    public static TriggerDefineEntry[] decodeEntries(ByteBuffer byteBuffer) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        int numEntries = byteBuffer.getInt();
        TriggerDefineEntry[] entries = new TriggerDefineEntry[numEntries];
        byte[] buf = new byte[0xff];
        for (int i = 0; i < numEntries; i++) {
            TriggerDefineEntry entry = new TriggerDefineEntry();
            entry.setEntryId(byteBuffer.getInt());
            int nameLen = byteBuffer.getInt();
            byteBuffer.get(buf, 0, nameLen);
            entry.setTriggerName(new String(buf, 0, nameLen - 1, StandardCharsets.UTF_8));
            entry.setUnknownA(byteBuffer.getInt());
            entry.setUnknownB(byteBuffer.getInt());
            entries[i] = entry;
        }
        return entries;
    }
}
