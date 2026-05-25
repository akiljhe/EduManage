package com.edumanage.scenes;

import com.edumanage.dao.GuruDAO;
import com.edumanage.dao.KelasDAO;
import com.edumanage.dao.MapelDAO;
import com.edumanage.dao.SiswaDAO;
import com.edumanage.utils.AnimationUtils;
import com.edumanage.utils.ThemeManager;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.chart.PieChart;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import com.edumanage.models.Siswa;

public class DashboardScene {

    private Stage stage;
    private BorderPane root;
    private StackPane contentArea;
    private SiswaDAO siswaDAO = new SiswaDAO();
    private GuruDAO guruDAO = new GuruDAO();
    private KelasDAO kelasDAO = new KelasDAO();
    private MapelDAO mapelDAO = new MapelDAO();
    private Thread clockThread;
    private boolean running = true;

    private VBox sidebar;
    private boolean sidebarExpanded = false;
    private static final double SIDEBAR_COLLAPSED = 56;
    private static final double SIDEBAR_EXPANDED = 220;

    private Button activeButton = null;
    private Button btnDashboard, btnSiswa, btnGuru, btnKelas, btnMapel, btnNilai;

    public DashboardScene(Stage stage) {
        this.stage = stage;
    }

    public Scene getScene() {
        root = new BorderPane();
        root.getStyleClass().add("edu-bg");

        sidebar = buildSidebar();
        root.setLeft(sidebar);

        contentArea = new StackPane();
        contentArea.getStyleClass().add("edu-content");
        showDashboardContent();
        root.setCenter(contentArea);

        Scene scene = new Scene(root, 1100, 700);
        ThemeManager.apply(scene);

        stage.setOnCloseRequest(e -> {
            running = false;
            if (clockThread != null) clockThread.interrupt();
        });

        return scene;
    }

