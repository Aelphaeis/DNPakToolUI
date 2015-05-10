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

import co.phoenixlab.dn.pak.FileEntry;
import co.phoenixlab.dn.pak.PakFile;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PakHandler {

    private static final Comparator<TreeItem<PakTreeEntry>> TREE_ITEM_COMPARATOR = (o1, o2) -> {
        boolean null1 = o1.getValue().entry == null;
        boolean null2 = o2.getValue().entry == null;
        //  Only true if both are null or not null
        if (null1 == null2) {
            return o1.getValue().name.compareToIgnoreCase(o2.getValue().name);
        }
        return -Boolean.compare(null1, null2);
    };

    private final List<PakFile> paks;
    private TreeItem<PakTreeEntry> root;

    public PakHandler(List<PakFile> paks) {
        this.paks = paks;
    }

    public void populate(TreeView<PakTreeEntry> treeView) {
        root = new TreeItem<>(new PakTreeEntry("", Paths.get(""), null, null));
        for (PakFile pakFile : paks) {
            Map<String, FileEntry> entries = pakFile.getEntryMap();
            entries.forEach((s, e) -> {
                if (s.startsWith("\\")) {
                    s = s.substring(1);
                }
                Path path = Paths.get(s);
                PakTreeEntry entry = new PakTreeEntry(e.name, path, e, pakFile);
                insert(root, entry, path);
            });
        }
        sort(root);
        treeView.setRoot(root);
    }

    private void sort(TreeItem<PakTreeEntry> item) {
        item.getChildren().sort(TREE_ITEM_COMPARATOR);
        item.getChildren().forEach(this::sort);
    }

    private TreeItem<PakTreeEntry> insert(TreeItem<PakTreeEntry> treeItem, PakTreeEntry entry, Path path) {
        TreeItem<PakTreeEntry> found = null;
        String sub = path.getName(0).toString();
        if (path.getNameCount() == 1) {
            for (TreeItem<PakTreeEntry> item : treeItem.getChildren()) {
                if (item.getValue().name.equals(sub)) {
                    found = item;
                    break;
                }
            }
            if (found == null) {
                found = new TreeItem<>(entry);
                treeItem.getChildren().add(found);
            } else {
                found.setValue(entry);
            }
            return found;
        }
        for (TreeItem<PakTreeEntry> item : treeItem.getChildren()) {
            if (item.getValue().name.equals(sub)) {
                found = item;
                break;
            }
        }
        if (found == null) {
            found = new TreeItem<>(new PakTreeEntry(sub, treeItem.getValue().path.resolve(sub), null, null));
            treeItem.getChildren().add(found);
        }
        return insert(found, entry, path.subpath(1, path.getNameCount()));
    }

    public PakTreeEntry find(Path path) {
        return Optional.ofNullable(find(root, path)).map(TreeItem::getValue).orElse(null);
    }

    private TreeItem<PakTreeEntry> find(TreeItem<PakTreeEntry> treeItem, Path path) {
        String sub = path.getName(0).toString();
        if (path.getNameCount() == 1) {
            for (TreeItem<PakTreeEntry> item : treeItem.getChildren()) {
                if (item.getValue().name.equals(sub)) {
                    return item;
                }
            }
            return null;
        }
        for (TreeItem<PakTreeEntry> item : treeItem.getChildren()) {
            if (item.getValue().name.equals(sub)) {
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
