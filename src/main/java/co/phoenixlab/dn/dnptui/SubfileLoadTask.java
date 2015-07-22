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

import co.phoenixlab.dn.pak.FileInfo;
import co.phoenixlab.dn.pak.PakFile;
import javafx.concurrent.Task;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.zip.InflaterOutputStream;

public class SubfileLoadTask extends Task<Void> {

    private final PakTreeEntry entry;
    private final Consumer<ByteBuffer> consumer;
    private final Object loadLock;
    private final Path tempDir;

    public SubfileLoadTask(PakTreeEntry entry, Consumer<ByteBuffer> consumer, Object loadLock, Path tempDir) {
        this.entry = entry;
        this.consumer = consumer;
        this.loadLock = loadLock;
        this.tempDir = tempDir;
    }

    @Override
    protected Void call() throws Exception {
        try {
            if (isCancelled()) {
                return null;
            }
            FileInfo fileInfo = entry.entry.getFileInfo();
            PakFile pakFile = entry.parent;
            pakFile.openIfNotOpen();
            String pakName = pakFile.getPath().getFileName().toString();
            int dotIndex = pakName.indexOf(".");
            if (dotIndex != -1) {
                pakName = pakName.substring(0, dotIndex);
            }
            if (isCancelled()) {
                return null;
            }
            Path temp = tempDir.resolve(pakName).resolve(fileInfo.getDiskOffset() + "." +
                    fileInfo.getDecompressedSize() + fileInfo.getFileName());
            Files.createDirectories(temp.getParent());
            if (Files.notExists(temp)) {
                Files.createFile(temp);
            }
            if (isCancelled()) {
                return null;
            }
            ByteArrayOutputStream bao = new ByteArrayOutputStream((int) fileInfo.getDecompressedSize());
            OutputStream out = new InflaterOutputStream(bao);
            WritableByteChannel writableByteChannel = Channels.newChannel(out);
            do {
                if (isCancelled()) {
                    return null;
                }
                synchronized (loadLock) {
                    try {
                        entry.parent.openIfNotOpen();
                        entry.parent.transferTo(entry.entry.getFileInfo(), writableByteChannel);
                        break;
                    } catch (ClosedByInterruptException cbie) {
                        return null;
                    } catch (ClosedChannelException ex) {
                        entry.parent.reopen();
                    }
                }
            } while (true);
            if (isCancelled()) {
                return null;
            }
            out.flush();
            ByteBuffer buffer = ByteBuffer.allocate((int) fileInfo.getDecompressedSize());
            buffer.put(bao.toByteArray());
            buffer.flip();
            if (isCancelled()) {
                return null;
            }
            consumer.accept(buffer);
        } catch (Exception e) {
            System.err.println("exception while loading " + entry.entry.getFileInfo().getFullPath() + " in " + entry.parent.getPath().getFileName().toString());
            e.printStackTrace();
        }
        return null;
    }
}
