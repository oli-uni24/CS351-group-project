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

    public BayState getBayState() {
        return bayState;
    }

    public void setBayState(BayState bayState) {
        this.bayState = bayState;
    }

    public Transport getTransport() {
        return transport;
    }

    public void setTransport(Transport transport) {
        this.transport = transport;
    }

    // Assigns a transport to this docking bay.
    // The Synchronized method prevents multiple threads from assigning transports to this bay simultaneously.
    public synchronized void assignBay(Transport t) {


        if (t == null) {
            throw new IllegalArgumentException("Transport is required");
        }

        // Check that the docking bay isn't closed.
        // A closed bay cannot accept transport.
        if (bayState == BayState.CLOSED) {
            throw new IllegalArgumentException("Bay " + id + "is closed  please find another bay");
        }

        // Check that the docking bay isn't already occupied.
        if (bayState == BayState.OCCUPIED) {
            throw new IllegalArgumentException("Bay " + id + "is occupied");
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

    // Exchanges the transports between this docking bay and another docking bay.
    // Both bays must be occupied before an exchange can take place.
    public void exchangeBay(DockingBay other) {
        if (other == null || other == this) {
            throw new IllegalArgumentException("Need a different bay to exchange with");
        }

        // Create two references to the docking bays involved in the exchange.
        // 'this' is the bay executing the method and 'other' is the bay passed into it.
        DockingBay bayA = this;
        DockingBay bayB = other;

        // Compare the IDs to establish a consistent locking order.
        // If bayA has a larger ID, swap the references so the smaller ID comes first.
        // This helps prevent deadlocks when multiple threads try to exchange the same bays.
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
                if (bayA.bayState != BayState.OCCUPIED || bayB.bayState != BayState.OCCUPIED){
                    throw new IllegalArgumentException("Both bays must be occupied in order to exchange");
                }

                //Now we temporarily store both transports
                Transport transportA = bayA.transport;
                Transport transportB = bayB.transport;

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
