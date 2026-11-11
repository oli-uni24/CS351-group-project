package org.strathclyde.controller;

import org.strathclyde.model.DockingBay;
import org.strathclyde.model.Transport;

public class DockingBayController {

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
        DockingBay bay1 = t1.getDockingBay(); // Assuming someone adds this method??
        DockingBay bay2 = t2.getDockingBay();

        if (bay1 == null || bay2 == null) {
            throw new IllegalArgumentException("Both transports must be docked to exchange.");
        }

        // Lock Ordering to prevent Deadlock!
        // We always lock the bay with the alphabetically smaller ID first.
        DockingBay firstLock = bay1.getId().compareTo(bay2.getId()) < 0 ? bay1 : bay2;
        DockingBay secondLock = (firstLock == bay1) ? bay2 : bay1;

        synchronized (firstLock) {
            synchronized (secondLock) {
                // Both bays are now safely locked.
                bay1.swapTransport(bay2);

                // Update the transports to point to their new bays
                t1.setDockingBay(bay2);
                t2.setDockingBay(bay1);
            }
        }
    }

    public void openOrCloseBay(DockingBay bay, boolean open) {
        if (open) {
            bay.openBay();
        } else {
            bay.closeBay(); // This will throw an error if the bay is OCCUPIED
        }
    }
}
