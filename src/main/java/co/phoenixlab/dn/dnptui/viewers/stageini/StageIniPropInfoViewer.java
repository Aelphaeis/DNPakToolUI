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

import co.phoenixlab.dn.dnptui.DNPTUIController;
import co.phoenixlab.dn.dnptui.viewers.TextViewer;
import co.phoenixlab.dn.dnptui.viewers.stageini.struct.Prop;
import co.phoenixlab.dn.dnptui.viewers.stageini.struct.PropInfo;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.stage.DirectoryChooser;
import org.joml.Vector3f;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;

public class StageIniPropInfoViewer extends TextViewer {

    private PropInfo propInfo;
    private DNPTUIController uiController;

    public StageIniPropInfoViewer() {
    }

    @Override
    public void init() {
        super.init();
        Button button = new Button("Export for 3ds max");
        button.setOnAction(this::handleExport);
        super.toolbar.getChildren().add(button);
    }

    @Override
    public void parse(ByteBuffer byteBuffer) {
        propInfo = new PropInfo(byteBuffer);
        final String content = propInfo.toString();
        Platform.runLater(() -> {
            textArea.setText(content);
        });
    }

    private void handleExport(ActionEvent e) {
        //  Output file
        //  Show directory chooser
        DirectoryChooser exportDirPathChooser = new DirectoryChooser();
        exportDirPathChooser.setTitle("Export into...");
        exportDirPathChooser.setInitialDirectory(uiController.getLastOpenedDir().toFile());
        File exportDir = exportDirPathChooser.showDialog(uiController.getStage());
        //  User hit cancel
        if (exportDir == null) {
            return;
        }
        Path outDir = exportDir.toPath().toAbsolutePath();
        //  Build a list of skn files we're using
        Set<String> skns = new HashSet<>();
        if (!writePropPlacement(outDir, skns)) {
            return;
        }
        //  TODO Export models, etc
    }

    private boolean writePropPlacement(Path outDir, Set<String> skns) {
        StringBuilder placementParamsBuilder = new StringBuilder();
        PropInfo localRef = propInfo;
        for (Prop prop : localRef.getEntries()) {
            switch (prop.getType()) {
                case ANIM:
                case DEFAULT:
                case TAGGED:
                    if (anyMatch(prop.getSknFile(), "camera.skn", "collisionbox.skn", "light.skn")) {
                        break;
                    }
                    placementParamsBuilder.append(prop.getSknFile().replace(".skn", ".obj")).append('|')
                            .append(vec3ToStr(prop.getPosition())).append('|')
                            .append(vec3ToStr(prop.getRotation())).append('|')
                            .append(vec3ToStr(prop.getScale())).append('\n');
                    skns.add(prop.getSknFile());
                    break;
            }
        }
        Path propPlacementFile = outDir.resolve("placement.csv");
        try {
            Files.write(propPlacementFile, placementParamsBuilder.toString().getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e1) {
            DNPTUIController.LOGGER.warn("Failed to write prop placement file", e1);
            return false;
        }
        return true;
    }

    private String vec3ToStr(Vector3f vec) {
        return String.format("%f|%f|%f", vec.x, vec.y, vec.z);
    }

    private boolean anyMatch(String s, String... any) {
        for (String a : any) {
            if (s.equalsIgnoreCase(a)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setMainUiController(DNPTUIController uiController) {
        this.uiController = uiController;
    }
}
