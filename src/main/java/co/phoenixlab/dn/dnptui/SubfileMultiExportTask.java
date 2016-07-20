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

package co.phoenixlab.dn.dnptui;

import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;

import java.nio.file.Path;

public class SubfileMultiExportTask extends Task<Void> {

    private final PakHandler handler;
    private final TreeItem<PakTreeEntry>[] treeItems;
    private final Path exportPath;


    public SubfileMultiExportTask(PakHandler handler, TreeItem<PakTreeEntry>[] treeItems, Path exportPath) {
        this.treeItems = treeItems;
        this.exportPath = exportPath;
        this.handler = handler;
    }

    @Override
    protected Void call() throws Exception {
        updateMessage("Exporting...");
        long startTime = System.currentTimeMillis();
        int i = 0;
        for (TreeItem<PakTreeEntry> treeItem : treeItems) {
            PakTreeEntry value = treeItem.getValue();
            if (value.isDirectory()) {
                updateMessage(String.format("Exporting folder %s (%d/%d)",
                    value.name, i, treeItems.length));
                handler.exportDirectory(treeItem, exportPath.resolve(value.path), this::updateProgress);
            } else {
                updateMessage(String.format("Exporting file %s (%d/%d)",
                    value.name, i, treeItems.length));
                handler.exportFile(value, exportPath.resolve(value.path));
            }
        }
        //  Add a sleep if we took less than 1s because people dont notice it completed
        long delta = System.currentTimeMillis() - startTime;
        if (delta < 1000) {
            Thread.sleep(1000 - delta);
        }
        updateMessage("Done");
        return null;
    }

    private void updateProgress(double d) {
        updateProgress(d, 1D);
    }
}
