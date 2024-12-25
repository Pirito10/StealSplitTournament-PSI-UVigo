package agents;

import java.text.DecimalFormat;
import java.util.Random;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class RandomAgent extends Agent {

    private int ID; // Variable para almacenar el ID del agente

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
        sd.setType("RandomAgent"); // Tipo de servicio
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

                switch (message.split("#")[0]) {
                    // Si es un mensaje de preparación de la competición...
                    case "Id": {
                        System.out.println("[Jugador " + ID + "] Mensaje recibido: " + message);

                        // Extraemos del contenido el ID
                        String[] partes = message.split("#");
                        ID = Integer.parseInt(partes[1]);

                        break;
                    }

                    // Si es un mensaje de nueva ronda...
                    case "NewGame": {
                        System.out.println("[Jugador " + ID + "] Mensaje recibido: " + message);
                        // No hacemos nada

                        break;
                    }

                    // Si es un mensaje de solicitud de acción...
                    case "Action": {
                        System.out.println("[Jugador " + ID + "] Mensaje recibido: " + message);

                        // Seleccionamos una respuesta aleatoriamente y construímos el mensaje
                        String action = new Random().nextBoolean() ? "D" : "C";
                        String reply = "Action#" + action;

                        // Enviamos el mensaje
                        sendReply(ACLMessage.INFORM, msg, reply);
                        System.out.println("[Jugador " + ID + "] Mensaje enviado: " + reply);

                        break;
                    }

                    // Si es un mensaje de resultados...
                    case "Results": {
                        System.out.println("[Jugador " + ID + "] Mensaje recibido: " + message);
                        // No hacemos nada

                        break;
                    }

                    // Si es un mensaje de fin de ronda...
                    case "RoundOver": {
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

                        break;
                    }

                    // Si es un mensaje de contabilidad...
                    case "Accounting": {
                        System.out.println("[Jugador " + ID + "] Mensaje recibido: " + message);
                        // No hacemos nada

                        break;
                    }

                    // Si es un mensaje de fin de torneo...
                    case "GameOver": {
                        System.out.println("[Jugador " + ID + "] Mensaje recibido: " + message);
                        // Eliminamos el agente
                        doDelete();

                        break;
                    }
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
