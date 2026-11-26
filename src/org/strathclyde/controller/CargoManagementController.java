package org.strathclyde.controller;

import org.strathclyde.common.ServerResponse;
import org.strathclyde.model.DockingBay;
import org.strathclyde.model.Transport;
import org.strathclyde.model.TransportRepository;
import org.strathclyde.service.EventLog;
import org.strathclyde.service.PersistenceManager;

import javax.sound.sampled.Port;

public class CargoManagementController {

    private final TransportRepository transportRepository;
    private final EventLog eventLog;
    private final PortController portController;
    public CargoManagementController(TransportRepository transportRepository, EventLog eventLog, PortController portController) {
        this.transportRepository = transportRepository;
        this.eventLog = eventLog;
        this.portController = portController;
    }

    public ServerResponse loadCargo(String transportId, int units) {
        Transport transport = transportRepository.findById(transportId);
        if (transport == null) {
            return notFound(transportId);
        }

        try {
            synchronized (transport) {
                transport.addCargo(units);

                eventLog.recordEvent("LOAD_CARGO", transport.getId(), bayIdOf(transport), "Loaded " + units + " units (cargo now " + transport.getCurrentCargo() + "/" + transport.getMaxCapacity() + ")");

                PersistenceManager.saveData(portController.getTransports(), portController.getDockingBays(), portController.getMaintenanceTeams());

                return new ServerResponse(true, "Loaded " + units + " units onto " + transport.getId()
                        + " (cargo now " + transport.getCurrentCargo() + "/" + transport.getMaxCapacity() + ")");
            }
        } catch (IllegalArgumentException e) {
            return new ServerResponse(false, e.getMessage());
        }
    }

    public ServerResponse unloadCargo(String transportId, int units) {
        Transport transport = transportRepository.findById(transportId);
        if (transport == null) {
            return notFound(transportId);
        }

        try {
            synchronized (transport) {
                transport.removeCargo(units);

                eventLog.recordEvent("UNLOAD_CARGO", transport.getId(), bayIdOf(transport),
                        "Unloaded " + units + " units (cargo now "
                                + transport.getCurrentCargo() + "/" + transport.getMaxCapacity() + ")");

                PersistenceManager.saveData(portController.getTransports(), portController.getDockingBays(), portController.getMaintenanceTeams());

                return new ServerResponse(true, "Unloaded " + units + " units from " + transport.getId()
                        + " (cargo now " + transport.getCurrentCargo() + "/" + transport.getMaxCapacity() + ")");
            }
        } catch (IllegalArgumentException e) {
            return new ServerResponse(false, e.getMessage());
        }


    }

    public ServerResponse transferCargo(String fromId, String toId, int units) {

            if (fromId != null && fromId.equals(toId)) {
            return new ServerResponse(false, "Cannot transfer cargo to the same transport");
        }

        Transport from = transportRepository.findById(fromId);
        if (from == null) {
            return notFound(fromId);
        }
        Transport to = transportRepository.findById(toId);
        if (to == null) {
            return notFound(toId);
        }

        Transport first = from.getId().compareTo(to.getId()) < 0 ? from : to;
        Transport second = (first == from) ? to : from;

        try {
            synchronized (first) {
                synchronized (second) {

                    from.removeCargo(units);

                    try {
                        to.addCargo(units);
                    } catch (IllegalArgumentException e) {
                        from.addCargo(units);
                        throw e;
                    }

                    eventLog.recordEvent("TRANSFER_CARGO", from.getId(), to.getId(),
                            "Transferred " + units + " units from " + from.getId() + " to " + to.getId());

                    PersistenceManager.saveData(portController.getTransports(), portController.getDockingBays(), portController.getMaintenanceTeams());

                    return new ServerResponse(true, "Transferred " + units + " units from "
                            + from.getId() + " to " + to.getId());
                }
            }
        } catch (IllegalArgumentException e) {
            return new ServerResponse(false, e.getMessage());
        }
    }

    private String bayIdOf(Transport transport) {
        DockingBay bay = transport.getDockingBay();
        return bay == null ? "none" : bay.getId();
    }

    private ServerResponse notFound(String transportId) {
        return new ServerResponse(false, "Transport " + transportId + " not found");
    }
}
