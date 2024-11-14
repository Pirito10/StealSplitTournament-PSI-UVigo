import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class Controller {

    // Constructor
    public Controller() {
    }

    // Referencia al agente principal
    private MainAgent mainAgent;

    // Método para recibir la referencia al agente principal
    public void setMainAgent(MainAgent mainAgent) {
        this.mainAgent = mainAgent;
    }

    @FXML
    private Button startButton;
    @FXML
    private Button stopButton;
    @FXML
    private Button continueButton;
    @FXML
    private TextField roundsField;
    @FXML
    private TextField feeField;
    @FXML
    private static TextArea logTextArea;

    // Método para gestionar el botón de inicio
    @FXML
    private void handleStartButtonAction() {
        // Leemos los valores rondas y porcentaje de comisión
        int R = Integer.parseInt(roundsField.getText());
        double F = Double.parseDouble(feeField.getText());

        // Iniciamos el torneo con los parámetros especificados
        mainAgent.startTournament(R, F);

        // Deshabilitamos el botón de inicio
        startButton.setDisable(true);

        // Habilitamos el botón de pausa
        stopButton.setDisable(false);

        // Deshabilitamos los campos de rondas y porcentaje de comisión
        roundsField.setDisable(true);
        // TODO: allow to change fee when game is paused
        feeField.setDisable(true);
    }

    // Método para gestionar el botón de pausa
    @FXML
    private void handlePauseButtonAction() {
        // Pausamos el torneo
        mainAgent.stopTournament();

        // Deshabilitamos el botón de pausa
        stopButton.setDisable(true);

        // Habilitamos el botón de continuar
        continueButton.setDisable(false);
    }

    // Método para gestionar el botón de continuar
    @FXML
    private void handleContinueButtonAction() {
        // Continuamos el torneo
        mainAgent.continueTournament();

        // Deshabilitamos el botón de continuar
        continueButton.setDisable(true);

        // Habilitamos el botón de pausa
        stopButton.setDisable(false);
    }

    // Método para añadir un mensaje al área de texto de log
    public static void addLogMessage(String message) {
        logTextArea.appendText(message + "\n");
    }
}
