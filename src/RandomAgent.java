import java.util.Random;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class RandomAgent extends Agent {

    @SuppressWarnings("unused")
    private int ID, N, R; // Variable para almacenar el ID del agente, el número de jugadores y de rondas
    @SuppressWarnings("unused")
    private float F; // Variable para almacenar el porcentaje de comisión

    @Override
    protected void setup() {

        // Pequeño delay inicial para que inicialice JADE
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("[Jugador X] Se ha inicializado un agente: " + getLocalName());

        // Creamos una descripción del agente
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID()); // AID = Agent ID
        ServiceDescription sd = new ServiceDescription();
        sd.setName(getLocalName()); // Nombre del servicio
        sd.setType("player"); // Tipo de servicio
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
                    // Extraemos del contenido el ID y los parámetros del torneo
                    String[] partes = message.split("#");
                    ID = Integer.parseInt(partes[1]);

                    partes = partes[2].split(",");
                    N = Integer.parseInt(partes[0]);
                    R = Integer.parseInt(partes[1]);
                    F = Float.parseFloat(partes[2]);

                    System.out.println("[Jugador " + ID + "] Mensaje recibido: " + message);

                    // Si es un mensaje de nueva ronda...
                } else if (message.startsWith("NewGame")) {
                    System.out.println("[Jugador " + ID + "] Mensaje recibido: " + message);
                    /**
                     * TODO
                     * ? ¿Sirve de algo saber contra quién juego en el agente random?
                     * // Extraemos del contenido los IDs
                     * String[] partes = message.split("#");
                     * partes = partes[1].split(",");
                     * 
                     * int player1 = Integer.parseInt(partes[0]);
                     * int player2 = Integer.parseInt(partes[0]);
                     * 
                     * 
                     * if (player1 == ID) {
                     * enemy = player2;
                     * } else {
                     * enemy = player1;
                     * }
                     */
                    // Si es un mensaje de solicitud de acción...
                } else if (message.startsWith("Action")) {
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
                    // TODO
                    // ? ¿De qué sirve saber los resultados de cada jugada en el agente random?

                    // Si es un mensaje de fin de ronda...
                } else if (message.startsWith("RoundOver")) {
                    System.out.println("[Jugador " + ID + "] Mensaje recibido: " + message);

                    // Extraemos del contenido toda la información
                    String[] partes = message.split("#");
                    @SuppressWarnings("unused")
                    int roundPayoff = Integer.parseInt(partes[2]);
                    double totalPayoff = Double.parseDouble(partes[3]);
                    @SuppressWarnings("unused")
                    double inflationRate = Double.parseDouble(partes[4]);
                    double stocks = Double.parseDouble(partes[5]);
                    double stockValue = Double.parseDouble(partes[6]);
                    // TODO
                    // ? ¿De qué sirve saber el payoff de la ronda y el valor de inflación en el
                    // ? agente random?

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

                    // Construímos el mensaje
                    String reply = action + "#" + MainAgent.round(amount);

                    // Enviamos el mensaje
                    sendReply(ACLMessage.INFORM, msg, reply);
                    System.out.println("[Jugador " + ID + "] Mensaje enviado: " + reply);

                    // Si es un mensaje de contabilidad...
                } else if (message.startsWith("Accounting")) {
                    System.out.println("[Jugador " + ID + "] Mensaje recibido: " + message);
                    // TODO
                    // ? ¿De qué sirve saber el payoff y los stocks en el agente random?

                    // Si es un mensaje de fin de torneo...
                } else if (message.startsWith("GameOver")) {
                    System.out.println("[Jugador ]" + ID + "] Mensaje recibido: " + message);
                    // TODO
                    // ? ¿De qué sirve saber que ha terminado el torneo en el agente random?
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
