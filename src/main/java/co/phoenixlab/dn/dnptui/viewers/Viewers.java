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

import co.phoenixlab.dn.dnptui.DNPTUIController;
import co.phoenixlab.dn.dnptui.PakTreeEntry;
import co.phoenixlab.dn.dnptui.viewers.stageini.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TreeItem;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Predicate;

public class Viewers {

    private static final Viewer DEFAULT_VIEWER = new DefaultViewer();

    private static final List<Pair<Predicate<TreeItem<PakTreeEntry>>, Viewer>> matcherViewers = new ArrayList<>();

    private static final Map<String, Viewer> fileExtensionViewers = new HashMap<>();

    static {
        //  Register Viewers here

        //  Images
        ImageViewer imageViewer = new ImageViewer();
        registerFXMLViewer(".png", imageViewer, "/co/phoenixlab/dn/dnptui/assets/viewers/image.fxml");
        registerFXMLViewer(".jpg", imageViewer, "/co/phoenixlab/dn/dnptui/assets/viewers/image.fxml");
        registerFXMLViewer(".jpeg", imageViewer, "/co/phoenixlab/dn/dnptui/assets/viewers/image.fxml");
        registerFXMLViewer(".tga", imageViewer, "/co/phoenixlab/dn/dnptui/assets/viewers/image.fxml");
        registerFXMLViewer(".dds", new DdsViewer(),
                "/co/phoenixlab/dn/dnptui/assets/viewers/image.fxml");

        //  Text
        registerFXMLViewer(".lua", new TextViewer(Charset.forName("EUC-KR")), "/co/phoenixlab/dn/dnptui/assets/viewers/text-no-controller.fxml");
        registerFXMLViewer(".cfg", "/co/phoenixlab/dn/dnptui/assets/viewers/text.fxml");
        registerFXMLViewer(".txt", "/co/phoenixlab/dn/dnptui/assets/viewers/text.fxml");
        registerFXMLViewer(".xml", "/co/phoenixlab/dn/dnptui/assets/viewers/text.fxml");
        registerFXMLViewer(".dmv", "/co/phoenixlab/dn/dnptui/assets/viewers/text.fxml");


        registerFXMLViewer(".env", new EnvViewer(), "/co/phoenixlab/dn/dnptui/assets/viewers/text-no-controller.fxml");
        registerFXMLViewer(".msh", new MshViewer(), "/co/phoenixlab/dn/dnptui/assets/viewers/text-no-controller.fxml");
        registerFXMLViewer(".skn", new SknViewer(), "/co/phoenixlab/dn/dnptui/assets/viewers/text-no-controller.fxml");
        registerFXMLViewer(".ani", new AniViewer(), "/co/phoenixlab/dn/dnptui/assets/viewers/text-no-controller.fxml");
        registerFXMLViewer(".cam", new CamViewer(), "/co/phoenixlab/dn/dnptui/assets/viewers/text-no-controller.fxml");

        //  Stage INI
        registerMatcherViewer("sectorsize\\.ini", new StageIniSectorSizeViewer(),
                "/co/phoenixlab/dn/dnptui/assets/viewers/text-no-controller.fxml");
        registerMatcherViewer("gridinfo\\.ini", new StageIniGridInfoViewer(),
                "/co/phoenixlab/dn/dnptui/assets/viewers/text-no-controller.fxml");
        registerMatcherViewer("default\\.ini", new StageIniDefaultViewer(),
                "/co/phoenixlab/dn/dnptui/assets/viewers/text-no-controller.fxml");
        registerMatcherViewer("textable\\.ini", new StageIniTexTableViewer(),
                "/co/phoenixlab/dn/dnptui/assets/viewers/text-no-controller.fxml");
        registerMatcherViewer("height\\.ini", new StageIniHeightViewer(),
                "/co/phoenixlab/dn/dnptui/assets/viewers/image.fxml");
        registerMatcherViewer("heightattribute\\.ini", new StageIniHeightAttributeViewer(),
            "/co/phoenixlab/dn/dnptui/assets/viewers/image.fxml");
        registerMatcherViewer("grasstable\\.ini", new StageIniGrassTableViewer(),
            "/co/phoenixlab/dn/dnptui/assets/viewers/image.fxml");
        registerMatcherViewer("alphatable\\.ini", new StageIniAlphaTableViewer(),
                "/co/phoenixlab/dn/dnptui/assets/viewers/image.fxml");
        registerMatcherViewer("triggerdefine\\.ini", new StageIniTriggerDefineViewer(),
                "/co/phoenixlab/dn/dnptui/assets/viewers/text-no-controller.fxml");
        registerMatcherViewer("propinfo\\.ini", new StageIniPropInfoViewer(),
                "/co/phoenixlab/dn/dnptui/assets/viewers/text-no-controller.fxml");
        registerMatcherViewer("trigger\\.ini", new StageIniTriggerViewer(),
                "/co/phoenixlab/dn/dnptui/assets/viewers/text-no-controller.fxml");

        //  Shaders
        registerMatcherViewer("dnshaders.dat", new ShaderViewer(),
            "/co/phoenixlab/dn/dnptui/assets/viewers/text-no-controller.fxml");
    }

