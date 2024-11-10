import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GUI extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            // Cargamos el fichero GUI.fxml
            Parent root = FXMLLoader.load(getClass().getResource("/GUI.fxml"));

            // Creamos la escena a partir del fichero cargado
            Scene scene = new Scene(root);

            // Configuramos y mostramos el stage
            primaryStage.setTitle("PSI Tournament");
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
