package com.edumanage.utils;

import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Window;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ThemeManager {

    public enum Theme { LIGHT, DARK, SYSTEM }

    private static final String LIGHT_CSS = "/com/edumanage/styles/light-theme.css";
    private static final String DARK_CSS = "/com/edumanage/styles/dark-theme.css";
    private static boolean fontsLoaded = false;
    private static Theme currentTheme = Theme.DARK; // default

    private ThemeManager() {}

    public static void apply(Scene scene) {
        if (!fontsLoaded) {
            loadFonts();
            fontsLoaded = true;
        }
        applyThemeToScene(scene);
    }

    public static void setTheme(Theme theme) {
        currentTheme = theme;
        // update all active scenes
        for (Window window : Window.getWindows()) {
            if (window.getScene() != null) {
                applyThemeToScene(window.getScene());
            }
        }
    }

    public static Theme getCurrentTheme() {
        return currentTheme;
    }

    private static void applyThemeToScene(Scene scene) {
        String cssToLoad = DARK_CSS;
        if (currentTheme == Theme.LIGHT) {
            cssToLoad = LIGHT_CSS;
        } else if (currentTheme == Theme.SYSTEM) {
            cssToLoad = isMacDarkMode() ? DARK_CSS : LIGHT_CSS;
        }
        
        String cssPath = ThemeManager.class.getResource(cssToLoad).toExternalForm();
        scene.getStylesheets().clear();
        scene.getStylesheets().add(cssPath);
    }

    private static boolean isMacDarkMode() {
        try {
            Process process = Runtime.getRuntime().exec("defaults read -g AppleInterfaceStyle");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            if (line != null && line.trim().equals("Dark")) {
                return true;
            }
        } catch (Exception e) {
            // ignore
        }
        return false; // Default to light if we can't determine
    }

    private static void loadFonts() {
        String[] fontFiles = {
            "/com/edumanage/fonts/Geist-Light.ttf",
            "/com/edumanage/fonts/Geist-Regular.ttf",
            "/com/edumanage/fonts/Geist-Medium.ttf",
            "/com/edumanage/fonts/Geist-SemiBold.ttf",
            "/com/edumanage/fonts/Geist-Bold.ttf"
        };
        for (String path : fontFiles) {
            try {
                Font.loadFont(ThemeManager.class.getResourceAsStream(path), 13);
            } catch(Exception e) {}
        }
    }
}
