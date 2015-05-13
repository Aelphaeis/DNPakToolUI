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

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.zip.InflaterOutputStream;

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

    public TreeItem<PakTreeEntry> populate() {
        root = new TreeItem<>(new PakTreeEntry("", Paths.get(""), null, null));
        Map<String, TreeItem<PakTreeEntry>> dirCache = new HashMap<>();
        for (PakFile pakFile : paks) {
            Map<String, FileEntry> entries = pakFile.getEntryMap();
            entries.forEach((s, e) -> {
                if (s.startsWith("\\")) {
                    s = s.substring(1);
                }
                Path path = Paths.get(s);
                PakTreeEntry entry = new PakTreeEntry(e.name, path, e, pakFile);
                insert(entry, dirCache);
            });
        }
        sort(root);
        dirCache.clear();
        System.gc();
        return root;
    }

    private void insert(PakTreeEntry entry, Map<String, TreeItem<PakTreeEntry>> dirCache) {
        Path entryPath = entry.path.getParent();
        if (entryPath == null) {
            insert(entry, root);
        } else {
            TreeItem<PakTreeEntry> parent = dirCache.get(entryPath.toString());
            if (parent == null) {
                parent = root;
                for (Path path : entryPath) {
                    parent = createDir(path.toString(), parent, dirCache);
                }
            }
            insert(entry, parent);
        }
    }

    private void insert(PakTreeEntry entry, TreeItem<PakTreeEntry> parent) {
        TreeItem<PakTreeEntry> entryTreeItem = new TreeItem<>(entry);
        parent.getChildren().add(entryTreeItem);
    }

    private TreeItem<PakTreeEntry> createDir(String name, TreeItem<PakTreeEntry> parent,
                                             Map<String, TreeItem<PakTreeEntry>> dirCache) {
        Path newPath = parent.getValue().path.resolve(name);
        TreeItem<PakTreeEntry> entryTreeItem = dirCache.get(newPath.toString());
        if (entryTreeItem == null) {
            entryTreeItem = new TreeItem<>(new PakTreeEntry(name, newPath,  null, null));
            dirCache.put(newPath.toString(), entryTreeItem);
            parent.getChildren().add(entryTreeItem);
        }
        return entryTreeItem;
    }


    private void sort(TreeItem<PakTreeEntry> item) {
        item.getChildren().sort(TREE_ITEM_COMPARATOR);
        item.getChildren().forEach(this::sort);
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

    public void exportFile(PakTreeEntry entry, Path exportPath) throws IOException {
        try (InflaterOutputStream outputStream = new InflaterOutputStream(Files.newOutputStream(exportPath,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
            WritableByteChannel byteChannel = Channels.newChannel(outputStream);
            entry.parent.openIfNotOpen();
            entry.parent.transferTo(entry.entry.getFileInfo(), byteChannel);
            outputStream.flush();
        }
    }

    public void exportDirectory(TreeItem<PakTreeEntry> treeItem, Path exportPath) throws IOException {
        for (TreeItem<PakTreeEntry> child : treeItem.getChildren()) {
            PakTreeEntry entry = child.getValue();
            Path path = exportPath.resolve(entry.name);
            if (entry.entry == null) {
                Files.createDirectory(path);
                exportDirectory(child, path);
            } else {
                exportFile(entry, path);
            }
        }
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
