package org.strathclyde.model;

import java.time.Instant;
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
            throw new IllegalArgumentException("Transport id is requried");
        }
        // Check that the maximum capacity cannot be a negative number.
        if (maxCapacity < 0) {
            throw new IllegalArgumentException("Max capactiy cannot be negative");
        }
        // Check that the starting cargo amount cannot be negative.
        if (currentCargo < 0) {
            throw new IllegalArgumentException("Current cargo must be between 0 and max capacity");
        }
        if (currentCargo > maxCapacity) {
            throw new IllegalArgumentException(
                    "Current cargo cannot exceed maximum capacity"
            );
        }

        // If all validation checks pass, assign the supplied values to the attributes.
        this.id = id;
        this.maxCapacity = maxCapacity;
        this.currentCargo = currentCargo;
        this.scheduledDeparture = scheduledDeparture;
        this.state = TransportState.WAITING;
    }

    public String getId() {
        return id;
    }

    public TransportState getState() {
        return state;
    }

    public void setState(TransportState state) {
        this.state = state;
    }

    public DockingBay getDockingBay() {
        return dockingBay;
    }

    public void setDockingBay(DockingBay dockingBay) {
        this.dockingBay = dockingBay;
    }

    public MaintenanceTeam getMaintenanceTeam() {
        return maintenanceTeam;
    }

    public void setMaintenanceTeam(MaintenanceTeam maintenanceTeam) {
        this.maintenanceTeam = maintenanceTeam;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public int getCurrentCargo() {
        return currentCargo;
    }

    public void setCurrentCargo(int currentCargo) {
        this.currentCargo = currentCargo;
    }

    public LocalDateTime getScheduledDeparture() {
        return scheduledDeparture;
    }

    public void setScheduledDeparture(LocalDateTime scheduledDeparture) {
        this.scheduledDeparture = scheduledDeparture;
    }

    // Adds cargo to the transport.
    // Synchronized prevents multiple threads from changing the cargo simultaneously.
    public synchronized void addCargo(int units) {
        if (units <= 0) {
            throw new IllegalArgumentException("Units must be positive");
        }

        // Only allow cargo changes when the transport is docked, being serviced or ready.
        if (state != TransportState.DOCKED && state != TransportState.SERVICING && state != TransportState.READY) {
            throw new IllegalArgumentException("Cannot change cargo while " + state);
        }

        // Check that adding the new cargo won't exceed the transport's maximum capacity.
        if (currentCargo + units > maxCapacity) {
            throw new IllegalArgumentException("Not enough capacity on " + id);
        }
        currentCargo += units;
    }

    // Removes cargo from the transport.
    // Synchronized prevents multiple threads from removing or changing cargo simultaneously.
    public synchronized void removeCargo(int units) {


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
        currentCargo -= units;
    }
}