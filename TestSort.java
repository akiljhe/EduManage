import javafx.scene.control.TableView;
public class TestSort {
    public static void main(String[] args) {
        TableView<String> tv = new TableView<>();
        tv.sortPolicyProperty().set(t -> true);
    }
}
