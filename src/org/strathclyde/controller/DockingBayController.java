package org.strathclyde.controller;

import org.strathclyde.model.DockingBay;
import org.strathclyde.model.Transport;
import org.strathclyde.service.EventLog;
import org.strathclyde.model.TransportState;
import org.strathclyde.common.ServerResponse;

public class DockingBayController {
    private final EventLog eventLog;
    private PortController portController;

    public DockingBayController(EventLog eventLog, PortController portController) {
        this.eventLog = eventLog;
        this.portController = portController;

    }

    // Simple method to allocate a bay
    public void allocateBay(DockingBay bay, Transport transport) {
        // The synchronized keyword in DockingBay.assignBay() handles the thread safety here.
        synchronized (transport) {
            bay.assignBay(transport); //already sets transport's bay and state to DOCKED
        }
    }

    /**
     * To prevent deadlock, we MUST lock the bays in a consistent order (alphabetical by ID).
     */

    public void exchangeBay(Transport t1, Transport t2) {

        Transport firstT = t1.getId().compareTo(t2.getId()) < 0 ? t1 : t2;
        Transport secondT = (firstT == t1) ? t2 : t1;

        synchronized(firstT){
            synchronized(secondT){
                DockingBay bay1 = t1.getDockingBay();
                DockingBay bay2 = t2.getDockingBay();

                if (bay1 == null || bay2 == null) {
                    throw new IllegalArgumentException(
                            "Both transports must be docked to exchange."
                    );
                }

                if (bay1 == bay2) {
                    throw new IllegalArgumentException(
                            "Both transports cannot occupy the same bay."
                    );
                }

                // Both transports must be DOCKED or READY
                if (!isExchangeAllowed(t1) || !isExchangeAllowed(t2)) {
                    throw new IllegalStateException(
                            "Both transports must be DOCKED or READY to exchange bays."
                    );
                }

                // Exchange the transports between the bays
                bay1.exchangeBay(bay2);

                // Record successful exchange
                eventLog.recordEvent(
                        "BAY_EXCHANGE",
                        t1.getId() + " and " + t2.getId(),
                        bay1.getId() + " and " + bay2.getId(),
                        "Successfully exchanged docking bays"
                );

            }
        }
    }

    // Check whether a transport is allowed to exchange bays
    private boolean isExchangeAllowed(Transport transport) {
        return transport.getState() == TransportState.DOCKED ||
                transport.getState() == TransportState.READY;
    }

    public void openOrCloseBay(DockingBay bay, boolean open) {
        if (open) {
            bay.openBay();
        } else {
            bay.closeBay(); // This will throw an error if the bay is OCCUPIED
        }
    }

    public ServerResponse allocateBay(String transportId, String bayId) {

        Transport transport = portController.findTransportById(transportId);
        DockingBay bay = portController.findDockingBayById(bayId);

        // Check if transport exists
        if (transport == null) {
            return new ServerResponse(false, "Transport not found.");
        }

        // Check if docking bay exists
        if (bay == null) {
            return new ServerResponse(false, "Docking bay not found.");
        }

        // Try to allocate the bay
        try {
            allocateBay(bay, transport);

            eventLog.recordEvent(
                    "BAY_ALLOCATION",
                    transportId,
                    bayId,
                    "Transport allocated to docking bay"
            );

            return new ServerResponse(true, "Bay allocated successfully.");

        } catch (IllegalStateException | IllegalArgumentException e) {
            return new ServerResponse(false, e.getMessage());
        }
    }

    public ServerResponse exchangeBays(String firstTransportId, String secondTransportId) {

        Transport firstTransport = portController.findTransportById(firstTransportId);
        Transport secondTransport = portController.findTransportById(secondTransportId);


        // Check whether both transports were found
        if (firstTransport == null) {
            return new ServerResponse(false, "First transport not found.");
        }

        if (secondTransport == null) {
            return new ServerResponse(false, "Second transport not found.");
        }

        try {
            // Call the existing method that performs the exchange
            exchangeBay(firstTransport, secondTransport);

            return new ServerResponse(true, "Docking bays exchanged successfully.");

        } catch (IllegalStateException | IllegalArgumentException e) {
            return new ServerResponse(false, e.getMessage());
        }
    }

    public ServerResponse openBay(String bayId) {

        DockingBay dockingBay = portController.findDockingBayById(bayId);

        // Check if the bay exists
        if (dockingBay == null) {
            return new ServerResponse(false, "Docking bay not found.");
        }

        try {
            // Open the bay
            openOrCloseBay(dockingBay, true);

            eventLog.recordEvent(
                    "BAY_OPENED",
                    "N/A",
                    bayId,
                    "Docking bay opened"
            );

            return new ServerResponse(true, "Docking bay opened successfully.");

        } catch (IllegalStateException | IllegalArgumentException e) {
            return new ServerResponse(false, e.getMessage());
        }
    }

    public ServerResponse closeBay(String bayId) {

        DockingBay dockingBay = portController.findDockingBayById(bayId);

        // Check if the bay exists
        if (dockingBay == null) {
            return new ServerResponse(false, "Docking bay not found.");
        }

        try {
            // Close the bay
            openOrCloseBay(dockingBay, false);

            eventLog.recordEvent(
                    "BAY_CLOSED",
                    "N/A",
                    bayId,
                    "Docking bay closed"
            );

            return new ServerResponse(true, "Docking bay closed successfully.");

        } catch (IllegalStateException | IllegalArgumentException e) {
            return new ServerResponse(false, e.getMessage());
        }
    }
}