    public static void registerFXMLViewer(String extension, String fxmlPath) {
        registerFXMLViewer(extension, null, fxmlPath);
    }

    public static void registerFXMLViewer(String extension, Viewer viewer, String fxmlPath) {
        viewer = loadFXMLViewer(fxmlPath, viewer);
        viewer.init();
        fileExtensionViewers.put(extension, viewer);
    }

    public static Viewer loadFXMLViewer(String fxmlPath) {
        return loadFXMLViewer(fxmlPath, null);
    }

    public static Viewer loadFXMLViewer(String fxmlPath, Viewer viewer) {
        try {
            FXMLLoader loader = new FXMLLoader(DNPTUIController.class.getResource(fxmlPath));
            if (viewer != null) {
                loader.setController(viewer);
            }
            loader.load();
            return loader.getController();
        } catch (IOException e) {
            //  TODO
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static void registerMatcherViewer(String matchPattern, Viewer viewer, String fxmlPath) {
        loadFXMLViewer(fxmlPath, viewer);
        viewer.init();
        matcherViewers.add(new Pair<>(t -> matches(t, matchPattern), viewer));
    }

    public static void registerMatcherViewer(String matchPattern, Viewer viewer) {
        viewer.init();
        matcherViewers.add(new Pair<>(t -> matches(t, matchPattern), viewer));
    }

    private static boolean matches(TreeItem<PakTreeEntry> entry, String matchPattern) {
        return entry != null && entry.getValue() != null &&
                entry.getValue().name.matches(matchPattern);
    }

    public static void registerMatcherViewer(Predicate<TreeItem<PakTreeEntry>> matcher, Viewer viewer) {
        viewer.init();
        matcherViewers.add(new Pair<>(matcher, viewer));
    }

    private Viewers() {
    }

    public static Viewer getViewer(TreeItem<PakTreeEntry> pakTreeItem) {
        if (pakTreeItem == null || pakTreeItem.getValue() == null) {
            return DEFAULT_VIEWER;
        }
        PakTreeEntry pakTreeEntry = pakTreeItem.getValue();
        String path = pakTreeEntry.path.toString();
        Optional<Viewer> viewer = matcherViewers.stream().
                filter(p -> p.t.test(pakTreeItem)).
                map(Pair::u).
                findFirst();

        return viewer.orElse(getViewerByFileExtension(pakTreeEntry.path.getFileName().toString()));
    }

    private static Viewer getViewerByFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        Optional<Viewer> ret = Optional.empty();
        if (lastDot != -1) {
            String ext = fileName.substring(lastDot);
            ret = Optional.ofNullable(fileExtensionViewers.get(ext));
        }
        return ret.orElse(DEFAULT_VIEWER);
    }

    static class Pair<T, U> {
        final T t;
        final U u;

        public Pair(T t, U u) {
            this.t = t;
            this.u = u;
        }

        public T t() {
            return t;
        }

        public U u() {
            return u;
        }
    }
}
