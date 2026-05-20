package com.edumanage;

import com.edumanage.scenes.LoginScene;
import com.edumanage.utils.DatabaseConfig;
import com.edumanage.utils.ThemeManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        DatabaseConfig.initializeDatabase();

        LoginScene loginScene = new LoginScene(primaryStage);
        primaryStage.setScene(loginScene.getScene());
        primaryStage.setTitle("EduManage");
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.show();

        primaryStage.setOnCloseRequest(e -> DatabaseConfig.closeConnection());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
