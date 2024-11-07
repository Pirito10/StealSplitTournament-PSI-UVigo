import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class RandomAgent extends Agent {

    private int ID, N, R; // Variable para almacenar el ID del agente, el número de jugadores y de rondas
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

                System.out.println("[Jugador X] Mensaje recibido: " + message);

                // Si es un mensaje de preparación de la competición
                if (message.startsWith("Id")) {
                    // Extraemos del contenido el ID y los parámetros del torneo
                    String[] partes = message.split("#");
                    ID = Integer.parseInt(partes[1]);

                    partes = partes[2].split(",");
                    N = Integer.parseInt(partes[0]);
                    R = Integer.parseInt(partes[1]);
                    F = Float.parseFloat(partes[2]);

                } else {
                    /* STEP 2 */
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
}
