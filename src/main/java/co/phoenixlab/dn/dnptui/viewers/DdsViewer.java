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
import co.phoenixlab.dn.dnptui.PakTreeEntry;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.util.converter.FormatStringConverter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;

public class DdsViewer implements Viewer {

    private Node displayNode;

    private Dds currentDds;
    private Image image;
    private final DdsImageDecoder decoder;
    @FXML
    private ImageView imageView;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private Spinner<Integer> zoomSpinner;
    @FXML
    private IntegerSpinnerValueFactory zoomValueFactory;

    private final DoubleProperty zoomProperty;
    private final IntegerProperty imageWidthProperty;
    private byte[] pngData;
    @FXML
    private BorderPane displayPane;
    @FXML
    private Label sizeLbl;

    private String fileName;
    @FXML
    private Button exportBtn;
    @FXML
    private Label zoomLbl;

    public DdsViewer() {
        decoder = new DdsImageDecoder();
        zoomProperty = new SimpleDoubleProperty(this, "zoom", 1D);
        imageWidthProperty = new SimpleIntegerProperty(this, "imageWidth");
    }

    @Override
    public void init() {
        zoomValueFactory = new IntegerSpinnerValueFactory(25, 400, 100);
        zoomValueFactory.setConverter(new FormatStringConverter<>(new DecimalFormat("#'%'")));
        zoomValueFactory.setValue(100);
        zoomValueFactory.setAmountToStepBy(25);
        zoomSpinner.setValueFactory(zoomValueFactory);
        //  Text
        zoomLbl.setText("Zoom:");
        exportBtn.setText("Export as PNG");

        //  Bindings
        zoomValueFactory.valueProperty().addListener((observable, oldValue, newValue) -> {
            zoomProperty.set(newValue.doubleValue() / 100D);
        });
        imageView.fitWidthProperty().bind(Bindings.multiply(imageWidthProperty, zoomProperty));

        displayNode = displayPane;
    }

    @FXML
    private void exportImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(fileName.replace(".dds", ".png"));
        fileChooser.setTitle("Export as...");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image", "*.png"));
        File file = fileChooser.showSaveDialog(displayNode.getScene().getWindow());
        if (file != null) {
            //  TODO Do this async
            try {
                Files.write(file.toPath(), pngData, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Node getDisplayNode() {
        return displayNode;
    }

    @Override
    public void parse(ByteBuffer byteBuffer) {
        currentDds = new Dds();
        try {
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            currentDds.read(byteBuffer);
            pngData = decoder.convertToPNG(currentDds);
            image = new Image(new ByteArrayInputStream(pngData));
            imageWidthProperty.bind(image.widthProperty());
            displayPane.setCenter(scrollPane);
            sizeLbl.setText(String.format("%dx%d", (int) image.getWidth(), (int) image.getHeight()));
        } catch (Exception e) {
            Label label = new Label("Error decoding DDS file:\n" + e.getMessage());
            label.setAlignment(Pos.CENTER);
            label.setTextAlignment(TextAlignment.CENTER);
            label.setPrefWidth(Double.MAX_VALUE);
            displayPane.setCenter(label);
            return;
        }
        layout();
    }

    private void layout() {
        imageView.setImage(image);
    }


    @Override
    public void onLoadStart(TreeItem<PakTreeEntry> pakTreeEntry) {
        fileName = pakTreeEntry.getValue().name;
    }

    @Override
    public void reset() {
        currentDds = null;
        image.cancel();
        imageWidthProperty.unbind();
        image = null;
        displayNode = null;
    }
}
