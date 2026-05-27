package com.edumanage.scenes;

import com.edumanage.dao.SiswaDAO;
import com.edumanage.dao.KelasDAO;
import com.edumanage.models.Siswa;
import com.edumanage.models.Kelas;
import com.edumanage.utils.AnimationUtils;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class SiswaScene {

    private Stage stage;
    private DashboardScene dashboard;
    private SiswaDAO siswaDAO = new SiswaDAO();
    private TableView<Siswa> tableView;
    private ObservableList<Siswa> masterData;

    private TextField tfNama, tfEmail, tfNis;
    private ComboBox<String> cbKelas;
    private Label lblInfo;
    private Siswa selectedSiswa = null;

    private int currentPage = 1;
    private int itemsPerPage = 10;
    private String searchText = "";
    private Label paginationLabel;
    private HBox pageButtonsBox;
    private HBox statsRow;

    public SiswaScene(Stage stage, DashboardScene dashboard) {
        this.stage = stage;
        this.dashboard = dashboard;
    }

    public VBox getContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(32));
        content.getStyleClass().add("edu-content");

        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label title = new Label("Data Siswa");
        title.getStyleClass().add("edu-title");
        title.setFont(Font.font("Geist", FontWeight.BOLD, 24));

        Label subtitle = new Label("Kelola data siswa dan informasi akademik");
        subtitle.getStyleClass().add("edu-subtitle");

        titleBox.getChildren().addAll(title, subtitle);
        header.getChildren().add(titleBox);

        tableView = buildTable();
        masterData = siswaDAO.getAllSiswa();

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) populateForm(sel);
        });

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
        btnExport.setOnAction(e -> com.edumanage.utils.CsvExporter.exportToCsv(tableView, "Data_Siswa", stage));
        
        HBox topControls = new HBox(12);
        topControls.setAlignment(Pos.CENTER_LEFT);
        topControls.getChildren().addAll(searchBar, spacer, btnExport);

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

        VBox tableWrapper = new VBox(0);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        VBox.setVgrow(tableWrapper, Priority.ALWAYS);
        HBox.setHgrow(tableWrapper, Priority.ALWAYS);
        tableWrapper.getChildren().addAll(searchBar, tableView, pagination);

        ScrollPane formScroll = new ScrollPane();
        formScroll.getStyleClass().add("edu-scroll");
        formScroll.setFitToWidth(true);
        formScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        formScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        formScroll.setPrefWidth(340);
        formScroll.setMinWidth(340);

        VBox formCard = buildFormCard();
        formScroll.setContent(formCard);

        statsRow = buildStatsCards();

        HBox mainArea = new HBox(16);
        VBox.setVgrow(mainArea, Priority.ALWAYS);
        mainArea.getChildren().addAll(tableWrapper, formScroll);

        content.getChildren().addAll(header, statsRow, topControls, mainArea);

        refreshTableView();

        FadeTransition ft = new FadeTransition(Duration.millis(400), content);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();

        return content;
    }

    @SuppressWarnings("unchecked")
    private TableView<Siswa> buildTable() {
        TableView<Siswa> tv = new TableView<>();
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tv.getStyleClass().add("edu-table");
        tv.setPlaceholder(new Label("Belum ada data siswa"));

        TableColumn<Siswa, String> colNama = new TableColumn<>("Nama Siswa");
        colNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colNama.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    HBox box = new HBox(12);
                    box.setAlignment(Pos.CENTER_LEFT);
                    
                    Label avatar = new Label(item.substring(0, Math.min(2, item.length())).toUpperCase());
                    avatar.setMinSize(28, 28);
                    avatar.setMaxSize(28, 28);
                    avatar.setAlignment(Pos.CENTER);
                    String[] colors = {"#3b82f6", "#10b981", "#8b5cf6", "#f59e0b", "#ef4444"};
                    String color = colors[item.length() % colors.length];
                    avatar.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-background-radius: 14; -fx-font-weight: bold; -fx-font-size: 11px;");
                    
                    Label nameLbl = new Label(item);
                    nameLbl.setStyle("-fx-text-fill: -edu-text; -fx-font-weight: 500;");
                    
                    box.getChildren().addAll(avatar, nameLbl);
                    setGraphic(box);
                }
            }
        });

        TableColumn<Siswa, String> colNis = new TableColumn<>("NIS");
        colNis.setCellValueFactory(new PropertyValueFactory<>("nis"));
        colNis.setMaxWidth(100);

        TableColumn<Siswa, String> colKelas = new TableColumn<>("Kelas");
        colKelas.setCellValueFactory(new PropertyValueFactory<>("kelas"));
        colKelas.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label lbl = new Label(item);
                    lbl.getStyleClass().add("edu-badge-kelas");
                    setGraphic(lbl);
                }
            }
        });
        colKelas.setMaxWidth(100);

        TableColumn<Siswa, String> colRata = new TableColumn<>("Rata-rata");
        colRata.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                String.format(Locale.US, "%.1f", data.getValue().hitungRataRata())));
        colRata.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    double val = getTableView().getItems().get(getIndex()).hitungRataRata();
                    String color = val >= 80 ? "#3b82f6" : val >= 70 ? "#f59e0b" : "#ef4444";
                    
                    Label num = new Label(item);
                    num.setStyle("-fx-font-weight: bold; -fx-text-fill: -edu-text;");
                    num.setPrefWidth(35);
                    
                    javafx.scene.shape.Rectangle barBg = new javafx.scene.shape.Rectangle(40, 4, Color.web("#27272a"));
                    barBg.setArcWidth(4); barBg.setArcHeight(4);
                    javafx.scene.shape.Rectangle barFg = new javafx.scene.shape.Rectangle(40 * (val/100.0), 4, Color.web(color));
                    barFg.setArcWidth(4); barFg.setArcHeight(4);
                    
                    StackPane bar = new StackPane(barBg, barFg);
                    bar.setAlignment(Pos.CENTER_LEFT);
                    
                    HBox box = new HBox(8, num, bar);
                    box.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(box);
                }
            }
        });
        colRata.setMaxWidth(130);
        colRata.setComparator((s1, s2) -> {
            try {
                return Double.compare(Double.parseDouble(s1), Double.parseDouble(s2));
            } catch (Exception e) {
                return s1.compareTo(s2);
            }
        });

        TableColumn<Siswa, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().isLulus() ? "Lulus" : "Tidak Lulus"));
        colStatus.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    boolean lulus = item.equals("Lulus");
                    Label pill = new Label(item);
                    pill.getStyleClass().add(lulus ? "edu-badge-lulus" : "edu-badge-gagal");
                    
                    javafx.scene.shape.Circle dot = new javafx.scene.shape.Circle(3, Color.web(lulus ? "#22c55e" : "#ef4444"));
                    pill.setGraphic(dot);
                    pill.setGraphicTextGap(6);
                    setGraphic(pill);
                }
            }
        });
        colStatus.setMaxWidth(110);

        tv.getColumns().addAll(colNama, colNis, colKelas, colRata, colStatus);
        
        tv.sortPolicyProperty().set(t -> {
            java.util.Comparator<Siswa> comparator = (java.util.Comparator<Siswa>) t.getComparator();
            if (comparator != null) {
                FXCollections.sort(masterData, comparator);
                currentPage = 1;
                refreshTableView();
            }
            return true;
        });

        return tv;
    }

    private void refreshTableView() {
        List<Siswa> filtered = masterData.stream()
                .filter(s -> {
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
            paginationLabel.setText(String.format("Menampilkan %d-%d dari %d data",
                    fromIndex + 1, toIndex, totalItems));
        } else {
            paginationLabel.setText("Tidak ada data");
        }

        if (statsRow != null) {
            statsRow.getChildren().setAll(buildStatsCards().getChildren());
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
        Label formTitle = new Label("Form Data Siswa");
        formTitle.getStyleClass().add("edu-title");
        formTitle.setFont(Font.font("Geist", FontWeight.BOLD, 15));
        
        Label formSubtitle = new Label("Klik baris tabel untuk mulai edit");
        formSubtitle.getStyleClass().add("edu-text-muted");
        formSubtitle.setFont(Font.font("Geist", 11));
        titleBox.getChildren().addAll(formTitle, formSubtitle);

        Separator sep = new Separator();
        sep.getStyleClass().add("edu-separator");

        tfNama = makeField("Masukkan nama siswa");
        tfNis = makeField("Nomor Induk Siswa");
        tfEmail = makeField("email@edumanage.id");
        cbKelas = new ComboBox<>();
        cbKelas.setPromptText("Pilih Kelas");
        cbKelas.getStyleClass().add("edu-combo-box");
        cbKelas.setMaxWidth(Double.MAX_VALUE);
        loadKelasOptions();

        lblInfo = new Label("");
        lblInfo.setWrapText(true);
        lblInfo.setFont(Font.font("Geist", 12));

        Label nilaiHint = new Label("💡 Input nilai dilakukan di menu \"Rapor & Nilai\"");
        nilaiHint.setWrapText(true);
        nilaiHint.setFont(Font.font("Geist", 11));
        nilaiHint.setTextFill(Color.web("#a1a1aa"));
        nilaiHint.setPadding(new Insets(8, 12, 8, 12));
        nilaiHint.setStyle("-fx-background-color: rgba(63,63,70,0.3); -fx-background-radius: 8;");

        HBox btnRow = new HBox(8);

        Button btnSimpan = new Button("Simpan");
        btnSimpan.getStyleClass().add("edu-btn-primary");
        btnSimpan.setFont(Font.font("Geist", FontWeight.SEMI_BOLD, 12));
        HBox.setHgrow(btnSimpan, Priority.ALWAYS);
        btnSimpan.setMaxWidth(Double.MAX_VALUE);
        AnimationUtils.addPressAnimation(btnSimpan);

        Button btnHapus = new Button("Hapus");
        btnHapus.getStyleClass().add("edu-btn-destructive");
        btnHapus.setFont(Font.font("Geist", FontWeight.SEMI_BOLD, 12));
        HBox.setHgrow(btnHapus, Priority.ALWAYS);
        btnHapus.setMaxWidth(Double.MAX_VALUE);
        AnimationUtils.addPressAnimation(btnHapus);

        Button btnBatal = new Button("Batal");
        btnBatal.getStyleClass().add("edu-btn-outline");
        btnBatal.setFont(Font.font("Geist", FontWeight.SEMI_BOLD, 12));
        HBox.setHgrow(btnBatal, Priority.ALWAYS);
        btnBatal.setMaxWidth(Double.MAX_VALUE);
        AnimationUtils.addPressAnimation(btnBatal);

        btnRow.getChildren().addAll(btnBatal, btnHapus, btnSimpan);

        btnSimpan.setOnAction(e -> {
            try {
                Siswa s = buildSiswaFromForm();
                if (selectedSiswa != null) {
                    s.setId(selectedSiswa.getId());
                    siswaDAO.updateSiswa(s);
                    showInfo("Data siswa berhasil diperbarui.", "#22C55E");
                } else {
                    siswaDAO.tambahSiswa(s);
                    showInfo("Siswa baru berhasil ditambahkan.", "#22C55E");
                }
                masterData = siswaDAO.getAllSiswa();
                refreshTableView();
                clearForm();
            } catch (Exception ex) {
                showInfo("Error: " + ex.getMessage(), "#EF4444");
            }
        });

        btnHapus.setOnAction(e -> {
            if (selectedSiswa != null) {
                String nama = selectedSiswa.getNama();
                siswaDAO.hapusSiswa(selectedSiswa.getId());
                showInfo("Data " + nama + " berhasil dihapus.", "#22C55E");
                masterData = siswaDAO.getAllSiswa();
                refreshTableView();
                clearForm();
            } else {
                showInfo("Pilih siswa yang ingin dihapus.", "#F59E0B");
            }
        });

        btnBatal.setOnAction(e -> {
            selectedSiswa = null;
            tableView.getSelectionModel().clearSelection();
            clearForm();
            showInfo("Isi form untuk menambah siswa baru.", "#3B82F6");
        });

        card.getChildren().addAll(
                titleBox, sep,
                makeLabel("Nama Lengkap *"), tfNama,
                makeLabel("NIS *"), tfNis,
                makeLabel("Email"), tfEmail,
                makeLabel("Kelas *"), cbKelas,
                nilaiHint,
                lblInfo, btnRow
        );
        return card;
    }

    private void populateForm(Siswa s) {
        selectedSiswa = s;
        tfNama.setText(s.getNama());
        tfNis.setText(s.getNis());
        tfEmail.setText(s.getEmail());
        cbKelas.setValue(s.getKelas());
        showInfo(s.getInfoLengkap(), "#3B82F6");
    }

    private Siswa buildSiswaFromForm() {
        if (tfNama.getText().isBlank() || tfNis.getText().isBlank()) {
            throw new IllegalArgumentException("Nama dan NIS tidak boleh kosong!");
        }
        if (cbKelas.getValue() == null) {
            throw new IllegalArgumentException("Kelas harus dipilih!");
        }
        return new Siswa(
                tfNama.getText().trim(),
                tfEmail.getText().trim(),
                tfNis.getText().trim(),
                cbKelas.getValue()
        );
    }

    private void clearForm() {
        selectedSiswa = null;
        tfNama.clear();
        tfNis.clear();
        tfEmail.clear();
        cbKelas.setValue(null);
    }

    private void showInfo(String msg, String color) {
        lblInfo.setText(msg);
        lblInfo.setTextFill(Color.web(color));
    }

    private TextField makeField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.getStyleClass().add("edu-input");
        return tf;
    }

    private Label makeLabel(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("edu-label");
        l.setPadding(new Insets(4, 0, 2, 0));
        return l;
    }

    private void loadKelasOptions() {
        KelasDAO kDao = new KelasDAO();
        List<String> kList = kDao.getAllKelas().stream().map(Kelas::getKodeKelas).collect(Collectors.toList());
        cbKelas.setItems(FXCollections.observableArrayList(kList));
    }

    private HBox buildStatsCards() {
        HBox row = new HBox(16);
        
        int totalSiswa = masterData.size();
        int totalKelas = new KelasDAO().getAllKelas().size();
        
        long lulus = masterData.stream().filter(Siswa::isLulus).count();
        long tidakLulus = totalSiswa - lulus;
        
        double avgKelulusan = totalSiswa == 0 ? 0 : (double) lulus / totalSiswa * 100;
        
        Siswa highest = null;
        for (Siswa s : masterData) {
            if (highest == null || s.hitungRataRata() > highest.hitungRataRata()) {
                highest = s;
            }
        }
        double avgAll = totalSiswa == 0 ? 0 : masterData.stream().mapToDouble(Siswa::hitungRataRata).average().orElse(0.0);
        String highestText = highest != null ? String.format("Tertinggi: %s (%.1f)", highest.getNama().split(" ")[0], highest.hitungRataRata()) : "-";
        
        row.getChildren().addAll(
            createStatCard(String.valueOf(totalSiswa), "Total Siswa", "Dari " + totalKelas + " kelas aktif", "#3b82f6"),
            createStatCard(String.valueOf(lulus), "Siswa Lulus", String.format(Locale.US, "%.0f%% tingkat kelulusan", avgKelulusan), "#10b981"),
            createStatCard(String.valueOf(tidakLulus), "Tidak Lulus", "Perlu bimbingan khusus", "#ef4444"),
            createStatCard(String.format(Locale.US, "%.1f", avgAll), "Rata-rata Nilai", highestText, "#8b5cf6")
        );
        return row;
    }

    private VBox createStatCard(String value, String title, String subtitle, String iconColor) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(16, 20, 16, 20));
        card.getStyleClass().add("edu-card");
        HBox.setHgrow(card, Priority.ALWAYS);
        
        Label lblVal = new Label(value);
        lblVal.setFont(Font.font("Geist", FontWeight.BOLD, 22));
        lblVal.setStyle("-fx-text-fill: " + iconColor + ";");
        
        Label lblTitle = new Label(title);
        lblTitle.setFont(Font.font("Geist", FontWeight.MEDIUM, 12));
        lblTitle.getStyleClass().add("edu-text-muted");
        
        Label lblSub = new Label(subtitle);
        lblSub.setFont(Font.font("Geist", 11));
        lblSub.setStyle("-fx-text-fill: #52525b;");
        
        card.getChildren().addAll(lblVal, lblTitle, lblSub);
        return card;
    }
}
