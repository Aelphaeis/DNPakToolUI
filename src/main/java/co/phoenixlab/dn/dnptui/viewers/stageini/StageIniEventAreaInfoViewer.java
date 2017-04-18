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

import co.phoenixlab.dds.Dds;
import co.phoenixlab.dds.DdsImageDecoder;
import co.phoenixlab.dn.dnptui.PakTreeEntry;
import co.phoenixlab.dn.dnptui.viewers.Viewer;
import co.phoenixlab.dn.dnptui.viewers.stageini.struct.GridInfo;
import co.phoenixlab.dn.pak.FileInfo;
import co.phoenixlab.dn.subfile.stage.eventarea.EventArea;
import co.phoenixlab.dn.subfile.stage.eventarea.EventAreaGroup;
import co.phoenixlab.dn.subfile.stage.eventarea.StageEventAreas;
import co.phoenixlab.dn.subfile.stage.eventarea.StageEventAreasTestReader;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.InflaterOutputStream;

public class StageIniEventAreaInfoViewer implements Viewer {

    public static final int DEFAULT_MAX_SIZE = 1024 * 1024;

    private long maxDisplaySize;

    private Node displayNode;

    @FXML
    protected BorderPane displayPane;

    @FXML
    protected CheckBox lineWrapToggleChkBox;

    @FXML
    protected HBox toolbar;

    @FXML
    protected TextArea textArea;

    private String data;

    private Charset charset;

    @FXML
    protected VBox listVbox;

    @FXML
    protected AnchorPane canvas;

    private Map<String, Entry> entries;
    private PakTreeEntry gridInfoEntry;
    private GridInfo gridInfo;
    private PakTreeEntry mapEntry;
    private Image mapImage;

    public StageIniEventAreaInfoViewer() {
        this(StandardCharsets.UTF_8);
    }

    public StageIniEventAreaInfoViewer(Charset charset) {
        maxDisplaySize = Long.getLong("co.phoenixlab.dn.dnptui.text.maxSize", DEFAULT_MAX_SIZE);
        this.charset = charset;
        entries = new HashMap<>();
    }

    @Override
    public void init() {
        displayNode = displayPane;
        textArea.wrapTextProperty().bind(lineWrapToggleChkBox.selectedProperty());
    }

    @Override
    public Node getDisplayNode() {
        return displayNode;
    }

    @Override
    public void onLoadStart(TreeItem<PakTreeEntry> treeItem) {
        //  Dispatch a load for gridinfo.ini
        ObservableList<TreeItem<PakTreeEntry>> children = treeItem.getParent().getParent().getChildren();
        for (TreeItem<PakTreeEntry> child : children) {
            PakTreeEntry value = child.getValue();
            if (value.name.equals("gridinfo.ini")) {
                gridInfoEntry = value;
            } else if (value.name.equals(treeItem.getParent().getParent().getValue().name + ".dds")) {
                mapEntry = value;
            }
        }
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

    private Image loadMap() throws Exception {
        if (mapEntry == null) {
            throw new NoSuchFileException("Can't load map");
        }
        FileInfo fileInfo = mapEntry.entry.getFileInfo();
        System.out.println(fileInfo.getFullPath());
        ByteArrayOutputStream bao = new ByteArrayOutputStream((int) Math.max(
            fileInfo.getDecompressedSize(),
            fileInfo.getCompressedSize()));
        OutputStream out = new InflaterOutputStream(bao);
        WritableByteChannel writableByteChannel = Channels.newChannel(out);
        mapEntry.parent.openIfNotOpen();
        mapEntry.parent.transferTo(fileInfo, writableByteChannel);

        out.flush();
        byte[] decompressedBytes = bao.toByteArray();
        ByteBuffer buffer = ByteBuffer.wrap(decompressedBytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        Dds dds = new Dds();
        dds.read(buffer);
        byte[] data = new DdsImageDecoder().convertToPNG(dds);
        return new Image(new ByteArrayInputStream(data));
    }

    @Override
    public void reset() {
        //  Default
        maxDisplaySize = Long.getLong("co.phoenixlab.dn.dnptui.text.maxSize", DEFAULT_MAX_SIZE);
        data = null;
        textArea.setText(null);
    }

    protected final String indentSpaces(String s, int level) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < level; i++) {
            builder.append(' ');
        }
        return s.replace("\n", "\n" + builder.toString());
    }

