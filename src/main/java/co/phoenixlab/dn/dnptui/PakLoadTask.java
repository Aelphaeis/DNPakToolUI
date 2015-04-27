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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PakLoadTask extends Task<PakHandler> {

    private final List<Path> paths;

    public PakLoadTask(Path path) {
        paths = Collections.singletonList(path);
    }

    public PakLoadTask(List<Path> paths) {
        this.paths = paths;
    }

    @Override
    protected PakHandler call() throws Exception {
        try {
            int numPaths = paths.size();
            PakFileReader reader = new PakFileReader();
            updateProgress(0, 1);
            List<PakFile> paks = new ArrayList<>(numPaths);
            Path path;
            for (int i = 0; i < numPaths; i++) {
                path = paths.get(i);
                updateMessage("Loading " + path.getFileName().toString());
                PakFile pakFile = reader.load(path);
                paks.add(pakFile);
                updateProgress(i + 1, numPaths);
            }
            updateMessage("Done");
            return new PakHandler(paks);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
