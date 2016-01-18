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

package co.phoenixlab.dn.dnptui.viewers;

import co.phoenixlab.dn.dnptui.DNPTApplication;
import co.phoenixlab.dn.dnptui.DNPTUIController;
import co.phoenixlab.dn.dnptui.PakTreeEntry;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.util.converter.FormatStringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public class ImageViewer implements Viewer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageViewer.class);

    private Node displayNode;

    protected Image image;
    @FXML
    protected ImageView imageView;
    @FXML
    protected ScrollPane scrollPane;
    @FXML
    protected Spinner<Integer> zoomSpinner;
    @FXML
    protected SpinnerValueFactory.IntegerSpinnerValueFactory zoomValueFactory;

    protected final DoubleProperty zoomProperty;
    protected final IntegerProperty imageWidthProperty;
    protected byte[] imageData;
    @FXML
    protected BorderPane displayPane;
    @FXML
    protected Label sizeLbl;

    protected String fileName;
    @FXML
    protected Button exportBtn;
    @FXML
    protected Label zoomLbl;
    @FXML
    protected HBox toolbar;
    protected DNPTUIController mainUiController;


    public ImageViewer() {
        zoomProperty = new SimpleDoubleProperty(this, "zoom", 1D);
        imageWidthProperty = new SimpleIntegerProperty(this, "imageWidth");
    }

    @Override
    public void init() {
        zoomValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(25, 400, 100);
        zoomValueFactory.setConverter(new FormatStringConverter<>(new DecimalFormat("#'%'")));
        zoomValueFactory.setValue(100);
        zoomValueFactory.setAmountToStepBy(25);
        zoomSpinner.setValueFactory(zoomValueFactory);
        //  Text
        zoomLbl.setText("Zoom:");
        exportBtn.setText("Export");

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
        fileChooser.setInitialFileName(getDefaultFileName());
        fileChooser.setTitle("Export as...");
        fileChooser.getExtensionFilters().add(getExtensionFilter());
        final File file = fileChooser.showSaveDialog(displayNode.getScene().getWindow());
        if (file != null) {
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    updateMessage("Exporting image");
                    Files.write(file.toPath(), getImageData(), CREATE, TRUNCATE_EXISTING);
                    //  Sleep for a second because people aren't noticing that its doing anything even with
                    //  the fade out "Done" at the end >.>
                    TimeUnit.SECONDS.sleep(1);
                    updateMessage("Done");
                    return null;
                }
            };
            mainUiController.showLoadingPopup(task);
            DNPTApplication.EXECUTOR_SERVICE.submit(task);
        }
    }

    protected byte[] getImageData() {
        return imageData;
    }

    protected FileChooser.ExtensionFilter getExtensionFilter() {
        String[] split = fileName.split("\\.");
        String ext = split[Math.max(1, split.length - 1)];
        return new FileChooser.ExtensionFilter(ext.toUpperCase()+ " Image", "*." + ext);
    }

    protected String getDefaultFileName() {
        return fileName;
    }

    @Override
    public Node getDisplayNode() {
        return displayNode;
    }

    @Override
    public void parse(ByteBuffer byteBuffer) {
        try {
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            image = new Image(new ByteArrayInputStream(decodeImageData(byteBuffer)));
            imageWidthProperty.bind(image.widthProperty());
            displayPane.setCenter(scrollPane);
            toolbar.setDisable(false);
            Platform.runLater(() -> {
                if (sizeLbl != null && image != null) {
                    sizeLbl.setText(String.format("%dx%d", (int) image.getWidth(), (int) image.getHeight()));
                }
            });
        } catch (Exception e) {
            LOGGER.warn("Error decoding image file \"" + fileName + "\"", e);
            String exceptionMsg = e.getMessage();
            if (exceptionMsg == null) {
                exceptionMsg = e.getClass().getSimpleName();
            }
            Label label = new Label("Error decoding image file:\n" + exceptionMsg);
            label.setAlignment(Pos.CENTER);
            label.setTextAlignment(TextAlignment.CENTER);
            label.setPrefWidth(Double.MAX_VALUE);

            displayPane.setCenter(label);
            toolbar.setDisable(true);
            return;
        }
        layout();
    }

    protected byte[] decodeImageData(ByteBuffer byteBuffer) throws Exception {
        imageData = new byte[byteBuffer.remaining()];
        byteBuffer.get(imageData);
        return imageData;
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
        if (image != null) {
            image.cancel();
            image = null;
        }
        imageWidthProperty.unbind();
    }

    @Override
    public void setMainUiController(DNPTUIController uiController) {
        mainUiController = uiController;
    }
}

