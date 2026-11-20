package org.strathclyde.controller;

import org.strathclyde.model.DockingBay;
import org.strathclyde.model.Transport;
import org.strathclyde.service.EventLog;
import org.strathclyde.model.TransportState;
import org.strathclyde.common.ServerResponse;
import java.util.List;

//TODO: remove not implemented method when implemented

public class DockingBayController {
    private final EventLog eventLog;
    private final List<DockingBay> dockingBays;
    private final List<Transport> transports;

    public DockingBayController(EventLog eventLog, List<DockingBay> dockingBays, List<Transport> transports) {
        this.eventLog = eventLog;
        this.dockingBays = dockingBays;
        this.transports = transports;
    }

    // Simple method to allocate a bay
    public void allocateBay(DockingBay bay, Transport transport) {
        // The synchronized keyword in DockingBay.assignBay() handles the thread safety here.
        bay.assignBay(transport);
        // Update the transport's docking bay reference
        transport.setDockingBay(bay);
    }

    /**
     * To prevent deadlock, we MUST lock the bays in a consistent order (alphabetical by ID).
     */

    public void exchangeBay(Transport t1, Transport t2) {

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

        // Lock bays in a consistent order to reduce deadlock risk
        DockingBay firstLock =
                bay1.getId().compareTo(bay2.getId()) < 0 ? bay1 : bay2;

        DockingBay secondLock = (firstLock == bay1) ? bay2 : bay1;

        synchronized (firstLock) {
            synchronized (secondLock) {

                // Check that the bays still belong to these transports
                if (bay1.getAllocatedTransport() != t1 ||
                        bay2.getAllocatedTransport() != t2) {
                    throw new IllegalStateException(
                            "Bay allocation has changed. Cannot exchange."
                    );
                }

                // Exchange the transports between the bays
                bay1.swapTransport(bay2);

                // Update each transport's bay reference
                t1.setDockingBay(bay2);
                t2.setDockingBay(bay1);

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

        Transport transport = null;
        DockingBay bay = null;

        // Find the transport using its ID
        for (Transport t : transports) {
            if (t.getId().equals(transportId)) {
                transport = t;
                break;
            }
        }

        // Find the docking bay using its ID
        for (DockingBay b : dockingBays) {
            if (b.getId().equals(bayId)) {
                bay = b;
                break;
            }
        }

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

        Transport firstTransport = null;
        Transport secondTransport = null;

        // Find the first transport
        for (Transport t : transports) {
            if (t.getId().equals(firstTransportId)) {
                firstTransport = t;
                break;
            }
        }

        // Find the second transport
        for (Transport t : transports) {
            if (t.getId().equals(secondTransportId)) {
                secondTransport = t;
                break;
            }
        }

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

        DockingBay bay = null;

        // Find the docking bay
        for (DockingBay b : dockingBays) {
            if (b.getId().equals(bayId)) {
                bay = b;
                break;
            }
        }

        // Check if the bay exists
        if (bay == null) {
            return new ServerResponse(false, "Docking bay not found.");
        }

        try {
            // Open the bay
            openOrCloseBay(bay, true);

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

        DockingBay bay = null;

        // Find the docking bay
        for (DockingBay b : dockingBays) {
            if (b.getId().equals(bayId)) {
                bay = b;
                break;
            }
        }

        // Check if the bay exists
        if (bay == null) {
            return new ServerResponse(false, "Docking bay not found.");
        }

        try {
            // Close the bay
            openOrCloseBay(bay, false);

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


    private ServerResponse notImplemented() {
        return new ServerResponse(false, "Nothing here yet");
    }
}
