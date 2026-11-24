package org.strathclyde.model;

public class DockingBay {

    // The ID is final because it should not change once the bay has been created.
    private final String id;
    private BayState bayState;
    private Transport transport;

    public DockingBay(String id) {

        // Check that an ID has been provided and that it isn't empty.
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Docking bay id is required");
        }

        this.id = id;
        this.bayState = BayState.AVAILABLE;
    }

    public String getId() {
        return id;
    }

    public synchronized BayState getBayState() {
        return bayState;
    }

    public synchronized Transport getTransport() {
        return transport;
    }

    // Assigns a transport to this docking bay.
    public synchronized void assignBay(Transport t) {

        if (t == null) {
            throw new IllegalArgumentException("Transport is required");
        }

        //stops another bay from assigning the same transport
        synchronized (t) {

            if (bayState != BayState.AVAILABLE) {
                throw new IllegalArgumentException("Bay " + id + "is not available");
            }

            // Check that the transport is currently WAITING before assigning it to a bay.
            if (t.getState() != TransportState.WAITING) {
                throw new IllegalArgumentException("Transport " + t.getId() + " is not waiting");
            }

            // Check that the transport hasn't already been assigned to another docking bay.
            if (t.getDockingBay() != null) {
                throw new IllegalArgumentException("Transport " + t.getId() + "already has a bay");
            }

            // If all checks pass, update both objects so they know about the assignment.
            // The docking bay stores the transport and changes its state to OCCUPIED
            // Update the transport so it knows which docking bay it has been assigned to.
            // 'this' refers to the current docking bay executing this method.
            // Update the transport's state from WAITING to DOCKED.
            transport = t;
            this.bayState = BayState.OCCUPIED;

            t.setDockingBay(this);
            t.setState(TransportState.DOCKED);
        }
    }

    // Exchanges the transports between this docking bay and another docking bay.
    // Both bays must be occupied before an exchange can take place.
    public void exchangeBay(DockingBay other) {

        if (other == null || other == this) {
            throw new IllegalArgumentException("Need a different bay to exchange with");
        }

        DockingBay bayA = this;
        DockingBay bayB = other;
        // Create two references to the docking bays involved in the exchange.
        // 'this' is the bay executing the method and 'other' is the bay passed into it.
        if (bayA.id.compareTo(bayB.id) > 0) {
            bayA = other;
            bayB = this;
        }

        // Lock the first docking bay before attempting to lock the second.
        // This prevents other threads from modifying the locked bay simultaneously.
        // Lock the second docking bay as well.
        // Both bays are now locked while the exchange takes place
        synchronized (bayA) {
            synchronized (bayB) {
                if (bayA.bayState != BayState.OCCUPIED || bayB.bayState != BayState.OCCUPIED) {
                    throw new IllegalArgumentException("Both bays must be occupied in order to exchange");
                }

                //Now we temporarily store both transports
                Transport transportA = bayA.transport;
                Transport transportB = bayB.transport;

                if (transportA == null || transportB == null) {
                    throw new IllegalArgumentException(
                            "Both bays must have an assigned transport");
                }

                if (transportA == transportB) {
                    throw new IllegalArgumentException(
                            "Both bays cannot contain the same transport");
                }

                //Lock both transports in ID order.
                Transport first = transportA;
                Transport second = transportB;

                if (first.getId().compareTo(second.getId()) > 0) {
                    first = transportB;
                    second = transportA;
                }

                synchronized (first) {
                    synchronized (second) {

                        // Both transports must be docked or ready.
                        if (!canExchange(transportA) ||
                                !canExchange(transportB)) {
                            throw new IllegalArgumentException(
                                    "Both transports must be DOCKED or READY");
                        }

                        // Check that the transport references match the bays.
                        if (transportA.getDockingBay() != bayA ||
                                transportB.getDockingBay() != bayB) {
                            throw new IllegalArgumentException(
                                    "Transport and docking bay assignments are inconsistent");
                        }

                        //Exchange transports
                        bayA.transport = transportB;
                        bayB.transport = transportA;

                        //Now we update transport references
                        transportA.setDockingBay(bayB);
                        transportB.setDockingBay(bayA);
                    }
                }
            }
        }
    }
    // Checks whether a transport is in a valid state for an exchange.
    private boolean canExchange(Transport transport) {

        TransportState state = transport.getState();

        return state == TransportState.DOCKED ||
                state == TransportState.READY;
    }

    public synchronized void closeBay(){

        if(bayState != BayState.AVAILABLE || transport != null){
            throw new IllegalArgumentException("Only an unoccupied available bay can be closed");
        }

        bayState = BayState.CLOSED;
    }

    public synchronized void openBay(){

        if (bayState != BayState.CLOSED || transport != null){
            throw new IllegalArgumentException("Only an unoccupied closed bay can be reopened");
        }

        bayState = BayState.AVAILABLE;
    }

}
