package com.edumanage.scenes;

import com.edumanage.dao.MapelDAO;
import com.edumanage.models.Mapel;
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

public class MapelScene {

    private MapelDAO mapelDAO;
    private TableView<Mapel> tableView;
    private ObservableList<Mapel> masterData;

    private TextField tfKodeMapel;
    private TextField tfNamaMapel;
    private Label lblInfo;
    private Mapel selectedMapel = null;

    private int currentPage = 1;
    private int itemsPerPage = 10;
    private String searchText = "";
    private Label paginationLabel;
    private HBox pageButtonsBox;

    public MapelScene() {
        mapelDAO = new MapelDAO();
    }

    public VBox getView() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(32));
        content.getStyleClass().add("edu-content");

        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label title = new Label("Data Mata Pelajaran");
        title.getStyleClass().add("edu-title");
        title.setFont(Font.font("Geist", FontWeight.BOLD, 24));

        Label subtitle = new Label("Kelola daftar mata pelajaran yang diajarkan");
        subtitle.getStyleClass().add("edu-subtitle");

        titleBox.getChildren().addAll(title, subtitle);
        header.getChildren().add(titleBox);

        tableView = buildTable();
        masterData = mapelDAO.getAllMapel();

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
        searchField.setPromptText("Cari mata pelajaran...");
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
        btnExport.setOnAction(e -> com.edumanage.utils.CsvExporter.exportToCsv(tableView, "Data_Mapel", tableView.getScene().getWindow()));
        
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

    private TableView<Mapel> buildTable() {
        TableView<Mapel> tv = new TableView<>();
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tv.getStyleClass().add("edu-table");
        tv.setPlaceholder(new Label("Belum ada data mata pelajaran"));

        TableColumn<Mapel, String> colKode = new TableColumn<>("Kode Mapel");
        colKode.setCellValueFactory(new PropertyValueFactory<>("kodeMapel"));
        colKode.setPrefWidth(120);

        TableColumn<Mapel, String> colNama = new TableColumn<>("Nama Mata Pelajaran");
        colNama.setCellValueFactory(new PropertyValueFactory<>("namaMapel"));
        colNama.setPrefWidth(250);

        tv.getColumns().addAll(colKode, colNama);
        tv.sortPolicyProperty().set(t -> {
            java.util.Comparator<Mapel> comparator = (java.util.Comparator<Mapel>) t.getComparator();
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
        List<Mapel> filtered = masterData.stream()
                .filter(m -> {
                    if (searchText.isEmpty()) return true;
                    String lower = searchText.toLowerCase();
                    return m.getKodeMapel().toLowerCase().contains(lower)
                            || m.getNamaMapel().toLowerCase().contains(lower);
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

        Label formTitle = new Label("Form Data Mapel");
        formTitle.getStyleClass().add("edu-title");
        formTitle.setFont(Font.font("Geist", FontWeight.BOLD, 15));

        Separator sep = new Separator();
        sep.getStyleClass().add("edu-separator");

        tfKodeMapel = makeField("Kode Mata Pelajaran");
        tfNamaMapel = makeField("Nama Mata Pelajaran");

        lblInfo = new Label("");
        lblInfo.setWrapText(true);
        lblInfo.setFont(Font.font("Geist", 12));

        HBox btnRow = new HBox(8);

        Button btnSimpan = new Button("Simpan");
        btnSimpan.getStyleClass().add("edu-btn-primary");
        btnSimpan.setFont(Font.font("Geist", FontWeight.SEMI_BOLD, 12));
        HBox.setHgrow(btnSimpan, Priority.ALWAYS);
        btnSimpan.setMaxWidth(Double.MAX_VALUE);
        AnimationUtils.addPressAnimation(btnSimpan);
        btnSimpan.setOnAction(e -> handleSimpan());

        Button btnHapus = new Button("Hapus");
        btnHapus.getStyleClass().add("edu-btn-destructive");
        btnHapus.setFont(Font.font("Geist", FontWeight.SEMI_BOLD, 12));
        HBox.setHgrow(btnHapus, Priority.ALWAYS);
        btnHapus.setMaxWidth(Double.MAX_VALUE);
        AnimationUtils.addPressAnimation(btnHapus);
        btnHapus.setOnAction(e -> handleHapus());

        Button btnBatal = new Button("Batal");
        btnBatal.getStyleClass().add("edu-btn-outline");
        btnBatal.setFont(Font.font("Geist", FontWeight.SEMI_BOLD, 12));
        HBox.setHgrow(btnBatal, Priority.ALWAYS);
        btnBatal.setMaxWidth(Double.MAX_VALUE);
        AnimationUtils.addPressAnimation(btnBatal);
        btnBatal.setOnAction(e -> clearForm());

        btnRow.getChildren().addAll(btnSimpan, btnHapus, btnBatal);

        card.getChildren().addAll(
                formTitle, sep,
                makeLabel("Kode Mata Pelajaran"), tfKodeMapel,
                makeLabel("Nama Mata Pelajaran"), tfNamaMapel,
                lblInfo, btnRow
        );

        return card;
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

    private void populateForm(Mapel mapel) {
        selectedMapel = mapel;
        tfKodeMapel.setText(mapel.getKodeMapel());
        tfNamaMapel.setText(mapel.getNamaMapel());
        showInfo("Mengedit mapel: " + mapel.getKodeMapel(), "#3B82F6");
    }

    private void clearForm() {
        selectedMapel = null;
        tfKodeMapel.clear();
        tfNamaMapel.clear();
        tableView.getSelectionModel().clearSelection();
        lblInfo.setText("");
    }

    private void handleSimpan() {
        String kode = tfKodeMapel.getText().trim();
        String nama = tfNamaMapel.getText().trim();

        if (kode.isEmpty() || nama.isEmpty()) {
            showInfo("Semua field harus diisi!", "#ef4444");
            return;
        }

        if (selectedMapel == null) {
            Mapel m = new Mapel(kode, nama);
            if (mapelDAO.tambahMapel(m)) {
                showInfo("Mapel berhasil ditambahkan.", "#10b981");
                masterData = mapelDAO.getAllMapel();
                refreshTableView();
                clearForm();
            } else {
                showInfo("Gagal menyimpan mapel (Kode duplikat).", "#ef4444");
            }
        } else {
            selectedMapel.setKodeMapel(kode);
            selectedMapel.setNamaMapel(nama);
            if (mapelDAO.updateMapel(selectedMapel)) {
                showInfo("Mapel berhasil diupdate.", "#10b981");
                masterData = mapelDAO.getAllMapel();
                refreshTableView();
                clearForm();
            } else {
                showInfo("Gagal mengupdate mapel.", "#ef4444");
            }
        }
    }

    private void handleHapus() {
        if (selectedMapel == null) {
            showInfo("Pilih mapel yang akan dihapus.", "#ef4444");
            return;
        }

        if (mapelDAO.hapusMapel(selectedMapel.getId())) {
            showInfo("Mapel berhasil dihapus.", "#10b981");
            masterData = mapelDAO.getAllMapel();
            refreshTableView();
            clearForm();
        } else {
            showInfo("Gagal menghapus mapel.", "#ef4444");
        }
    }

    private void showInfo(String msg, String color) {
        lblInfo.setText(msg);
        lblInfo.setTextFill(javafx.scene.paint.Color.web(color));
    }
}
