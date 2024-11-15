import java.util.concurrent.CountDownLatch;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GUI extends Application {

    // Referencia al agente principal
    private static MainAgent mainAgent;

    // Variable para controlar si la interfaz se ha cargado
    private static final CountDownLatch latch = new CountDownLatch(1);

    // Método para lanzar la interfaz
    @Override
    public void start(Stage primaryStage) {
        try {
            // Cargamos el fichero GUI.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI.fxml"));
            Parent root = loader.load();

            // Cargamos el controlador para gestionar la interfaz
            Controller controller = loader.getController();
            // Le pasamos la referencia al agente principal
            controller.setMainAgent(mainAgent);
            // Pasamos la referencia del controlador al agente principal
            mainAgent.setController(controller);

            // Creamos la escena
            Scene scene = new Scene(root);

            // Configuramos y mostramos el stage
            primaryStage.setTitle("PSI Tournament");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.show();

            // Liberamos el latch para marcar que se ha cargado la interfaz
            latch.countDown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Método para recibir la referencia al agente principal
    public static void setMainAgent(MainAgent agent) {
        mainAgent = agent;
    }

    public static CountDownLatch getLatch() {
        return latch;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
