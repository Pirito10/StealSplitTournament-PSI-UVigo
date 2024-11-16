import java.text.DecimalFormat;
import java.util.ArrayList;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import javafx.application.Application;
import javafx.application.Platform;

public class MainAgent extends Agent {

    // Referencia al controlador
    private static Controller controller;

    // Variables para almacenar el número de jugadores, de rondas y el porcentaje de
    // comisión
    private static int N, R;
    private static double F;

    // Lista para almacenar los jugadores
    private static ArrayList<Player> players = new ArrayList<>();

    // Matriz con los payoffs
    private static final int[][][] matrix = {
            { { 2, 2 }, { 0, 4 } },
            { { 4, 0 }, { 0, 0 } }
    };

    // Variable de control para pausar el agente
    private static boolean stop = false;

    // Variable de control para el envío de los logs
    private static boolean verbose = true;

    // Variable de control para el delay
    private static boolean delay = true;

    @Override
    protected void setup() {

        // Pasamos la referencia del agente principal a la interfaz
        GUI.setMainAgent(this);

        // Iniciamos la interfaz gráfica en un nuevo hilo
        new Thread(() -> Application.launch(GUI.class)).start();

        // Esperamos a que se inicialice la interfaz
        try {
            GUI.getLatch().await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        log("Se ha inicializado el agente principal");

        // Esperamos a que se pulse el botón de inicio
        waitStart();

        // Comportamiento para buscar otros agentes
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                log("Buscando agentes jugadores...");

                // Creamos una plantilla con el tipo de agente que queremos buscar
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("player"); // Tipo de servicio buscado
                template.addServices(sd);

                try {
                    // Buscamos los agentes que coincidan con la descripción
                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    if (result.length >= 2) {
                        log("Se han encontrado " + result.length + " jugadores:");

                        // Recorremos la lista de agentes
                        for (DFAgentDescription agentDesc : result) {
                            // Obtenemos el AID de cada agente, creamos un nuevo jugador y lo almacenamos en
                            // la lista
                            // TODO: intentar obtener el tipo de agente
                            AID agentID = agentDesc.getName();
                            String agentName = agentID.getLocalName();
                            players.add(new Player(agentID, agentName, "RandomAgent"));
                            log("\t- " + agentName);
                        }
                        // Si hay uno o cero jugadores, no se continúa
                    } else if (result.length == 1) {
                        log("Solo se ha encontrado un agente jugador");
                        doDelete();
                    } else {
                        log("No se han encontrado agentes jugadores");
                        doDelete();
                    }
                } catch (FIPAException e) {
                    e.printStackTrace();
                    doDelete();
                }
            }
        });

        // Comportamiento para informar a los jugadores sobre la competición
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                log("Informando a los jugadores sobre la competición...");

                // Obtenemos el número de jugadores
                N = players.size();

                // Recorremos la lista de jugadores
                for (int i = 0; i < players.size(); i++) {
                    // Obtenemos el jugador, le asignamos un ID y construímos su mensaje
                    Player player = players.get(i);
                    player.setID(i);
                    String message = "Id#" + i + "#" + N + "," + R + "," + F;

                    // Enviamos el mensaje
                    sendMessage(ACLMessage.INFORM, player.getAID(), message);

                    log("Mensaje enviado a jugador con ID " + i + ": " + message);
                }

                // Iteramos sobre el número de rondas
                for (int round = 1; round <= R; round++) {
                    // ! TEMPORAL DELAY
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // Comprobamos si se ha pulsado el botón de pausa
                    checkStop();

                    log("\nIniciando la ronda " + round + "...");

                    // Reiniciamos el payoff de la ronda de cada jugador
                    for (Player player : players) {
                        player.setRoundMoney(0);
                    }

                    // Iteramos sobre cada pareja de jugadores
                    for (int i = 0; i < players.size() - 1; i++) {
                        for (int j = i + 1; j < players.size(); j++) {
                            // Obtenemos los jugadores, y construímos su mensaje
                            Player player1 = players.get(i);
                            Player player2 = players.get(j);
                            String message = "NewGame#" + i + "#" + j;

                            // Enviamos los mensajes
                            sendMessage(ACLMessage.INFORM, player1.getAID(), message);
                            sendMessage(ACLMessage.INFORM, player2.getAID(), message);

                            log("Mensaje enviado a jugadores con ID " + i + "," + j + ": " + message);

                            // Construímos el mensaje de solicitud de acción
                            message = "Action";

                            // Enviamos el mensaje al primer jugador
                            sendMessage(ACLMessage.REQUEST, player1.getAID(), message);
                            log("Mensaje enviado a jugador con ID " + i + ": " + message);
                            // Esperamos la respuesta
                            ACLMessage reply_player1 = blockingReceive();
                            log("Mensaje recibido del jugador con ID " + i + ": " + reply_player1.getContent());
                            // Extraemos del contenido la acción seleccionada
                            String action_player1 = reply_player1.getContent().split("#")[1];

                            // Enviamos el mensaje al segundo jugador
                            sendMessage(ACLMessage.REQUEST, player2.getAID(), message);
                            log("Mensaje enviado a jugador con ID " + j + ": " + message);
                            // Esperamos la respuesta
                            ACLMessage reply_player2 = blockingReceive();
                            log("Mensaje recibido del jugador con ID " + j + ": " + reply_player2.getContent());
                            // Extraemos del contenido la acción seleccionada
                            String action_player2 = reply_player2.getContent().split("#")[1];

                            // Procesamos el resultado
                            int[] payoffs = getPayoffs(action_player1, action_player2);

                            // Actualizamos el payoff de la ronda de cada jugador
                            player1.addRoundMoney(payoffs[0]);
                            player2.addRoundMoney(payoffs[1]);

                            // Actualizamos el payoff acumulado de cada jugador
                            player1.addMoney(payoffs[0]);
                            player2.addMoney(payoffs[1]);

                            // Actualizamos las estadísticas de cada jugador
                            if (payoffs[0] > payoffs[1]) {
                                player1.addWin();
                                player2.addLoss();
                            } else if (payoffs[0] < payoffs[1]) {
                                player1.addLoss();
                                player2.addWin();
                            } else {
                                player1.addTie();
                                player2.addTie();
                            }

                            // Construímos el mensaje de resultados
                            message = "Results#" + i + "," + j + "#" + action_player1 + "," + action_player2 + "#"
                                    + payoffs[0] + "," + payoffs[1];

                            // Enviamos los mensajes
                            sendMessage(ACLMessage.INFORM, player1.getAID(), message);
                            sendMessage(ACLMessage.INFORM, player2.getAID(), message);
                            log("Mensaje enviado a jugadores con ID " + i + "," + j + ": " + message);
                        }
                    }

                    // Recorremos la lista de jugadores
                    for (int i = 0; i < players.size(); i++) {

                        // Obtenemos el jugador
                        Player player = players.get(i);

                        // Aplicamos el índice de inflación a su payoff acumulado
                        player.removeMoney(player.getMoney() * (1 - getInflationRate(round)));

                        // Construímos el mensaje de fin de ronda
                        String message = "RoundOver#" + i + "#" + player.getRoundMoney() + "#" + player.getMoney() + "#"
                                + getInflationRate(round)
                                + "#" + player.getStocks() + "#" + getIndexValue(round);

                        // Enviamos el mensaje
                        sendMessage(ACLMessage.REQUEST, player.getAID(), message);
                        log("Mensaje enviado a jugador con ID " + i + ": " + message);
                        // Esperamos la respuesta
                        ACLMessage reply = blockingReceive();
                        log("Mensaje recibido del jugador con ID " + i + ": " + reply.getContent());

                        // Extraemos del contenido la acción y la cantidad
                        String action = reply.getContent().split("#")[0];
                        double amount = Double.parseDouble(reply.getContent().split("#")[1]);

                        // Si la acción es comprar...
                        if (action.equals("Buy")) {
                            // Comprobamos si se puede realizar la compra
                            if (amount * getIndexValue(round) <= player.getMoney()) {
                                // Quitamos del payoff la cantidad gastada
                                player.removeMoney(amount * getIndexValue(round));
                                // Añadimos los stocks comprados
                                player.addStocks(amount);
                            }
                            // Si la acción es vender...
                        } else {
                            // Comprobamos si se puede realizar la venta
                            if (amount <= player.getStocks()) {
                                // Quitamos los stocks vendidos
                                player.removeStocks(amount);
                                // Añadimos el payoff obtenido, aplicando la comisión de venta
                                player.addMoney((amount * getIndexValue(round)) * (1 - F));
                            }
                        }

                        // Construímos el mensaje de contabilidad
                        message = "Accounting#" + i + "#" + player.getMoney() + "#"
                                + player.getStocks();

                        // Enviamos el mensaje
                        sendMessage(ACLMessage.INFORM, player.getAID(), message);
                        log("Mensaje enviado a jugador con ID " + i + ": " + message);
                    }
                }

                log("\nTorneo finalizado, informando a los jugadores...");

                // Recorremos la lista de jugadores
                for (int i = 0; i < players.size(); i++) {

                    // Obtenemos el jugador
                    Player player = players.get(i);

                    // Sumamos a su payoff acumulado el valor de venta de sus stocks, aplicando la
                    // comisión de venta
                    player.addMoney((player.getStocks() * getIndexValue(R)) * (1 - F));

                    // Construímos el mensaje de fin de torneo
                    String message = "GameOver#" + i + "#" + player.getMoney();

                    // Enviamos el mensaje
                    sendMessage(ACLMessage.REQUEST, player.getAID(), message);
                    log("Mensaje enviado a jugador con ID " + i + ": " + message);
                }
            }
        });
    }

    @Override
    protected void takeDown() {
        log("Se ha terminado el agente principal");
    }

    // Método para enviar un mensaje a un agente
    public void sendMessage(int performative, AID receiver, String message) {
        ACLMessage msg = new ACLMessage(performative);
        msg.setContent(message);
        msg.addReceiver(receiver);
        send(msg);
    }

    // Método para obtener los payoffs de un enfrentamiento
    public int[] getPayoffs(String action_player1, String action_player2) {
        int index_player1, index_player2;

        // Obtenemos los índices de la matriz
        if (action_player1.equals("C")) {
            index_player1 = 0;
        } else {
            index_player1 = 1;
        }

        if (action_player2.equals("C")) {
            index_player2 = 0;
        } else {
            index_player2 = 1;
        }

        // Obtenemos los payoffs de la matriz
        return matrix[index_player1][index_player2];
    }

    // Método que calcula el valor del stock en función de la ronda
    private double getIndexValue(int round) {
        double indexValue = Math.log(round + 1);
        return round(indexValue);
    }

    // Método que calcula la tasa de inflación en función de la ronda
    private double getInflationRate(int round) {
        double inflationRate = 0.5 + 0.5 * Math.sin(round * Math.PI / 10);
        return round(inflationRate);
    }

    // TODO: hacer privado, para que los demás alumnos no dependan de mi MainAgent
    // Método para limitar valores a dos dígitos decimales
    public static double round(double value) {
        // Formateador para limitar a dos decimales
        DecimalFormat df = new DecimalFormat("#.##");
        return Double.parseDouble(df.format(value));
    }

    // Método para recibir la referencia al controlador
    public void setController(Controller controller) {
        MainAgent.controller = controller;
    }

    // Método para empezar el torneo
    public synchronized void startTournament(int R, double F) {
        // Inicializamos el número de rondas y el porcentaje de comisión
        MainAgent.R = R;
        MainAgent.F = F;

        // Despertamos al hilo
        notify();
    }

    // Método para esperar a que se pulse el botón de inicio
    private synchronized void waitStart() {
        // Dormimos al hilo hasta que se pulse el botón de inicio
        try {
            wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Método para pausar el torneo
    public void stopTournament() {
        stop = true;
    }

    // Método para comprobar si se ha pulsado el botón de pausa
    private synchronized void checkStop() {
        // Dormimos al hilo hasta que se pulse el botón de continuar
        if (stop) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Método para continuar el torneo
    public synchronized void continueTournament() {
        stop = false;
        // Despertamos al hilo
        notify();
    }

    // Método para registrar mensajes
    private void log(String message) {
        // Mostramos el mensaje por consola
        System.out.println("[Main] " + message);

        // Si los logs están habilitados, enviamos el mensaje a la interfaz
        if (verbose) {
            Platform.runLater(() -> controller.logMessage(message));
        }
    }

    // Método para invertir el valor de envío de los logs
    public void setVerbose(boolean verbose) {
        MainAgent.verbose = verbose;
    }

    // Método para invertir el valor de delay
    public void setDelay(boolean delay) {
        MainAgent.delay = delay;
    }
}