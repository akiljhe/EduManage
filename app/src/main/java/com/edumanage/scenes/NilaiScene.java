package com.edumanage.scenes;

import com.edumanage.dao.*;
import com.edumanage.models.*;
import com.edumanage.utils.AnimationUtils;
import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;
import java.util.stream.Collectors;

public class NilaiScene {

    private Stage stage;
    private DashboardScene dashboard;
    private SiswaDAO siswaDAO = new SiswaDAO();
    private MapelDAO mapelDAO = new MapelDAO();
    private NilaiDAO nilaiDAO = new NilaiDAO();
    private KelasDAO kelasDAO = new KelasDAO();

    private ObservableList<Siswa> masterData;
    private List<Mapel> currentMapelList = new ArrayList<>();

    private int currentPage = 1;
    private int itemsPerPage = 10;
    private String searchText = "";
    private String filterKelas = null;
    private Label paginationLabel;
    private HBox pageButtonsBox;
    private HBox statsRow;

    private VBox tableContainer;
    private TableView<Siswa> tableView;

    private VBox formContainer;
    private Label lblInfo;
    private Map<Integer, TextField> nilaiFields = new LinkedHashMap<>();
    private Siswa selectedSiswa = null;

    public NilaiScene(Stage stage, DashboardScene dashboard) {
        this.stage = stage;
        this.dashboard = dashboard;
    }

