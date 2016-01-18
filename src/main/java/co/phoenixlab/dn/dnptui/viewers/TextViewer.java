/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Vincent Zhang/PhoenixLAB
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

import co.phoenixlab.dn.dnptui.PakTreeEntry;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class TextViewer implements Viewer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TextViewer.class);

    private long maxDisplaySize;

    private Node displayNode;

    @FXML
    protected BorderPane displayPane;

    @FXML
    protected CheckBox lineWrapToggleChkBox;

    @FXML
    protected TextArea textArea;

    public TextViewer() {
    }


    @Override
    public void init() {
        displayNode = displayPane;
    }

    @Override
    public Node getDisplayNode() {
        return displayNode;
    }

    @Override
    public void parse(ByteBuffer byteBuffer) {
        byte[] buf = new byte[byteBuffer.remaining()];
        byteBuffer.get(buf);
        String data = new String(buf, StandardCharsets.UTF_8);
        textArea.setText(data);
    }

    @Override
    public void onLoadStart(TreeItem<PakTreeEntry> treeItem) {

    }

    @Override
    public void reset() {
        //  64 MB default
        maxDisplaySize = Long.getLong("co.phoenixlab.dn.dnptui.text.maxSize", 67108864);
    }
}
