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

import co.phoenixlab.dn.pak.DirEntry;
import co.phoenixlab.dn.pak.FileEntry;
import co.phoenixlab.dn.pak.PakFile;
import javafx.scene.control.TreeView;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleConsumer;

public class PakHandler {

    private final List<PakFile> paks;
    private final DirEntry root;

    private int numSubfiles;
    private int subfilesIndexed;
    private DoubleConsumer buildListner;

    public PakHandler(List<PakFile> paks) {
        this.paks = paks;
        root = new DirEntry("", null);
    }

    public void build(DoubleConsumer progressListener) {
        buildListner = progressListener;
        numSubfiles = paks.stream().mapToInt(PakFile::getNumFiles).sum();
        subfilesIndexed = 0;
        buildListner.accept(0D);
        paks.forEach(this::consolidate);
    }
    
    private void consolidate(PakFile pakFile) {
        Map<String, FileEntry> entryMap = pakFile.getEntryMap();
        
    }

    private void incrProg() {
        ++subfilesIndexed;
        buildListner.accept((double) subfilesIndexed / (double) numSubfiles);
    }

    public void populate(TreeView treeView) {

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
