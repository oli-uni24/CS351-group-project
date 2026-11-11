package org.strathclyde.controller;

import org.strathclyde.model.DockingBay;
import org.strathclyde.model.MaintenanceTeam;
import org.strathclyde.model.Transport;
import org.strathclyde.model.TransportState;

public class TransportController {
    private final DockingBayController bayController;

    public TransportController(DockingBayController bayController) {
        this.bayController = bayController;
    }

    public void allocateBay(Transport transport, DockingBay bay) {
        // Check valid state before allocating (Spec rule: must be WAITING)
        if (transport.getState() == TransportState.WAITING) {
            bayController.allocateBay(bay, transport);
            transport.setState(TransportState.DOCKED);
        } else {
            throw new IllegalStateException("Transport must be WAITING to dock.");
        }
    }

    public void assignTeam(MaintenanceTeam team, Transport transport) {
        // Lock the transport to prevent two teams being assigned at once
        synchronized (transport) {
            if (transport.getState() != TransportState.DOCKED) {
                throw new IllegalStateException("Transport must be DOCKED to receive maintenance.");
            }

            // This will throw an error if the team is already busy
            team.assignTeam(transport);

            transport.setMaintenanceTeam(team);
            transport.setState(TransportState.SERVICING);
        }
    }

    public void completeMaintenance(Transport transport) {
        if (transport.getState() == TransportState.SERVICING) {
            MaintenanceTeam team = transport.getMaintenanceTeam();
            if (team != null) {
                team.completeMaintenance();
                transport.setMaintenanceTeam(null);
            }
            transport.setState(TransportState.READY);
        }
    }
}