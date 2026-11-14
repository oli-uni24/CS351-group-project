package org.strathclyde.controller;

import org.strathclyde.model.DockingBay;
import org.strathclyde.model.Transport;
import org.strathclyde.service.EventLog;
import org.strathclyde.model.TransportState;

public class DockingBayController {
    private final EventLog eventLog;

    public DockingBayController(EventLog eventLog) {
        this.eventLog = eventLog;
    }

    // Simple method to allocate a bay
    public void allocateBay(DockingBay bay, Transport transport) {
        // The synchronized keyword in DockingBay.assignBay() handles the thread safety here.
        bay.assignBay(transport);
        // (In the real app, my teams code will update the transport's state to DOCKED)
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
}
