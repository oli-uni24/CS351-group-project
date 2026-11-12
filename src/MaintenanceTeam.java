public class MaintenanceTeam {

    // The ID is final because it should not change once the team has been created.
    private final String id;
    private TeamState teamState;
    private Transport transport;

    public MaintenanceTeam(String id){

        // Check that an ID has been provided and that it isn't empty.
        if (id == null || id.isBlank()){
            throw new IllegalArgumentException("Maintenance team id is required");
        }

        this.id = id;
        this.teamState = TeamState.AVAILABLE;
    }

    public String getId() {
        return id;
    }

    public TeamState getTeamState() {
        return teamState;
    }

    public Transport getTransport() {
        return transport;
    }

    // Assigns an available maintenance team to a transport.
    // The Synchronized method prevents multiple threads from assigning this team simultaneously.
    public synchronized void assignTeam(Transport t){

        // Check that a valid transport has been provided.
        if (t == null){
            throw new IllegalArgumentException("Team " + id + " is not available");
        }

        // Check that the maintenance team is AVAILABLE before accepting an assignment.
        // A team that is already BUSY cannot be assigned to another transport.
        if (teamState != TeamState.AVAILABLE){
            throw new IllegalArgumentException("Team " + t.getId() + " is not docked");
        }

        // Check that the transport is currently DOCKED before maintenance can begin.
        // A transport must be docked before a maintenance team can work on it.
        if(t.getState() != TransportState.DOCKED){
            throw new IllegalArgumentException("Transport " + t.getId() + "already has a team");
        }

        // If all checks pass, assign the transport to this maintenance team.
        // Change the team's state from AVAILABLE to BUSY.
        // Update the transport so it knows which maintenance team has been assigned to it.
        // 'this' refers to the current MaintenanceTeam object.
        // Update the transport's state from DOCKED to SERVICING.
        this.transport = t;
        this.teamState = TeamState.BUSY;
        t.setMaintenanceTeam(this);
        t.setState(TransportState.SERVICING);
    }

    // Completes the maintenance work and releases the maintenance team.
    // The Synchronized method prevents multiple threads from completing the same maintenance task simultaneously.
    public synchronized void completeMaintenance() {

        // Check that the team is currently BUSY and has a transport assigned to it.
        // Maintenance cannot be completed if the team isn't working on a transport.
        if (teamState != TeamState.BUSY || transport == null){
            throw new IllegalArgumentException("Team " + id + " is not assigned to a transport");
        }

        // Check that the assigned transport is actually in the SERVICING state.
        // This ensures maintenance is only completed when the transport is being serviced.
        if (transport.getState() != TransportState.SERVICING) {
            throw new IllegalArgumentException("Transport " + transport.getId() + " is not being serviced");
        }

        //Temporarily store the transport before removing it from the team.
        Transport finished = transport;

        //Remove the transport from the team
        //Change the maintanence team's state from BUSY back to AVAILABLE
        //Remove the maintenance team reference from the transport
        //Update the transport's state from SERVICING to READY
        this.transport = null;
        this.teamState = TeamState.AVAILABLE;
        finished.setMaintenanceTeam(null);
        finished.setState(TransportState.READY);
    }

}
