import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GUI extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            // Carga el archivo .fxml
            Parent root = FXMLLoader.load(getClass().getResource("/GUI.fxml"));

            // Crea la escena con el layout cargado
            Scene scene = new Scene(root);

            // Configura el stage
            primaryStage.setTitle("Interfaz del Torneo");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
