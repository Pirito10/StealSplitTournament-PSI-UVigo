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

public class NN_Agent extends Agent {

    // Variables para almacenar el ID del agente, el del rival, el número de
    // jugadores y el porcentaje de comisión
    private int ID, opponentID, N;
    private double F;

    // Variable para almacenar el SOM (Self-Organizing Map)
    private SOM som;

    // Tamaño de la cuadrícula del SOM
    private static final int GRID_SIZE = 22;

    // Mapa para almacenar el historial de cada oponente
    private HashMap<Integer, LinkedList<String>> opponentsHistories = new HashMap<>();

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
        sd.setType("NN_Agent"); // Tipo de servicio
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

                        // Inicializamos el historial de cada oponente
                        for (int i = 0; i < N; i++) {
                            // Excluímos nuestro propio ID
                            if (i != ID) {
                                opponentsHistories.put(i, new LinkedList<>());
                            }
                        }

                        // Inicializamos el SOM con una cuadrícula 5x5 y entradas de tamaño 2 (C y D)
                        som = new SOM(GRID_SIZE, 2);

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

                        // Obtenemos el historial del oponente
                        LinkedList<String> opponentHistory = opponentsHistories.get(opponentID);

                        // Creamos un vector de frecuencias para el SOM con dos valores (C y D)
                        double[] opponentHistoryVector = new double[2];

                        // Recorremos el historial actualizando el vector
                        for (String action : opponentHistory) {
                            if (action.equals("C")) {
                                opponentHistoryVector[0] += 1;
                            } else if (action.equals("D")) {
                                opponentHistoryVector[1] += 1;
                            }
                        }

                        // Normalizamos el vector
                        double sum = opponentHistoryVector[0] + opponentHistoryVector[1];
                        if (sum > 0) {
                            opponentHistoryVector[0] /= sum;
                            opponentHistoryVector[1] /= sum;
                        }

                        // Usamos el SOM para obtener la BMU (Best Matching Unit)
                        String bmuPosition = som.sGetBMU(opponentHistoryVector, true);

                        // Obtenemos las coordenadas de la BMU
                        int x = Integer.parseInt(bmuPosition.split(",")[0]);
                        int y = Integer.parseInt(bmuPosition.split(",")[1]);

                        // Decidimos la acción según la distancia en la cuadrícula
                        String action;
                        if (x + y < GRID_SIZE / 2) {
                            action = "C";
                        } else {
                            action = "D";
                        }

                        // Construímos el mensaje
                        String reply = "Action#" + action;

                        // Enviamos el mensaje
                        sendReply(ACLMessage.INFORM, msg, reply);
                        System.out.println("[Jugador " + ID + "] Mensaje enviado: " + reply);

                        break;
                    }

                    // Si es un mensaje de resultados...
                    case "Results": {
                        System.out.println("[Jugador " + ID + "] Mensaje recibido: " + message);

                        // Extraemos del contenido la acción del oponente
                        String[] partes = message.split("#");
                        String opponentAction = (ID == Integer.parseInt(partes[1].split(",")[0]))
                                ? partes[2].split(",")[1]
                                : partes[2].split(",")[0];

                        // Actualizamos el historial del oponente
                        opponentsHistories.get(opponentID).add(opponentAction);

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

class SOM {
    // WorldGrid.java, DlgInfoBrain.java
    private int iGridSide; // Side of the SOM 2D grid
    private int[][] iNumTimesBMU; // Number of times a cell has been a BMU
    private int[] iBMU_Pos = new int[2]; // BMU position in the grid

    private int iInputSize; // Size of the input vector
    private int iRadio; // BMU radio to modify neurons
    private double dLearnRate = 1.0; // Learning rate for this SOM
    private double dDecLearnRate = 0.999; // Used to reduce the learning rate
    private double[] dBMU_Vector = null; // BMU state
    private double[][][] dGrid; // SOM square grid + vector state per neuron

    /**
     * This is the class constructor that creates the 2D SOM grid
     * 
     * @param iSideAux      the square side
     * @param iInputSizeAux the dimensions for the input data
     * 
     */
    public SOM(int iSideAux, int iInputSizeAux) {
        iInputSize = iInputSizeAux;
        iGridSide = iSideAux;
        iRadio = iGridSide / 10;
        dBMU_Vector = new double[iInputSize];
        dGrid = new double[iGridSide][iGridSide][iInputSize];
        iNumTimesBMU = new int[iGridSide][iGridSide];

        vResetValues();
    }

    public void vResetValues() {
        dLearnRate = 1.0;
        iNumTimesBMU = new int[iGridSide][iGridSide];
        iBMU_Pos[0] = -1;
        iBMU_Pos[1] = -1;

        for (int i = 0; i < iGridSide; i++) // Initializing the SOM grid/network
            for (int j = 0; j < iGridSide; j++)
                for (int k = 0; k < iInputSize; k++)
                    dGrid[i][j][k] = Math.random();
    }

    public double[] dvGetBMU_Vector() {
        return dBMU_Vector;
    }

    public double dGetLearnRate() {
        return dLearnRate;
    }

    public double[] dGetNeuronWeights(int x, int y) {
        return dGrid[x][y];
    }

    /**
     * This is the main method that returns the coordinates of the BMU and trains
     * its neighbors
     * 
     * @param dmInput contains the input vector
     * @param bTrain  training or testing phases
     * 
     */
    public String sGetBMU(double[] dmInput, boolean bTrain) {
        int x = 0, y = 0;
        double dNorm, dNormMin = Double.MAX_VALUE;
        String sReturn;

        for (int i = 0; i < iGridSide; i++) // Finding the BMU
            for (int j = 0; j < iGridSide; j++) {
                dNorm = 0;
                for (int k = 0; k < iInputSize; k++) // Calculating the norm
                    dNorm += (dmInput[k] - dGrid[i][j][k]) * ((dmInput[k] - dGrid[i][j][k]));

                if (dNorm < dNormMin) {
                    dNormMin = dNorm;
                    x = i;
                    y = j;
                }
            } // Leaving the loop with the x,y positions for the BMU

        if (bTrain) {
            int xAux = 0;
            int yAux = 0;
            for (int v = -iRadio; v <= iRadio; v++) // Adjusting the neighborhood
                for (int h = -iRadio; h <= iRadio; h++) {
                    xAux = x + h;
                    yAux = y + v;

                    if (xAux < 0) // Assuming a torus world
                        xAux += iGridSide;
                    else if (xAux >= iGridSide)
                        xAux -= iGridSide;

                    if (yAux < 0)
                        yAux += iGridSide;
                    else if (yAux >= iGridSide)
                        yAux -= iGridSide;

                    for (int k = 0; k < iInputSize; k++)
                        dGrid[xAux][yAux][k] += dLearnRate * (dmInput[k] - dGrid[xAux][yAux][k]) / (1 + v * v + h * h);
                }
        }

        sReturn = "" + x + "," + y;
        iBMU_Pos[0] = x;
        iBMU_Pos[1] = y;
        dBMU_Vector = dGrid[x][y].clone();
        iNumTimesBMU[x][y]++;
        dLearnRate *= dDecLearnRate;

        return sReturn;
    }
} // from the class SOM