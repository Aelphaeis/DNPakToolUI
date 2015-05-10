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

import co.phoenixlab.dn.pak.PakFile;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public class PakHandler {

    private final List<PakFile> paks;
    private TreeItem<PakTreeEntry> root;

    public PakHandler(List<PakFile> paks) {
        this.paks = paks;
    }

    public void populate(TreeView<PakTreeEntry> treeView) {
        root = new TreeItem<>(new PakTreeEntry("", Paths.get(""), null, null));


        treeView.setRoot(root);
    }

    public PakTreeEntry find(Path path) {
        return Optional.ofNullable(find(root, path)).map(TreeItem::getValue).orElse(null);
    }

    private TreeItem<PakTreeEntry> find(TreeItem<PakTreeEntry> treeItem, Path path) {
        if (path.getNameCount() == 1) {
            for (TreeItem<PakTreeEntry> item : treeItem.getChildren()) {
                if (item.getValue().name.equals(path.toString())) {
                    return item;
                }
            }
            return null;
        }
        Path sub = path.getName(0);
        for (TreeItem<PakTreeEntry> item : treeItem.getChildren()) {
            if (item.getValue().name.equals(sub.toString())) {
                return find(item, path.subpath(1, path.getNameCount()));
            }
        }
        return null;
    }


    public void unload() {
        paks.forEach(this::tryClosePak);
        paks.clear();
    }

    private void tryClosePak(PakFile pakFile) {
        try {
            pakFile.close();
        } catch (IOException e) {
            //  Don't care
        }
    }

}