    protected final String indentTabs(String s, int level) {
        return indentTabs(s, level, false);
    }

    protected final String indentTabs(String s, int level, boolean leading) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < level; i++) {
            builder.append('\t');
        }
        String s1 = builder.toString();
        return (leading ? s1 : "") + s.replace("\n", "\n" + s1);
    }


    @Override
    public void parse(ByteBuffer byteBuffer) {
        try {
            gridInfo = loadGridInfo();
            mapImage = loadMap();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(baos);
        StageEventAreasTestReader reader = new StageEventAreasTestReader(printStream);
        StageEventAreas areas = reader.read(byteBuffer);
        String content = new String(baos.toByteArray(), StandardCharsets.UTF_8);

        entries.clear();
        List<Node> nodes = new ArrayList<>();
        for (EventAreaGroup group : areas.getGroups()) {
            if (group.getNumAreas() > 0) {
                List<Entry> eList = group.getAreas().stream().map(Entry::new).collect(Collectors.toList());
                eList.forEach(e -> entries.put(e.area.getAreaName(), e));
                ListView<Entry> view = new ListView<>(FXCollections.observableList(eList));
                view.setCellFactory(param -> {
                    CheckBoxListCell<Entry> cell = new CheckBoxListCell<Entry>() {
                        @Override
                        public void updateItem(Entry item, boolean empty) {
                            super.updateItem(item, empty);
                            if (item != null) {
                                super.setText(item.area.getEventAreaId() + ": " + item.area.getAreaName());
                            }
                        }
                    };
                    cell.setSelectedStateCallback(param1 -> param1.selected);
                    return cell;
                });
                TitledPane titledPane = new TitledPane("Group " + group.getGroupId(), view);
                nodes.add(titledPane);
            }
        }

        Platform.runLater(() -> {
            textArea.setText(content);
            listVbox.getChildren().clear();
            listVbox.getChildren().addAll(nodes);
            redrawCanvas();
        });
    }

    private void onCheckboxChanged(EventArea area, boolean checked) {
        redrawCanvas();
    }

    private void redrawCanvas() {
        canvas.getChildren().clear();
        ImageView imageView = new ImageView(mapImage);
        imageView.setFitWidth(canvas.getWidth());
        imageView.setFitHeight(canvas.getHeight());
        AnchorPane.setLeftAnchor(imageView, 0D);
        AnchorPane.setBottomAnchor(imageView, 0D);
        canvas.getChildren().add(imageView);
        entries.values().stream().filter(entry -> entry.selected.get()).forEach(e -> {
            int width = gridInfo.getGridWidth() * 100;
            int canvasWidth = (int) canvas.getWidth();
            int xStart = rescale(width, canvasWidth, e.area.getBbStartX());
            int zStart = rescale(width, canvasWidth, e.area.getBbStartZ());
            int xEnd = rescale(width, canvasWidth, e.area.getBbEndX());
            int zEnd = rescale(width, canvasWidth, e.area.getBbEndZ());
            Rectangle rectangle = new Rectangle(0, 0, Math.abs(xEnd - xStart), Math.abs(zEnd - zStart));
            rectangle.setFill(Color.rgb(0, 255, 0, 0.0675));
            rectangle.setStroke(Color.GREEN);
            rectangle.setStrokeWidth(1);
            rectangle.setRotate(e.area.getBbRot());
            AnchorPane.setLeftAnchor(rectangle, (double) xStart);
            AnchorPane.setBottomAnchor(rectangle, (double) zStart);
            canvas.getChildren().add(rectangle);
        });

    }

    private int rescale(int len, int cLen, float pos) {
        return (int) (pos / len * cLen);
    }

    class Entry {
        final SimpleBooleanProperty selected;
        final EventArea area;

        Entry(EventArea area) {
            this.selected = new SimpleBooleanProperty(true);
            selected.addListener((ov, oldVal, newVal) -> {
                if (oldVal != newVal) {
                    onCheckboxChanged(area, newVal);
                }
            });
            this.area = area;
        }
    }
}
