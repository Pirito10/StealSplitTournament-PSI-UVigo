import java.util.ArrayList;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class MainAgent extends Agent {

    // Lista para almacenar los AID de los jugadores
    private ArrayList<AID> players = new ArrayList<>();
    // Variables para almacenar el número de jugadores, de rondas y el porcentaje de
    // comisión
    private int N;
    private final int R = 2;
    private final double F = 0.1;

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

                // Iteramos sobre el número de rondas
                for (int round = 1; round <= R; round++) {
                    System.out.println("\n[Main] Iniciando la ronda " + round + "...");

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

                            sendMessage(ACLMessage.REQUEST, player1, message);
                            ACLMessage reply_player1 = blockingReceive();
                            System.out.println(
                                    "[Main] Mensaje recibido del jugador con ID " + i + ":"
                                            + reply_player1.getContent());
                            sendMessage(ACLMessage.REQUEST, player2, message);
                            ACLMessage reply_player2 = blockingReceive();
                            System.out.println(
                                    "[Main] Mensaje recibido del jugador con ID " + j + ":"
                                            + reply_player2.getContent());
                        }
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
}
