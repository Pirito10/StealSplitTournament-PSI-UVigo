package agents;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Random;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class RL_Agent extends Agent {

    // Variables para almacenar el ID del agente, el del rival, el número de
    // jugadores, de rondas
    // y el porcentaje de comisión
    private static int ID, ID_rival, N, R;
    private static double F;

    // HashMap para almacenar el historial de acciones de cada jugador
    private HashMap<Integer, HashMap<Integer, String>> history = new HashMap<>();

    @Override
    protected void setup() {

        // Pequeño delay inicial para que inicialice JADE
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("[Jugador X] Se ha inicializado un agente: " + getLocalName());

        // Creamos una descripción del agente
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID()); // AID = Agent ID
        ServiceDescription sd = new ServiceDescription();
        sd.setName(getLocalName()); // Nombre del servicio
        sd.setType("RL_Agent"); // Tipo de servicio
        dfd.addServices(sd);

        // Registramos el agente en el DF (Directory Facilitator)
        try {
            DFService.register(this, dfd);
            System.out.println("[Jugador X] Agente registrado en el DF");
        } catch (FIPAException e) {
            e.printStackTrace();
            doDelete();
        }

        // Comportamiento que se pone a la espera de recibir un mensaje
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                // Esperamos a recibir un mensaje
                ACLMessage msg = blockingReceive();
                String message = msg.getContent();

                // Si es un mensaje de preparación de la competición...
                if (message.startsWith("Id")) {
                    System.out.println("[Jugador " + ID + "] Mensaje recibido: " + message);

                    // Extraemos del contenido el ID, el número de jugadores, de rondas y el
                    // porcentaje de comisión
                    String[] partes = message.split("#");
                    ID = Integer.parseInt(partes[1]);
                    N = Integer.parseInt(partes[2].split(",")[0]);
                    R = Integer.parseInt(partes[2].split(",")[1]);
                    F = Double.parseDouble(partes[2].split(",")[2]);

                    // Inicializamos el historial de cada jugador
                    for (int i = 0; i < N; i++) {
                        // Excluímos nuestro propio ID
                        if (i != ID) {
                            history.put(i, new HashMap<>());
                        }
                    }
                }
                // Si es un mensaje de nueva ronda...
                else if (message.startsWith("NewGame")) {
                    System.out.println("[Jugador " + ID + "] Mensaje recibido: " + message);

                    // Extraemos del contenido los IDs de los jugadores
                    String[] partes = message.split("#");
                    int ID1 = Integer.parseInt(partes[1]);
                    int ID2 = Integer.parseInt(partes[2]);

                    // Buscamos el ID del rival
                    if (ID1 == ID) {
                        ID_rival = ID2;
                    } else {
                        ID_rival = ID1;
                    }
                }
                // Si es un mensaje de solicitud de acción...
                else if (message.startsWith("Action")) {
                    System.out.println("[Jugador " + ID + "] Mensaje recibido: " + message);

                    // Seleccionamos una respuesta aleatoriamente y construímos el mensaje
                    String action = new Random().nextBoolean() ? "D" : "C";
                    String reply = "Action#" + action;

                    // Enviamos el mensaje
                    sendReply(ACLMessage.INFORM, msg, reply);
                    System.out.println("[Jugador " + ID + "] Mensaje enviado: " + reply);

                    // Si es un mensaje de resultados...
                } else if (message.startsWith("Results")) {
                    System.out.println("[Jugador " + ID + "] Mensaje recibido: " + message);

                    // Si es un mensaje de fin de ronda...
                } else if (message.startsWith("RoundOver")) {
                    System.out.println("[Jugador " + ID + "] Mensaje recibido: " + message);

                    // Extraemos del contenido toda la información necesaria
                    String[] partes = message.split("#");
                    double totalPayoff = Double.parseDouble(partes[3]);
                    double stocks = Double.parseDouble(partes[5]);
                    double stockValue = Double.parseDouble(partes[6]);

                    // Seleccionamos una acción aleatoriamente
                    String action;
                    action = new Random().nextBoolean() ? "Buy" : "Sell";

                    // Seleccionamos una cantidad
                    double amount;
                    // Si es compra, comprobamos si podemos comprar
                    if (action.equals("Buy")) {
                        if (totalPayoff == 0) {
                            amount = 0;
                        } else {
                            // Entre 0 y el máximo que podemos comprar con lo que tenemos
                            amount = new Random().nextDouble(totalPayoff / stockValue);
                        }
                        // Si es venta, comprobamos si podemos vender
                    } else if (stocks == 0) {
                        amount = 0;
                    } else {
                        // Entre 0 y todo el stock que tenemos
                        amount = new Random().nextDouble(stocks);
                    }

                    // Redondeamos la cantidad a dos dígitos decimales
                    amount = Double.parseDouble((new DecimalFormat("#.##")).format(amount));

                    // Construímos el mensaje
                    String reply = action + "#" + amount;

                    // Enviamos el mensaje
                    sendReply(ACLMessage.INFORM, msg, reply);
                    System.out.println("[Jugador " + ID + "] Mensaje enviado: " + reply);

                    // Si es un mensaje de contabilidad...
                } else if (message.startsWith("Accounting")) {
                    System.out.println("[Jugador " + ID + "] Mensaje recibido: " + message);

                    // Si es un mensaje de fin de torneo...
                } else if (message.startsWith("GameOver")) {
                    System.out.println("[Jugador " + ID + "] Mensaje recibido: " + message);
                    doDelete();
                }
            }
        });
    }

    @Override
    protected void takeDown() {
        // Eliminamos el agente del DF
        try {
            DFService.deregister(this);
            System.out.println("[Jugador " + ID + "] Agente desregistrado en el DF");
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        System.out.println("[Jugador " + ID + "] Se ha terminado el agente");
    }

    // Método para enviar un mensaje a un agente
    public void sendReply(int performative, ACLMessage original_message, String message) {
        ACLMessage msg = original_message.createReply();
        msg.setContent(message);
        msg.setPerformative(performative);
        send(msg);
    }
}