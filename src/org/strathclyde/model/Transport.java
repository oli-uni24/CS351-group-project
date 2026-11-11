package org.strathclyde.model;

import java.time.LocalDateTime;

public class Transport {
    private String id;
    private TransportState state;
    private DockingBay dockingBay;
    private MaintenanceTeam maintenanceTeam;
    private LocalDateTime scheduledDepartureTime;

    public Transport(String id) {
        this.id = id;
        this.state = TransportState.WAITING;
        // Default to 10 seconds from now for testing
        this.scheduledDepartureTime = LocalDateTime.now().plusSeconds(10);
    }

    public String getId() { return id; }

    // NOTE: We make getState and setState synchronized to protect against the background thread!
    public synchronized TransportState getState() { return state; }
    public synchronized void setState(TransportState state) { this.state = state; }

    public DockingBay getDockingBay() { return dockingBay; }
    public void setDockingBay(DockingBay bay) { this.dockingBay = bay; }

    public MaintenanceTeam getMaintenanceTeam() { return maintenanceTeam; }
    public void setMaintenanceTeam(MaintenanceTeam team) { this.maintenanceTeam = team; }

    public synchronized LocalDateTime getScheduledDepartureTime() { return scheduledDepartureTime; }
    public synchronized void setScheduledDepartureTime(LocalDateTime time) { this.scheduledDepartureTime = time; }
}