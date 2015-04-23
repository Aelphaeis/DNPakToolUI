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
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class DNPTUIController {

    private DNPTApplication application;
    private Stage stage;
    private Scene scene;

    @FXML private Label titleLbl;
    @FXML private Button maxRestoreBtn;
    @FXML private Button findBtn;
    @FXML private Button exportBtn;
    @FXML private Button exportFolderBtn;
    @FXML private Button closePakBtn;

    private final BooleanProperty noPakLoadedProperty;
    private final StringProperty openedFilePathProperty;
    private final BooleanProperty maximizedProperty;

    private Path lastOpenedDir;

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
        maximizedProperty.bind(stage.maximizedProperty());
        findBtn.disableProperty().bind(noPakLoadedProperty);
        exportBtn.disableProperty().bind(noPakLoadedProperty);
        exportFolderBtn.disableProperty().bind(noPakLoadedProperty);
        closePakBtn.disableProperty().bind(noPakLoadedProperty);
        titleLbl.textProperty().bind(Bindings.concat("DN Pak Tool - ").concat(openedFilePathProperty));
        maximizedProperty.addListener((observable, oldValue, newValue) -> {
            maxRestoreBtn.setId(newValue ? "window-restore-button" : null);
        });

        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.125D), scene.getRoot());
        fadeTransition.setFromValue(0D);
        fadeTransition.setToValue(1D);
        fadeTransition.playFromStart();
    }

    public void showClosePrompt(ActionEvent event) {
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

    public void iconify(ActionEvent event) {
        stage.setIconified(true);
    }

    public void toggleMax(ActionEvent event) {
        boolean old = stage.isMaximized();
        stage.setMaximized(!old);
    }

    public void openPak(ActionEvent event) {
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

    }

    public void openVirtualPak(ActionEvent event) {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setInitialDirectory(lastOpenedDir.toFile());
        dirChooser.setTitle("Choose a Pak file");
        Optional.ofNullable(dirChooser.showDialog(stage)).
                map(File::toPath).
                ifPresent(this::loadPak);
    }

    private void loadVirtualPak(Path dir) {
        lastOpenedDir = dir;
        //  Show loading dialog

        //  Dispatch job

    }

    public void find(ActionEvent event) {

    }

    public void exportFile(ActionEvent event) {

    }

    public void exportFolder(ActionEvent event) {

    }

    public void closePak(ActionEvent event) {

    }
}
