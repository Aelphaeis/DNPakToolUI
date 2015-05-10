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

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DNPTUIController {

    public static final int MIN_WIDTH = 550;
    public static final int MIN_HEIGHT = 400;
    private DNPTApplication application;
    private Stage stage;
    private Scene scene;

    @FXML private BorderPane root;
    @FXML private AnchorPane topBar;
    @FXML private VBox bottomDrag;
    @FXML private HBox rightDrag;
    @FXML private HBox leftDrag;
    @FXML private Label titleLbl;
    @FXML private Button maxRestoreBtn;
    @FXML private Button findBtn;
    @FXML private Button exportBtn;
    @FXML private Button exportFolderBtn;
    @FXML private Button closePakBtn;
    @FXML private ScrollPane navScrollPane;
    @FXML private SplitPane splitPane;
    @FXML private TreeView<PakTreeEntry> treeView;

    private Image navFolderIcon;

    private double xOff;
    private double yOff;

    private final BooleanProperty noPakLoadedProperty;
    private final StringProperty openedFilePathProperty;
    private final BooleanProperty maximizedProperty;

    private Path lastOpenedDir;

    private PakHandler handler;

    public DNPTUIController() {
        noPakLoadedProperty = new SimpleBooleanProperty(this, "noPakLoaded", true);
        openedFilePathProperty = new SimpleStringProperty(this, "openedFilePath", "No File");
        maximizedProperty = new SimpleBooleanProperty(this, "maximized", false);
        lastOpenedDir = Paths.get(System.getProperty("user.dir"));
    }

    public void setStageSceneApp(Stage stage, Scene scene, DNPTApplication application) {
        this.stage = stage;
        this.scene = scene;
        this.application = application;
        scene.getRoot().setOpacity(0D);
    }

    public void init() {
        findBtn.disableProperty().bind(noPakLoadedProperty);
        exportBtn.disableProperty().bind(noPakLoadedProperty);
        exportFolderBtn.disableProperty().bind(noPakLoadedProperty);
        closePakBtn.disableProperty().bind(noPakLoadedProperty);
        titleLbl.textProperty().bind(Bindings.concat("DN Pak Tool - ").concat(openedFilePathProperty));
        maximizedProperty.bind(stage.maximizedProperty());
        maximizedProperty.addListener((observable, oldValue, newValue) -> {
            maxRestoreBtn.setId(newValue ? "window-restore-button" : null);
            if (newValue) {
                leftDrag.setId(null);
                rightDrag.setId(null);
                bottomDrag.setId(null);
                topBar.setId(null);
            } else {
                leftDrag.setId("side-drag");
                rightDrag.setId("side-drag");
                bottomDrag.setId("bottom-drag");
                topBar.setId("top-drag");
            }
        });
        try (InputStream inputStream =
                     getClass().getResourceAsStream("/co/phoenixlab/dn/dnptui/assets/nav/folder.png")) {
            navFolderIcon = new Image(inputStream);
        } catch (IOException e) {
            //  TODO Exception handling
        }
        treeView.setCellFactory(param -> new TreeCell<PakTreeEntry>() {
            @Override
            protected void updateItem(PakTreeEntry item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    ImageView imageView = new ImageView(navFolderIcon);
                    if (item.entry != null) {
                        setText(item.name);
                        //  TODO Icon
                        setGraphic(null);
                    } else {
                        setText(item.name + "\\");
                        setGraphic(imageView);
                    }
                }
            }
        });

        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        Platform.runLater(() ->
                navScrollPane.prefViewportHeightProperty().
                        bind(root.heightProperty().subtract(126)));

        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.125D), scene.getRoot());
        fadeTransition.setFromValue(0D);
        fadeTransition.setToValue(1D);
        fadeTransition.playFromStart();
    }

    @FXML
    private void showClosePrompt(ActionEvent event) {
        Dialog<ButtonType> exitDialog = new Dialog<>();
        DialogPane dialogPane = new DialogPane();
        dialogPane.getStylesheets().add(getClass().
                getResource("/co/phoenixlab/dn/dnptui/assets/stylesheet.css").toExternalForm());
        dialogPane.getStyleClass().add("exit-dialog");
        Label label = new Label("Are you sure you want to quit?");
        label.getStyleClass().add("exit-dialog-lbl");
        dialogPane.setContent(label);
        dialogPane.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
        exitDialog.initStyle(StageStyle.TRANSPARENT);
        exitDialog.setDialogPane(dialogPane);
        exitDialog.setTitle("DN Pak Tool");
        exitDialog.showAndWait().
                filter(b -> ButtonType.YES == b).
                ifPresent(this::quit);
    }

    private void quit(Object dummy) {
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.5D), scene.getRoot());
        fadeTransition.setFromValue(1D);
        fadeTransition.setToValue(0D);
        fadeTransition.setOnFinished(e -> application.stop());
        fadeTransition.playFromStart();
    }

    @FXML
    private void iconify(ActionEvent event) {
        stage.setIconified(true);
    }

    @FXML
    private void toggleMax(ActionEvent event) {
        boolean old = stage.isMaximized();
        stage.setMaximized(!old);
        root.requestLayout();
    }

    @FXML
    private void openPak(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(lastOpenedDir.toFile());
        fileChooser.setTitle("Choose a Pak file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Dragon Nest Package File", "*.pak"));
        Optional.ofNullable(fileChooser.showOpenDialog(stage)).
                map(File::toPath).
                ifPresent(this::loadPak);
    }

    private void loadPak(Path path) {
        lastOpenedDir = path.getParent();
        openedFilePathProperty.set(path.toString());
        PakLoadTask task = new PakLoadTask(path, this::onLoadFinished);
        connectTaskToUI(task);
        DNPTApplication.EXECUTOR_SERVICE.submit(task);
    }

    @FXML
    private void openVirtualPak(ActionEvent event) {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setInitialDirectory(lastOpenedDir.toFile());
        dirChooser.setTitle("Choose a Pak file");
        Optional.ofNullable(dirChooser.showDialog(stage)).
                map(File::toPath).
                ifPresent(this::loadVirtualPak);
    }

    private void loadVirtualPak(Path dir) {
        lastOpenedDir = dir;
        openedFilePathProperty.set(dir.toString() + " (Virtual)");
        //  Build path list
        List<Path> paths;
        BiPredicate<Path, BasicFileAttributes> test = (p, a) -> p.getFileName().toString().endsWith(".pak");
        test = test.and((p, a) -> !a.isDirectory());
        try (Stream<Path> matches = Files.find(dir, 1, test)) {
            paths = matches.collect(Collectors.toList());
        } catch (IOException e) {
            //  Err
            e.printStackTrace();
            return;
        }
        PakLoadTask task = new PakLoadTask(paths, this::onLoadFinished);
        connectTaskToUI(task);
        DNPTApplication.EXECUTOR_SERVICE.submit(task);
    }

    private void connectTaskToUI(PakLoadTask task) {
        //  Show loading indicator

        //  Wire up properties

        //  Dispatch job
    }

    public void onLoadFinished(PakHandler handler) {
        this.handler = handler;
        noPakLoadedProperty.set(false);
        this.handler.populate(treeView);
    }

    @FXML
    private void find(ActionEvent event) {

    }

    @FXML
    private void exportFile(ActionEvent event) {

    }

    @FXML
    private void exportFolder(ActionEvent event) {

    }

    @FXML
    private void closePak(ActionEvent event) {
        closePak();
    }

    public void closePak() {
        noPakLoadedProperty.set(true);
        openedFilePathProperty.setValue("No File");
        treeView.setRoot(null);
        if (this.handler != null) {
            this.handler.unload();
        }
    }

    @FXML
    private void windowDragging(MouseEvent event) {
        if (!maximizedProperty.get() && event.getButton() == MouseButton.PRIMARY) {
            stage.setX(event.getScreenX() - xOff);
            stage.setY(event.getScreenY() - yOff);
        }
    }

    @FXML
    private void windowDragStart(MouseEvent event) {
        if (!maximizedProperty.get() && event.getButton() == MouseButton.PRIMARY) {
            xOff = event.getSceneX();
            yOff = event.getSceneY();
        }
    }

    @FXML
    private void windowVerticalResize(MouseEvent event) {
        if (!maximizedProperty.get() && event.getButton() == MouseButton.PRIMARY) {
            double y = event.getScreenY() - stage.getY();
            if (y > MIN_HEIGHT) {
                stage.setHeight(y);
            }
        }
    }

    @FXML
    private void windowHorizontalResize(MouseEvent event) {
        boolean left = false;
        if (event.getSource() == leftDrag) {
            left = true;
        }
        if (!maximizedProperty.get() && event.getButton() == MouseButton.PRIMARY) {
            double x = event.getScreenX() - stage.getX();
            if (left) {
                double newWidth = -x + stage.getWidth();
                if (newWidth > MIN_WIDTH) {
                    stage.setX(stage.getX() + x);
                    stage.setWidth(newWidth);
                }
            } else {
                if (x > MIN_WIDTH) {
                    stage.setWidth(x);
                }
            }
        }
    }
}
