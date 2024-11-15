import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;

public class Controller {

    // Constructor
    public Controller() {
    }

    // Referencia al agente principal
    private MainAgent mainAgent;

    // Método para recibir la referencia al agente principal
    public void setMainAgent(MainAgent agent) {
        mainAgent = agent;
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
    private Button resetButton;
    @FXML
    private TextArea logTextArea;
    @FXML
    private ToggleButton verboseButton;
    @FXML
    private ToggleButton delayButton;
    @FXML
    private Button clearButton;

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

    // Método para gestionar el botón de reinicio de estadísticas
    @FXML
    private void handleResetButtonAction() {
        // TODO
    }

    // Método para añadir un mensaje al área de texto de log
    public void logMessage(String message) {
        logTextArea.appendText(message + "\n");
    }

    // Método para gestionar el botón de verbose
    @FXML
    private void handleVerboseButtonAction() {
        // Leemos el estado del botón
        boolean verbose = verboseButton.isSelected();

        // Enviamos el estado al agente principal
        mainAgent.setVerbose(verbose);

        // Invertimos el texto del botón
        if (verbose) {
            verboseButton.setText("Logging: ON");
        } else {
            verboseButton.setText("Logging: OFF");
        }
    }

    // Método para gestionar el botón de delay
    @FXML
    private void handleDelayButtonAction() {
        // Leemos el estado del botón
        boolean delay = delayButton.isSelected();

        // Enviamos el estado al agente principal
        mainAgent.setDelay(delay);

        // Invertimos el texto del botón
        if (delay) {
            delayButton.setText("Delay: ON");
        } else {
            delayButton.setText("Delay: OFF");
        }
    }

    // Método para gestionar el botón de limpiar
    @FXML
    private void handleClearButtonAction() {
        // Eliminamos todo el contenido de área de texto
        logTextArea.clear();
    }
}
