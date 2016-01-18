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

import co.phoenixlab.dds.Dds;
import co.phoenixlab.dds.DdsImageDecoder;
import javafx.stage.FileChooser;

import java.nio.ByteBuffer;

public class DdsViewer extends ImageViewer {

    private Dds currentDds;
    private final DdsImageDecoder decoder;


    public DdsViewer() {
        super();
        decoder = new DdsImageDecoder();
    }

    @Override
    public void init() {
        super.init();
        exportBtn.setText("Export as PNG");
    }

    protected byte[] getImageData() {
        return imageData;
    }

    protected FileChooser.ExtensionFilter getExtensionFilter() {
        return new FileChooser.ExtensionFilter("PNG Image", "*.png");
    }

    protected String getDefaultFileName() {
        return fileName.replace(".dds", ".png");
    }

    @Override
    protected byte[] decodeImageData(ByteBuffer byteBuffer) throws Exception {
        currentDds = new Dds();
        currentDds.read(byteBuffer);
        imageData = decoder.convertToPNG(currentDds);
        return imageData;
    }

    @Override
    public void reset() {
        currentDds = null;
        super.reset();
    }
}
