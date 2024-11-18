import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
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
    private TableColumn<Player, String> IDColumn;
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
    private TableColumn<Player, Void> removePlayerColumn;
    @FXML
    private Button resetButton;
    @FXML
    private TextArea logTextArea;
    @FXML
    private ToggleButton verboseButton;
    @FXML
    private Button clearButton;
    @FXML
    private TextField delayField;
    @FXML
    private Button delayButton;

    // Lista para almacenar los jugadores
    private static ArrayList<Player> players = new ArrayList<>();

    // Variable para controlar el estado del torneo
    private boolean tournamentFinished = false;

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
        IDColumn.setCellValueFactory(new PropertyValueFactory<>("ID"));
        winsColumn.setCellValueFactory(new PropertyValueFactory<>("wins"));
        tiesColumn.setCellValueFactory(new PropertyValueFactory<>("ties"));
        lossesColumn.setCellValueFactory(new PropertyValueFactory<>("losses"));
        moneyColumn.setCellValueFactory(new PropertyValueFactory<>("money"));
        stocksColumn.setCellValueFactory(new PropertyValueFactory<>("stocks"));
        removePlayerColumn.setCellFactory(_ -> new TableCell<>() {
            private final Button removePlayerButton = new Button("X");

            // Método que se ejecuta cada vez que se actualiza la tabla
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                // Si la celda no está vacía, configuramos el botón
                if (!empty) {
                    // Obtenemos el jugador correspondiente
                    Player currentPlayer = getTableView().getItems().get(getIndex());

                    // Configuramos la acción del botón
                    removePlayerButton.setOnAction(_ -> handleRemovePlayerButtonAction(currentPlayer));

                    // Configuramos su estado
                    removePlayerButton.setDisable(tournamentFinished);

                    // Ponemos el botón en la celda
                    setGraphic(removePlayerButton);
                } else {
                    setGraphic(null);
                }
            }
        });
    }

    // Método para gestionar el botón de inicio
    @FXML
    private void handleStartButtonAction() {
        // Leemos los valores rondas y porcentaje de comisión
        int R = Integer.parseInt(roundsField.getText());
        double F = Double.parseDouble(feeField.getText());

        // Si el número de rondas no es válido, ponemos el valor por defecto
        if (R <= 0) {
            R = 500;
        }

        // Si el porcentaje de comisión no es válido, ponemos el valor por defecto
        if (F < 0 || F > 1) {
            F = 0.01;
        }

        // Iniciamos el torneo con los parámetros especificados
        mainAgent.startTournament(R, F);

        // Deshabilitamos el botón de inicio
        startButton.setDisable(true);

        // Habilitamos el botón de pausa
        stopButton.setDisable(false);

        // Deshabilitamos los campos de rondas y porcentaje de comisión
        roundsField.setDisable(true);
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

        // Habilitamos el campo de porcentaje de comisión
        feeField.setDisable(false);
    }

    // Método para gestionar el botón de continuar
    @FXML
    private void handleContinueButtonAction() {
        // Leemos el valor de porcentaje de comisión
        double F = Double.parseDouble(feeField.getText());

        // Si el porcentaje de comisión no es válido, ponemos el valor por defecto
        if (F < 0 || F > 1) {
            F = 0.01;
        }

        // Continuamos el torneo
        mainAgent.continueTournament(F);

        // Deshabilitamos el botón de continuar
        continueButton.setDisable(true);

        // Habilitamos el botón de pausa
        stopButton.setDisable(false);

        // Deshabilitamos el campo de porcentaje de comisión
        feeField.setDisable(true);
    }

    // Método para agregar jugadores a la tabla de jugadores
    public void addPlayer(Player player) {
        // Añadimos el jugador a la lista
        players.add(player);
        // Actualizamos la tabla de los jugadores
        updatePlayersTable();
    }

    // Método para actualizar la tabla de los jugadores
    public void updatePlayersTable() {
        // Refrescamos la lista de jugadores de la tabla
        playersTable.setItems(FXCollections.observableArrayList(players));
        playersTable.refresh();
    }

    // Método para eliminar un jugador del torneo
    public void handleRemovePlayerButtonAction(Player player) {
        // Eliminamos el jugador de la lista
        players.remove(player);
        // Actualizamos la tabla de los jugadores
        updatePlayersTable();
        // Añadimos el jugador a la lista para eliminar del agente principal
        mainAgent.playersToRemove.add(player);
    }

    // Método para gestionar el botón de reinicio de estadísticas
    @FXML
    private void handleResetButtonAction() {
        // Reiniciamos las estadísticas de todos los jugadores
        for (Player player : players) {
            player.resetStats();
        }
        // Actualizamos la tabla de los jugadores
        updatePlayersTable();
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

    // Método para gestionar el botón de limpiar
    @FXML
    private void handleClearButtonAction() {
        // Eliminamos todo el contenido de área de texto
        logTextArea.clear();
    }

    // Método para gestionar el botón de delay
    @FXML
    private void handleDelayButtonAction() {
        try {
            // Obtenemos la cantidad de delay en milisegundos
            int delay = Integer.parseInt(delayField.getText());

            // Establecemos el nuevo valor de delay
            if (delay >= 0) {
                mainAgent.setDelay(delay);
            }
        } catch (NumberFormatException e) {
            // Ignoramos el valor si no es correcto
        }
    }

    // Método para deshabilitar todos los botones y campos, y mostrar una alerta con
    // los resultados del torneo
    public void finishTournament(String results) {

        // Marcamos que ha terminado el torneo y actualizamos la tabla de los jugadores
        tournamentFinished = true;
        updatePlayersTable();

        // Deshabilitamos todos los botones y campos
        stopButton.setDisable(true);
        resetButton.setDisable(true);
        verboseButton.setDisable(true);
        clearButton.setDisable(true);
        delayField.setDisable(true);
        delayButton.setDisable(true);

        // Creamos una alerta de tipo INFORMATION
        Alert alert = new Alert(AlertType.INFORMATION);
        // Ponemos título a la ventana
        alert.setTitle("Tournament results");
        // Ponemos un encabezado
        alert.setHeaderText("The tournament has finished");
        // Añadimos los resultados
        alert.setContentText(results);

        // Mostramos la alerta y esperamos a que el usuario la cierre
        alert.showAndWait();
    }
}
