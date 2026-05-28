package com.edumanage.scenes;

import java.util.List;
import java.util.stream.Collectors;

import com.edumanage.dao.GuruDAO;
import com.edumanage.dao.MapelDAO;
import com.edumanage.models.Guru;
import com.edumanage.models.Mapel;
import com.edumanage.utils.AnimationUtils;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

public class GuruScene {

    private Stage stage;
    private DashboardScene dashboard;
    private GuruDAO guruDAO = new GuruDAO();
    private TableView<Guru> /* deklarasi tabel guru */ tableView;
    private ObservableList<Guru> masterData;

    private TextField tfNama, tfEmail, tfNip, tfJabatan;
    private ComboBox<String> cbMapel;
    private Label lblInfo;
    private Guru selectedGuru = null;

    private int currentPage = 1;
    private int itemsPerPage = 10;
    private String searchText = "";
    private Label paginationLabel;
    private HBox pageButtonsBox;

    public GuruScene(Stage stage, DashboardScene dashboard) {
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
        Label title = new Label("Data Guru");
        title.getStyleClass().add("edu-title");
        title.setFont(Font.font("Geist", FontWeight.BOLD, 24));

        Label subtitle = new Label("Kelola data guru dan mata pelajaran");
        subtitle.getStyleClass().add("edu-subtitle");

        titleBox.getChildren().addAll(title, subtitle);
        header.getChildren().add(titleBox);

        tableView = buildTable();
        masterData = guruDAO.getAllGuru();

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
        searchField.setPromptText("Cari guru...");
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
        btnExport.setOnAction(e -> com.edumanage.utils.CsvExporter.exportToCsv(tableView, "Data_Guru", stage));
        
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

    @SuppressWarnings("unchecked")
    private TableView<Guru> /* deklarasi tabel guru */ buildTable() {
        TableView<Guru> /* deklarasi tabel guru */ tv = new TableView<>();
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tv.getStyleClass().add("edu-table");
        tv.setPlaceholder(new Label("Belum ada data guru"));

        TableColumn<Guru, String> colNama = new TableColumn<>("Nama");
        colNama.setCellValueFactory(new PropertyValueFactory<>("nama"));

        TableColumn<Guru, String> colNip = new TableColumn<>("NIP");
        colNip.setCellValueFactory(new PropertyValueFactory<>("nip"));
        colNip.setMaxWidth(100);

        TableColumn<Guru, String> colMapel = new TableColumn<>("Mata Pelajaran");
        colMapel.setCellValueFactory(new PropertyValueFactory<>("mataPelajaran"));

        TableColumn<Guru, String> colJabatan = new TableColumn<>("Jabatan");
        colJabatan.setCellValueFactory(new PropertyValueFactory<>("jabatan"));

        TableColumn<Guru, String> colRole = new TableColumn<>("Role");
        colRole.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getRole()));
        colRole.setMaxWidth(70);

