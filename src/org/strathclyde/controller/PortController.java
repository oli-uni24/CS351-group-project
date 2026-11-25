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

    // --- View Methods for my testing---

    public void viewAllResources() {
        System.out.println("\n--- PORT WANDER STATUS ---");

        List<Transport> transports = transportRepository.getAllTransports();

        System.out.println("Transports: " + transports.size());
        for (Transport t : transports) {
            System.out.println(" - " + t.getId() + " [" + t.getState() + "]");
        }

        System.out.println("Docking Bays: " + dockingBays.size());
        for (DockingBay b : dockingBays) {
            System.out.println(" - " + b.getId() + " [" + b.getBayState() + "]");
        }

        System.out.println("Maintenance Teams: " + maintenanceTeams.size());
        for (MaintenanceTeam m : maintenanceTeams) {
            System.out.println(" - " + m.getId() + " [" + m.getTeamState() + "]");
        }
        System.out.println("--------------------------\n");
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

        List<Transport> transports = transportRepository.getAllTransports();

        message.append("TRANSPORTS:\n");
        for (Transport t : transports) {
            message.append(t.getId())
                    .append(" [")
                    .append(t.getState())
                    .append(", ")
                    .append(t.getCurrentCargo())
                    .append(" cargo]\n");
        }

        message.append("\nDOCKING BAYS:\n");
        for (DockingBay b : dockingBays) {
            message.append(b.getId())
                    .append(" [")
                    .append(b.getBayState())
                    .append("]\n");
        }

        message.append("\nMAINTENANCE TEAMS:\n");
        for (MaintenanceTeam m : maintenanceTeams) {
            message.append(m.getId())
                    .append(" [")
                    .append(m.getTeamState())
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