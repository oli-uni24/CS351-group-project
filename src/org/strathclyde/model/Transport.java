package org.strathclyde.model;

import java.time.LocalDateTime;

public class Transport {

    // The ID and max capacity are final because they should not change once created
    private final String id;
    private TransportState state;
    private DockingBay dockingBay;
    private MaintenanceTeam maintenanceTeam;
    private final int maxCapacity;
    private int currentCargo;
    private LocalDateTime scheduledDeparture;

    public Transport(String id, int maxCapacity, int currentCargo, LocalDateTime scheduledDeparture) {

        // Check that an ID has been provided and that it isn't empty.
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Transport id is required");
        }
        // Check that the maximum capacity cannot be a negative number.
        if (maxCapacity < 0) {
            throw new IllegalArgumentException("Max capacity cannot be negative");
        }
        //Check that the starting cargo is between 0 and the maximum capacity.
        if (currentCargo < 0 || currentCargo > maxCapacity) {
            throw new IllegalArgumentException("Current cargo must be between 0 and max capacity");
        }

        // If all validation checks pass, assign the supplied values to the attributes.
        this.id = id;
        this.maxCapacity = maxCapacity;
        this.currentCargo = currentCargo;
        this.scheduledDeparture = scheduledDeparture;
        this.state = TransportState.WAITING;
    }

    // used only by persistenceManager to restore a saved transport in its saved state
    // skips the normal transition check, because a loaded transport didnt just change state
    public Transport(String id, int maxCapacity, int currentCargo, LocalDateTime scheduledDeparture, TransportState savedState) {
        this(id, maxCapacity, currentCargo, scheduledDeparture); //runs existing validation
        this.state = savedState;
    }

    //All getters and setters are synchronised so that every thread
    //always sees the latest values, not an out-of-date copy
    public String getId() {
        return id;
    }

    public synchronized TransportState getState() {
        return state;
    }

    //only allow valid transport state transitions
    public synchronized void setState(TransportState newState) {
        if (newState == null) {
            throw new IllegalArgumentException("Transport state is required");
        }

        if (state == newState) {
            return;
        }

        boolean validTransition =
                (state == TransportState.WAITING && newState == TransportState.DOCKED)
                        || (state == TransportState.DOCKED && newState == TransportState.SERVICING)
                        || (state == TransportState.SERVICING && newState == TransportState.READY)
                        || (state == TransportState.READY && newState == TransportState.DEPARTED);

        if (!validTransition) {
            throw new IllegalArgumentException("Invalid transport state transition from " + state + " to " + newState);
        }

        this.state = newState;
    }

    public synchronized DockingBay getDockingBay() {
        return dockingBay;
    }

    public synchronized void setDockingBay(DockingBay dockingBay) {
        this.dockingBay = dockingBay;
    }

    public synchronized MaintenanceTeam getMaintenanceTeam() {
        return maintenanceTeam;
    }

    public synchronized void setMaintenanceTeam(MaintenanceTeam maintenanceTeam) {
        this.maintenanceTeam = maintenanceTeam;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public synchronized int getCurrentCargo() {
        return currentCargo;
    }

    //no need to set current cargo as addCargo() will set it once
    //cargo is added

    public synchronized LocalDateTime getScheduledDeparture() {
        return scheduledDeparture;
    }

    public synchronized void setScheduledDeparture(LocalDateTime scheduledDeparture) {
        this.scheduledDeparture = scheduledDeparture;
    }

    // Adds cargo to the transport.
    // Synchronized prevents multiple threads from changing the cargo simultaneously.
    public synchronized int addCargo(int units) {
        if (units <= 0) {
            throw new IllegalArgumentException("Units must be positive");
        }

        // Only allow cargo changes when the transport is docked, being serviced or ready.
        if (state != TransportState.DOCKED && state != TransportState.SERVICING && state != TransportState.READY) {
            throw new IllegalArgumentException("Cannot change cargo while " + state);
        }

        // Check that adding the new cargo won't exceed the transport's maximum capacity.
        if (units > maxCapacity - currentCargo) {
            throw new IllegalArgumentException("Not enough capacity on " + id);
        }
        return currentCargo += units;
    }

    // Removes cargo from the transport.
    // Synchronized prevents multiple threads from removing or changing cargo simultaneously.
    public synchronized int removeCargo(int units) {

        if (units <= 0) {
            throw new IllegalArgumentException("Units must be positive");
        }

        // Only allow cargo changes when the transport is docked, being serviced or ready.
        if (state != TransportState.DOCKED &&
                state != TransportState.SERVICING &&
                state != TransportState.READY) {
            throw new IllegalArgumentException("Cannot change cargo while " + state);
        }
        if (units > currentCargo) {
            throw new IllegalArgumentException("Not enough cargo on " + id);
        }
        return currentCargo -= units;
    }
}