        tv.getColumns().addAll(colNama, colNip, colMapel, colJabatan, colRole);
        tv.sortPolicyProperty().set(t -> {
            java.util.Comparator<Guru> comparator = (java.util.Comparator<Guru>) t.getComparator();
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
        List<Guru> filtered = masterData.stream()
                .filter(g -> {
                    if (searchText.isEmpty()) return true;
                    String lower = searchText.toLowerCase();
                    return g.getNama().toLowerCase().contains(lower)
                            || g.getNip().toLowerCase().contains(lower)
                            || g.getMataPelajaran().toLowerCase().contains(lower);
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

        Label formTitle = new Label("Form Data Guru");
        formTitle.getStyleClass().add("edu-title");
        formTitle.setFont(Font.font("Geist", FontWeight.BOLD, 15));

        Separator sep = new Separator();
        sep.getStyleClass().add("edu-separator");

        tfNama = makeField("Nama Lengkap");
        tfNip = makeField("NIP");
        tfEmail = makeField("Email");
        cbMapel = new ComboBox<>();
        cbMapel.setPromptText("Pilih Mata Pelajaran");
        cbMapel.setMaxWidth(Double.MAX_VALUE);
        cbMapel.getStyleClass().add("edu-combo-box");
        loadMapelOptions();
        
        tfJabatan = makeField("Jabatan");

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

        Button btnHapus = new Button("Hapus");
        btnHapus.getStyleClass().add("edu-btn-destructive");
        btnHapus.setFont(Font.font("Geist", FontWeight.SEMI_BOLD, 12));
        HBox.setHgrow(btnHapus, Priority.ALWAYS);
        btnHapus.setMaxWidth(Double.MAX_VALUE);
        AnimationUtils.addPressAnimation(btnHapus);

        Button btnBaru = new Button("Baru");
        btnBaru.getStyleClass().add("edu-btn-outline");
        btnBaru.setFont(Font.font("Geist", FontWeight.SEMI_BOLD, 12));
        HBox.setHgrow(btnBaru, Priority.ALWAYS);
        btnBaru.setMaxWidth(Double.MAX_VALUE);
        AnimationUtils.addPressAnimation(btnBaru);

        btnRow.getChildren().addAll(btnSimpan, btnHapus, btnBaru);

        btnSimpan.setOnAction(e -> {
            try {
                Guru g = buildGuruFromForm();
                if (selectedGuru != null) {
                    g.setId(selectedGuru.getId());
                    guruDAO.updateGuru(g);
                    showInfo("Data guru berhasil diperbarui.", "#22C55E");
                } else {
                    guruDAO.tambahGuru(g);
                    showInfo("Guru baru berhasil ditambahkan.", "#22C55E");
                }
                masterData = guruDAO.getAllGuru();
                refreshTableView();
                clearForm();
            } catch (Exception ex) {
                showInfo("Error: " + ex.getMessage(), "#EF4444");
            }
        });

        btnHapus.setOnAction(e -> {
            if (selectedGuru != null) {
                String nama = selectedGuru.getNama();
                guruDAO.hapusGuru(selectedGuru.getId());
                showInfo("Data " + nama + " berhasil dihapus.", "#22C55E");
                masterData = guruDAO.getAllGuru();
                refreshTableView();
                clearForm();
            } else {
                showInfo("Pilih guru yang ingin dihapus.", "#F59E0B");
            }
        });

        btnBaru.setOnAction(e -> {
            selectedGuru = null;
            tableView.getSelectionModel().clearSelection();
            clearForm();
            showInfo("Isi form untuk menambah guru baru.", "#3B82F6");
        });

        card.getChildren().addAll(
                formTitle, sep,
                makeLabel("Nama"), tfNama,
                makeLabel("NIP"), tfNip,
                makeLabel("Email"), tfEmail,
                makeLabel("Mata Pelajaran"), cbMapel,
                makeLabel("Jabatan"), tfJabatan,
                lblInfo, btnRow
        );
        return card;
    }

    private void populateForm(Guru g) {
        selectedGuru = g;
        tfNama.setText(g.getNama());
        tfNip.setText(g.getNip());
        tfEmail.setText(g.getEmail());
        cbMapel.setValue(g.getMataPelajaran());
        tfJabatan.setText(g.getJabatan());
        showInfo(g.getInfoLengkap(), "#3B82F6");
    }

    private Guru buildGuruFromForm() {
        if (tfNama.getText().isBlank() || tfNip.getText().isBlank()) {
            throw new IllegalArgumentException("Nama dan NIP tidak boleh kosong!");
        }
        String mapelVal = cbMapel.getValue() != null ? cbMapel.getValue() : "";
        return new Guru(
                tfNama.getText().trim(),
                tfEmail.getText().trim(),
                tfNip.getText().trim(),
                mapelVal,
                tfJabatan.getText().trim()
        );
    }

    private void clearForm() {
        selectedGuru = null;
        tfNama.clear();
        tfNip.clear();
        tfEmail.clear();
        cbMapel.getSelectionModel().clearSelection();
        tfJabatan.clear();
        lblInfo.setText("");
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

    private void loadMapelOptions() {
        MapelDAO mapelDAO = new MapelDAO();
        List<String> mapelNames = mapelDAO.getAllMapel().stream()
                .map(Mapel::getNamaMapel)
                .collect(Collectors.toList());
        cbMapel.setItems(FXCollections.observableArrayList(mapelNames));
    }
}
