import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.strathclyde.model.MaintenanceTeam;
import org.strathclyde.model.TeamState;
import org.strathclyde.model.Transport;
import org.strathclyde.model.TransportState;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MaintenanceTeamTest {

    private final LocalDateTime departure =
            LocalDateTime.parse("2026-11-27T10:00");

    //Helper method to create transport in DOCKED state
    private Transport createDockedTransport(String id) {
        Transport transport = new Transport(id, 100, 0, departure);
        transport.setState(TransportState.DOCKED);
        return transport;
    }

    //Constructor Tests
    @Test
    void createMaintenanceTeamSuccessfully() {
        MaintenanceTeam team = new MaintenanceTeam("MT001");

        assertEquals("MT001", team.getId());
        Assertions.assertEquals(TeamState.AVAILABLE, team.getTeamState());
        assertNull(team.getTransport());
    }


    @Test
    void createMaintenanceTeamWithNullId() {
        try {
            new MaintenanceTeam(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Maintenance team id is required", e.getMessage());
        }
    }

    @Test
    void createMaintenanceTeamWithEmptyId() {
        try {
            new MaintenanceTeam("");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Maintenance team id is required", e.getMessage());
        }
    }

    @Test
    void createMaintenanceTeamWithBlankId() {
        try {
            new MaintenanceTeam("   ");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Maintenance team id is required", e.getMessage());
        }
    }

    //Assign team tests
    @Test
    void assignTeamSuccessfully() {
        MaintenanceTeam team = new MaintenanceTeam("MT001");
        Transport transport = createDockedTransport("T001");

        team.assignTeam(transport);

        assertEquals(TeamState.BUSY, team.getTeamState());
        assertEquals(transport, team.getTransport());
        assertEquals(team, transport.getMaintenanceTeam());
        assertEquals(TransportState.SERVICING, transport.getState());
    }

    @Test
    void assignTeamWithNullTransport() {
        MaintenanceTeam team = new MaintenanceTeam("MT001");

        try {
            team.assignTeam(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Transport is required", e.getMessage());
        }

        assertEquals(TeamState.AVAILABLE, team.getTeamState());
    }

    @Test
    void assignTeamWhenTeamIsAlreadyBusy() {
        MaintenanceTeam team = new MaintenanceTeam("MT001");

        Transport firstTransport = createDockedTransport("T001");
        Transport secondTransport = createDockedTransport("T002");

        team.assignTeam(firstTransport);

        try {
            team.assignTeam(secondTransport);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Team MT001 is not available", e.getMessage());
        }

        assertEquals(firstTransport, team.getTransport());
    }

    @Test
    void assignTeamWhenTransportIsNotDocked() {
        MaintenanceTeam team = new MaintenanceTeam("MT001");

        Transport transport = new Transport(
                "T001", 100, 0, departure
        );

        try {
            team.assignTeam(transport);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Transport T001 is not docked", e.getMessage());
        }

        assertEquals(TeamState.AVAILABLE, team.getTeamState());
    }

    @Test
    void assignTeamWhenTransportAlreadyHasTeam() {
        MaintenanceTeam team = new MaintenanceTeam("MT001");
        Transport transport = createDockedTransport("T001");

        MaintenanceTeam existingTeam = new MaintenanceTeam("MT002");
        transport.setMaintenanceTeam(existingTeam);

        try {
            team.assignTeam(transport);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals(
                    "Transport T001 already has a maintenance team",
                    e.getMessage()
            );
        }

        assertEquals(TeamState.AVAILABLE, team.getTeamState());
    }

    //Complete maintenance tests

    @Test
    void completeMaintenanceSuccessfully() {
        MaintenanceTeam team = new MaintenanceTeam("MT001");
        Transport transport = createDockedTransport("T001");

        team.assignTeam(transport);
        team.completeMaintenance();

        assertEquals(TeamState.AVAILABLE, team.getTeamState());
        assertNull(team.getTransport());
        assertNull(transport.getMaintenanceTeam());
        assertEquals(TransportState.READY, transport.getState());
    }

    @Test
    void completeMaintenanceWhenTeamIsNotAssigned() {
        MaintenanceTeam team = new MaintenanceTeam("MT001");

        try {
            team.completeMaintenance();
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals(
                    "Team MT001 is not assigned to a transport",
                    e.getMessage()
            );
        }
    }

    @Test
    void completeMaintenanceWhenTransportIsNotBeingServiced() {
        MaintenanceTeam team = new MaintenanceTeam("MT001");
        Transport transport = createDockedTransport("T001");

        team.assignTeam(transport);


        transport.setState(TransportState.READY);

        try {
            team.completeMaintenance();
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals(
                    "Transport T001 is not being serviced",
                    e.getMessage()
            );
        }
    }

    @Test
    void completeMaintenanceWhenTeamAssignmentIsInconsistent() {
        MaintenanceTeam team = new MaintenanceTeam("MT001");
        Transport transport = createDockedTransport("T001");

        team.assignTeam(transport);


        transport.setMaintenanceTeam(null);

        try {
            team.completeMaintenance();
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals(
                    "Transport maintenance team assignment is inconsistent",
                    e.getMessage()
            );
        }
    }
}
