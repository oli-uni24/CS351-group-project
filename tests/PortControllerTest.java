
import org.junit.jupiter.api.Test;
import org.strathclyde.controller.PortController;
import org.strathclyde.model.*;
import org.strathclyde.service.EventLog;
import org.strathclyde.common.ServerResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PortControllerTest {

    private EventLog eventLog = new EventLog();
    private TransportRepository repository = new TransportRepository();
    private PortController controller =
            new PortController(eventLog, repository);

    @Test
    void addTransportSuccessfully() {
        System.out.println("\n========== TEST: Add Transport ==========");

        Transport transport = new Transport("T1", 100, 0, null);

        System.out.println("Adding transport T1...");
        controller.addTransport(transport);

        Transport result = controller.findTransportById("T1");

        System.out.println("Searching for transport T1...");
        System.out.println("Expected: T1");
        System.out.println("Actual: " +
                (result == null ? "null" : result.getId()));

        assertEquals(transport, result);

        System.out.println("RESULT: Transport added and found successfully.");
    }

    @Test
    void addAndFindMaintenanceTeam() {
        System.out.println("\n========== TEST: Add Maintenance Team ==========");

        MaintenanceTeam team = new MaintenanceTeam("M1");

        System.out.println("Adding maintenance team M1");
        controller.addMaintenanceTeam(team);

        MaintenanceTeam result =
                controller.findMaintenanceTeamById("M1");

        System.out.println("Searching for maintenance team M1");
        System.out.println("Expected: M1");
        System.out.println("Actual: " +
                (result == null ? "null" : result.getId()));

        assertEquals(team, result);

        System.out.println("RESULT: Maintenance team added successfully.");
    }

    @Test
    void getResourceListsReturnsAddedResources() {
        System.out.println("\n========== TEST: Get Resource Lists ==========");

        Transport transport = new Transport("T1", 100, 0, null);
        DockingBay bay = new DockingBay("B1");
        MaintenanceTeam team = new MaintenanceTeam("M1");

        System.out.println("Adding transport T1");
        controller.addTransport(transport);

        System.out.println("Adding docking bay B1");
        controller.addDockingBay(bay);

        System.out.println("Adding maintenance team M1");
        controller.addMaintenanceTeam(team);

        List<Transport> transports = controller.getTransports();
        List<DockingBay> bays = controller.getDockingBays();
        List<MaintenanceTeam> teams = controller.getMaintenanceTeams();

        System.out.println("\nChecking resource counts:");
        System.out.println("Transports: " + transports.size());
        System.out.println("Docking bays: " + bays.size());
        System.out.println("Maintenance teams: " + teams.size());

        assertEquals(1, transports.size());
        assertEquals(1, bays.size());
        assertEquals(1, teams.size());

        System.out.println("\nChecking resource IDs:");

        System.out.println("Transport ID: " + transports.get(0).getId());
        System.out.println("Bay ID: " + bays.get(0).getId());
        System.out.println("Team ID: " + teams.get(0).getId());

        assertEquals("T1", transports.get(0).getId());
        assertEquals("B1", bays.get(0).getId());
        assertEquals("M1", teams.get(0).getId());

        System.out.println("RESULT: All resources were returned correctly.");
    }

    @Test
    void viewResourcesShowsAddedResources() {
        System.out.println("\n========== TEST: View Resources ==========");

        System.out.println("Adding test resources");

        controller.addTransport(new Transport("T1", 100, 0, null));
        controller.addDockingBay(new DockingBay("B1"));
        controller.addMaintenanceTeam(new MaintenanceTeam("M1"));
        ServerResponse response = controller.viewResources();

        System.out.println(response.getMessage());

        assertTrue(response.isSuccess());
        assertTrue(response.getMessage().contains("T1"));
        assertTrue(response.getMessage().contains("B1"));
        assertTrue(response.getMessage().contains("M1"));
        assertTrue(response.getMessage().contains("WAITING"));
        assertTrue(response.getMessage().contains("AVAILABLE"));

        System.out.println("RESULT: Resource view contains all expected resources.");
    }

    @Test
    void viewEventLogShowsRecordedEvents() {
        System.out.println("\n========== TEST: View Event Log ==========");

        eventLog.recordEvent(
                "TRANSPORT_CREATED",
                "T1",
                "PORT",
                "Transport created successfully"
        );

        ServerResponse response = controller.viewEventLog();

        System.out.println("\nEvent log returned:");
        System.out.println(response.getMessage());

        assertTrue(response.isSuccess());
        assertTrue(response.getMessage().contains("EVENT LOG"));
        assertTrue(response.getMessage().contains("TRANSPORT_CREATED"));
        assertTrue(response.getMessage().contains("T1"));

        System.out.println("RESULT: Event log displays the recorded event successfully.");
    }
}
