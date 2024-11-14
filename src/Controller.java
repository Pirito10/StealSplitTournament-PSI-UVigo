import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class Controller {

    // Botón de inicio de torneo
    @FXML
    private Button startButton;

    // Referencia al agente principal
    private MainAgent mainAgent;

    // Constructor con referencia al agente principal
    public Controller(MainAgent mainAgent) {
        this.mainAgent = mainAgent;
    }

    // Método para gestionar el botón de inicio
    @FXML
    private void handleStartButtonAction() {
        System.out.println("Botón de Start presionado");
        mainAgent.iniciarTorneo();
    }
}
