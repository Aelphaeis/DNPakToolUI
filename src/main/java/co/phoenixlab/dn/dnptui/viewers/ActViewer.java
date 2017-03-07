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

import co.phoenixlab.dn.subfile.act.Act;
import co.phoenixlab.dn.subfile.act.TestActReader;
import javafx.application.Platform;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class ActViewer extends TextViewer {

    @Override
    public void init() {
        super.init();

    }

    @Override
    public void parse(ByteBuffer byteBuffer) {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(baos);
        TestActReader reader = new TestActReader(printStream);
        Act act = reader.read(byteBuffer);
        String content = new String(baos.toByteArray(), StandardCharsets.UTF_8);
        Platform.runLater(() -> textArea.setText(content));
    }

    @Override
    public void reset() {
        displayPane.setCenter(textArea);
    }
}
