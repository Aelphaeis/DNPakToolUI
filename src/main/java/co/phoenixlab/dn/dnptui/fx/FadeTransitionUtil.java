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

package co.phoenixlab.dn.dnptui.fx;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.scene.Node;
import javafx.util.Duration;


public class FadeTransitionUtil {

    public static FadeTransition fadeTransitionOut(Duration duration, Node node) {
        return fadeTransitionOut(duration, node, null);
    }

    public static FadeTransition fadeTransitionOut(Duration duration, Node node, Runnable onFinished) {
        FadeTransition fadeTransition = new FadeTransition(duration, node);
        fadeTransition.setInterpolator(Interpolator.EASE_IN);
        fadeTransition.setFromValue(1D);
        fadeTransition.setToValue(0D);
        if (onFinished != null) {
            fadeTransition.setOnFinished(e -> onFinished.run());
        }
        return fadeTransition;
    }

    public static FadeTransition fadeTransitionIn(Duration duration, Node node) {
        return fadeTransitionIn(duration, node, null);
    }

    public static FadeTransition fadeTransitionIn(Duration duration, Node node, Runnable onFinished) {
        FadeTransition fadeTransition = new FadeTransition(duration, node);
        fadeTransition.setInterpolator(Interpolator.EASE_OUT);
        fadeTransition.setFromValue(0D);
        fadeTransition.setToValue(1D);
        if (onFinished != null) {
            fadeTransition.setOnFinished(e -> onFinished.run());
        }
        return fadeTransition;
    }

}
