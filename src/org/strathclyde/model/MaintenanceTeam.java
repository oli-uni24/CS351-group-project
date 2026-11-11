package org.strathclyde.model;

public class MaintenanceTeam {
    private final String id;
    private TeamState state;
    private SpaceTransport assignedTransport;

    public MaintenanceTeam(String id) {
        this.id = id;
        this.state = TeamState.AVAILABLE;
        this.assignedTransport = null;
    }

    public String getId() { return id; }
    public synchronized TeamState getState() { return state; }

    public synchronized void assignTeam(SpaceTransport transport) {
        if (this.state == TeamState.BUSY) {
            throw new IllegalStateException("Team is already busy.");
        }
        this.assignedTransport = transport;
        this.state = TeamState.BUSY;
    }

    public synchronized void completeMaintenance() {
        if (this.state == TeamState.BUSY) {
            this.assignedTransport = null;
            this.state = TeamState.AVAILABLE;
        }
    }
}
