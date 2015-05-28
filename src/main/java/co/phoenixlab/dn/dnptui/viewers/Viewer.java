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
import javafx.scene.Node;
import javafx.scene.control.TreeItem;

import java.nio.ByteBuffer;

public interface Viewer {

    /**
     * Called from the UI thread once {@link #parse(ByteBuffer)} finishes parsing the file data
     * @return The node to display in the view pane
     */
    Node getDisplayNode();

    /**
     * Called from the loading thread (aka not from the UI thread) after the file data has been decompressed,
     * passing the raw file data to this viewer. The viewer should parse out the file data and construct the UI
     * for displaying the data, and have its root ready for display (via {@link #getDisplayNode()}) once finished.
     * @param byteBuffer A ByteBuffer containing the raw, decompressed file data
     */
    void parse(ByteBuffer byteBuffer);

    /**
     * Called from the UI thread when the selection changes. For leaf nodes (subfiles), the system will load the data
     * from the pak asynchronously and call {@link #parse(ByteBuffer)}) when finished. For directories, the Viewer
     * itself must make arrangements to load children nodes, as by default no children files will be loaded.
     * @param treeItem The TreeItem of the item to load
     */
    void onLoadStart(TreeItem<PakTreeEntry> treeItem);

    /**
     * Resets/clears this viewer for later reuse
     */
    void reset();
}
