package com.edumanage.scenes;

import com.edumanage.utils.AnimationUtils;
import com.edumanage.utils.ThemeManager;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoginScene {

    private Stage stage;

    public LoginScene(Stage stage) {
        this.stage = stage;
    }

    public Scene getScene() {
        com.edumanage.utils.AuroraBackground root = new com.edumanage.utils.AuroraBackground();
        root.setAlignment(Pos.CENTER);

        HBox card = buildCard();
        root.getChildren().add(card);

        Scene scene = new Scene(root, 900, 600);
        ThemeManager.apply(scene);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), card);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        return scene;
    }

    private HBox buildCard() {
        HBox card = new HBox(0);
        card.setMaxWidth(400);
        card.setMaxHeight(480);
        card.getStyleClass().add("edu-login-card");

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#000000", 0.12));
        shadow.setRadius(32);
        shadow.setOffsetY(8);
        card.setEffect(shadow);

        Rectangle clip = new Rectangle();
        clip.setArcWidth(24);
        clip.setArcHeight(24);
        clip.widthProperty().bind(card.widthProperty());
        clip.heightProperty().bind(card.heightProperty());
        card.setClip(clip);

        StackPane leftPanel = buildLeftPanel();

        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        card.getChildren().addAll(leftPanel);

        return card;
    }

    private StackPane buildLeftPanel() {
        StackPane panel = new StackPane();
        panel.getStyleClass().add("edu-login-left");
        panel.setMinWidth(400);

        Circle glowCircle = new Circle(200);
        glowCircle.setFill(new RadialGradient(
                0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#A78BFA", 0.25)),
                new Stop(0.3, Color.web("#60A5FA", 0.2)),
                new Stop(0.6, Color.web("#F472B6", 0.15)),
                new Stop(1, Color.TRANSPARENT)
        ));
        glowCircle.setMouseTransparent(true);
        glowCircle.setOpacity(0);

        BoxBlur blur = new BoxBlur(60, 60, 3);
        glowCircle.setEffect(blur);

        panel.setOnMouseMoved(e -> {
            glowCircle.setTranslateX(e.getX() - panel.getWidth() / 2);
            glowCircle.setTranslateY(e.getY() - panel.getHeight() / 2);
        });
        panel.setOnMouseEntered(e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(200), glowCircle);
            ft.setToValue(1);
            ft.play();
        });
        panel.setOnMouseExited(e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(200), glowCircle);
            ft.setToValue(0);
            ft.play();
        });

        VBox form = buildForm();
        form.setMaxWidth(320);

        panel.getChildren().addAll(glowCircle, form);
        StackPane.setAlignment(form, Pos.CENTER);

        return panel;
    }

    private VBox buildForm() {
        VBox form = new VBox(0);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(40, 32, 40, 32));

        Label title = new Label("Login");
        title.setFont(Font.font("Geist", FontWeight.EXTRA_BOLD, 32));
        title.getStyleClass().add("edu-title");
        title.setPadding(new Insets(0, 0, 8, 0));

        Label subtitle = new Label("Masuk ke EduManage");
        subtitle.getStyleClass().add("edu-text-muted");
        subtitle.setFont(Font.font("Geist", 13));
        subtitle.setPadding(new Insets(0, 0, 28, 0));

        Label usernameLabel = new Label("Username");
        usernameLabel.getStyleClass().add("edu-label");
        usernameLabel.setPadding(new Insets(0, 0, 6, 0));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Masukkan username...");
        usernameField.getStyleClass().add("edu-input");
        usernameField.setMaxWidth(Double.MAX_VALUE);

        Region gap1 = new Region();
        gap1.setMinHeight(16);

        Label passwordLabel = new Label("Password");
        passwordLabel.getStyleClass().add("edu-label");
        passwordLabel.setPadding(new Insets(0, 0, 6, 0));

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Masukkan password...");
        passwordField.getStyleClass().add("edu-input");
        passwordField.setMaxWidth(Double.MAX_VALUE);
        
        passwordField.setStyle("-fx-font-size: 10px; -fx-padding: 11.5 10 11.5 10;");

        Region gap2 = new Region();
        gap2.setMinHeight(8);

        Label errorLabel = new Label("");
        errorLabel.setFont(Font.font("Geist", 12));
        errorLabel.setTextFill(Color.web("#EF4444"));
        errorLabel.setMaxWidth(Double.MAX_VALUE);

        Region gap3 = new Region();
        gap3.setMinHeight(8);

        Button loginBtn = new Button("Login");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setFont(Font.font("Geist", FontWeight.SEMI_BOLD, 14));
        loginBtn.getStyleClass().add("edu-btn-primary");
        loginBtn.setPadding(new Insets(12, 0, 12, 0));
        AnimationUtils.addPressAnimation(loginBtn);

        Runnable doLogin = () -> {
            String user = usernameField.getText().trim();
            String pass = passwordField.getText().trim();

            if (user.equals("admin") && pass.equals("admin123")) {
                FadeTransition ft = new FadeTransition(Duration.millis(300), stage.getScene().getRoot());
                ft.setFromValue(1);
                ft.setToValue(0);
                ft.setOnFinished(ev -> {
                    DashboardScene dashboard = new DashboardScene(stage);
                    stage.setScene(dashboard.getScene());
                    stage.setTitle("EduManage - Dashboard");
                });
                ft.play();
            } else {
                errorLabel.setText("Username atau password salah!");
                FadeTransition shake = new FadeTransition(Duration.millis(100), errorLabel);
                shake.setFromValue(0);
                shake.setToValue(1);
                shake.play();
            }
        };

        loginBtn.setOnAction(e -> doLogin.run());
        passwordField.setOnAction(e -> doLogin.run());

        form.getChildren().addAll(
                title,
                subtitle,
                usernameLabel,
                usernameField,
                gap1,
                passwordLabel,
                passwordField,
                gap2,
                errorLabel,
                gap3,
                loginBtn
        );

        return form;
    }

}