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
import co.phoenixlab.dds.InvalidDdsException;
import co.phoenixlab.dn.dnptui.PakTreeEntry;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.TextAlignment;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DdsViewer implements Viewer {

    private Node displayNode;

    private Dds currentDds;
    private Image image;
    private final DdsImageDecoder decoder;

    private ImageView imageView;
    private ScrollPane scrollPane;
    private Spinner<Integer> zoomSpinner;
    private IntegerSpinnerValueFactory zoomValueFactory;

    private final DoubleProperty zoomProperty;
    private final IntegerProperty imageWidthProperty;
    private byte[] pngData;

    public DdsViewer() {
        decoder = new DdsImageDecoder();
        zoomProperty = new SimpleDoubleProperty(this, "zoom", 1D);
        imageWidthProperty = new SimpleIntegerProperty(this, "imageWidth");
        preLayout();
    }

    private void preLayout() {
        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        scrollPane = new ScrollPane(imageView);
        zoomValueFactory = new IntegerSpinnerValueFactory(25, 400, 100);
        zoomValueFactory.setValue(100);
        zoomValueFactory.setAmountToStepBy(25);
        zoomSpinner = new Spinner<>(zoomValueFactory);
        zoomSpinner.setEditable(false);

        BorderPane borderPane = new BorderPane(scrollPane, zoomSpinner, null, null, null);

        //  Bindings
        zoomValueFactory.valueProperty().addListener((observable, oldValue, newValue) -> {
            zoomProperty.set(newValue.doubleValue() / 100D);
        });
        imageView.fitWidthProperty().bind(Bindings.multiply(imageWidthProperty, zoomProperty));

        displayNode = borderPane;
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
        } catch (InvalidDdsException e) {
            Label label = new Label("Error decoding DDS file:\n" + e.getMessage());
            label.setAlignment(Pos.CENTER);
            label.setTextAlignment(TextAlignment.CENTER);
            label.setPrefWidth(Double.MAX_VALUE);
            displayNode = label;
            return;
        }
        layout();
    }

    private void layout() {
        imageView.setImage(image);
    }



    @Override
    public void onLoadStart(TreeItem<PakTreeEntry> pakTreeEntry) {

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
