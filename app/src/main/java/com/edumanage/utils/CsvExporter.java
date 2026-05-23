package com.edumanage.utils;

import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.PrintWriter;
import java.util.stream.Collectors;

public class CsvExporter {

    /**
     * Mengekspor data dari TableView ke format CSV.
     *
     * @param table       TableView yang ingin diekspor
     * @param defaultName Nama file default
     * @param owner       Window induk untuk dialog
     */
    public static <T> void exportToCsv(TableView<T> table, String defaultName, Window owner) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Simpan ke Excel (CSV)");
        fileChooser.setInitialFileName(defaultName + ".csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));
        
        File file = fileChooser.showSaveDialog(owner);
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                
                
                String headers = table.getColumns().stream()
                        .map(TableColumn::getText)
                        .map(CsvExporter::escapeSpecialCharacters)
                        .collect(Collectors.joining(","));
                writer.println(headers);

                
                ObservableList<T> items = table.getItems();
                for (T item : items) {
                    StringBuilder rowString = new StringBuilder();
                    for (int i = 0; i < table.getColumns().size(); i++) {
                        TableColumn<T, ?> col = table.getColumns().get(i);
                        Object cellData = col.getCellData(item);
                        String cellValue = (cellData == null) ? "" : cellData.toString();
                        
                        rowString.append(escapeSpecialCharacters(cellValue));
                        if (i < table.getColumns().size() - 1) {
                            rowString.append(",");
                        }
                    }
                    writer.println(rowString.toString());
                }
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.initOwner(owner);
                alert.setTitle("Ekspor Berhasil");
                alert.setHeaderText(null);
                alert.setContentText("Data berhasil diekspor ke: " + file.getAbsolutePath());
                alert.showAndWait();
                
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initOwner(owner);
                alert.setTitle("Ekspor Gagal");
                alert.setHeaderText(null);
                alert.setContentText("Terjadi kesalahan saat menyimpan file: " + ex.getMessage());
                alert.showAndWait();
            }
        }
    }

    private static String escapeSpecialCharacters(String data) {
        if (data == null) {
            return "";
        }
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }
}
