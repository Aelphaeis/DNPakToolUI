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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.zip.InflaterOutputStream;

public class PakHandler {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PakHandler.class);

    /**
     * Compares two TreeItems by their values. Alphabetically sorts folders first and files second
     */
    private static final Comparator<TreeItem<PakTreeEntry>> TREE_ITEM_COMPARATOR = (o1, o2) -> {
        boolean null1 = o1.getValue().isDirectory();
        boolean null2 = o2.getValue().isDirectory();
        //  Only true if both are null or not null
        if (null1 == null2) {
            return o1.getValue().name.compareToIgnoreCase(o2.getValue().name);
        }
        return -Boolean.compare(null1, null2);
    };

    /**
     * List of loaded pak files
     */
    private final List<PakFile> paks;
    /**
     * Root TreeItem in the navigation pane
     */
    private TreeItem<PakTreeEntry> root;

    /**
     * Constructs a new PakHandler with the given loaded paks
     */
    public PakHandler(List<PakFile> paks) {
        this.paks = paks;
    }

    /**
     * Creates the tree structure for the navigation pane of all the loaded files.
     *
     * @return The TreeItem representing the root of the tree, directory "" (empty string).
     */
    public TreeItem<PakTreeEntry> populate() {
        root = new TreeItem<>(new PakTreeEntry("", Paths.get(""), null, null));
        //  Map cache to speed up the insertion process. We don't need to traverse the tree for every single entry
        //  since we cache all the directories that have been created, so insertion goes from always O(n) (n = path
        //  depth) to O(1) in best case. In reality, we see a massive speedup since many directories can contain a large
        //  number of subfiles, so the map lookup (O(1)) becomes the most common operation.
        Map<String, TreeItem<PakTreeEntry>> dirCache = new HashMap<>();
        for (PakFile pakFile : paks) {
            Map<String, FileEntry> entries = pakFile.getEntryMap();
            entries.forEach((s, e) -> {
                //  Strip leading \
                if (s.startsWith("\\")) {
                    s = s.substring(1);
                }
                Path path = Paths.get(s);
                PakTreeEntry entry = new PakTreeEntry(e.name, path, e, pakFile);
                insert(entry, dirCache);
            });
        }
        //  Sort the tree recursively
        sort(root);
        //  We no longer need the cache
        dirCache.clear();
        System.gc();
        return root;
    }

    /**
     * Inserts an entry into the tree recursively, utilizing the cache if possible.
     * <p>
     * Time Complexity
     * <ul>
     * <li>In the worst case, this operation is O(n), where every directory in the path must be created.</li>
     * <li>In the best case, this operation is O(1), where the parent directory of the entry is in the cache.</li>
     * </ul>
     *
     * @param entry    The entry to insert
     * @param dirCache The directory cache to use
     */
    private void insert(PakTreeEntry entry, Map<String, TreeItem<PakTreeEntry>> dirCache) {
        Path entryPath = entry.path.getParent();
        TreeItem<PakTreeEntry> entryTreeItem = new TreeItem<>(entry);
        if (entryPath == null) {
            root.getChildren().add(entryTreeItem);
        } else {
            TreeItem<PakTreeEntry> parent = dirCache.get(entryPath.toString());
            if (parent == null) {
                parent = root;
                for (Path path : entryPath) {
                    parent = createDir(path.toString(), parent, dirCache);
                }
            }
            parent.getChildren().add(entryTreeItem);
        }
    }

    /**
     * Creates a directory and inserts it as a child of the given parent.
     *
     * @param name     The directory's name
     * @param parent   The parent directory to add the new entry as a child
     * @param dirCache The directory cache for this directory to be added to
     * @return The created directory TreeItem
     */
    private TreeItem<PakTreeEntry> createDir(String name, TreeItem<PakTreeEntry> parent,
                                             Map<String, TreeItem<PakTreeEntry>> dirCache) {
        Path newPath = parent.getValue().path.resolve(name);
        TreeItem<PakTreeEntry> entryTreeItem = dirCache.get(newPath.toString());
        if (entryTreeItem == null) {
            entryTreeItem = new TreeItem<>(new PakTreeEntry(name, newPath, null, null));
            dirCache.put(newPath.toString(), entryTreeItem);
            parent.getChildren().add(entryTreeItem);
        }
        return entryTreeItem;
    }

    /**
     * Recursively sorts the given tree.
     *
     * @param item The TreeItem node to start sorting from.
     */
    private void sort(TreeItem<PakTreeEntry> item) {
        item.getChildren().sort(TREE_ITEM_COMPARATOR);
        item.getChildren().forEach(this::sort);
    }


    /**
     * Finds the PakTreeEntry found at path, or null if the path does not exist.
     *
     * @param path The path of the entry to find
     * @return The PakTreeEntry at path, or null if no such element exists
     */
    public PakTreeEntry find(Path path) {
        return Optional.ofNullable(find(root, path)).map(TreeItem::getValue).orElse(null);
    }

    /**
     * Recursive implementation for find.
     *
     * @param treeItem The parent TreeItem to search in
     * @param path     The desired path to find, relative to treeItem
     * @return The found TreeItem, or null if no such element exists
     */
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

    /**
     * Exports a file to the provided path.
     *
     * @param entry      The entry of the file to export
     * @param exportPath The location to export the file
     * @throws IOException If there was an I/O error during exporting
     */
    public void exportFile(PakTreeEntry entry, Path exportPath) throws IOException {
        try (InflaterOutputStream outputStream = new InflaterOutputStream(Files.newOutputStream(exportPath,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
            WritableByteChannel byteChannel = Channels.newChannel(outputStream);
            do {
                try {
                    entry.parent.openIfNotOpen();
                    entry.parent.transferTo(entry.entry.getFileInfo(), byteChannel);
                    break;
                } catch (ClosedChannelException ex) {
                    entry.parent.reopen();
                }
            } while (true);
            outputStream.flush();
            LOGGER.info("Subfile {} exported to {}", entry.path, exportPath);
        }
    }

    /**
     * Exports an entire directory and all of its children into the provided directory. Recursively operates on
     * subdirectories.
     *
     * @param treeItem   The directory TreeItem to export
     * @param exportPath The directory to export the contents of treeItem into
     * @throws IOException If there was an I/O error during exporting
     */
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
        LOGGER.info("Subdirectory {} exported to {}", treeItem.getValue().path, exportPath);
    }

    /**
     * Unloads all the pak files managed by this handler
     */
    public void unload() {
        paks.forEach(this::tryClosePak);
        paks.clear();
        LOGGER.info("Paks unloaded");
    }

    /**
     * Helper method for unload()
     *
     * @param pakFile The pakFile to attempt to unload
     */
    private void tryClosePak(PakFile pakFile) {
        try {
            pakFile.close();
        } catch (IOException e) {
            LOGGER.warn("Unable to close pak " + pakFile.getPath().toString(), e);
        }
    }

}