    public VBox getContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(32));
        content.getStyleClass().add("edu-content");

        masterData = siswaDAO.getAllSiswa();

        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        VBox titleBox = new VBox(4);
        Label title = new Label("Rapor & Nilai Siswa");
        title.getStyleClass().add("edu-title");
        title.setFont(Font.font("Geist", FontWeight.BOLD, 24));
        Label subtitle = new Label("Kelola nilai siswa untuk semua mata pelajaran");
        subtitle.getStyleClass().add("edu-subtitle");
        titleBox.getChildren().addAll(title, subtitle);
        header.getChildren().add(titleBox);

        statsRow = buildStatsRow();
        HBox filterBar = buildFilterBar();

        HBox searchBar = new HBox(12);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        searchBar.getStyleClass().add("edu-search-bar");
        javafx.scene.image.ImageView searchIcon = new javafx.scene.image.ImageView(new javafx.scene.image.Image(getClass().getResourceAsStream("/icons/search.png")));
        searchIcon.setFitWidth(16);
        searchIcon.setFitHeight(16);
        TextField searchField = new TextField();
        searchField.setPromptText("Cari siswa...");
        searchField.setPrefWidth(280);
        searchField.getStyleClass().add("edu-search-input");
        searchField.textProperty().addListener((obs, old, val) -> {
            searchText = val == null ? "" : val;
            currentPage = 1;
            refreshTableView();
        });
        searchBar.getChildren().addAll(searchIcon, searchField);
        
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        Button btnExport = new Button("Ekspor Excel");
        btnExport.getStyleClass().add("edu-btn-outline");
        btnExport.setOnAction(e -> com.edumanage.utils.CsvExporter.exportToCsv(tableView, "Data_Nilai", tableView.getScene().getWindow()));
        
        HBox topControls = new HBox(12);
        topControls.setAlignment(Pos.CENTER_LEFT);
        topControls.getChildren().addAll(searchBar, spacer, btnExport);

        tableView = buildBasicTable();

        HBox pagination = new HBox(12);
        pagination.setAlignment(Pos.CENTER_LEFT);
        pagination.getStyleClass().add("edu-pagination");
        paginationLabel = new Label();
        paginationLabel.getStyleClass().add("edu-text-muted");
        paginationLabel.setFont(Font.font("Geist", 12));
        Region pagSpacer = new Region();
        HBox.setHgrow(pagSpacer, Priority.ALWAYS);
        pageButtonsBox = new HBox(6);
        pageButtonsBox.setAlignment(Pos.CENTER_RIGHT);
        pagination.getChildren().addAll(paginationLabel, pagSpacer, pageButtonsBox);

        tableContainer = new VBox(0);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        VBox.setVgrow(tableContainer, Priority.ALWAYS);
        HBox.setHgrow(tableContainer, Priority.ALWAYS);
        tableContainer.getChildren().addAll(searchBar, tableView, pagination);

        ScrollPane formScroll = new ScrollPane();
        formScroll.getStyleClass().add("edu-scroll");
        formScroll.setFitToWidth(true);
        formScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        formScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        formScroll.setPrefWidth(340);
        formScroll.setMinWidth(340);

        formContainer = buildFormCard();
        formScroll.setContent(formContainer);

        HBox mainArea = new HBox(16);
        VBox.setVgrow(mainArea, Priority.ALWAYS);
        mainArea.getChildren().addAll(tableContainer, formScroll);

        content.getChildren().addAll(header, statsRow, topControls, filterBar, mainArea);

        refreshTableView();

        FadeTransition ft = new FadeTransition(Duration.millis(400), content);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();

        return content;
    }

    private HBox buildFilterBar() {
        HBox bar = new HBox(8);
        bar.setAlignment(Pos.CENTER_LEFT);

        Label filterLabel = new Label("Filter Kelas:");
        filterLabel.setFont(Font.font("Geist", FontWeight.MEDIUM, 12));
        filterLabel.setTextFill(Color.web("#a1a1aa"));

        ToggleGroup tg = new ToggleGroup();

        ToggleButton btnAll = new ToggleButton("Semua");
        btnAll.setToggleGroup(tg);
        btnAll.getStyleClass().add("edu-filter-btn");
        btnAll.setSelected(true);
        btnAll.setOnAction(e -> {
            filterKelas = null;
            currentPage = 1;
            rebuildTableForFilter();
        });

        bar.getChildren().addAll(filterLabel, btnAll);

        for (Kelas k : kelasDAO.getAllKelas()) {
            ToggleButton tb = new ToggleButton(k.getKodeKelas());
            tb.setToggleGroup(tg);
            tb.getStyleClass().add("edu-filter-btn");
            tb.setOnAction(e -> {
                filterKelas = k.getKodeKelas();
                currentPage = 1;
                rebuildTableForFilter();
            });
            bar.getChildren().add(tb);
        }

        ScrollPane scrollBar = new ScrollPane(bar);
        scrollBar.setFitToHeight(true);
        scrollBar.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollBar.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollBar.getStyleClass().add("edu-scroll");
        scrollBar.setMaxHeight(40);

        HBox wrapper = new HBox(scrollBar);
        HBox.setHgrow(scrollBar, Priority.ALWAYS);
        return wrapper;
    }

    private void rebuildTableForFilter() {
        if (filterKelas != null) {
            currentMapelList = new ArrayList<>(mapelDAO.getMapelByKelas(filterKelas));
        } else {
            currentMapelList = new ArrayList<>();
        }

        VBox parent = (VBox) tableView.getParent();
        int idx = parent.getChildren().indexOf(tableView);

        tableView = filterKelas != null ? buildNilaiTableWithSubjects() : buildBasicTable();
        VBox.setVgrow(tableView, Priority.ALWAYS);

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) showGradeForm(sel);
        });

        parent.getChildren().set(idx, tableView);
        refreshTableView();
    }

    @SuppressWarnings("unchecked")
    private TableView<Siswa> buildBasicTable() {
        TableView<Siswa> tv = new TableView<>();
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tv.getStyleClass().add("edu-table");
        tv.setPlaceholder(new Label("Pilih kelas untuk melihat nilai per mapel"));

        TableColumn<Siswa, String> colNama = buildNamaColumn();
        TableColumn<Siswa, String> colNis = buildNisColumn();
        TableColumn<Siswa, String> colKelas = buildKelasColumn();
        TableColumn<Siswa, String> colRata = buildRataColumn();
        TableColumn<Siswa, String> colStatus = buildStatusColumn();

        tv.getColumns().addAll(colNama, colNis, colKelas, colRata, colStatus);

        tv.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) showGradeForm(sel);
        });

        tv.sortPolicyProperty().set(t -> {
            Comparator<Siswa> comparator = (Comparator<Siswa>) t.getComparator();
            if (comparator != null) {
                FXCollections.sort(masterData, comparator);
                currentPage = 1;
                refreshTableView();
            }
            return true;
        });

        return tv;
    }

    @SuppressWarnings("unchecked")
    private TableView<Siswa> buildNilaiTableWithSubjects() {
        TableView<Siswa> tv = new TableView<>();
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tv.getStyleClass().add("edu-table");
        tv.setPlaceholder(new Label("Belum ada data nilai"));

        TableColumn<Siswa, String> colNama = buildNamaColumn();
        colNama.setMinWidth(130);
        TableColumn<Siswa, String> colNis = buildNisColumn();

        tv.getColumns().addAll(colNama, colNis);

        for (Mapel mapel : currentMapelList) {
            TableColumn<Siswa, String> col = new TableColumn<>(mapel.getNamaMapel());
            col.setCellValueFactory(data -> {
                double val = data.getValue().getNilai(mapel.getNamaMapel());
                return new SimpleStringProperty(val > 0 ? String.format(Locale.US, "%.0f", val) : "-");
            });
            col.setCellFactory(tc -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null); setText(null);
                    } else {
                        Label lbl = new Label(item);
                        lbl.setFont(Font.font("Geist", FontWeight.MEDIUM, 11));
                        if (!item.equals("-")) {
                            double v = Double.parseDouble(item);
                            String c = v >= 80 ? "#3b82f6" : v >= 70 ? "#f59e0b" : "#ef4444";
                            lbl.setTextFill(Color.web(c));
                        } else {
                            lbl.setTextFill(Color.web("#52525b"));
                        }
                        setGraphic(lbl);
                    }
                }
            });
            col.setMaxWidth(80);
            col.setMinWidth(50);
            col.setComparator((s1, s2) -> {
                if (s1.equals("-") && s2.equals("-")) return 0;
                if (s1.equals("-")) return -1;
                if (s2.equals("-")) return 1;
                try {
                    return Double.compare(Double.parseDouble(s1), Double.parseDouble(s2));
                } catch (Exception e) {
                    return s1.compareTo(s2);
                }
            });
            tv.getColumns().add(col);
        }

        TableColumn<Siswa, String> colRata = buildRataColumn();
        TableColumn<Siswa, String> colPredikat = new TableColumn<>("Predikat");
        colPredikat.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPredikat()));
        colPredikat.setMaxWidth(110);
        colPredikat.setStyle("-fx-font-size: 11px;");

        TableColumn<Siswa, String> colStatus = buildStatusColumn();

        tv.getColumns().addAll(colRata, colPredikat, colStatus);
        tv.sortPolicyProperty().set(t -> {
            Comparator<Siswa> comparator = (Comparator<Siswa>) t.getComparator();
            if (comparator != null) {
                FXCollections.sort(masterData, comparator);
                currentPage = 1;
                refreshTableView();
            }
            return true;
        });

        return tv;
    }

    private TableColumn<Siswa, String> buildNamaColumn() {
        TableColumn<Siswa, String> col = new TableColumn<>("Nama Siswa");
        col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNama()));
        col.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); }
                else {
                    HBox box = new HBox(8);
                    box.setAlignment(Pos.CENTER_LEFT);
                    Label avatar = new Label(item.substring(0, Math.min(2, item.length())).toUpperCase());
                    avatar.setMinSize(24, 24); avatar.setMaxSize(24, 24);
                    avatar.setAlignment(Pos.CENTER);
                    String[] colors = {"#3b82f6", "#10b981", "#8b5cf6", "#f59e0b", "#ef4444"};
                    avatar.setStyle("-fx-background-color: " + colors[item.length() % colors.length] +
                            "; -fx-text-fill: white; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 10px;");
                    Label n = new Label(item);
                    n.setStyle("-fx-text-fill: -edu-text; -fx-font-weight: 500; -fx-font-size: 11px;");
                    box.getChildren().addAll(avatar, n);
                    setGraphic(box);
                }
            }
        });
        col.setMinWidth(140);
        return col;
    }

    private TableColumn<Siswa, String> buildNisColumn() {
        TableColumn<Siswa, String> col = new TableColumn<>("NIS");
        col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNis()));
        col.setMaxWidth(90);
        col.setStyle("-fx-font-size: 11px;");
        return col;
    }

    private TableColumn<Siswa, String> buildKelasColumn() {
        TableColumn<Siswa, String> col = new TableColumn<>("Kelas");
        col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getKelas()));
        col.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); }
                else {
                    Label lbl = new Label(item);
                    lbl.getStyleClass().add("edu-badge-kelas");
                    setGraphic(lbl);
                }
            }
        });
        col.setMaxWidth(90);
        return col;
    }

    private TableColumn<Siswa, String> buildRataColumn() {
        TableColumn<Siswa, String> col = new TableColumn<>("Rata-rata");
        col.setCellValueFactory(data -> new SimpleStringProperty(
                String.format(Locale.US, "%.1f", data.getValue().hitungRataRata())));
        col.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); }
                else {
                    double val = Double.parseDouble(item);
                    Label lbl = new Label(item);
                    lbl.setFont(Font.font("Geist", FontWeight.BOLD, 11));
                    String c = val >= 80 ? "#22c55e" : val >= 70 ? "#f59e0b" : "#ef4444";
                    lbl.setTextFill(Color.web(c));
                    setGraphic(lbl);
                }
            }
        });
        col.setMaxWidth(80);
        col.setComparator((s1, s2) -> {
            try {
                return Double.compare(Double.parseDouble(s1), Double.parseDouble(s2));
            } catch (Exception e) {
                return s1.compareTo(s2);
            }
        });
        return col;
    }

    private TableColumn<Siswa, String> buildStatusColumn() {
        TableColumn<Siswa, String> col = new TableColumn<>("Status");
        col.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().isLulus() ? "Lulus" : "Tidak Lulus"));
        col.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); }
                else {
                    boolean lulus = item.equals("Lulus");
                    Label pill = new Label(item);
                    pill.getStyleClass().add(lulus ? "edu-badge-lulus" : "edu-badge-gagal");
                    pill.setStyle("-fx-font-size: 10px;");
                    javafx.scene.shape.Circle dot = new javafx.scene.shape.Circle(2.5, Color.web(lulus ? "#22c55e" : "#ef4444"));
                    pill.setGraphic(dot);
                    pill.setGraphicTextGap(4);
                    setGraphic(pill);
                }
            }
        });
        col.setMaxWidth(95);
        return col;
    }

    private void refreshTableView() {
        List<Siswa> filtered = masterData.stream()
                .filter(s -> {
                    if (filterKelas != null && !s.getKelas().equals(filterKelas)) return false;
                    if (searchText.isEmpty()) return true;
                    String lower = searchText.toLowerCase();
                    return s.getNama().toLowerCase().contains(lower)
                            || s.getNis().toLowerCase().contains(lower)
                            || s.getKelas().toLowerCase().contains(lower);
                })
                .collect(Collectors.toList());

        int totalItems = filtered.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / itemsPerPage));
        if (currentPage > totalPages) currentPage = totalPages;

        int fromIndex = Math.min((currentPage - 1) * itemsPerPage, totalItems);
        int toIndex = Math.min(fromIndex + itemsPerPage, totalItems);

        if (tableView.getItems() == null) {
            tableView.setItems(FXCollections.observableArrayList(filtered.subList(fromIndex, toIndex)));
        } else {
            tableView.getItems().setAll(filtered.subList(fromIndex, toIndex));
        }

        if (totalItems > 0) {
            paginationLabel.setText(String.format("Menampilkan %d-%d dari %d siswa", fromIndex + 1, toIndex, totalItems));
        } else {
            paginationLabel.setText("Tidak ada siswa");
        }

        if (statsRow != null) {
            statsRow.getChildren().setAll(buildStatsRow().getChildren());
        }

        pageButtonsBox.getChildren().clear();

        Button prevBtn = new Button("Previous");
        prevBtn.getStyleClass().add("edu-page-btn");
        prevBtn.setDisable(currentPage <= 1);
        prevBtn.setOnAction(e -> { currentPage--; refreshTableView(); });
        AnimationUtils.addPressAnimation(prevBtn);
        pageButtonsBox.getChildren().add(prevBtn);

        int startPage = Math.max(1, currentPage - 2);
        int endPage = Math.min(totalPages, startPage + 4);
        for (int i = startPage; i <= endPage; i++) {
            int page = i;
            Button pageBtn = new Button(String.valueOf(page));
            pageBtn.getStyleClass().add(page == currentPage ? "edu-page-btn-active" : "edu-page-btn");
            pageBtn.setOnAction(e -> { currentPage = page; refreshTableView(); });
            AnimationUtils.addPressAnimation(pageBtn);
            pageButtonsBox.getChildren().add(pageBtn);
        }

        Button nextBtn = new Button("Next");
        nextBtn.getStyleClass().add("edu-page-btn");
        nextBtn.setDisable(currentPage >= totalPages);
        nextBtn.setOnAction(e -> { currentPage++; refreshTableView(); });
        AnimationUtils.addPressAnimation(nextBtn);
        pageButtonsBox.getChildren().add(nextBtn);
    }

    private VBox buildFormCard() {
        VBox card = new VBox(16);
        card.setPadding(new Insets(24));
        card.getStyleClass().add("edu-card");

        VBox titleBox = new VBox(2);
        Label formTitle = new Label("Input Nilai");
        formTitle.getStyleClass().add("edu-title");
        formTitle.setFont(Font.font("Geist", FontWeight.BOLD, 15));
        Label formSubtitle = new Label("Klik siswa di tabel untuk input nilai");
        formSubtitle.getStyleClass().add("edu-text-muted");
        formSubtitle.setFont(Font.font("Geist", 11));
        titleBox.getChildren().addAll(formTitle, formSubtitle);

        Separator sep = new Separator();
        sep.getStyleClass().add("edu-separator");

        Label hint = new Label("Pilih kelas di filter, lalu klik siswa");
        hint.setFont(Font.font("Geist", 12));
        hint.setTextFill(Color.web("#a1a1aa"));
        hint.setWrapText(true);

        lblInfo = new Label("");
        lblInfo.setWrapText(true);
        lblInfo.setFont(Font.font("Geist", 12));

        card.getChildren().addAll(titleBox, sep, hint, lblInfo);
        return card;
    }

    private void showGradeForm(Siswa siswa) {
        selectedSiswa = siswa;
        formContainer.getChildren().clear();
        nilaiFields.clear();

        List<Mapel> siswaMapel = mapelDAO.getMapelByKelas(siswa.getKelas());

        VBox titleBox = new VBox(2);
        Label formTitle = new Label("Input Nilai");
        formTitle.getStyleClass().add("edu-title");
        formTitle.setFont(Font.font("Geist", FontWeight.BOLD, 15));
        Label formSubtitle = new Label("Edit nilai untuk siswa yang dipilih");
        formSubtitle.getStyleClass().add("edu-text-muted");
        formSubtitle.setFont(Font.font("Geist", 11));
        titleBox.getChildren().addAll(formTitle, formSubtitle);

        Separator sep = new Separator();
        sep.getStyleClass().add("edu-separator");

        VBox infoBox = new VBox(4);
        infoBox.setPadding(new Insets(12));
        infoBox.setStyle("-fx-background-color: rgba(59,130,246,0.1); -fx-background-radius: 8;");

        Label nameLabel = new Label(siswa.getNama());
        nameLabel.setFont(Font.font("Geist", FontWeight.BOLD, 14));
        nameLabel.setTextFill(Color.web("#3b82f6"));

        Label detailLabel = new Label("NIS: " + siswa.getNis() + "  |  Kelas: " + siswa.getKelas());
        detailLabel.setFont(Font.font("Geist", 11));
        detailLabel.setTextFill(Color.web("#a1a1aa"));

        Label mapelCount = new Label(siswaMapel.size() + " mata pelajaran");
        mapelCount.setFont(Font.font("Geist", 10));
        mapelCount.setTextFill(Color.web("#71717a"));

        infoBox.getChildren().addAll(nameLabel, detailLabel, mapelCount);

        formContainer.getChildren().addAll(titleBox, sep, infoBox);

        for (Mapel mapel : siswaMapel) {
            Label lbl = new Label(mapel.getNamaMapel());
            lbl.getStyleClass().add("edu-label");
            lbl.setPadding(new Insets(4, 0, 2, 0));

            TextField tf = new TextField();
            tf.setPromptText("Masukkan nilai " + mapel.getNamaMapel());
            tf.getStyleClass().add("edu-input");

            double existing = siswa.getNilai(mapel.getNamaMapel());
            if (existing > 0) {
                tf.setText(String.format(Locale.US, "%.0f", existing));
            }

            tf.setTextFormatter(new TextFormatter<>(change -> {
                String text = change.getControlNewText();
                if (text.matches("\\d*(\\.\\d*)?")) {
                    return change;
                }
                return null;
            }));

            nilaiFields.put(mapel.getId(), tf);
            formContainer.getChildren().addAll(lbl, tf);
        }

        lblInfo = new Label("");
        lblInfo.setWrapText(true);
        lblInfo.setFont(Font.font("Geist", 12));

        HBox btnRow = new HBox(8);

        Button btnSimpan = new Button("Simpan Nilai");
        btnSimpan.getStyleClass().add("edu-btn-primary");
        btnSimpan.setFont(Font.font("Geist", FontWeight.SEMI_BOLD, 12));
        HBox.setHgrow(btnSimpan, Priority.ALWAYS);
        btnSimpan.setMaxWidth(Double.MAX_VALUE);
        AnimationUtils.addPressAnimation(btnSimpan);
        btnSimpan.setOnAction(e -> handleSimpanNilai());

        Button btnBatal = new Button("Batal");
        btnBatal.getStyleClass().add("edu-btn-outline");
        btnBatal.setFont(Font.font("Geist", FontWeight.SEMI_BOLD, 12));
        HBox.setHgrow(btnBatal, Priority.ALWAYS);
        btnBatal.setMaxWidth(Double.MAX_VALUE);
        AnimationUtils.addPressAnimation(btnBatal);
        btnBatal.setOnAction(e -> {
            selectedSiswa = null;
            tableView.getSelectionModel().clearSelection();
            formContainer.getChildren().clear();
            formContainer.getChildren().addAll(buildFormCard().getChildren());
        });

        btnRow.getChildren().addAll(btnBatal, btnSimpan);

        formContainer.getChildren().addAll(lblInfo, btnRow);
    }

    private void handleSimpanNilai() {
        if (selectedSiswa == null) return;

        for (Map.Entry<Integer, TextField> entry : nilaiFields.entrySet()) {
            int mapelId = entry.getKey();
            String text = entry.getValue().getText().trim();
            double nilai = 0;
            if (!text.isEmpty()) {
                try {
                    nilai = Double.parseDouble(text);
                    if (nilai < 0 || nilai > 100) {
                        showInfo("Nilai harus antara 0-100!", "#ef4444");
                        return;
                    }
                } catch (NumberFormatException e) {
                    showInfo("Format nilai tidak valid!", "#ef4444");
                    return;
                }
            }
            if (!nilaiDAO.simpanNilai(selectedSiswa.getId(), mapelId, nilai)) {
                showInfo("Gagal menyimpan beberapa nilai.", "#ef4444");
                return;
            }
        }

        showInfo("Nilai berhasil disimpan!", "#22C55E");
        masterData = siswaDAO.getAllSiswa(); // re-fetch
        refreshTableView();
    }

    private void showInfo(String msg, String color) {
        lblInfo.setText(msg);
        lblInfo.setTextFill(Color.web(color));
    }

    private HBox buildStatsRow() {
        HBox row = new HBox(16);

        int totalSiswa = masterData.size();
        long lulus = masterData.stream().filter(Siswa::isLulus).count();
        long tidakLulus = totalSiswa - lulus;
        double avgKelulusan = totalSiswa == 0 ? 0 : (double) lulus / totalSiswa * 100;

        Siswa highest = null, lowest = null;
        for (Siswa s : masterData) {
            if (s.hitungRataRata() <= 0) continue;
            if (highest == null || s.hitungRataRata() > highest.hitungRataRata()) highest = s;
            if (lowest == null || s.hitungRataRata() < lowest.hitungRataRata()) lowest = s;
        }

        double avgAll = totalSiswa == 0 ? 0 : masterData.stream()
                .filter(s -> s.hitungRataRata() > 0)
                .mapToDouble(Siswa::hitungRataRata).average().orElse(0.0);

        String highName = highest != null ? highest.getNama().split(" ")[0] + " (" + String.format("%.1f", highest.hitungRataRata()) + ")" : "-";
        String lowName = lowest != null ? lowest.getNama().split(" ")[0] + " (" + String.format("%.1f", lowest.hitungRataRata()) + ")" : "-";

        row.getChildren().addAll(
                createStatCard(String.valueOf(lulus), "Siswa Lulus", String.format(Locale.US, "%.0f%% kelulusan", avgKelulusan), "#10b981", null),
                createStatCard(String.valueOf(tidakLulus), "Tidak Lulus", "Perlu bimbingan", "#ef4444", null),
                createStatCard(String.format(Locale.US, "%.1f", avgAll), "Rata-rata", String.valueOf(mapelDAO.getAllMapel().size()) + " mapel", "#3b82f6", null),
                createStatCard(null, "Tertinggi", highName, "#f59e0b",
                        new String[]{"M6 9.5l6-5.5 6 5.5", "M6 16l6-5.5 6 5.5"}),
                createStatCard(null, "Terendah", lowName, "#8b5cf6",
                        new String[]{"M6 8.5l6 5.5 6-5.5", "M6 15l6 5.5 6-5.5"})
        );
        return row;
    }

    private VBox createStatCard(String value, String title, String subtitle, String color, String[] svgPaths) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(14, 16, 14, 16));
        card.getStyleClass().add("edu-card");
        HBox.setHgrow(card, Priority.ALWAYS);

        if (svgPaths != null) {
            javafx.scene.Group iconGroup = new javafx.scene.Group();
            for (String pathData : svgPaths) {
                javafx.scene.shape.SVGPath svg = new javafx.scene.shape.SVGPath();
                svg.setContent(pathData);
                svg.setFill(Color.TRANSPARENT);
                svg.setStroke(Color.web(color));
                svg.setStrokeWidth(2.2);
                svg.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
                svg.setStrokeLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
                iconGroup.getChildren().add(svg);
            }
            StackPane iconWrapper = new StackPane(iconGroup);
            iconWrapper.setMinSize(24, 24);
            iconWrapper.setMaxSize(24, 24);
            iconWrapper.setAlignment(Pos.CENTER_LEFT);
            card.getChildren().add(iconWrapper);
        } else {
            Label lblVal = new Label(value);
            lblVal.setFont(Font.font("Geist", FontWeight.BOLD, 20));
            lblVal.setStyle("-fx-text-fill: " + color + ";");
            card.getChildren().add(lblVal);
        }

        Label lblTitle = new Label(title);
        lblTitle.setFont(Font.font("Geist", FontWeight.MEDIUM, 11));
        lblTitle.getStyleClass().add("edu-text-muted");

        Label lblSub = new Label(subtitle);
        lblSub.setFont(Font.font("Geist", 10));
        lblSub.setStyle("-fx-text-fill: #52525b;");
        lblSub.setWrapText(true);

        card.getChildren().addAll(lblTitle, lblSub);
        return card;
    }
}
