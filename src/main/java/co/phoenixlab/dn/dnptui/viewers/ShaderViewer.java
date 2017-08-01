/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Vincent Zhang/PhoenixLAB
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

import co.phoenixlab.dn.dnptui.DNPTApplication;
import co.phoenixlab.dn.dnptui.DNPTUIController;
import co.phoenixlab.dn.subfile.shader.Shader;
import co.phoenixlab.dn.subfile.shader.ShaderPack;
import co.phoenixlab.dn.subfile.shader.ShaderPackReader;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public class ShaderViewer extends TextViewer {

    private ShaderPack shaderPack;
    protected Button exportBtn;
    protected DNPTUIController mainUiController;


    @Override
    public void init() {
        super.init();
        exportBtn = new Button("Export all");
        exportBtn.setOnAction(this::export);
        toolbar.getChildren().add(exportBtn);
    }


    @Override
    public void parse(ByteBuffer byteBuffer) {
        StringBuilder builder = new StringBuilder();

        ShaderPackReader reader = new ShaderPackReader();
        shaderPack = reader.read(byteBuffer);

        builder.append("Magic: ").append(String.format("0x%08X", shaderPack.getMagic())).append("\n");
        builder.append("NumEntries: ").append(String.format("%,d", shaderPack.getNumShaders())).append("\n");

        shaderPack.getShaders().forEach(s ->
        {
            builder.append("ShaderEntry\n");
            builder.append("\t").append(s.getName()).append("\n");
            builder.append("\tQuality: ").append(s.getQuality()).append("\n");
            builder.append("\tSize: ").append(String.format("0x%08X", s.getShaderDataSize())).append(" bytes\n");
        });

        final String content = builder.toString();
        Platform.runLater(() -> textArea.setText(content));
    }


    private void export(Object dummy) {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Export to...");
        final File file = dirChooser.showDialog(displayNode.getScene().getWindow());
        final ShaderPack pack = this.shaderPack;
        if (file != null) {
            Path root = file.toPath().toAbsolutePath();
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    updateMessage("Exporting shaders");

                    List<Shader> shaders = pack.getShaders();
                    for (int i = 0, shadersSize = shaders.size(); i < shadersSize; i++) {
                        Shader shader = shaders.get(i);
                        Path outDir = root.resolve(String.format("%d", shader.getQuality()));
                        Files.createDirectories(outDir);
                        Path outFile = outDir.resolve(shader.getName());
                        Files.write(outFile, shader.getShaderData(), CREATE, TRUNCATE_EXISTING);
                        updateProgress(i, shadersSize);
                    }

                    TimeUnit.SECONDS.sleep(1);
                    updateMessage("Done");
                    return null;
                }
            };
            mainUiController.showLoadingPopup(task);
            DNPTApplication.EXECUTOR_SERVICE.submit(task);
        }
    }

    @Override
    public void setMainUiController(DNPTUIController uiController) {
        mainUiController = uiController;
    }

    @Override
    public void reset() {
        shaderPack = null;
        super.reset();
    }
}
