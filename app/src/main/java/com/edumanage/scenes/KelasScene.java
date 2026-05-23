package com.edumanage.scenes;

import com.edumanage.dao.KelasDAO;
import com.edumanage.models.Kelas;
import com.edumanage.utils.AnimationUtils;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.List;
import java.util.stream.Collectors;

public class KelasScene {

    private KelasDAO kelasDAO;
    private TableView<Kelas> tableView;
    private ObservableList<Kelas> masterData;

    private TextField tfKodeKelas;
    private TextField tfWaliKelas;
    private Label lblInfo;
    private Kelas selectedKelas = null;

    private int currentPage = 1;
    private int itemsPerPage = 10;
    private String searchText = "";
    private Label paginationLabel;
    private HBox pageButtonsBox;

    public KelasScene() {
        kelasDAO = new KelasDAO();
    }

    public VBox getView() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(32));
        content.getStyleClass().add("edu-content");

        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label title = new Label("Data Kelas");
        title.getStyleClass().add("edu-title");
        title.setFont(Font.font("Geist", FontWeight.BOLD, 24));

        Label subtitle = new Label("Kelola daftar kelas dan informasi wali kelas");
        subtitle.getStyleClass().add("edu-subtitle");

        titleBox.getChildren().addAll(title, subtitle);
        header.getChildren().add(titleBox);

        tableView = buildTable();
        masterData = kelasDAO.getAllKelas();

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
        searchField.setPromptText("Cari kelas atau wali kelas...");
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
        btnExport.setOnAction(e -> com.edumanage.utils.CsvExporter.exportToCsv(tableView, "Data_Kelas", tableView.getScene().getWindow()));
        
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

        HBox mainArea = new HBox(16);
        VBox.setVgrow(mainArea, Priority.ALWAYS);
        mainArea.getChildren().addAll(tableWrapper, formScroll);

        content.getChildren().addAll(header, topControls, mainArea);

        refreshTableView();

        FadeTransition ft = new FadeTransition(Duration.millis(400), content);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();

        return content;
    }

    private TableView<Kelas> buildTable() {
        TableView<Kelas> tv = new TableView<>();
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tv.getStyleClass().add("edu-table");
        tv.setPlaceholder(new Label("Belum ada data kelas"));

        TableColumn<Kelas, String> colKode = new TableColumn<>("Kode Kelas");
        colKode.setCellValueFactory(new PropertyValueFactory<>("kodeKelas"));
        colKode.setPrefWidth(120);

        TableColumn<Kelas, String> colWali = new TableColumn<>("Wali Kelas");
        colWali.setCellValueFactory(new PropertyValueFactory<>("waliKelas"));
        colWali.setPrefWidth(250);

        tv.getColumns().addAll(colKode, colWali);
        tv.sortPolicyProperty().set(t -> {
            java.util.Comparator<Kelas> comparator = (java.util.Comparator<Kelas>) t.getComparator();
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
        List<Kelas> filtered = masterData.stream()
                .filter(k -> {
                    if (searchText.isEmpty()) return true;
                    String lower = searchText.toLowerCase();
                    return (k.getKodeKelas() != null && k.getKodeKelas().toLowerCase().contains(lower))
                            || (k.getWaliKelas() != null && k.getWaliKelas().toLowerCase().contains(lower));
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
            Button pageBtn = new Button(String.valueOf(i));
            pageBtn.getStyleClass().add(i == currentPage ? "edu-page-btn-active" : "edu-page-btn");
            int pageNum = i;
            pageBtn.setOnAction(e -> { currentPage = pageNum; refreshTableView(); });
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
        VBox card = new VBox(20);
        card.getStyleClass().add("edu-card");
        card.setPadding(new Insets(24));

        Label formTitle = new Label("Form Data Kelas");
        formTitle.getStyleClass().add("edu-title");
        formTitle.setFont(Font.font("Geist", FontWeight.BOLD, 16));

        tfKodeKelas = makeField("Kode Kelas (cth: X-A)");
        tfWaliKelas = makeField("Wali Kelas");

        GridPane grid = new GridPane();
        grid.setVgap(16);
        grid.setHgap(12);
        grid.getColumnConstraints().addAll(
                new ColumnConstraints(100),
                new ColumnConstraints(180, 180, Double.MAX_VALUE, Priority.ALWAYS, javafx.geometry.HPos.LEFT, true)
        );

        grid.addRow(0, makeLabel("Kode Kelas"), tfKodeKelas);
        grid.addRow(1, makeLabel("Wali Kelas"), tfWaliKelas);

        lblInfo = new Label("");
        lblInfo.setWrapText(true);
        lblInfo.setFont(Font.font("Geist", 12));
        lblInfo.setMinHeight(Region.USE_PREF_SIZE);

        HBox btnRow = new HBox(12);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        Button btnSimpan = new Button("Simpan");
        btnSimpan.getStyleClass().add("edu-btn-primary");
        btnSimpan.setFont(Font.font("Geist", FontWeight.SEMI_BOLD, 13));
        AnimationUtils.addPressAnimation(btnSimpan);

        Button btnBatal = new Button("Batal");
        btnBatal.getStyleClass().add("edu-btn-outline");
        btnBatal.setFont(Font.font("Geist", FontWeight.SEMI_BOLD, 13));
        AnimationUtils.addPressAnimation(btnBatal);

        Button btnHapus = new Button("Hapus");
        btnHapus.getStyleClass().add("edu-btn-danger");
        btnHapus.setFont(Font.font("Geist", FontWeight.SEMI_BOLD, 13));
        btnHapus.setDisable(true); 
        AnimationUtils.addPressAnimation(btnHapus);

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            btnHapus.setDisable(sel == null);
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnRow.getChildren().addAll(btnHapus, spacer, btnBatal, btnSimpan);

        btnSimpan.setOnAction(e -> handleSimpan());
        btnBatal.setOnAction(e -> clearForm());
        btnHapus.setOnAction(e -> handleHapus());

        card.getChildren().addAll(formTitle, grid, lblInfo, btnRow);
        return card;
    }

    private Label makeLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Geist", 13));
        l.getStyleClass().add("edu-label");
        l.setPadding(new Insets(4, 0, 2, 0));
        return l;
    }

    private TextField makeField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.getStyleClass().add("edu-input");
        return tf;
    }

    private void populateForm(Kelas k) {
        selectedKelas = k;
        tfKodeKelas.setText(k.getKodeKelas());
        tfWaliKelas.setText(k.getWaliKelas());
        lblInfo.setText("");
    }

    private void handleSimpan() {
        String kode = tfKodeKelas.getText();
        String wali = tfWaliKelas.getText();

        if (kode == null || kode.trim().isEmpty()) {
            lblInfo.setText("Kode kelas tidak boleh kosong.");
            lblInfo.setStyle("-fx-text-fill: #ef4444;");
            return;
        }

        boolean isNew = (selectedKelas == null);
        Kelas k = isNew ? new Kelas(kode, wali) : selectedKelas;
        
        if (!isNew) {
            k.setKodeKelas(kode);
            k.setWaliKelas(wali);
        }

        boolean success = isNew ? kelasDAO.tambahKelas(k) : kelasDAO.updateKelas(k);

        if (success) {
            lblInfo.setText("Data berhasil disimpan!");
            lblInfo.setStyle("-fx-text-fill: #10b981;");
            masterData = kelasDAO.getAllKelas(); 
            refreshTableView();
            clearForm();
        } else {
            lblInfo.setText("Gagal menyimpan data. Mungkin kode duplikat?");
            lblInfo.setStyle("-fx-text-fill: #ef4444;");
        }
    }

    private void handleHapus() {
        if (selectedKelas != null) {
            boolean success = kelasDAO.hapusKelas(selectedKelas.getId());
            if (success) {
                lblInfo.setText("Data berhasil dihapus.");
                lblInfo.setStyle("-fx-text-fill: #10b981;");
                masterData = kelasDAO.getAllKelas(); 
                refreshTableView();
                clearForm();
            } else {
                lblInfo.setText("Gagal menghapus data.");
                lblInfo.setStyle("-fx-text-fill: #ef4444;");
            }
        }
    }

    private void clearForm() {
        selectedKelas = null;
        tfKodeKelas.clear();
        tfWaliKelas.clear();
        tableView.getSelectionModel().clearSelection();
    }
}
