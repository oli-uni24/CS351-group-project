package org.strathclyde.server;

import org.strathclyde.controller.CargoManagementController;
import org.strathclyde.controller.DockingBayController;
import org.strathclyde.controller.PortController;
import org.strathclyde.controller.TransportController;
import org.strathclyde.model.DockingBay;
import org.strathclyde.model.MaintenanceTeam;
import org.strathclyde.model.Transport;
import org.strathclyde.model.TransportRepository;
import org.strathclyde.service.DepartureScheduler;
import org.strathclyde.service.EventLog;
import org.strathclyde.service.PersistenceManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PortWanderServer {

    private final static int PORT = 50000;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private final ClientRegistry registry = new ClientRegistry();
    private final EventLog eventLog = new EventLog();
    private final TransportRepository transportRepository = new TransportRepository();
    private final PortController portController = new PortController(eventLog, transportRepository);
    private final DockingBayController bayController = new DockingBayController(eventLog, portController);
    private final TransportController transportController = new TransportController(eventLog, transportRepository, portController);
    private final CargoManagementController cargoController = new CargoManagementController(transportRepository, eventLog, portController);
    private final DepartureScheduler departureScheduler = new DepartureScheduler(transportRepository, eventLog);
    private final PersistenceManager persistenceManager = new PersistenceManager();

    private ServerSocket serverSocket = null; // listens for incoming connections

    public PortWanderServer(){
        // send every new event to all connected terminals
        eventLog.addListener(new NotificationService(registry));

        PersistenceManager persistence = new PersistenceManager();
        List<Transport> transports = new ArrayList<>();
        List<DockingBay> bays = new ArrayList<>();
        List<MaintenanceTeam> teams = new ArrayList<>();

        persistence.loadData(transports, bays, teams);

        for(Transport t : transports){
            transportRepository.addTransport(t);
        }
        for(DockingBay b : bays)   {
            portController.addDockingBay(b);
        }
        for (MaintenanceTeam m : teams){
            portController.addMaintenanceTeam(m);
        }
    }

    public void startServer() throws IOException {
        // create the server socket to listen to incoming requests
        serverSocket = new ServerSocket(PORT);
        System.out.println("Server listening on port " + PORT);
        departureScheduler.start();
        acceptConnections();
    }

    private void acceptConnections() {
        while (!serverSocket.isClosed()) {
            try {
                Socket socket = serverSocket.accept(); // blocks until a client connects
                threadPool.execute(new ClientSessionHandler(socket, registry, portController, bayController, transportController, cargoController));
            } catch (IOException e) {
                if (!serverSocket.isClosed()) { //if its not running, socket was closed on purpose
                    System.err.println("Accept connection failed: " + e.getMessage());
                }
            }
        }
    }

    public void shutdown() {
        try {
            if (serverSocket != null) serverSocket.close(); //accept() will throw and close loop
        } catch (IOException e) {
        } //ignored
        //save everthing on shutdown
        System.out.println("System saving and shutting down...");
        departureScheduler.stop();
        threadPool.shutdown();
        persistenceManager.saveData(
                portController.getTransports(),
                portController.getDockingBays(),
                portController.getMaintenanceTeams());
    }

  public static void main(String[] args) throws IOException {
        PortWanderServer server = new PortWanderServer();
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
        server.startServer();
    }

}

