import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class RandomAgent extends Agent {

    private String ID; // Variable para almacenar el ID del agente

    @Override
    protected void setup() {

        // Pequeño delay inicial para que inicialice JADE
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Obtenemos el ID del agente (último carácter de su nombre, JugadorAgentX)
        if (getLocalName().matches("RandomAgent\\d+")) {
            ID = getLocalName().replace("RandomAgent", "");
        } else {
            ID = "null";
        }

        // Mensaje de inicialización
        System.out.println("[Jugador " + ID + "] Se ha inicializado un agente: " + getLocalName());

        // Registramos el agente en el DF (Directory Facilitator)
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID()); // AID = Agent ID
        ServiceDescription sd = new ServiceDescription();
        sd.setName(getLocalName()); // Nombre del servicio
        sd.setType("jugador"); // Tipo de servicio
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
            System.out.println("[Jugador " + ID + "] Agente registrado en el DF");
        } catch (FIPAException e) {
            e.printStackTrace();
        }
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
