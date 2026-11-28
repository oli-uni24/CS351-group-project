import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.strathclyde.controller.PortController;
import org.strathclyde.controller.TransportController;
import org.strathclyde.model.*;
import org.strathclyde.service.EventLog;
import org.strathclyde.common.ServerResponse;

import static org.junit.jupiter.api.Assertions.*;

public class TransportControllerTest {

    private EventLog eventLog;
    private TransportRepository transportRepository;
    private PortController portController;
    private TransportController transportController;

    @BeforeEach
    void setUp() {
        eventLog = new EventLog();
        transportRepository = new TransportRepository();
        portController = new PortController(eventLog, transportRepository);
        transportController = new TransportController(eventLog, transportRepository, portController);
    }

    //Adding tests

    @Test
    void addTransportSuccessfully() {
        ServerResponse response = transportController.addTransport(100, "2026-11-27T10:00");

        assertTrue(response.isSuccess());
        assertTrue(response.getMessage().contains("created successfully"));
    }

    @Test
    void addTransportWithMissingDepartureTime() {
        ServerResponse response = transportController.addTransport(100, "");

        assertFalse(response.isSuccess());
        assertEquals("Departure time is required", response.getMessage());
    }

    @Test
    void addTransportWithNullDepartureTime() {
        ServerResponse response = transportController.addTransport(100, null);

        assertFalse(response.isSuccess());
        assertEquals("Departure time is required", response.getMessage());
    }

    @Test
    void addTransportWithInvalidDepartureTime() {
        ServerResponse response = transportController.addTransport(100, "invalid-date");

        assertFalse(response.isSuccess());
        assertEquals("Invalid departure time. Use format YYYY-MM-DDTHH:MM e.g. 2026-11-27T10:00", response.getMessage());
    }

    @Test
    void addTransportWithInvalidCapacity() {
        ServerResponse response = transportController.addTransport(-100, "2026-11-27T10:00");

        assertFalse(response.isSuccess());
    }

    //View transport tests
    @Test
    void viewTransportNotFound() {
        ServerResponse response = transportController.viewTransport("T999");

        assertFalse(response.isSuccess());
        assertEquals("Transport T999 not found", response.getMessage());
    }

    @Test
    void viewTransportSuccessfully() {
        ServerResponse addResponse = transportController.addTransport(
                100, "2026-11-27T10:00"
        );

        String transportId = addResponse.getMessage()
                .replace("Transport ", "")
                .replace(" created successfully", "");

        ServerResponse response = transportController.viewTransport(transportId);

        assertTrue(response.isSuccess());
        assertTrue(response.getMessage().contains(transportId));
        assertTrue(response.getMessage().contains("Docking Bay: none"));
        assertTrue(response.getMessage().contains("Maintenance Team: none"));
        assertTrue(response.getMessage().contains("/100"));
    }

    //Assign team tests

    @Test
    void assignTeamTransportNotFound() {
        ServerResponse response = transportController.assignTeam(
                "T999", "MT001"
        );

        assertFalse(response.isSuccess());
        assertEquals("Transport T999 not found", response.getMessage());
    }

    @Test
    void assignTeamNotFound() {
        ServerResponse addResponse = transportController.addTransport(
                100, "2026-11-27T10:00"
        );

        String transportId = addResponse.getMessage()
                .replace("Transport ", "")
                .replace(" created successfully", "");

        ServerResponse response = transportController.assignTeam(
                transportId, "MT999"
        );

        assertFalse(response.isSuccess());
    }

    //Complete Maintenance Tests

    @Test
    void completeMaintenanceTransportNotFound() {
        ServerResponse response = transportController.completeMaintenance("T999");

        assertFalse(response.isSuccess());
        assertEquals("Transport T999 not found", response.getMessage());
    }

    @Test
    void completeMaintenanceWithoutAssignedTeam() {
        ServerResponse addResponse = transportController.addTransport(
                100, "2026-11-27T10:00"
        );

        String transportId = addResponse.getMessage()
                .replace("Transport ", "")
                .replace(" created successfully", "");

        ServerResponse response = transportController.completeMaintenance(
                transportId
        );

        assertFalse(response.isSuccess());
        assertEquals(
                "No Maintenace team assigned to this transport",
                response.getMessage()
        );
    }
}