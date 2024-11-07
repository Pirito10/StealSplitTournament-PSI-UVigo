import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class MainAgent extends Agent {

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
                    if (result.length > 0) {
                        System.out.println("[Main] Se han encontrado " + result.length + " jugadores:");
                        for (DFAgentDescription agentDesc : result) {
                            AID agentID = agentDesc.getName();
                            System.out.println("\t- " + agentID.getLocalName());
                        }
                    } else {
                        System.out.println("[Main] No se encontraron agentes jugadores.");
                    }
                } catch (FIPAException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void takeDown() {
        System.out.println("[Main] Se ha terminado el agente principal");
    }
}
