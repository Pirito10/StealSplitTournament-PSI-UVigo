import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class MainAgent extends Agent {

    // Variables para almacenar el número de jugadores, de rondas y el porcentaje de
    // comisión
    private int N;
    private final int R = 4;
    private final double F = 0.1;

    // Lista para almacenar los AID de los jugadores
    private ArrayList<AID> players = new ArrayList<>();

    // Matriz con los payoffs para cada
    private final int[][][] matrix = {
            { { 2, 2 }, { 0, 4 } },
            { { 4, 0 }, { 0, 0 } }
    };

    // Formateador para limitar a dos decimales
    private static final DecimalFormat df = new DecimalFormat("#.##");

    @Override
    protected void setup() {

        // Pequeño delay inicial para que inicialice JADE
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("[Main] Se ha inicializado el agente principal");

        // Comportamiento para buscar otros agentes
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                System.out.println("[Main] Buscando agentes jugadores...");

                // Creamos una plantilla con el tipo de agente que queremos buscar
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("player"); // Tipo de servicio buscado
                template.addServices(sd);

                try {
                    // Buscamos los agentes que coincidan con la descripción
                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    if (result.length >= 2) {
                        System.out.println("[Main] Se han encontrado " + result.length + " jugadores:");

                        // Recorremos la lista de agentes
                        for (DFAgentDescription agentDesc : result) {
                            // Obtenemos el AID de cada agente, y lo almacenamos en la lista
                            AID agentID = agentDesc.getName();
                            players.add(agentID);
                            System.out.println("\t- " + agentID.getLocalName());
                        }
                        // Si hay uno o cero jugadores, no se continúa
                    } else if (result.length == 1) {
                        System.out.println("[Main] Solo se ha encontrado un agente jugador");
                        doDelete();
                    } else {
                        System.out.println("[Main] No se han encontrado agentes jugadores");
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
                System.out.println("[Main] Informando a los jugadores sobre la competición...");

                // Obtenemos el número de jugadores
                N = players.size();

                // Recorremos la lista de jugadores
                for (int i = 0; i < players.size(); i++) {
                    // Obtenemos el AID del jugador, y construímos su mensaje
                    AID player = players.get(i);
                    String message = "Id#" + i + "#" + N + "," + R + "," + F;

                    // Enviamos el mensaje
                    sendMessage(ACLMessage.INFORM, player, message);

                    System.out.println("[Main] Mensaje enviado a jugador con ID " + i + ": " + message);
                }

                // HashMap para almacenar el payoff acumulado de cada jugador
                HashMap<AID, Double> totalPayoffs = new HashMap<>();
                // Inicializamos el HashMap a cero
                for (AID player : players) {
                    totalPayoffs.put(player, 0.0);
                }

                // HashMap para almacenar los stocks de cada jugador
                HashMap<AID, Double> totalStocks = new HashMap<>();
                // Inicializamos el HashMap a cero
                for (AID player : players) {
                    totalStocks.put(player, 0.0);
                }

                // Iteramos sobre el número de rondas
                for (int round = 1; round <= R; round++) {
                    System.out.println("\n[Main] Iniciando la ronda " + round + "...");

                    // HashMap para acumular el payoff de cada jugador durante la ronda
                    HashMap<AID, Integer> roundPayoffs = new HashMap<>();
                    // Inicializamos el HashMap a cero
                    for (AID player : players) {
                        roundPayoffs.put(player, 0);
                    }

                    // Iteramos sobre cada pareja de jugadores
                    for (int i = 0; i < players.size() - 1; i++) {
                        for (int j = i + 1; j < players.size(); j++) {
                            // Obtenemos los AID de los jugadores, y construímos su mensaje
                            AID player1 = players.get(i);
                            AID player2 = players.get(j);
                            String message = "NewGame#" + i + "#" + j;

                            // Enviamos los mensajes
                            sendMessage(ACLMessage.INFORM, player1, message);
                            sendMessage(ACLMessage.INFORM, player2, message);

                            System.out.println(
                                    "[Main] Mensaje enviado a jugadores con ID " + i + "," + j + ": " + message);

                            // Construímos el mensaje de solicitud de acción
                            message = "Action";

                            // Enviamos el mensaje al primer jugador
                            sendMessage(ACLMessage.REQUEST, player1, message);
                            System.out.println("[Main] Mensaje enviado a jugador con ID " + i + ": " + message);
                            // Esperamos la respuesta
                            ACLMessage reply_player1 = blockingReceive();
                            System.out.println(
                                    "[Main] Mensaje recibido del jugador con ID " + i + ":"
                                            + reply_player1.getContent());
                            // Extraemos del contenido la acción seleccionada
                            String action_player1 = reply_player1.getContent().split("#")[1];

                            // Enviamos el mensaje al segundo jugador
                            sendMessage(ACLMessage.REQUEST, player2, message);
                            System.out.println("[Main] Mensaje enviado a jugador con ID " + j + ": " + message);
                            // Esperamos la respuesta
                            ACLMessage reply_player2 = blockingReceive();
                            System.out.println(
                                    "[Main] Mensaje recibido del jugador con ID " + j + ":"
                                            + reply_player2.getContent());
                            // Extraemos del contenido la acción seleccionada
                            String action_player2 = reply_player2.getContent().split("#")[1];

                            // Procesamos el resultado
                            int[] payoffs = getPayoffs(action_player1, action_player2);

                            // Actualizamos el payoff de la ronda de cada jugador
                            roundPayoffs.put(player1, roundPayoffs.get(player1) + payoffs[0]);
                            roundPayoffs.put(player2, roundPayoffs.get(player2) + payoffs[1]);

                            // Actualizamos el payoff acumulado de cada jugador
                            totalPayoffs.put(player1, totalPayoffs.get(player1) + payoffs[0]);
                            totalPayoffs.put(player2, totalPayoffs.get(player2) + payoffs[1]);

                            // Construímos el mensaje de resultados
                            message = "Results#" + i + "," + j + "#" + action_player1 + "," + action_player2 + "#"
                                    + payoffs[0] + "," + payoffs[1];

                            // Enviamos los mensajes
                            sendMessage(ACLMessage.INFORM, player1, message);
                            sendMessage(ACLMessage.INFORM, player2, message);
                            System.out.println(
                                    "[Main] Mensaje enviado a jugadores con ID " + i + "," + j + ": " + message);
                        }
                    }

                    // Recorremos la lista de jugadores
                    for (int i = 0; i < players.size(); i++) {

                        // Obtenemos el jugador
                        AID player = players.get(i);
                        // Obtenemos su payoff de la ronda
                        int roundPayoff = roundPayoffs.get(player);
                        // TODO aplicar el valor de inflación
                        // Obtenemos su payoff acumulado
                        double totalPayoff = totalPayoffs.get(player);
                        // Obtenemos sus stocks
                        double stocks = totalStocks.get(player);

                        String message = "RoundOver#" + i + "#" + roundPayoff + "#" + totalPayoff + "#"
                                + getInflationRate(round)
                                + "#" + stocks + "#" + getIndexValue(round);

                        // Enviamos el mensaje
                        sendMessage(ACLMessage.REQUEST, player, message);
                    }
                }
            }
        });
    }

    @Override
    protected void takeDown() {
        System.out.println("[Main] Se ha terminado el agente principal");
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
    public double getIndexValue(int round) {
        double indexValue = 100 * Math.log(round + 1) + 50;
        return Double.parseDouble(df.format(indexValue));
    }

    // Método que calcula la tasa de inflación en función de la ronda
    public double getInflationRate(int round) {
        double inflationRate = 0.5 + 0.5 * Math.sin(round * Math.PI / 10);
        return Double.parseDouble(df.format(inflationRate));
    }
}
