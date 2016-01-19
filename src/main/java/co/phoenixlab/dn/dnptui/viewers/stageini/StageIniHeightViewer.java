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
import java.nio.ByteOrder;
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

        final int unknownA = byteBuffer.getInt();
        int numEntries = byteBuffer.getInt();
        int width = gridInfo.getGridWidth() * 2 + 1;
        int height = gridInfo.getGridLength() * 2 + 1;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        if (width * height != numEntries) {
            throw new IllegalArgumentException("Dimensions do not agree: " + width * height + " vs " + numEntries);
        }
        //  Heightmap data is BIG ENDIAN unlike the rest of DN
        //  Probably sent to a vertex shader directly
        byteBuffer.order(ByteOrder.BIG_ENDIAN);

        boolean wide = (byteBuffer.remaining() / numEntries) == 4;
        for (int i = 0; i < numEntries; i++) {
            int v;
            if (wide) {
                long value = Integer.toUnsignedLong(byteBuffer.getInt());
                //  Normalize to an 8 bit value
                value = value * 0xFF / 0xFFFFFFFF;
                v = (int) value & 0xFF;
            } else {
                int value = Short.toUnsignedInt(byteBuffer.getShort());
                //  Normalize to an 8 bit value
                value = value * 0xFF / 0xFFFF;
                v = value & 0xFF;
            }
            int rgba = 0xFF000000;
            rgba |= v | ((v << 8) & 0xFF00) | ((v << 16) & 0xFF0000);
            int x = i % width;
            int y = i / width;
            image.setRGB(x, y, rgba);
        }
        if (byteBuffer.remaining() != 0) {
            throw new IllegalStateException("Buffer not empty: " + byteBuffer.remaining());
        }
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        ByteArrayOutputStream imgOut = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", imgOut);
        imageData = imgOut.toByteArray();
        Platform.runLater(() -> unknownALbl.setText(String.format("0x%08X", unknownA)));
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
