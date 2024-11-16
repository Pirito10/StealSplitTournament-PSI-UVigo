import java.util.ArrayList;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.PropertyValueFactory;

public class Controller {

    // Constructor
    public Controller() {
    }

    // Identificadores de elementos de la interfaz
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
    private TableView<Player> playersTable;
    @FXML
    private TableColumn<Player, String> nameColumn;
    @FXML
    private TableColumn<Player, String> typeColumn;
    @FXML
    private TableColumn<Player, Integer> winsColumn;
    @FXML
    private TableColumn<Player, Integer> tiesColumn;
    @FXML
    private TableColumn<Player, Integer> lossesColumn;
    @FXML
    private TableColumn<Player, Double> moneyColumn;
    @FXML
    private TableColumn<Player, Double> stocksColumn;
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

    // Lista para almacenar los jugadores
    private static ArrayList<Player> players = new ArrayList<>();

    // Referencia al agente principal
    private MainAgent mainAgent;

    // Método para recibir la referencia al agente principal
    public void setMainAgent(MainAgent agent) {
        mainAgent = agent;
    }

    // Inicializamos la tabla de los jugadores
    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        winsColumn.setCellValueFactory(new PropertyValueFactory<>("wins"));
        tiesColumn.setCellValueFactory(new PropertyValueFactory<>("ties"));
        lossesColumn.setCellValueFactory(new PropertyValueFactory<>("losses"));
        moneyColumn.setCellValueFactory(new PropertyValueFactory<>("money"));
        stocksColumn.setCellValueFactory(new PropertyValueFactory<>("stocks"));
    }

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

    // Método para agregar jugadores a la tabla de jugadores
    public void addPlayer(Player player) {
        // Añadimos el jugador a la lista
        players.add(player);
        // Añadimos el jugador a la tabla
        playersTable.setItems(FXCollections.observableArrayList(players));
    }

    // Método para actualizar la tabla de los jugadores
    public void updatePlayersTable() {
        playersTable.refresh();
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
