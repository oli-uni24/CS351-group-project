
package org.strathclyde.model;

public class DockingBay {

    // The ID should not change once the bay has been created.
    private final String id;
    private BayState state;
    private Transport allocatedTransport;

    public DockingBay(String id) {

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Docking bay id is required");
        }

        this.id = id;
        this.state = BayState.AVAILABLE;
        this.allocatedTransport = null;
    }

    public String getId() {
        return id;
    }

    public synchronized BayState getState() {
        return state;
    }

    public synchronized BayState getBayState() {
        return state;
    }

    public synchronized Transport getAllocatedTransport() {
        return allocatedTransport;
    }

    public synchronized Transport getTransport() {
        return allocatedTransport;
    }

    // Assigns transport to this docking bay.
    public synchronized void assignBay(Transport t) {

        if (t == null) {
            throw new IllegalArgumentException("Transport is required");
        }

        synchronized (t) {

            if (state != BayState.AVAILABLE) {
                throw new IllegalArgumentException(
                        "Bay " + id + " is not available");
            }

            if (t.getState() != TransportState.WAITING) {
                throw new IllegalArgumentException(
                        "Transport " + t.getId() + " is not waiting");
            }

            if (t.getDockingBay() != null) {
                throw new IllegalArgumentException(
                        "Transport " + t.getId() + " already has a bay");
            }

            allocatedTransport = t;
            state = BayState.OCCUPIED;

            t.setDockingBay(this);
            t.setState(TransportState.DOCKED);
        }
    }

    // Releases the transport from this docking bay.
    public synchronized void releaseBay() {
        this.allocatedTransport = null;

        if (this.state != BayState.CLOSED) {
            this.state = BayState.AVAILABLE;
        }
    }

    // Exchanges transports between two docking bays.
    public void exchangeBay(DockingBay other) {

        if (other == null || other == this) {
            throw new IllegalArgumentException(
                    "Need a different bay to exchange with");
        }

        DockingBay bayA = this;
        DockingBay bayB = other;

        // Lock bays in ID order to prevent deadlocks.
        if (bayA.id.compareTo(bayB.id) > 0) {
            bayA = other;
            bayB = this;
        }

        synchronized (bayA) {
            synchronized (bayB) {

                if (bayA.state != BayState.OCCUPIED ||
                        bayB.state != BayState.OCCUPIED) {
                    throw new IllegalArgumentException(
                            "Both bays must be occupied in order to exchange");
                }

                Transport transportA = bayA.allocatedTransport;
                Transport transportB = bayB.allocatedTransport;

                if (transportA == null || transportB == null) {
                    throw new IllegalArgumentException(
                            "Both bays must have an assigned transport");
                }

                if (transportA == transportB) {
                    throw new IllegalArgumentException(
                            "Both bays cannot contain the same transport");
                }

                // Lock transports in ID order.
                Transport first = transportA;
                Transport second = transportB;

                if (first.getId().compareTo(second.getId()) > 0) {
                    first = transportB;
                    second = transportA;
                }

                synchronized (first) {
                    synchronized (second) {

                        if (!canExchange(transportA) ||
                                !canExchange(transportB)) {
                            throw new IllegalArgumentException(
                                    "Both transports must be DOCKED or READY");
                        }

                        // Check that transport references match the bays.
                        if (transportA.getDockingBay() != bayA ||
                                transportB.getDockingBay() != bayB) {
                            throw new IllegalArgumentException(
                                    "Transport and docking bay assignments are inconsistent");
                        }

                        // Exchange transports.
                        bayA.allocatedTransport = transportB;
                        bayB.allocatedTransport = transportA;

                        // Update transport references.
                        transportA.setDockingBay(bayB);
                        transportB.setDockingBay(bayA);
                    }
                }
            }
        }
    }

    // Checks whether a transport is in a valid state for an exchange.
    private boolean canExchange(Transport transport) {

        TransportState transportState = transport.getState();

        return transportState == TransportState.DOCKED ||
                transportState == TransportState.READY;
    }

    public synchronized void closeBay() {

        if (state != BayState.AVAILABLE || allocatedTransport != null) {
            throw new IllegalArgumentException(
                    "Only an unoccupied available bay can be closed");
        }

        state = BayState.CLOSED;
    }

    public synchronized void openBay() {

        if (state != BayState.CLOSED || allocatedTransport != null) {
            throw new IllegalArgumentException(
                    "Only an unoccupied closed bay can be reopened");
        }

        state = BayState.AVAILABLE;
    }
}
