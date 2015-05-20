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

import co.phoenixlab.dn.dnptui.fx.FadeTransitionUtil;
import co.phoenixlab.dn.dnptui.fx.SpriteAnimation;
import co.phoenixlab.dn.pak.DNPakTool;
import javafx.animation.Animation;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
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
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.*;
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

    /**
     * Minimum screen width
     */
    public static final int MIN_WIDTH = 550;
    /**
     * Minimum screen height
     */
    public static final int MIN_HEIGHT = 400;
    /**
     * Common stylesheet for application
     */
    private static final String STYLESHEET = DNPakTool.class.
            getResource("/co/phoenixlab/dn/dnptui/assets/stylesheet.css").toExternalForm();
    /**
     * Application instance
     */
    private DNPTApplication application;
    /**
     * Primary stage
     */
    private Stage stage;
    /**
     * Primary scene
     */
    private Scene scene;

    /**
     * The type of item that's selected in the navigation pane
     */
    enum SelectionType {
        FOLDER,
        FILE,
        NONE
    }

    //  FXML elements
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
    /**
     * The folder icon used in the navigation pane. Shared instance
     */
    private Image navFolderIcon;
    /**
     * X offset for window repositioning
     */
    private double xOff;
    /**
     * Y offset for window repositioning
     */
    private double yOff;
    /**
     * Property tracking whether or not a pak is currently <b>not</b> loaded
     */
    private final BooleanProperty noPakLoadedProperty;
    /**
     * Property tracking the type of item selected in the navigation pane
     */
    private final ObjectProperty<SelectionType> selectionTypeProperty;
    /**
     * Property tracking the selected item in the navigation pane
     */
    private final ObjectProperty<TreeItem<PakTreeEntry>> selectedProperty;
    /**
     * Property tracking the file path to the currently opened pak/virtal pak
     */
    private final StringProperty openedFilePathProperty;
    /**
     * Property tracking whether or not the application window is maximized
     */
    private final BooleanProperty maximizedProperty;
    /**
     * The last directory that was opened
     */
    private Path lastOpenedDir;
    /**
     * The active PakHandler, or null if no pak/virtual pak is loaded
     */
    private PakHandler handler;

    public DNPTUIController() {
        noPakLoadedProperty = new SimpleBooleanProperty(this, "noPakLoaded", true);
        selectionTypeProperty = new SimpleObjectProperty<>(this, "selectionType", SelectionType.NONE);
        selectedProperty = new SimpleObjectProperty<>(this, "selected", null);
        openedFilePathProperty = new SimpleStringProperty(this, "openedFilePath", "No File");
        maximizedProperty = new SimpleBooleanProperty(this, "maximized", false);
        lastOpenedDir = Paths.get(System.getProperty("user.dir"));
    }

    /**
     * Sets the primary stage, scene, and application instance for this controller.
     *
     * @param stage       The primary stage to use
     * @param scene       The primary scene to use
     * @param application The application instance
     */
    public void setStageSceneApp(Stage stage, Scene scene, DNPTApplication application) {
        this.stage = stage;
        this.scene = scene;
        this.application = application;
        scene.getRoot().setOpacity(0D);
    }

    /**
     * Initializes the controller after the stage has been shown.
     */
    public void init() {
        //  Property bindings
        //  Disable the find button when no pak is loaded
        findBtn.disableProperty().bind(noPakLoadedProperty);
        //  Disable the export file button when no pak is loaded or the selection is not a file
        exportBtn.disableProperty().bind(noPakLoadedProperty.
                or(selectionTypeProperty.isNotEqualTo(SelectionType.FILE)));
        //  Disable the export folder button when no pak is loaded or the selection is not a directory
        exportFolderBtn.disableProperty().bind(noPakLoadedProperty.
                or(selectionTypeProperty.isNotEqualTo(SelectionType.FOLDER)));
        //  Disable the close pak button when no pak is loaded
        closePakBtn.disableProperty().bind(noPakLoadedProperty);
        //  Bind the window title to display the currently loaded pak/virtual pak
        titleLbl.textProperty().bind(Bindings.concat("DN Pak Tool - ").concat(openedFilePathProperty));
        //  Bind the maximized property to the stage's maximized property
        maximizedProperty.bind(stage.maximizedProperty());
        //  Toggle the max/restore button icon and enable/disable the window resize handles on maximize change
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
        //  Load the shared folder icon
        try (InputStream inputStream =
                     getClass().getResourceAsStream("/co/phoenixlab/dn/dnptui/assets/nav/folder.png")) {
            navFolderIcon = new Image(inputStream);
        } catch (IOException e) {
            //  TODO Exception handling
        }
        //  Create the navigation pane's cell factory
        treeView.setCellFactory(param -> new TreeCell<PakTreeEntry>() {
            @Override
            protected void updateItem(PakTreeEntry item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    //  Item is empty - clear children
                    setText(null);
                    setGraphic(null);
                } else {
                    //  Nodes
                    if (item.entry != null) {
                        //  File/leaf nodes
                        setText(item.name);
                        //  TODO Icon
                        setGraphic(null);
                    } else {
                        //  Directory nodes
                        setText(item.name);
                        //  ImageView instances cannot be shared between cells
                        setGraphic(new ImageView(navFolderIcon));
                    }
                }
            }

            @Override
            public void updateSelected(boolean selected) {
                super.updateSelected(selected);
                if (selected) {
                    //  Update the selection type and selection properties
                    selectionTypeProperty.set(getItem().entry == null ? SelectionType.FOLDER : SelectionType.FILE);
                    selectedProperty.set(getTreeItem());
                }
            }
        });
        treeView.setShowRoot(false);
        //  Enforce minimum window dimensions
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        //  Ensure our navpane scrollpane is the right size always
        Platform.runLater(() ->
                navScrollPane.prefViewportHeightProperty().
                        bind(root.heightProperty().subtract(126)));

        //  Fade the window in
        FadeTransitionUtil.fadeTransitionIn(Duration.seconds(0.25D), scene.getRoot()).
                play();
    }

    /**
     * EventHandler for the close button. Shows a close prompt.
     *
     * @param event The button click event
     */
    @FXML
    private void showClosePrompt(ActionEvent event) {
        //  Create popup window
        Stage closeStage = new Stage(StageStyle.TRANSPARENT);
        closeStage.initOwner(stage);
        closeStage.initModality(Modality.APPLICATION_MODAL);
        closeStage.setTitle("DN Pak Tool");
        VBox root = new VBox(14);
        root.setAlignment(Pos.CENTER);
        Scene closeScene = new Scene(root, 200, 100, Color.TRANSPARENT);
        closeScene.getStylesheets().add(STYLESHEET);
        root.getStyleClass().add("dialog");
        closeStage.setScene(closeScene);
        Label promptLbl = new Label("Are you sure you want to quit?");
        HBox buttonBar = new HBox(20);
        buttonBar.setAlignment(Pos.CENTER);
        buttonBar.setMaxWidth(Double.MAX_VALUE);
        int buttonWidth = 60;
        Button yesButton = new Button("Yes");
        Button noButton = new Button("No");
        yesButton.setOnAction(ae -> {
            FadeTransitionUtil.fadeTransitionOut(Duration.seconds(0.125D), root).
                    play();
            quit(null);
        });
        noButton.setOnAction(ae -> {
            FadeTransitionUtil.fadeTransitionOut(Duration.seconds(0.125D), root, closeStage::close).
                    play();
        });
        yesButton.setPrefWidth(buttonWidth);
        noButton.setPrefWidth(buttonWidth);
        buttonBar.getChildren().addAll(yesButton, noButton);
        root.getChildren().addAll(promptLbl, buttonBar);

        closeScene.getRoot().setOpacity(0D);
        closeStage.show();
        closeStage.setX(stage.getX() + stage.getWidth() / 2 - closeStage.getWidth() / 2);
        closeStage.setY(stage.getY() + stage.getHeight() / 2 - closeStage.getHeight() / 2);
        FadeTransitionUtil.fadeTransitionIn(Duration.seconds(0.25D), closeScene.getRoot()).
                play();
    }

    /**
     * Quits the application
     *
     * @param dummy Any value (including null). Only present to allow for method reference in lambda
     */
    private void quit(Object dummy) {
        FadeTransitionUtil.fadeTransitionOut(Duration.seconds(0.5D), scene.getRoot(), application::stop).
                play();
    }

    /**
     * EventHandler for the iconify/minimize button. Iconifies/minimizes the application window.
     *
     * @param event The button click event
     */
    @FXML
    private void iconify(ActionEvent event) {
        stage.setIconified(true);
    }

    /**
     * EventHandler for the maximize/restore button. Maximizes/restores the application window.
     *
     * @param event The button click event
     */
    @FXML
    private void toggleMax(ActionEvent event) {
        boolean old = stage.isMaximized();
        stage.setMaximized(!old);
        root.requestLayout();
    }

    /**
     * EventHandler for the open pak button. Shows a file chooser to select a pak file to load.
     *
     * @param event The button click event
     */
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

    /**
     * Dispatches a task to load a single pak file and displays a loading dialog.
     *
     * @param path The path to the pak file to load
     */
    private void loadPak(Path path) {
        lastOpenedDir = path.getParent();
        openedFilePathProperty.set(path.toString());
        PakLoadTask task = new PakLoadTask(path, this::onLoadFinished);
        connectTaskToUI(task);
        DNPTApplication.EXECUTOR_SERVICE.submit(task);
    }

    /**
     * EventHandler for the open virtual pak button. Shows a directory chooser to select a folder of pak files to load.
     *
     * @param event The button click event
     */
    @FXML
    private void openVirtualPak(ActionEvent event) {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setInitialDirectory(lastOpenedDir.toFile());
        dirChooser.setTitle("Choose a Pak file");
        Optional.ofNullable(dirChooser.showDialog(stage)).
                map(File::toPath).
                ifPresent(this::loadVirtualPak);
    }

    /**
     * Dispatches a task to load a directory of paks (virtual pak) and displays a loading dialog.
     *
     * @param dir The path to the directory containing the paks to load
     */
    private void loadVirtualPak(Path dir) {
        lastOpenedDir = dir;
        openedFilePathProperty.set(dir.toString() + " (Virtual)");
        //  Build path list
        List<Path> paths;
        //  Only accept files ending in .pak and not a directory
        BiPredicate<Path, BasicFileAttributes> test = (p, a) -> p.getFileName().toString().endsWith(".pak");
        test = test.and((p, a) -> !a.isDirectory());
        try (Stream<Path> matches = Files.find(dir, 1, test)) {
            paths = matches.collect(Collectors.toList());
        } catch (IOException e) {
            //  TODO Error handling
            e.printStackTrace();
            return;
        }
        //  Create task
        PakLoadTask task = new PakLoadTask(paths, this::onLoadFinished);
        connectTaskToUI(task);
        DNPTApplication.EXECUTOR_SERVICE.submit(task);
    }

    private void connectTaskToUI(Task task) {
        resetProperties();
        //  Create popup window
        Stage loadingStage = new Stage(StageStyle.TRANSPARENT);
        loadingStage.initOwner(stage);
        loadingStage.setTitle("Loading");
        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);
        Scene scene = new Scene(root, 140, 100, Color.color(0.1, 0.1, 0.1, 0.25));
        scene.getStylesheets().add(STYLESHEET);
        root.getStyleClass().add("dialog");
        loadingStage.setScene(scene);
        //  Create the throbber/spinner
        Image spinnerImage;
        try (InputStream inputStream =
                     getClass().getResourceAsStream("/co/phoenixlab/dn/dnptui/assets/spinner.png")) {
            spinnerImage = new Image(inputStream);
        } catch (IOException e) {
            //  TODO Exception handling
            throw new RuntimeException(e);
        }
        ImageView spinner = new ImageView(spinnerImage);
        spinner.setFitHeight(32);
        spinner.setFitWidth(32);
        spinner.setViewport(new Rectangle2D(0, 0, 32, 32));
        SpriteAnimation spriteAnimation = new SpriteAnimation(spinner, Duration.seconds(1), 18, 18, 0, 0, 64, 64, 18);
        spriteAnimation.setCycleCount(Animation.INDEFINITE);
        //  Task information (e.g. loading Resource00.pak, building file tree)
        Label infoLbl = new Label();
        infoLbl.setTextAlignment(TextAlignment.CENTER);
        infoLbl.setAlignment(Pos.CENTER);
        root.getChildren().addAll(spinner, infoLbl);
        //  Wire up properties
        //  Task information
        infoLbl.textProperty().bind(task.messageProperty());
        //  Keep the window centered relative to parent
        ChangeListener<Number> xPosListener = (observable, oldValue, newValue) ->
                loadingStage.setX(newValue.doubleValue() + stage.getWidth() / 2 - loadingStage.getWidth() / 2);
        ChangeListener<Number> yPosListener = (observable, oldValue, newValue) ->
                loadingStage.setY(newValue.doubleValue() + stage.getHeight() / 2 - loadingStage.getHeight() / 2);
        task.setOnSucceeded(e -> {
            FadeTransitionUtil.fadeTransitionOut(Duration.seconds(0.5D), scene.getRoot(), () -> {
                //  Stop animations, destroy window, remove listeners
                spriteAnimation.stop();
                loadingStage.close();
                infoLbl.textProperty().unbind();
                stage.xProperty().removeListener(xPosListener);
                stage.yProperty().removeListener(yPosListener);
            }).play();
        });
        stage.xProperty().addListener(xPosListener);
        stage.yProperty().addListener(yPosListener);
        //  Display
        scene.getRoot().setOpacity(0D);
        loadingStage.show();
        spriteAnimation.playFromStart();
        FadeTransitionUtil.fadeTransitionIn(Duration.seconds(0.25D), scene.getRoot()).
                play();
        //  Center the window
        xPosListener.changed(null, null, stage.getX());
        yPosListener.changed(null, null, stage.getY());
    }

    /**
     * Called when the load task has finished
     *
     * @param handler  The handler created by the load task
     * @param treeRoot The root TreeItem built by the load task
     */
    public void onLoadFinished(PakHandler handler, TreeItem<PakTreeEntry> treeRoot) {
        if (this.handler != null) {
            //  Unload the previous handler, if it existed
            this.handler.unload();
            this.handler = null;
        }
        this.handler = handler;
        //  Clear old root
        treeView.setRoot(null);
        //  Set new root on next pulse
        Platform.runLater(() -> {
            treeView.setRoot(treeRoot);
            noPakLoadedProperty.set(false);
            treeRoot.setExpanded(true);
        });
    }

    /**
     * EventHandler for the find button. Shows a search dialog.
     *
     * @param event The button click event
     */
    @FXML
    private void find(ActionEvent event) {
        //  TODO Implement
    }

    /**
     * EventHandler for the export file button. Shows a file chooser for the export location.
     *
     * @param event The button click event
     */
    @FXML
    private void exportFile(ActionEvent event) {
        //  Accept iff the selected item is a file. Normally the button is disabled if it isn't, but we
        //  revalidate in case something went wrong or some idiot called this method manually.
        if (selectionTypeProperty.get() == SelectionType.FILE) {
            exportFile(selectedProperty.get());
        }
    }

    /**
     * Shows a file chooser and exports the selected file to the chosen location.
     *
     * @param entry The selected TreeItem
     */
    public void exportFile(TreeItem<PakTreeEntry> entry) {
        //  No entry selected
        if (entry == null) {
            return;
        }
        PakTreeEntry treeEntry = entry.getValue();
        //  Not a valid entry or is a directory
        if (treeEntry == null || treeEntry.entry == null) {
            return;
        }
        //  Show file chooser
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export as...");
        chooser.setInitialDirectory(lastOpenedDir.toFile());
        chooser.setInitialFileName(treeEntry.name);
        File file = chooser.showSaveDialog(stage);
        //  User hit cancel
        if (file == null) {
            return;
        }
        try {
            handler.exportFile(treeEntry, file.toPath());
        } catch (IOException e) {
            e.printStackTrace();    //  TODO
        }
    }

    /**
     * EventHandler for the export folder button. Shows a directory chooser for the export location.
     *
     * @param event The button click event
     */
    @FXML
    private void exportFolder(ActionEvent event) {
        //  Accept iff the selected item is a folder. Normally the button is disabled if it isn't, but we
        //  revalidate in case something went wrong or some idiot called this method manually.
        if (selectionTypeProperty.get() == SelectionType.FOLDER) {
            exportFolder(selectedProperty.get());
        }
    }

    /**
     * Shows a directory chooser and exports the selected directory to the chosen location.
     *
     * @param entry The selected TreeItem
     */
    public void exportFolder(TreeItem<PakTreeEntry> entry) {
        //  No entry selected
        if (entry == null) {
            return;
        }
        PakTreeEntry treeEntry = entry.getValue();
        //  Not a valid entry or is a file
        if (treeEntry == null || treeEntry.entry != null) {
            return;
        }
        //  Show directory chooser
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Export into...");
        chooser.setInitialDirectory(lastOpenedDir.toFile());
        File file = chooser.showDialog(stage);
        //  User hit cancel
        if (file == null) {
            return;
        }
        try {
            handler.exportDirectory(entry, file.toPath());
        } catch (IOException e) {
            e.printStackTrace();    //  TODO
        }
    }


    /**
     * EventHandler for the close pak button. Closes the open pak file/virtual pak.
     *
     * @param event The button event
     */
    @FXML
    private void closePak(ActionEvent event) {
        closePak();
    }

    /**
     * Closes the open pak/virtual pak
     */
    public void closePak() {
        resetProperties();
        openedFilePathProperty.setValue("No File");
        treeView.setRoot(null);
        if (this.handler != null) {
            this.handler.unload();
            this.handler = null;
        }
        System.gc();
    }

    /**
     * Resets the selection and pak loaded properties to their defaults (no selected/not loaded)
     */
    private void resetProperties() {
        noPakLoadedProperty.set(true);
        selectionTypeProperty.set(SelectionType.NONE);
        selectedProperty.set(null);
    }

    /**
     * EventHandler for the top bar being dragged
     *
     * @param event The mouse event
     */
    @FXML
    private void windowDragging(MouseEvent event) {
        if (!maximizedProperty.get() && event.getButton() == MouseButton.PRIMARY) {
            stage.setX(event.getScreenX() - xOff);
            stage.setY(event.getScreenY() - yOff);
        }
    }

    /**
     * EventHandler for the top bar drag starting
     *
     * @param event The mouse event
     */
    @FXML
    private void windowDragStart(MouseEvent event) {
        if (!maximizedProperty.get() && event.getButton() == MouseButton.PRIMARY) {
            xOff = event.getSceneX();
            yOff = event.getSceneY();
        }
    }

    /**
     * EventHandler for the bottom bar being dragged for window height resize
     *
     * @param event The mouse event
     */
    @FXML
    private void windowVerticalResize(MouseEvent event) {
        if (!maximizedProperty.get() && event.getButton() == MouseButton.PRIMARY) {
            double y = event.getScreenY() - stage.getY();
            if (y > MIN_HEIGHT) {
                stage.setHeight(y);
            }
        }
    }

    /**
     * EventHandler for the side bars being dragged for window width resize
     *
     * @param event The mouse event
     */
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
