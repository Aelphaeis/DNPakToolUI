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
import co.phoenixlab.dn.pak.PakFileReader;
import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

public class PakLoadTask extends Task<PakLoadTask.Tuple> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PakLoadTask.class);

    private final List<Path> paths;
    private final BiConsumer<PakHandler, TreeItem<PakTreeEntry>> onDone;

    public PakLoadTask(Path path, BiConsumer<PakHandler, TreeItem<PakTreeEntry>> onDone) {
        paths = Collections.singletonList(path);
        this.onDone = onDone;
    }

    public PakLoadTask(List<Path> paths, BiConsumer<PakHandler, TreeItem<PakTreeEntry>> onDone) {
        this.paths = paths;
        this.onDone = onDone;
    }

    @Override
    protected Tuple call() throws Exception {
        try {
            int numPaths = paths.size();
            PakFileReader reader = new PakFileReader();
            List<PakFile> paks = new ArrayList<>(numPaths);
            Path path;
            long files = 0;
            for (int i = 0; i < numPaths;) {
                path = paths.get(i);
                ++i;
                LOGGER.info("Loading {} ({}/{})", path, i, numPaths);
                updateMessage("Loading " + path.getFileName().toString());
                PakFile pakFile = reader.load(path);
                files += pakFile.getNumFiles();
                paks.add(pakFile);
                updateProgress(i, numPaths);
            }
            LOGGER.info("Loaded {} files", files);
            PakHandler handler = new PakHandler(paks);
            updateMessage("Building file tree");
            LOGGER.info("Building file tree");
            TreeItem<PakTreeEntry> root = handler.populate();
            updateMessage("Done");
            Tuple tuple = new Tuple();
            tuple.handler = handler;
            tuple.root = root;
            LOGGER.info("Pak load job completed");
            return tuple;
        } catch (Exception e) {
            LOGGER.warn("Failed to load pak(s)", e);
            throw e;
        }
    }

    @Override
    protected void succeeded() {
        Tuple tuple = getValue();
        onDone.accept(tuple.handler, tuple.root);
    }

    class Tuple {
        PakHandler handler;
        TreeItem<PakTreeEntry> root;
    }

}

