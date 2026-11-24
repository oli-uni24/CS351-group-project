package org.strathclyde.controller;

import org.strathclyde.model.MaintenanceTeam;
import org.strathclyde.model.Transport;
import org.strathclyde.model.TransportState;
import org.strathclyde.service.EventLog;
import org.strathclyde.common.ServerResponse;

//TODO: remove not implemented method when implemented

public class TransportController {

    private final EventLog eventLog;

    public TransportController( EventLog eventLog) {
        this.eventLog = eventLog;
    }


    public void assignTeam(MaintenanceTeam team, Transport transport) {
        // Lock the transport to prevent two teams being assigned at once
        synchronized (transport) {
            if(team == null){
                throw new IllegalStateException("Maintenance team cannot be null.");
            }
            if (transport.getState() != TransportState.DOCKED) {
                throw new IllegalStateException("Transport must be DOCKED to receive maintenance.");
            }

            // This will throw an error if the team is already busy
            team.assignTeam(transport);

            transport.setMaintenanceTeam(team);
            transport.setState(TransportState.SERVICING);

            // Record successful maintenance assignment
            eventLog.recordEvent(
                    "MAINTENANCE_ASSIGNMENT",
                    transport.getId(),
                    team.getId(),
                    "Maintenance team assigned"
            );
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

    public ServerResponse addTransport(int maxCapacity, String departureTime) {
        return notImplemented();
    }

    //one transport's details
    //message has one item per line
    public ServerResponse viewTransport(){
        return notImplemented();
    }

    public ServerResponse assignTeam(String transportId, String teamId){
        return notImplemented();
    }

    public ServerResponse completeMaintenance(String transportId){
        return notImplemented();
    }

    private ServerResponse notImplemented() {
        return new ServerResponse(false, "Nothing here yet");
    }
}