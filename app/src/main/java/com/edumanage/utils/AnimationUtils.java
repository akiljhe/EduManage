package com.edumanage.utils;

import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.scene.Node;
import javafx.util.Duration;

public class AnimationUtils {

    private AnimationUtils() {}

    public static void addPressAnimation(Node node) {
        ScaleTransition pressDown = new ScaleTransition(Duration.millis(80), node);
        pressDown.setToX(0.96);
        pressDown.setToY(0.96);
        pressDown.setInterpolator(Interpolator.EASE_IN);

        ScaleTransition releaseUp = new ScaleTransition(Duration.millis(120), node);
        releaseUp.setToX(1.0);
        releaseUp.setToY(1.0);
        releaseUp.setInterpolator(Interpolator.EASE_OUT);

        node.setOnMousePressed(e -> {
            releaseUp.stop();
            pressDown.playFromStart();
        });

        node.setOnMouseReleased(e -> {
            pressDown.stop();
            releaseUp.playFromStart();
        });
    }

    public static void addHoverScale(Node node, double scale) {
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(150), node);
        scaleUp.setToX(scale);
        scaleUp.setToY(scale);
        scaleUp.setInterpolator(Interpolator.EASE_OUT);

        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(150), node);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);
        scaleDown.setInterpolator(Interpolator.EASE_OUT);

        node.setOnMouseEntered(e -> scaleUp.play());
        node.setOnMouseExited(e -> scaleDown.play());
    }
}
