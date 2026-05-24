import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TestSortArrow extends Application {
    private ObservableList<String> master = FXCollections.observableArrayList("C", "A", "B", "D", "E", "F", "G", "H");
    private TableView<String> tv = new TableView<>();
    
    @Override
    public void start(Stage stage) {
        TableColumn<String, String> col = new TableColumn<>("Name");
        col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()));
        tv.getColumns().add(col);
        
        tv.sortPolicyProperty().set(t -> {
            Comparator<String> comp = (Comparator<String>) t.getComparator();
            if (comp != null) {
                FXCollections.sort(master, comp);
                refresh();
            }
            return true; 
        });
        
        refresh();
        
        stage.setScene(new Scene(new VBox(tv), 400, 300));
        stage.show();
    }
    
    private void refresh() {
        
        List<String> sub = master.stream().limit(3).collect(Collectors.toList());
        tv.setItems(FXCollections.observableArrayList(sub));
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
