package com.edumanage.utils;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class AuroraBackground extends StackPane {

    public AuroraBackground() {
        Pane animationPane = new Pane();
        animationPane.setManaged(false); 
        animationPane.getStyleClass().add("edu-bg"); 

        
        Circle c1 = createAuroraBlob(Color.web("#3b82f6", 0.5), 400); 
        Circle c2 = createAuroraBlob(Color.web("#8b5cf6", 0.4), 500); 
        Circle c3 = createAuroraBlob(Color.web("#0ea5e9", 0.4), 300); 
        
        
        c1.setCenterX(200); c1.setCenterY(100);
        c2.setCenterX(600); c2.setCenterY(400);
        c3.setCenterX(100); c3.setCenterY(500);

        animationPane.getChildren().addAll(c1, c2, c3);

        
        TranslateTransition t1 = new TranslateTransition(Duration.seconds(20), c1);
        t1.setByX(400); t1.setByY(200);
        t1.setAutoReverse(true); t1.setCycleCount(Animation.INDEFINITE);
        t1.setInterpolator(Interpolator.EASE_BOTH);
        t1.play();

        
        TranslateTransition t2 = new TranslateTransition(Duration.seconds(25), c2);
        t2.setByX(-300); t2.setByY(-200);
        t2.setAutoReverse(true); t2.setCycleCount(Animation.INDEFINITE);
        t2.setInterpolator(Interpolator.EASE_BOTH);
        t2.play();

       
        TranslateTransition t3 = new TranslateTransition(Duration.seconds(18), c3);
        t3.setByX(200); t3.setByY(-300);
        t3.setAutoReverse(true); t3.setCycleCount(Animation.INDEFINITE);
        t3.setInterpolator(Interpolator.EASE_BOTH);
        t3.play();

    
        this.widthProperty().addListener((obs, oldV, newV) -> {
            animationPane.setPrefWidth(newV.doubleValue());
        });
        this.heightProperty().addListener((obs, oldV, newV) -> {
            animationPane.setPrefHeight(newV.doubleValue());
        });

        
        Pane overlay = new Pane();
        overlay.setStyle("-fx-background-color: -edu-bg; -fx-opacity: 0.85;"); 

        this.getChildren().addAll(animationPane, overlay);
    }

    private Circle createAuroraBlob(Color color, double radius) {
        Circle circle = new Circle(radius);
        RadialGradient gradient = new RadialGradient(
                0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
                new Stop(0, color),
                new Stop(1, Color.TRANSPARENT)
        );
        circle.setFill(gradient);
        return circle;
    }
}
