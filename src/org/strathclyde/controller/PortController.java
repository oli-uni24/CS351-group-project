package org.strathclyde.controller;

import org.strathclyde.model.DockingBay;
import org.strathclyde.model.MaintenanceTeam;
import org.strathclyde.model.Transport;

import java.util.ArrayList;
import java.util.List;

public class PortController {
    // These lists act as your "Database" in memory
    private final List<Transport> transports;
    private final List<DockingBay> dockingBays;
    private final List<MaintenanceTeam> maintenanceTeams;

    public PortController() {
        this.transports = new ArrayList<>();
        this.dockingBays = new ArrayList<>();
        this.maintenanceTeams = new ArrayList<>();
    }

    // --- Admin Methods ---

    public void addTransport(Transport transport) {
        transports.add(transport);
        System.out.println("Admin added transport: " + transport.getId());
    }

    public void addDockingBay(DockingBay bay) {
        dockingBays.add(bay);
    }

    public void addMaintenanceTeam(MaintenanceTeam team) {
        maintenanceTeams.add(team);
    }

    // --- View Methods ---

    public void viewAllResources() {
        System.out.println("\n--- PORT WANDER STATUS ---");
        System.out.println("Transports: " + transports.size());
        for (Transport t : transports) {
            System.out.println(" - " + t.getId() + " [" + t.getState() + "]");
        }

        System.out.println("Docking Bays: " + dockingBays.size());
        for (DockingBay b : dockingBays) {
            System.out.println(" - " + b.getId() + " [" + b.getState() + "]");
        }

        System.out.println("Maintenance Teams: " + maintenanceTeams.size());
        for (MaintenanceTeam m : maintenanceTeams) {
            System.out.println(" - " + m.getId() + " [" + m.getState() + "]");
        }
        System.out.println("--------------------------\n");
    }

    // Getters to use during integration
    public List<Transport> getTransports() { return transports; }
    public List<DockingBay> getDockingBays() { return dockingBays; }
    public List<MaintenanceTeam> getMaintenanceTeams() { return maintenanceTeams; }
}