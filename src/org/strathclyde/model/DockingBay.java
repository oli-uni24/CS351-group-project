
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
    public void assignBay(Transport t) {

        if (t == null) {
            throw new IllegalArgumentException("Transport is required");
        }
        // always lock the transport first
        synchronized (t) {

            // then lock the bay
            synchronized (this) {
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
    }

    public void releaseBay(Transport transport) {

        if (transport == null) {
            return;
        }

        synchronized (transport) {
            synchronized (this) {

                // Check that this bay is assigned to this transport
                if (allocatedTransport != transport) {
                    return;
                }

                allocatedTransport = null;

                if (state != BayState.CLOSED) {
                    state = BayState.AVAILABLE;
                }

                // Remove the bay reference from the transport
                transport.setDockingBay(null);
            }
        }
    }


    public void exchangeBay(DockingBay other) {

        if (other == null || other == this) {
            throw new IllegalArgumentException(
                    "Need a different bay to exchange with");
        }

        // Read the allocated transports.
        // Each getter locks its bay briefly and releases it.
        Transport transportA = this.getAllocatedTransport();
        Transport transportB = other.getAllocatedTransport();

        if (transportA == null || transportB == null) {
            throw new IllegalArgumentException(
                    "Both bays must have an assigned transport");
        }

        if (transportA == transportB) {
            throw new IllegalArgumentException(
                    "Both bays cannot contain the same transport");
        }

        // Sort transports by ID.
        Transport firstTransport = transportA;
        Transport secondTransport = transportB;

        if (firstTransport.getId().compareTo(secondTransport.getId()) > 0) {
            firstTransport = transportB;
            secondTransport = transportA;
        }

        // Sort bays by ID.
        DockingBay firstBay = this;
        DockingBay secondBay = other;

        if (firstBay.id.compareTo(secondBay.id) > 0) {
            firstBay = other;
            secondBay = this;
        }

        // Lock transports first, then docking bays.
        synchronized (firstTransport) {
            synchronized (secondTransport) {

                synchronized (firstBay) {
                    synchronized (secondBay) {

                        // Check assignments have not changed.
                        if (this.allocatedTransport != transportA ||
                                other.allocatedTransport != transportB) {
                            throw new IllegalArgumentException(
                                    "Bay assignments have changed. Try again.");
                        }

                        // Check both bays are occupied.
                        if (this.state != BayState.OCCUPIED ||
                                other.state != BayState.OCCUPIED) {
                            throw new IllegalArgumentException(
                                    "Both bays must be occupied in order to exchange");
                        }

                        // Check transport states.
                        if (!canExchange(transportA) ||
                                !canExchange(transportB)) {
                            throw new IllegalArgumentException(
                                    "Both transports must be DOCKED or READY");
                        }

                        // Check transport and bay references match.
                        if (transportA.getDockingBay() != this ||
                                transportB.getDockingBay() != other) {
                            throw new IllegalArgumentException(
                                    "Transport and docking bay assignments are inconsistent");
                        }

                        //  Exchange transports.
                        this.allocatedTransport = transportB;
                        other.allocatedTransport = transportA;

                        // Update transport references.
                        transportA.setDockingBay(other);
                        transportB.setDockingBay(this);
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
