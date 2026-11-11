package org.strathclyde.model;

public class DockingBay {
    private final String id;
    private BayState state;
    private SpaceTransport allocatedTransport;

    public DockingBay(String id) {
        this.id = id;
        this.state = BayState.AVAILABLE;
        this.allocatedTransport = null;
    }

    // Getters
    public String getId() { return id; }
    public synchronized BayState getState() { return state; }
    public synchronized SpaceTransport getAllocatedTransport() { return allocatedTransport; }

    // Synchronized method to prevent race conditions
    public synchronized void assignBay(SpaceTransport transport) {
        if (this.state != BayState.AVAILABLE) {
            throw new IllegalStateException("Cannot assign transport. Bay is " + this.state);
        }
        this.allocatedTransport = transport;
        this.state = BayState.OCCUPIED;
    }

    public synchronized void releaseBay() {
        this.allocatedTransport = null;
        this.state = BayState.AVAILABLE;
    }

    public synchronized void closeBay() {
        if (this.state == BayState.OCCUPIED) {
            throw new IllegalStateException("Cannot close an occupied bay.");
        }
        this.state = BayState.CLOSED;
    }

    public synchronized void openBay() {
        if (this.state == BayState.CLOSED) {
            this.state = BayState.AVAILABLE;
        }
    }

    // Used during an exchange
    public synchronized void swapTransport(DockingBay otherBay) {
        SpaceTransport temp = this.allocatedTransport;
        this.allocatedTransport = otherBay.allocatedTransport;
        otherBay.allocatedTransport = temp;
    }
}
