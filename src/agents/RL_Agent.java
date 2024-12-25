package agents;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedList;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class RL_Agent extends Agent {

    // Variables para almacenar el ID del agente, el del rival, el número de
    // jugadores y el porcentaje de comisión
    private int ID, opponentID, N;
    private double F;

    // Variable para almacenar la acción a realizar
    private String action;

    // Mapa para almacenar la tabla Q de cada oponente
    private HashMap<Integer, HashMap<String, Double>> qTables = new HashMap<>();

    // Variables para almacenar el historial de inflación y precios de los stocks
    private LinkedList<Double> inflationHistory = new LinkedList<>();
    private LinkedList<Double> stockPriceHistory = new LinkedList<>();

    // Tamaño de la ventana deslizante para las predicciones
    private static final int WINDOW_SIZE = 20;

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

                switch (message.split("#")[0]) {
                    // Si es un mensaje de preparación de la competición...
                    case "Id": {
                        System.out.println("[Jugador " + ID + "] Mensaje recibido: " + message);

                        // Extraemos del contenido el ID, el número de jugadores y el
                        // porcentaje de comisión
                        String[] partes = message.split("#");
                        ID = Integer.parseInt(partes[1]);
                        N = Integer.parseInt(partes[2].split(",")[0]);
                        F = Double.parseDouble(partes[2].split(",")[2]);

                        // Inicializamos la tabla Q de cada jugador
                        for (int i = 0; i < N; i++) {
                            // Excluímos nuestro propio ID
                            if (i != ID) {
                                // Creamos un mapa que representa la tabla Q
                                HashMap<String, Double> qValues = new HashMap<>();
                                // Le damos valores inciales a las acciones, favoreciendo la D
                                qValues.put("C", 2.0);
                                qValues.put("D", 2.2);
                                // Añadimos la tabla Q al mapa de tablas Q
                                qTables.put(i, qValues);
                            }
                        }

                        break;
                    }

                    // Si es un mensaje de nueva ronda...
                    case "NewGame": {
                        System.out.println("[Jugador " + ID + "] Mensaje recibido: " + message);

                        // Extraemos del contenido los IDs de los jugadores
                        String[] partes = message.split("#");
                        int ID1 = Integer.parseInt(partes[1]);
                        int ID2 = Integer.parseInt(partes[2]);

                        // Buscamos el ID del oponente
                        opponentID = (ID1 == ID) ? ID2 : ID1;

                        break;
                    }

                    // Si es un mensaje de solicitud de acción...
                    case "Action": {
                        System.out.println("[Jugador " + ID + "] Mensaje recibido: " + message);

                        // Obtenemos la tabla Q del oponente
                        HashMap<String, Double> opponentQTable = qTables.get(opponentID);

                        // Elegimos la acción que maximice la recompensa esperada y construímos el
                        // mensaje
                        action = opponentQTable.get("C") >= opponentQTable.get("D") ? "C" : "D";
                        String reply = "Action#" + action;

                        // Enviamos el mensaje
                        sendReply(ACLMessage.INFORM, msg, reply);
                        System.out.println("[Jugador " + ID + "] Mensaje enviado: " + reply);

                        break;
                    }

                    // Si es un mensaje de resultados...
                    case "Results": {
                        System.out.println("[Jugador " + ID + "] Mensaje recibido: " + message);

                        // Extraemos del contenido el payoff
                        String[] partes = message.split("#");
                        String[] payoffs = partes[3].split(",");
                        double payoff = (ID == Integer.parseInt(partes[1].split(",")[0]))
                                ? Double.parseDouble(payoffs[0])
                                : Double.parseDouble(payoffs[1]);

                        // Obtenemos la tabla Q del oponente
                        HashMap<String, Double> opponentQTable = qTables.get(opponentID);
                        // Obtenemos el valor de la acción que realizamos
                        double currentQValue = opponentQTable.getOrDefault(action, 0.0);
                        // Calculamos el nuevo valor para la acción y lo guardamos en la tabla Q
                        double updatedQValue = currentQValue + 0.1 * (payoff - currentQValue);
                        opponentQTable.put(action, updatedQValue);

                        break;
                    }

                    // Si es un mensaje de fin de ronda...
                    case "RoundOver": {
                        System.out.println("[Jugador " + ID + "] Mensaje recibido: " + message);

                        // Extraemos del contenido toda la información necesaria
                        String[] partes = message.split("#");
                        double totalPayoff = Double.parseDouble(partes[3]);
                        double inflationRate = Double.parseDouble(partes[4]);
                        double stocks = Double.parseDouble(partes[5]);
                        double stockValue = Double.parseDouble(partes[6]);

                        // Actualizamos los historiales de inflación y precios de los stocks
                        updateHistory(inflationHistory, inflationRate);
                        updateHistory(stockPriceHistory, stockValue);

                        // Tomamos la mejor decisión basada en predicciones futuras
                        String reply = makeDecision(totalPayoff, stocks, stockValue, inflationRate, F);

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

    // Método para actualizar un historial
    private void updateHistory(LinkedList<Double> history, double newValue) {
        // Si el historial es demasiado grande, eliminamos el valor más antiguo
        if (history.size() >= WINDOW_SIZE) {
            history.removeFirst();
        }
        // Añadimos el nuevo valor
        history.addLast(newValue);
    }

    // Método para predecir el valor de la próxima ronda basado en un historial
    private double predictNextValue(LinkedList<Double> history) {
        // Si no hay datos, devolvemos un valor predeterminado
        if (history.isEmpty()) {
            return 0.0;
        }
        // Devolvemos un promedio basado en el historial
        return history.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    // Método para tomar la decisión de acción y cantidad basada en predicciones
    private String makeDecision(
            double totalPayoff, double stocks, double stockValue, double inflationRate, double commissionRate) {

        // Predecimos los valores futuros
        double predictedInflation = predictNextValue(inflationHistory);
        double predictedStockPrice = predictNextValue(stockPriceHistory);

        // Variables para almacenar la acción y la cantidad
        String action;
        double amount = 0.0;

        // Si se espera un aumento significativo en el precio de los stocks (más de un
        // 10% sobre el valor actual) o inflación alta (superior al 10%), compramos lo
        // máximo posible
        if (predictedStockPrice > stockValue * 1.1 || predictedInflation > 0.1) {
            action = "Buy";
            amount = totalPayoff / stockValue;
        }
        // Si se espera una caída significativa en el precio de los stocks (más de 10%
        // bajo el valor actual), vendemos todo
        else if (predictedStockPrice * (1 - commissionRate) < stockValue * 0.9 && stocks > 0) {
            action = "Sell";
            amount = stocks;
        }
        // Si no hay cambios significativos, mantenemos lo que tenemos
        else {
            action = "Buy";
            amount = 0.0;
        }

        // Redondeamos la cantidad a dos dígitos decimales
        amount = Double.parseDouble((new DecimalFormat("#.##")).format(amount));

        // Construímos y devolvemos el mensaje
        return action + "#" + amount;
    }
}