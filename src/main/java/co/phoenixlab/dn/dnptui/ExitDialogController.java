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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ExitDialogController {

    @FXML private HBox buttonBar;
    @FXML private Label promptLbl;
    @FXML private VBox root;

    private Stage stage;
    private Runnable quitAction;

    public ExitDialogController() {
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setQuitAction(Runnable quitAction) {
        this.quitAction = quitAction;
    }

    @FXML
    private void onYesBtn(ActionEvent event) {
        buttonBar.setDisable(true);
        FadeTransitionUtil.fadeTransitionOut(Duration.seconds(0.125D), root).
                play();
        quitAction.run();
    }

    @FXML
    private void onNoBtn(ActionEvent event) {
        buttonBar.setDisable(true);
        FadeTransitionUtil.fadeTransitionOut(Duration.seconds(0.125D), root, stage::close).
                play();
    }
}
