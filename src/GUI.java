import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GUI extends Application {

    // Referencia al agente principal
    private static MainAgent mainAgent;

    // Método para recibir la referencia al agente principal
    public static void setMainAgent(MainAgent agent) {
        mainAgent = agent;
    }

    // Método para lanzar la interfaz
    @Override
    public void start(Stage primaryStage) {
        try {
            // Cargamos el fichero GUI.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI.fxml"));

            // Creamos el controlador para gestionar la interfaz
            Controller controller = new Controller(mainAgent);
            loader.setController(controller);

            // Creamos la escena
            Parent root = loader.load();
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
