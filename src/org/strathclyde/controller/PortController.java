package org.strathclyde.controller;

import org.strathclyde.model.DockingBay;
import org.strathclyde.model.MaintenanceTeam;
import org.strathclyde.model.Transport;
import org.strathclyde.common.ServerResponse;
import org.strathclyde.service.EventLog;
import org.strathclyde.model.TransportRepository;

import java.util.ArrayList;
import java.util.List;

public class PortController {
    // These lists act as your "Database" in memory
    private final TransportRepository transportRepository;
    private final List<DockingBay> dockingBays;
    private final List<MaintenanceTeam> maintenanceTeams;
    private final EventLog eventLog;

    public PortController(EventLog eventLog, TransportRepository transportRepository) {
        this.transportRepository = transportRepository;
        this.dockingBays = new ArrayList<>();
        this.maintenanceTeams = new ArrayList<>();
        this.eventLog = eventLog;
    }

    // --- Admin Methods ---

    public void addTransport(Transport transport) {
        transportRepository.addTransport(transport);
        System.out.println("Admin added transport: " + transport.getId());
    }

    public synchronized void addDockingBay(DockingBay bay) {
        dockingBays.add(bay);
    }

    public synchronized void addMaintenanceTeam(MaintenanceTeam team) {
        maintenanceTeams.add(team);
    }

    // Getters to use during integration
    public synchronized List<Transport> getTransports() {
        return transportRepository.getAllTransports();
    }

    public synchronized List<DockingBay> getDockingBays() {
        return dockingBays;
    }

    public synchronized List<MaintenanceTeam> getMaintenanceTeams() {
        return maintenanceTeams;
    }

    // Added viewResources and viewEventLog
    //all transports, bays, teams and cargo
    // message holds one item per line
    public ServerResponse viewResources() {

        StringBuilder message = new StringBuilder();

        message.append("--- PORT WANDER STATUS ---\n");

        // Display transports
        message.append("\nTRANSPORTS:\n");

        for (Transport t : transportRepository.getAllTransports()) {

            // Get the assigned docking bay
            String bayId = "none";

            if (t.getDockingBay() != null) {
                bayId = t.getDockingBay().getId();
            }

            // Get the assigned maintenance team
            String teamId = "none";

            if (t.getMaintenanceTeam() != null) {
                teamId = t.getMaintenanceTeam().getId();
            }

            message.append(t.getId())
                    .append(" [")
                    .append(t.getState())
                    .append(", bay ")
                    .append(bayId)
                    .append(", team ")
                    .append(teamId)
                    .append(", cargo ")
                    .append(t.getCurrentCargo())
                    .append("/")
                    .append(t.getMaxCapacity())
                    .append("]\n");
        }

        // Display docking bays
        message.append("\nDOCKING BAYS:\n");

        for (DockingBay b : dockingBays) {

            String transportId = "none";

            if (b.getAllocatedTransport() != null) {
                transportId = b.getAllocatedTransport().getId();
            }

            message.append(b.getId())
                    .append(" [")
                    .append(b.getBayState())
                    .append(", transport ")
                    .append(transportId)
                    .append("]\n");
        }

        // Display maintenance teams
        message.append("\nMAINTENANCE TEAMS:\n");

        for (MaintenanceTeam m : maintenanceTeams) {

            String transportId = "none";

            if (m.getTransport() != null) {
                transportId = m.getTransport().getId();
            }

            message.append(m.getId())
                    .append(" [")
                    .append(m.getTeamState())
                    .append(", transport ")
                    .append(transportId)
                    .append("]\n");
        }

        return new ServerResponse(true, message.toString());
    }
    //event log
    // message holds one event per line
    public ServerResponse viewEventLog() {
        StringBuilder message = new StringBuilder();

        message.append("--- EVENT LOG ---\n");

        List<String> events = eventLog.getEvents();

        if (events.isEmpty()) {
            message.append("No events recorded.");
        } else {
            for (String event : events) {
                message.append(event).append("\n");
            }
        }

        return new ServerResponse(true, message.toString());
    }

    public Transport findTransportById(String transportId) {
        return transportRepository.findById(transportId);
    }


    public DockingBay findDockingBayById(String bayId) {
        for (DockingBay b : dockingBays) {
            if (b.getId().equals(bayId)) {
                return b;
            }
        }
        return null;
    }

    public MaintenanceTeam findMaintenanceTeamById(String teamId) {
        for (MaintenanceTeam m : maintenanceTeams) {
            if (m.getId().equals(teamId)) {
                return m;
            }
        }
        return null;
    }
}