    private VBox buildSidebar() {
        VBox sb = new VBox(0);
        sb.getStyleClass().add("edu-sidebar");
        sb.setPrefWidth(SIDEBAR_COLLAPSED);
        sb.setMinWidth(SIDEBAR_COLLAPSED);
        sb.setMaxWidth(SIDEBAR_COLLAPSED);

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(sb.widthProperty());
        clip.heightProperty().bind(sb.heightProperty());
        sb.setClip(clip);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 12, 12, 14));
        header.setMinHeight(54);
        header.setMaxHeight(54);
        header.getStyleClass().add("edu-sidebar-header");

        StackPane logoBg = new StackPane();
        logoBg.setMinSize(30, 30);
        logoBg.setMaxSize(30, 30);
        logoBg.setStyle("-fx-background-color: -edu-accent; -fx-background-radius: 8;");
        
        SVGPath hat = new SVGPath();
        hat.setContent("M22 10v6M2 10l10-5 10 5-10 5z M6 12v5c3 3 9 3 12 0v-5");
        hat.setStyle("-fx-stroke: white; -fx-stroke-width: 1.5; -fx-fill: transparent; -fx-stroke-line-cap: round; -fx-stroke-line-join: round;");
        
        hat.setScaleX(0.7);
        hat.setScaleY(0.7);
        logoBg.getChildren().add(hat);

        Label logoText = new Label("EduManage");
        logoText.setFont(Font.font("Geist", FontWeight.BOLD, 18));
        logoText.getStyleClass().add("edu-sidebar-text");
        
        header.getChildren().addAll(logoBg, logoText);

        Region sep1 = new Region();
        sep1.getStyleClass().add("edu-separator");
        sep1.setMaxWidth(Double.MAX_VALUE);

        VBox navSection = new VBox(2);
        navSection.setPadding(new Insets(8, 8, 8, 8));
        VBox.setVgrow(navSection, Priority.ALWAYS);

        btnDashboard = createSidebarItem("Dashboard",
                new String[]{"M3 3h8v8H3z", "M13 3h8v8h-8z", "M3 13h8v8H3z", "M13 13h8v8h-8z"},
                true);
        btnSiswa = createSidebarItem("Data Siswa",
                new String[]{"M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2", "M 13 7 A 4 4 0 0 1 9 11 A 4 4 0 0 1 5 7 A 4 4 0 0 1 9 3 A 4 4 0 0 1 13 7 Z", "M22 21v-2a4 4 0 0 0-3-3.87", "M16 3.13a4 4 0 0 1 0 7.75"},
                false);
        btnGuru = createSidebarItem("Data Guru",
                new String[]{"M22 10v6M2 10l10-5 10 5-10 5z", "M6 12v5c3 3 9 3 12 0v-5"},
                false);
        btnKelas = createSidebarItem("Data Kelas",
                new String[]{"M8 6h13", "M8 12h13", "M8 18h13", "M3 6h.01", "M3 12h.01", "M3 18h.01"},
                false);
        btnMapel = createSidebarItem("Mata Pelajaran",
                new String[]{"M4 19.5A2.5 2.5 0 0 1 6.5 17H20", "M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"},
                false);
        btnNilai = createSidebarItem("Rapor & Nilai",
                new String[]{"M 9 2 H 15 A 1 1 0 0 1 16 3 V 5 A 1 1 0 0 1 15 6 H 9 A 1 1 0 0 1 8 5 V 3 A 1 1 0 0 1 9 2 Z", "M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2", "M12 11h4", "M12 16h4", "M8 11h.01", "M8 16h.01"},
                false);

        btnDashboard.setOnAction(e -> { setActiveNav(btnDashboard); showDashboardContent(); });
        btnSiswa.setOnAction(e -> { setActiveNav(btnSiswa); showSiswaContent(); });
        btnGuru.setOnAction(e -> { setActiveNav(btnGuru); showGuruContent(); });
        btnKelas.setOnAction(e -> { setActiveNav(btnKelas); showKelasContent(); });
        btnMapel.setOnAction(e -> { setActiveNav(btnMapel); showMapelContent(); });
        btnNilai.setOnAction(e -> { setActiveNav(btnNilai); showNilaiContent(); });

        activeButton = btnDashboard;

        Region sep2 = new Region();
        sep2.getStyleClass().add("edu-separator");
        sep2.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(sep2, new Insets(4, 0, 4, 0));

        navSection.getChildren().addAll(btnDashboard, btnSiswa, btnGuru, btnKelas, btnMapel, sep2, btnNilai);

        VBox bottomSection = new VBox(2);
        bottomSection.setPadding(new Insets(8, 8, 8, 8));

        Region sep3 = new Region();
        sep3.getStyleClass().add("edu-separator");
        sep3.setMaxWidth(Double.MAX_VALUE);

        Button btnTheme = new Button();
        btnTheme.getStyleClass().add("edu-sidebar-item");
        btnTheme.setMaxWidth(Double.MAX_VALUE);
        btnTheme.setAlignment(Pos.CENTER_LEFT);
        btnTheme.setGraphicTextGap(12);
        
        
        javafx.scene.image.ImageView themeIcon = new javafx.scene.image.ImageView(new javafx.scene.image.Image(getClass().getResourceAsStream("/icons/moon.png")));
        themeIcon.setFitWidth(16);
        themeIcon.setFitHeight(16);
        btnTheme.setGraphic(themeIcon);
        btnTheme.setText("Tema: Dark");
        btnTheme.setStyle("-fx-text-fill: -edu-text-muted; -fx-font-family: 'Geist'; -fx-font-size: 13px; -fx-font-weight: 500;");
        
        btnTheme.setOnAction(e -> {
            com.edumanage.utils.ThemeManager.Theme current = com.edumanage.utils.ThemeManager.getCurrentTheme();
            if (current == com.edumanage.utils.ThemeManager.Theme.DARK) {
                com.edumanage.utils.ThemeManager.setTheme(com.edumanage.utils.ThemeManager.Theme.LIGHT);
                btnTheme.setText("Tema: Light");
                themeIcon.setImage(new javafx.scene.image.Image(getClass().getResourceAsStream("/icons/sun.png")));
            } else if (current == com.edumanage.utils.ThemeManager.Theme.LIGHT) {
                com.edumanage.utils.ThemeManager.setTheme(com.edumanage.utils.ThemeManager.Theme.SYSTEM);
                btnTheme.setText("Tema: System");
                
            } else {
                com.edumanage.utils.ThemeManager.setTheme(com.edumanage.utils.ThemeManager.Theme.DARK);
                btnTheme.setText("Tema: Dark");
                themeIcon.setImage(new javafx.scene.image.Image(getClass().getResourceAsStream("/icons/moon.png")));
            }
        });

        Button logoutBtn = createSidebarItem("Keluar",
                new String[]{"M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4", "M 16 17 L 21 12 L 16 7", "M 21 12 L 9 12"},
                false);
        logoutBtn.getStyleClass().add("edu-sidebar-logout");
        logoutBtn.setOnAction(e -> {
            running = false;
            if (clockThread != null) clockThread.interrupt();
            LoginScene login = new LoginScene(stage);
            stage.setScene(login.getScene());
            stage.setTitle("EduManage - Login");
        });

        HBox accountRow = new HBox(10);
        accountRow.setAlignment(Pos.CENTER_LEFT);
        accountRow.setPadding(new Insets(8, 8, 8, 10));
        accountRow.getStyleClass().add("edu-sidebar-account");

        Label avatar = new Label("AR");
        avatar.setFont(Font.font("Geist", FontWeight.SEMI_BOLD, 12));
        avatar.setMinSize(28, 28);
        avatar.setMaxSize(28, 28);
        avatar.setAlignment(Pos.CENTER);
        avatar.setStyle("-fx-background-color: #6d28d9; -fx-text-fill: white; -fx-background-radius: 14;");

        Label accountName = new Label("Admin");
        accountName.setFont(Font.font("Geist", FontWeight.MEDIUM, 13));
        accountName.getStyleClass().add("edu-sidebar-text");

        accountRow.getChildren().addAll(avatar, accountName);

        bottomSection.getChildren().addAll(sep3, btnTheme, logoutBtn, accountRow);

        sb.getChildren().addAll(header, sep1, navSection, bottomSection);

        sb.setOnMouseEntered(e -> expandSidebar());
        sb.setOnMouseExited(e -> collapseSidebar());

        return sb;
    }

    private Button createSidebarItem(String text, String[] svgPaths, boolean active) {
        Button btn = new Button();
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setMnemonicParsing(false);

        javafx.scene.Group iconGroup = new javafx.scene.Group();
        for(String pathData : svgPaths) {
            SVGPath svg = new SVGPath();
            svg.setContent(pathData);
            svg.getStyleClass().add("edu-sidebar-icon-stroke");
            iconGroup.getChildren().add(svg);
        }

        StackPane iconWrapper = new StackPane(iconGroup);
        iconWrapper.setMinSize(18, 18);
        iconWrapper.setMaxSize(18, 18);
        iconWrapper.setPrefSize(18, 18);

        Label label = new Label(text);
        label.setFont(Font.font("Geist", FontWeight.MEDIUM, 13));
        label.getStyleClass().add("edu-sidebar-text");
        label.setTextOverrun(OverrunStyle.CLIP);
        label.setEllipsisString("");
        label.visibleProperty().bind(btn.widthProperty().greaterThan(100));

        HBox content = new HBox(12);
        content.setAlignment(Pos.CENTER_LEFT);
        content.getChildren().addAll(iconWrapper, label);
        content.setMouseTransparent(true);

        btn.setGraphic(content);
        btn.getStyleClass().add(active ? "edu-sidebar-active" : "edu-sidebar-item");
        AnimationUtils.addPressAnimation(btn);

        return btn;
    }

    private void setActiveNav(Button active) {
        Button[] all = {btnDashboard, btnSiswa, btnGuru, btnKelas, btnMapel, btnNilai};
        for (Button b : all) {
            b.getStyleClass().removeAll("edu-sidebar-item", "edu-sidebar-active");
            if (b == active) {
                b.getStyleClass().add("edu-sidebar-active");
            } else {
                b.getStyleClass().add("edu-sidebar-item");
            }
        }
        activeButton = active;
    }

    private void expandSidebar() {
        if (sidebarExpanded) return;
        sidebarExpanded = true;

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(200),
                        new KeyValue(sidebar.prefWidthProperty(), SIDEBAR_EXPANDED, Interpolator.EASE_BOTH),
                        new KeyValue(sidebar.minWidthProperty(), SIDEBAR_EXPANDED, Interpolator.EASE_BOTH),
                        new KeyValue(sidebar.maxWidthProperty(), SIDEBAR_EXPANDED, Interpolator.EASE_BOTH)
                )
        );
        timeline.play();
    }

    private void collapseSidebar() {
        if (!sidebarExpanded) return;
        sidebarExpanded = false;

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(200),
                        new KeyValue(sidebar.prefWidthProperty(), SIDEBAR_COLLAPSED, Interpolator.EASE_BOTH),
                        new KeyValue(sidebar.minWidthProperty(), SIDEBAR_COLLAPSED, Interpolator.EASE_BOTH),
                        new KeyValue(sidebar.maxWidthProperty(), SIDEBAR_COLLAPSED, Interpolator.EASE_BOTH)
                )
        );
        timeline.play();
    }

    public void showDashboardContent() {
        VBox content = new VBox(24);
        content.setPadding(new Insets(32));
        // TODO: diskusikan padding ini
        content.getStyleClass().add("edu-content");

        VBox titleBox = new VBox(4);

        Label pageTitle = new Label("Dashboard");
        pageTitle.getStyleClass().add("edu-title");
        pageTitle.setFont(Font.font("Geist", FontWeight.BOLD, 24));

        Label clockLabel = new Label();
        clockLabel.getStyleClass().add("edu-text-muted");

        titleBox.getChildren().addAll(pageTitle, clockLabel);

        startClockThread(clockLabel);

        HBox statsRow = new HBox(16);
        statsRow.setAlignment(Pos.CENTER_LEFT);

        int totalSiswa = siswaDAO.countSiswa();
        int totalGuru = guruDAO.countGuru();
        int totalKelas = kelasDAO.getAllKelas().size();
        int totalMapel = mapelDAO.getAllMapel().size();

        VBox cardSiswa = buildStatCard("Total Siswa", String.valueOf(totalSiswa), "#3B82F6");
        VBox cardGuru = buildStatCard("Total Guru", String.valueOf(totalGuru), "#22C55E");
        VBox cardKelas = buildStatCard("Jumlah Kelas", String.valueOf(totalKelas), "#F59E0B");
        VBox cardMapel = buildStatCard("Mata Pelajaran", String.valueOf(totalMapel), "#8B5CF6");

        HBox.setHgrow(cardSiswa, Priority.ALWAYS);
        HBox.setHgrow(cardGuru, Priority.ALWAYS);
        HBox.setHgrow(cardKelas, Priority.ALWAYS);
        HBox.setHgrow(cardMapel, Priority.ALWAYS);

        statsRow.getChildren().addAll(cardSiswa, cardGuru, cardKelas, cardMapel);

        HBox bottomRow = new HBox(16);
        bottomRow.setAlignment(Pos.TOP_CENTER);
        
        VBox pieChartCard = buildPieChart();
        pieChartCard.setMinWidth(400);
        
        VBox leaderboardCard = buildLeaderboard();
        leaderboardCard.setMinWidth(400);
        
        bottomRow.getChildren().addAll(pieChartCard, leaderboardCard);

        content.getChildren().addAll(titleBox, statsRow, bottomRow);

        FadeTransition ft = new FadeTransition(Duration.millis(300), content);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();

        contentArea.getChildren().setAll(content);
    }

    private void startClockThread(Label clockLabel) {
        if (clockThread != null) {
            running = false;
            clockThread.interrupt();
        }
        running = true;

        clockThread = new Thread(() -> {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy | HH:mm:ss");
            while (running && !Thread.currentThread().isInterrupted()) {
                String now = LocalDateTime.now().format(fmt);
                Platform.runLater(() -> clockLabel.setText(now));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        clockThread.setDaemon(true);
        clockThread.setName("ClockThread");
        clockThread.start();
    }

    private VBox buildStatCard(String title, String value, String accent) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.getStyleClass().add("edu-stat-card");

        Region topBorder = new Region();
        topBorder.setMinHeight(3);
        topBorder.setMaxHeight(3);
        topBorder.setStyle("-fx-background-color: " + accent + "; -fx-background-radius: 8 8 0 0;");

        Circle dot = new Circle(5);
        dot.setFill(Color.web(accent));

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("edu-text-muted");
        titleLabel.setFont(Font.font("Geist", 12));

        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        titleRow.getChildren().addAll(dot, titleLabel);

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("edu-stat-value");

        card.getChildren().addAll(topBorder, titleRow, valueLabel);
        AnimationUtils.addHoverScale(card, 1.02);

        return card;
    }

    private VBox buildPieChart() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.getStyleClass().add("edu-card");
        HBox.setHgrow(card, Priority.ALWAYS);

        Label lblTitle = new Label("Statistik Kelulusan");
        lblTitle.setFont(Font.font("Geist", FontWeight.BOLD, 16));
        lblTitle.getStyleClass().add("edu-text");

        List<Siswa> allSiswa = siswaDAO.getAllSiswa();
        int lulus = 0;
        int tidakLulus = 0;
        for (Siswa s : allSiswa) {
            double avg = s.hitungRataRata();
            if (avg >= 75) lulus++;
            else tidakLulus++;
        }

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Lulus (" + lulus + ")", lulus),
                new PieChart.Data("Tidak Lulus (" + tidakLulus + ")", tidakLulus)
        );
        PieChart chart = new PieChart(pieChartData);
        chart.setLabelsVisible(true);
        chart.setLegendVisible(true);
        chart.setMinHeight(250);

        card.getChildren().addAll(lblTitle, chart);
        return card;
    }

    private VBox buildLeaderboard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.getStyleClass().add("edu-card");
        HBox.setHgrow(card, Priority.ALWAYS);

        Label lblTitle = new Label("Top 5 Siswa Berprestasi");
        lblTitle.setFont(Font.font("Geist", FontWeight.BOLD, 16));
        lblTitle.getStyleClass().add("edu-text");

        TableView<Siswa> table = new TableView<>();
        table.getStyleClass().add("edu-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Siswa, String> colNama = new TableColumn<>("Nama Siswa");
        colNama.setCellValueFactory(new PropertyValueFactory<>("nama"));

        TableColumn<Siswa, String> colKelas = new TableColumn<>("ID Kelas");
        colKelas.setCellValueFactory(new PropertyValueFactory<>("kelas"));
        colKelas.setMaxWidth(100);
        colKelas.setMinWidth(80);

        TableColumn<Siswa, Double> colAvg = new TableColumn<>("Rata-rata");
        colAvg.setCellValueFactory(cellData -> {
            Siswa s = cellData.getValue();
            return new javafx.beans.property.SimpleDoubleProperty(s.hitungRataRata()).asObject();
        });
        colAvg.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                    getStyleClass().add("edu-text");
                }
            }
        });
        colAvg.setMaxWidth(100);
        colAvg.setMinWidth(80);

        table.getColumns().addAll(colNama, colKelas, colAvg);

        List<Siswa> allSiswa = siswaDAO.getAllSiswa();
        List<Siswa> top5 = allSiswa.stream()
                .sorted((s1, s2) -> Double.compare(s2.hitungRataRata(), s1.hitungRataRata()))
                .limit(5)
                .collect(Collectors.toList());

        table.setItems(FXCollections.observableArrayList(top5));
        table.setMinHeight(250);
        
        card.getChildren().addAll(lblTitle, table);
        return card;
    }

    private void showSiswaContent() {
        SiswaScene siswaScene = new SiswaScene(stage, this);
        contentArea.getChildren().setAll(siswaScene.getContent());
    }

    private void showGuruContent() {
        GuruScene guruScene = new GuruScene(stage, this);
        contentArea.getChildren().setAll(guruScene.getContent());
    }

    private void showNilaiContent() {
        NilaiScene nilaiScene = new NilaiScene(stage, this);
        contentArea.getChildren().setAll(nilaiScene.getContent());
    }

    private void showKelasContent() {
        KelasScene kelasScene = new KelasScene();
        contentArea.getChildren().setAll(kelasScene.getView());
    }

    private void showMapelContent() {
        MapelScene mapelScene = new MapelScene();
        contentArea.getChildren().setAll(mapelScene.getView());
    }

    public StackPane getContentArea() { return contentArea; }
    public Stage getStage() { return stage; }

    public void stopClockThread() {
        running = false;
        if (clockThread != null) clockThread.interrupt();
    }
}
