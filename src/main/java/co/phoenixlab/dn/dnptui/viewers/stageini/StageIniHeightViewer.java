/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Vincent Zhang/PhoenixLAB
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

package co.phoenixlab.dn.dnptui.viewers.stageini;

import co.phoenixlab.dn.dnptui.PakTreeEntry;
import co.phoenixlab.dn.dnptui.viewers.ImageViewer;
import co.phoenixlab.dn.dnptui.viewers.stageini.struct.GridInfo;
import co.phoenixlab.dn.pak.FileInfo;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.file.NoSuchFileException;
import java.util.zip.InflaterOutputStream;

public class StageIniHeightViewer extends ImageViewer {

    private PakTreeEntry gridInfoEntry;

    private Label unknownALbl;

    @Override
    public void init() {
        super.init();
        exportBtn.setText("Export Heightmap (PNG)");
        unknownALbl = new Label();
        toolbar.getChildren().add(unknownALbl);
    }


    @Override
    public void onLoadStart(TreeItem<PakTreeEntry> pakTreeEntry) {
        super.onLoadStart(pakTreeEntry);

        //  Dispatch a load for gridinfo.ini
        ObservableList<TreeItem<PakTreeEntry>> children = pakTreeEntry.getParent().getParent().getChildren();
        for (TreeItem<PakTreeEntry> child : children) {
            PakTreeEntry value = child.getValue();
            if (value.name.equals("gridinfo.ini")) {
                gridInfoEntry = value;
                break;
            }
        }
    }

    @Override
    protected FileChooser.ExtensionFilter getExtensionFilter() {
        return new FileChooser.ExtensionFilter("PNG Image", "*.png");
    }

    @Override
    protected String getDefaultFileName() {
        return fileName.replace(".ini", ".png");
    }

    @Override
    protected byte[] decodeImageData(ByteBuffer byteBuffer) throws Exception {
        GridInfo gridInfo = loadGridInfo();
        //  We add 1 because heightmap uses vertex heights, not tile heights, and the number of
        //  verticies for a grid of width n is n + 1
        //  Each tile also has two samples as well

        final float unknownA = byteBuffer.getFloat();
        int numEntries = byteBuffer.getInt();
        int dim = (int) Math.sqrt(numEntries);

        int gWidth = gridInfo.getGridWidth();
        int gHeight = gridInfo.getGridLength();

        int density = 1;
        //  Terrain heightmap is encoded in length * density + 1 strips that are width * density + 1 vertices long
        int nVSWidth = dim * density;
        int nVSHeight = dim * density;
        int expected = nVSWidth * nVSWidth;
        if (expected != numEntries) {
            throw new IllegalArgumentException("Dimensions do not agree: " + expected + " vs " + numEntries);
        }
        BufferedImage image = new BufferedImage(nVSWidth, nVSHeight + 1, BufferedImage.TYPE_INT_ARGB);
        for (int stripNum = 0; stripNum < nVSHeight; ++stripNum) {
            for (int vertRow = 0; vertRow < nVSWidth; ++vertRow) {
                int v0 = 0xFFFF - (byteBuffer.getShort() & 0xFFFF);
//                int v1 = 0xFFFF - (byteBuffer.getShort() & 0xFFFF);
                //  We'll squish this into one byte each
                v0 = v0 * 0xFF / 0xFFFF;
//                v1 = v1 * 0xFF / 0xFFFF;
                int rgba0 = 0xFF000000 | (v0 & 0xFF) | ((v0 << 8) & 0xFF00) | ((v0 << 16) & 0xFF0000);
//                int rgba1 = 0xFF000000 | (v1 & 0xFF) | ((v1 << 8) & 0xFF00) | ((v1 << 16) & 0xFF0000);
                image.setRGB(vertRow, stripNum, rgba0);
//                image.setRGB(vertRow, stripNum + 1, rgba1);
            }
        }
//        if (byteBuffer.remaining() != 0) {
//            throw new IllegalStateException("Buffer not empty: " + byteBuffer.remaining());
//        }
        ByteArrayOutputStream imgOut = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", imgOut);
        imageData = imgOut.toByteArray();
        Platform.runLater(() -> unknownALbl.setText(String.format("%.2f", unknownA)));
        return imageData;
    }

    private GridInfo loadGridInfo() throws Exception {
        if (gridInfoEntry == null) {
            throw new NoSuchFileException("Can't load gridinfo.ini");
        }
        FileInfo fileInfo = gridInfoEntry.entry.getFileInfo();
        ByteArrayOutputStream bao = new ByteArrayOutputStream((int) Math.max(
                fileInfo.getDecompressedSize(),
                fileInfo.getCompressedSize()));
        OutputStream out = new InflaterOutputStream(bao);
        WritableByteChannel writableByteChannel = Channels.newChannel(out);
        gridInfoEntry.parent.openIfNotOpen();
        gridInfoEntry.parent.transferTo(fileInfo, writableByteChannel);

        out.flush();
        byte[] decompressedBytes = bao.toByteArray();
        ByteBuffer buffer = ByteBuffer.wrap(decompressedBytes);
        GridInfo gridInfo = new GridInfo();
        gridInfo.read(buffer);
        return gridInfo;
    }

    @Override
    public void reset() {
        gridInfoEntry = null;
        super.reset();
    }
}
