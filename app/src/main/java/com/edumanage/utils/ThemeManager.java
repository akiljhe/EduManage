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
    private static Theme currentTheme = Theme.DARK;

    private ThemeManager() {}

    public static void apply(Scene scene) {
        if (!fontsLoaded) {
            loadFonts();
            fontsLoaded = true;
        }
        startSystemThemePoller();
        applyThemeToScene(scene);
    }

    public static void setTheme(Theme theme) {
        currentTheme = theme;
        
        for (Window window : Window.getWindows()) {
            if (window.getScene() != null) {
                applyThemeToScene(window.getScene());
            }
        }
    }

    public static Theme getCurrentTheme() {
        return currentTheme;
    }

    private static javafx.animation.Timeline themePoller;
    private static Boolean lastSystemWasDark = null;

    public static void startSystemThemePoller() {
        if (themePoller == null) {
            themePoller = new javafx.animation.Timeline(new javafx.animation.KeyFrame(javafx.util.Duration.seconds(3), e -> {
                if (currentTheme == Theme.SYSTEM) {
                    boolean isDarkNow = isSystemDarkMode();
                    if (lastSystemWasDark != null && lastSystemWasDark != isDarkNow) {
                        setTheme(Theme.SYSTEM);
                    }
                }
            }));
            themePoller.setCycleCount(javafx.animation.Animation.INDEFINITE);
            themePoller.play();
        }
    }

    private static void applyThemeToScene(Scene scene) {
        String cssToLoad = DARK_CSS;
        if (currentTheme == Theme.LIGHT) {
            cssToLoad = LIGHT_CSS;
        } else if (currentTheme == Theme.SYSTEM) {
            boolean isDark = isSystemDarkMode();
            lastSystemWasDark = isDark;
            cssToLoad = isDark ? DARK_CSS : LIGHT_CSS;
        }
        
        String cssPath = ThemeManager.class.getResource(cssToLoad).toExternalForm();
        if (!scene.getStylesheets().contains(cssPath)) {
            scene.getStylesheets().clear();
            scene.getStylesheets().add(cssPath);
        }
    }

    public static boolean isSystemDarkMode() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac")) {
            return isMacDarkMode();
        } else if (os.contains("win")) {
            return isWindowsDarkMode();
        }
        return false;
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
        }
        return false;
    }

    private static boolean isWindowsDarkMode() {
        try {
            Process process = Runtime.getRuntime().exec(new String[] {
                "reg", "query", "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize", "/v", "AppsUseLightTheme"
            });
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("REG_DWORD")) {
                    // 0x0 means dark theme, 0x1 means light theme
                    return line.contains("0x0");
                }
            }
        } catch (Exception e) {
        }
        return false;
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
