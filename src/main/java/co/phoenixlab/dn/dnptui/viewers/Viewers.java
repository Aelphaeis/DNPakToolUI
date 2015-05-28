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
import javafx.scene.control.TreeItem;

import java.util.*;
import java.util.function.Predicate;

public class Viewers {

    private static final Viewer DEFAULT_VIEWER = new DefaultViewer();

    private static final List<Pair<Predicate<TreeItem<PakTreeEntry>>, Viewer>> matcherViewers = new ArrayList<>();

    private static final Map<String, Viewer> fileExtensionViewers = new HashMap<>();

    static {
        //  Register Viewers here
        fileExtensionViewers.put(".dds", new DdsViewer());
    }

    private Viewers() {}

    public static Viewer getViewer(TreeItem<PakTreeEntry> pakTreeItem) {
        if (pakTreeItem == null || pakTreeItem.getValue() == null) {
            return DEFAULT_VIEWER;
        }
        PakTreeEntry pakTreeEntry = pakTreeItem.getValue();
        String path = pakTreeEntry.path.toString();
        Optional<Viewer> viewer = matcherViewers.stream().
                filter(p -> p.t.test(pakTreeItem)).
                map(Pair::u).
                findFirst();

        return viewer.orElse(getViewerByFileExtension(pakTreeEntry.path.getFileName().toString()));
    }

    private static Viewer getViewerByFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        Optional<Viewer> ret = Optional.empty();
        if (lastDot != -1) {
            String ext = fileName.substring(lastDot);
            ret = Optional.ofNullable(fileExtensionViewers.get(ext));
        }
        return ret.orElse(DEFAULT_VIEWER);
    }

    static class Pair<T, U> {
        final T t;
        final U u;

        public Pair(T t, U u) {
            this.t = t;
            this.u = u;
        }

        public T t() {
            return t;
        }

        public U u() {
            return u;
        }
    }
}
