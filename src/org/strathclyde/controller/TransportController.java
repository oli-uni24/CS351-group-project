package org.strathclyde.controller;

import com.sun.tools.javac.Main;
import org.strathclyde.model.*;
import org.strathclyde.service.EventLog;
import org.strathclyde.common.ServerResponse;
import org.strathclyde.service.PersistenceManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

//TODO: remove not implemented method when implemented

public class TransportController {

    private final EventLog eventLog;
    private final TransportRepository transportRepository;
    private final PortController portController;

    public TransportController( EventLog eventLog, TransportRepository transportRepository, PortController portController) {
        this.eventLog = eventLog;
        this.transportRepository = transportRepository;
        this.portController = portController;
    }

    //Adds a new transport
    public ServerResponse addTransport(int maxCapacity, String departureTime) {

        try {
            if (departureTime == null || departureTime.isBlank()) {
                return new ServerResponse(false,
                        "Departure time is required");
            }

            LocalDateTime departure = LocalDateTime.parse(departureTime);

            Transport transport =
                    transportRepository.createTransport(maxCapacity, departure);

            eventLog.recordEvent(
                    "TRANSPORT_CREATED",
                    transport.getId(),
                    "PORT",
                    "Transport created successfully"
            );

            PersistenceManager.saveData(portController.getTransports(), portController.getDockingBays(), portController.getMaintenanceTeams());

            return new ServerResponse(true,
                    "Transport " + transport.getId() + " created successfully");


        } catch (DateTimeParseException e) {
            return new ServerResponse(false,
                    "Invalid departure time. Use format YYYY-MM-DDTHH:MM");

        } catch (IllegalArgumentException e) {
            return new ServerResponse(false, e.getMessage());
        }

    }

    public void completeMaintenance(Transport transport) {
        synchronized (transport) {
            if (transport.getState() != TransportState.SERVICING) {
                throw new IllegalStateException("Transport must be SERVICING to complete maintenance.");
            }
            MaintenanceTeam team = transport.getMaintenanceTeam();
            if (team == null) {
                throw new IllegalStateException(
                        "No maintenance team assigned to this transport."
                );
            }

            team.completeMaintenance();
            transport.setMaintenanceTeam(null);

            transport.setState(TransportState.READY);

            // Record successful maintenance completion
            eventLog.recordEvent(
                    "MAINTENANCE_COMPLETED",
                    transport.getId(),
                    team.getId(),
                    "Maintenance completed successfully"
            );
        }
    }

    //Returns details of one transport
    public ServerResponse viewTransport(String transportId){


            //Look up the transport using its ID.
            //If it doesnt exist, then unsuccessful response is returned
            Transport transport = portController.findTransportById(transportId);

            if (transport == null) {
                return new ServerResponse(false,
                        "Transport " + transportId + " not found");
            }

            //Store the transport details in local variables.
            //This will allow us to be able to build the response after releasing the lock
            DockingBay bay;
            MaintenanceTeam team;
            String state;
            int cargo;
            LocalDateTime departure;

            //Synched so transport on another thread cant change
            //This helps prevent the response from containing a mixture
            // of old and new values if another client updates the transport
            synchronized (transport) {
                bay = transport.getDockingBay();
                team = transport.getMaintenanceTeam();
                state = transport.getState().toString();
                cargo = transport.getCurrentCargo();
                departure = transport.getScheduledDeparture();
            }

        // Display "none" if the transport has no allocated bay or team.
        String bayId = transport.getDockingBay() == null ? "none" : bay.getId();
        String teamId = transport.getMaintenanceTeam() == null ? "none" : team.getId();

        // Build a readable response containing the transport's details.
        // Cargo is displayed as current quantity / maximum capacity.
        String message = "--- TRANSPORT DETAILS --- \n"
                + "ID: " + transport.getId() + "\n"
                + "State " + transport.getState() + "\n"
                + "Docking Bay: " + bayId + "\n"
                + "Maintenance Team: " + teamId + "\n"
                + "Cargo " + transport.getCurrentCargo() + "\n"
                + "/" + transport.getMaxCapacity() + "\n"
                + "Scheduled Departue: " + transport.getScheduledDeparture();

        PersistenceManager.saveData(portController.getTransports(), portController.getDockingBays(), portController.getMaintenanceTeams());

        //return the transport details
        return new ServerResponse(true, message);
    }

    public ServerResponse assignTeam(String transportId, String teamId){
        Transport transport = portController.findTransportById(transportId);
        MaintenanceTeam team = portController.findMaintenanceTeamById(teamId);

        if (transport == null) {
            return new ServerResponse(false, "Transport " + transportId + " not found");
        }


        if (team == null) {
            return new ServerResponse(false, "Transport " + teamId + " not found");
        }

        try {

            //MainteanceTeam handles the actual assignment and state changes
            team.assignTeam(transport);

            //Record successful maintenance assignment
            eventLog.recordEvent("MAINTENANCE_ASSIGNMENT", transport.getId(), team.getId(), "Maintenance team assigned");

            PersistenceManager.saveData(portController.getTransports(), portController.getDockingBays(), portController.getMaintenanceTeams());

            return new ServerResponse(true, "Maintenance team " + teamId + " assigned to transport" + transportId);

        } catch (IllegalArgumentException | IllegalStateException e) {
            return new ServerResponse(false, e.getMessage());
        }
    }

    public ServerResponse completeMaintenance(String transportId){
        Transport transport = portController.findTransportById(transportId);

        if (transport == null) {
            return new ServerResponse(false, "Transport " + transportId + " not found");
        }

        try {
            MaintenanceTeam team = transport.getMaintenanceTeam();

            if (team == null) {
                return new ServerResponse(false, "No Maintenace team assigned to this transport");
            }

            //Handles the completion and state changes
            team.completeMaintenance();

            //Record successful maintenance completion
            eventLog.recordEvent("MAINTENANCE_COMPLETED", transport.getId(), team.getId(), "Maintenance completed successfully");

            PersistenceManager.saveData(portController.getTransports(), portController.getDockingBays(), portController.getMaintenanceTeams());

            return new ServerResponse(true, "Maintenance completed for transport " + transportId);

        } catch (IllegalArgumentException | IllegalStateException e){
            return new ServerResponse(false, e.getMessage());
        }
    }

}