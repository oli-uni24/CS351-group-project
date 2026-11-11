import java.time.Instant;

public class Transport {

    private final String id;
    private TransportState state;
    private DockingBay dockingBay;
    private MaintenanceTeam maintenanceTeam;
    private final int maxCapacity;
    private int currentCargo;
    private Instant scheduledDeparture;

    public Transport (String id, int maxCapacity, int currentCargo, Instant scheduledDeparture){
        if(id == null || id.isBlank()){
            throw new IllegalArgumentException("Transport id is requried");
        }
        if (maxCapacity < 0){
            throw new IllegalArgumentException("Max capactiy cannot be negative");
        }
        if(currentCargo < 0){
            throw new IllegalArgumentException("Current cargo must be between 0 and max capacity");
        }

        this.id = id;
        this.maxCapacity = maxCapacity;
        this.currentCargo = currentCargo;
        this.scheduledDeparture = scheduledDeparture;
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

    public Instant getScheduledDeparture() {
        return scheduledDeparture;
    }

    public void setScheduledDeparture(Instant scheduledDeparture) {
        this.scheduledDeparture = scheduledDeparture;
    }
}
