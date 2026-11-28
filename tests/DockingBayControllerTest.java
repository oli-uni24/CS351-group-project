
import org.junit.jupiter.api.Test;
import org.strathclyde.controller.DockingBayController;
import org.strathclyde.controller.PortController;
import org.strathclyde.model.*;
import org.strathclyde.service.EventLog;
import org.strathclyde.common.ServerResponse;

import static org.junit.jupiter.api.Assertions.*;

class DockingBayControllerTest {

    private EventLog eventLog = new EventLog();
    private TransportRepository repository = new TransportRepository();
    private PortController portController =
            new PortController(eventLog, repository);
    private DockingBayController controller =
            new DockingBayController(eventLog, portController);

    @Test
    void allocateBaySuccessfully() {
        System.out.println("\n========== TEST: Allocate Bay Successfully ==========");

        Transport transport = new Transport("T1", 100, 0, null);
        DockingBay bay = new DockingBay("B1");

        portController.addTransport(transport);
        portController.addDockingBay(bay);

        System.out.println("ACTION: Allocating bay B1 to transport T1");
        ServerResponse response = controller.allocateBay("T1", "B1");

        System.out.println("EXPECTED: Allocation should succeed.");
        System.out.println("ACTUAL: " + response.isSuccess());
        System.out.println("Transport state: " + transport.getState());
        System.out.println("Transport docking bay: " + transport.getDockingBay().getId());
        System.out.println("Bay allocated transport: " + bay.getAllocatedTransport().getId());
        System.out.println("Bay state: " + bay.getBayState());

        assertTrue(response.isSuccess());
        assertEquals(TransportState.DOCKED, transport.getState());
        assertEquals(bay, transport.getDockingBay());
        assertEquals(transport, bay.getAllocatedTransport());
        assertEquals(BayState.OCCUPIED, bay.getBayState());

        System.out.println("RESULT: TEST PASSED");
    }

    @Test
    void allocationFailsWhenBayIsAlreadyOccupied() {
        System.out.println("\n========== TEST: Allocation Fails When Bay Is Occupied ==========");

        Transport first = new Transport("T1", 100, 0, null);
        Transport second = new Transport("T2", 100, 0, null);
        DockingBay bay = new DockingBay("B1");

        portController.addTransport(first);
        portController.addTransport(second);
        portController.addDockingBay(bay);

        System.out.println("ACTION: Allocating bay B1 to T1");
        ServerResponse firstResponse = controller.allocateBay("T1", "B1");

        System.out.println("First allocation success: " + firstResponse.isSuccess());
        assertTrue(firstResponse.isSuccess());

        System.out.println("ACTION: Trying to allocate the same bay B1 to T2");
        ServerResponse response = controller.allocateBay("T2", "B1");

        System.out.println("EXPECTED: Second allocation should fail.");
        System.out.println("ACTUAL: " + response.isSuccess());
        System.out.println("Expected bay occupant: T1");
        System.out.println("Actual bay occupant: " + bay.getAllocatedTransport().getId());
        System.out.println("T2 docking bay: " + second.getDockingBay());

        assertFalse(response.isSuccess());
        assertEquals(first, bay.getAllocatedTransport());
        assertNull(second.getDockingBay());

        System.out.println("RESULT: TEST PASSED");
    }

    @Test
    void openClosedBaySuccessfully() {
        System.out.println("\n========== TEST: Open a Closed Bay ==========");

        DockingBay bay = new DockingBay("B1");
        bay.closeBay();
        portController.addDockingBay(bay);

        System.out.println("Initial bay state: " + bay.getBayState());
        System.out.println("ACTION: Opening bay B1");

        ServerResponse response = controller.openBay("B1");

        System.out.println("EXPECTED: Bay state should be AVAILABLE.");
        System.out.println("ACTUAL: " + bay.getBayState());
        System.out.println("Response success: " + response.isSuccess());

        assertTrue(response.isSuccess());
        assertEquals(BayState.AVAILABLE, bay.getBayState());

        System.out.println("RESULT: TEST PASSED");
    }

    @Test
    void closeAvailableBaySuccessfully() {
        System.out.println("\n========== TEST: Close an Available Bay ==========");

        DockingBay bay = new DockingBay("B1");
        portController.addDockingBay(bay);

        System.out.println("Initial bay state: " + bay.getBayState());
        System.out.println("ACTION: Closing bay B1");

        ServerResponse response = controller.closeBay("B1");

        System.out.println("EXPECTED: Bay state should be CLOSED.");
        System.out.println("ACTUAL: " + bay.getBayState());
        System.out.println("Response success: " + response.isSuccess());

        assertTrue(response.isSuccess());
        assertEquals(BayState.CLOSED, bay.getBayState());

        System.out.println("RESULT: TEST PASSED");
    }

    @Test
    void exchangeBaysSuccessfully() {
        System.out.println("\n========== TEST: Exchange Bays Successfully ==========");

        Transport first = new Transport("T1", 100, 0, null);
        Transport second = new Transport("T2", 100, 0, null);
        DockingBay bay1 = new DockingBay("B1");
        DockingBay bay2 = new DockingBay("B2");

        portController.addTransport(first);
        portController.addTransport(second);
        portController.addDockingBay(bay1);
        portController.addDockingBay(bay2);

        System.out.println("ACTION: Allocating B1 to T1");
        ServerResponse allocation1 = controller.allocateBay("T1", "B1");

        System.out.println("ACTION: Allocating B2 to T2");
        ServerResponse allocation2 = controller.allocateBay("T2", "B2");

        assertTrue(allocation1.isSuccess());
        assertTrue(allocation2.isSuccess());

        System.out.println("Before exchange:");
        System.out.println("T1 bay: " + first.getDockingBay().getId());
        System.out.println("T2 bay: " + second.getDockingBay().getId());

        System.out.println("ACTION: Exchanging bays between T1 and T2");
        ServerResponse response = controller.exchangeBays("T1", "T2");

        System.out.println("EXPECTED: T1 should have B2 and T2 should have B1.");
        System.out.println("ACTUAL: T1 bay = " + first.getDockingBay().getId());
        System.out.println("ACTUAL: T2 bay = " + second.getDockingBay().getId());
        System.out.println("B1 occupant: " + bay1.getAllocatedTransport().getId());
        System.out.println("B2 occupant: " + bay2.getAllocatedTransport().getId());
        System.out.println("Response success: " + response.isSuccess());

        assertTrue(response.isSuccess());
        assertEquals(bay2, first.getDockingBay());
        assertEquals(bay1, second.getDockingBay());
        assertEquals(second, bay1.getAllocatedTransport());
        assertEquals(first, bay2.getAllocatedTransport());

        boolean eventRecorded = eventLog.getEvents().stream()
                .anyMatch(event -> event.contains("BAY_EXCHANGE"));

        System.out.println("EXPECTED: Event log should contain BAY_EXCHANGE.");
        System.out.println("ACTUAL: Event recorded = " + eventRecorded);

        assertTrue(eventRecorded);

        System.out.println("RESULT: TEST PASSED");
    }
}
