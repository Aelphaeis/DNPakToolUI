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

import co.phoenixlab.dn.dnptui.update.UpdateCheckTask;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class DNPTApplication extends Application {

    public static final ScheduledExecutorService EXECUTOR_SERVICE;
    private static final Logger LOGGER = LoggerFactory.getLogger(DNPTApplication.class);

    static {
        EXECUTOR_SERVICE = Executors.newScheduledThreadPool(2);
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            String currentVersion = getClass().getPackage().getImplementationVersion();
            LOGGER.info("Starting DNPakToolUI v{}", currentVersion);
            //  Dispatch update checker
            EXECUTOR_SERVICE.submit(new UpdateCheckTask(currentVersion));
            //  Load the primary scene FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/phoenixlab/dn/dnptui/assets/scene.fxml"));
            Parent root = loader.load();
            LOGGER.debug("Loaded scene from FXML");
            Scene scene = new Scene(root, 1000, 700);
            scene.setFill(Color.TRANSPARENT);
            primaryStage.initStyle(StageStyle.TRANSPARENT);
            primaryStage.setScene(scene);
            primaryStage.setTitle("DN Pak Tool");
            //  Load application icons
            int[] iconSizes = {24, 32, 64, 128};
            for (int i : iconSizes) {
                try (InputStream stream =
                             getClass().getResourceAsStream("/co/phoenixlab/dn/dnptui/assets/window/icon_" + i +
                                     ".png")) {
                    primaryStage.getIcons().add(new Image(stream));
                } catch (IOException e) {
                    LOGGER.warn("Unable to load " + i + "px icon", e);
                }
            }
            LOGGER.debug("Loaded {} icons", primaryStage.getIcons().size());
            //  Set up controller
            DNPTUIController controller = loader.getController();
            controller.setStageSceneApp(primaryStage, scene, this);
            LOGGER.debug("Displaying scene");
            primaryStage.show();
            controller.init();
            LOGGER.info("Startup complete");
        } catch (Exception e) {
            LOGGER.error("Unexpected error during startup", e);
        }
    }

    @Override
    public void stop() {
        LOGGER.info("Stopping DNPakToolUI");
        //  Nothing much to do here cleanup wise
        //  Stop application
        System.exit(0);
    }
}
