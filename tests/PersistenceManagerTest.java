
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;
import org.strathclyde.model.*;
import org.strathclyde.service.PersistenceManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PersistenceManagerTest {

    private static final Path DATA_FILE =
            Paths.get("spaceport_data.txt");

    private static final Path EVENT_FILE =
            Paths.get("spaceport_events.txt");

    @TempDir
    Path tempDir;

    private boolean dataFileExisted;
    private boolean eventFileExisted;

    @BeforeEach
    void backupExistingFiles() throws IOException {
        System.out.println("\nPreparing PersistenceManager tests");

        dataFileExisted = Files.exists(DATA_FILE);
        eventFileExisted = Files.exists(EVENT_FILE);

        if (dataFileExisted) {
            Files.copy(DATA_FILE,
                    tempDir.resolve("spaceport_data_backup.txt"));
            System.out.println("Existing data file backed up.");
        }

        if (eventFileExisted) {
            Files.copy(EVENT_FILE,
                    tempDir.resolve("spaceport_events_backup.txt"));
            System.out.println("Existing event file backed up.");
        }

        Files.deleteIfExists(DATA_FILE);
        Files.deleteIfExists(EVENT_FILE);

        System.out.println("Test files prepared.");
    }

    @AfterEach
    void restoreExistingFiles() throws IOException {
        System.out.println("Restoring original files...");

        Files.deleteIfExists(DATA_FILE);
        Files.deleteIfExists(EVENT_FILE);

        if (dataFileExisted) {
            Files.copy(
                    tempDir.resolve("spaceport_data_backup.txt"),
                    DATA_FILE
            );
            System.out.println("Original data file restored.");
        }

        if (eventFileExisted) {
            Files.copy(
                    tempDir.resolve("spaceport_events_backup.txt"),
                    EVENT_FILE
            );
            System.out.println("Original event file restored.");
        }

        System.out.println("Cleanup completed.");
    }

    @Test
    void saveAndLoadTransportSuccessfully() {
        System.out.println("\n========== TEST: Save and Load Transport ==========");

        PersistenceManager manager = new PersistenceManager();

        Transport transport =
                new Transport("T1", 100, 25, null);

        List<Transport> transports = new ArrayList<>();
        transports.add(transport);

        System.out.println("ACTION: Saving transport T1...");
        PersistenceManager.saveData(
                transports,
                new ArrayList<>(),
                new ArrayList<>()
        );

        List<Transport> loadedTransports = new ArrayList<>();

        System.out.println("ACTION: Loading saved transport...");
        manager.loadData(
                loadedTransports,
                new ArrayList<>(),
                new ArrayList<>()
        );

        System.out.println("EXPECTED: One transport should be loaded.");
        System.out.println("ACTUAL: Number of transports = "
                + loadedTransports.size());

        if (!loadedTransports.isEmpty()) {
            System.out.println("Loaded transport ID: "
                    + loadedTransports.get(0).getId());
            System.out.println("Loaded capacity: "
                    + loadedTransports.get(0).getMaxCapacity());
            System.out.println("Loaded cargo: "
                    + loadedTransports.get(0).getCurrentCargo());
            System.out.println("Loaded state: "
                    + loadedTransports.get(0).getState());
        }

        assertEquals(1, loadedTransports.size());
        assertEquals("T1", loadedTransports.get(0).getId());
        assertEquals(100, loadedTransports.get(0).getMaxCapacity());
        assertEquals(25, loadedTransports.get(0).getCurrentCargo());
        assertEquals(TransportState.WAITING,
                loadedTransports.get(0).getState());

        System.out.println("RESULT: TEST PASSED");
    }

    @Test
    void saveAndLoadDockingBayAndMaintenanceTeam() {
        System.out.println("\n========== TEST: Save & Load - Bay & Team ==========");

        PersistenceManager manager = new PersistenceManager();

        DockingBay bay = new DockingBay("B1");
        bay.closeBay();

        MaintenanceTeam team = new MaintenanceTeam("M1");

        List<DockingBay> bays = new ArrayList<>();
        bays.add(bay);

        List<MaintenanceTeam> teams = new ArrayList<>();
        teams.add(team);

        System.out.println("ACTION: Saving docking bay B1 and maintenance team M1");
        PersistenceManager.saveData(
                new ArrayList<>(),
                bays,
                teams
        );

        List<DockingBay> loadedBays = new ArrayList<>();
        List<MaintenanceTeam> loadedTeams = new ArrayList<>();

        System.out.println("ACTION: Loading saved bay and team");
        manager.loadData(
                new ArrayList<>(),
                loadedBays,
                loadedTeams
        );

        System.out.println("EXPECTED: One bay and one maintenance team should be loaded.");
        System.out.println("ACTUAL: Number of bays = " + loadedBays.size());
        System.out.println("ACTUAL: Number of teams = " + loadedTeams.size());

        if (!loadedBays.isEmpty()) {
            System.out.println("Loaded bay ID: " + loadedBays.get(0).getId());
            System.out.println("Loaded bay state: " + loadedBays.get(0).getBayState());
        }

        if (!loadedTeams.isEmpty()) {
            System.out.println("Loaded team ID: " + loadedTeams.get(0).getId());
            System.out.println("Loaded team state: " + loadedTeams.get(0).getTeamState());
        }

        assertEquals(1, loadedBays.size());
        assertEquals("B1", loadedBays.get(0).getId());
        assertEquals(BayState.CLOSED, loadedBays.get(0).getBayState());

        assertEquals(1, loadedTeams.size());
        assertEquals("M1", loadedTeams.get(0).getId());
        assertEquals(TeamState.AVAILABLE,
                loadedTeams.get(0).getTeamState());

        System.out.println("RESULT: TEST PASSED");
    }

    @Test
    void loadDataWhenSaveFileDoesNotExist() {
        System.out.println("\n========== TEST: Load When Data File Does Not Exist ==========");

        PersistenceManager manager = new PersistenceManager();

        List<Transport> transports = new ArrayList<>();
        List<DockingBay> bays = new ArrayList<>();
        List<MaintenanceTeam> teams = new ArrayList<>();

        System.out.println("ACTION: Trying to load data when the save file does not exist");

        assertDoesNotThrow(() ->
                manager.loadData(transports, bays, teams)
        );

        System.out.println("EXPECTED: All resource lists should be empty.");
        System.out.println("ACTUAL: Transports = " + transports.size());
        System.out.println("ACTUAL: Bays = " + bays.size());
        System.out.println("ACTUAL: Teams = " + teams.size());

        assertTrue(transports.isEmpty());
        assertTrue(bays.isEmpty());
        assertTrue(teams.isEmpty());

        System.out.println("RESULT: TEST PASSED");
    }

    @Test
    void saveAndLoadEventsSuccessfully() {
        System.out.println("\n========== TEST: Save and Load Events ==========");

        PersistenceManager manager = new PersistenceManager();

        List<String> events = List.of(
                "EVENT 1 | TRANSPORT_CREATED | T1",
                "EVENT 2 | BAY_ALLOCATION | T1"
        );

        System.out.println("ACTION: Saving two events");
        PersistenceManager.saveEvents(events);

        System.out.println("ACTION: Loading saved events");
        List<String> loadedEvents = manager.loadEvents();

        System.out.println("EXPECTED: Two events should be loaded.");
        System.out.println("ACTUAL: Number of events = " + loadedEvents.size());

        for (String event : loadedEvents) {
            System.out.println("Loaded event: " + event);
        }

        assertEquals(2, loadedEvents.size());
        assertEquals(events.get(0), loadedEvents.get(0));
        assertEquals(events.get(1), loadedEvents.get(1));

        System.out.println("RESULT: TEST PASSED");
    }

    @Test
    void loadEventsWhenEventFileDoesNotExist() {
        System.out.println("\n========== TEST: Load When Event File Does Not Exist ==========");

        PersistenceManager manager = new PersistenceManager();

        System.out.println("ACTION: Trying to load events when the event file does not exist");

        List<String> loadedEvents = manager.loadEvents();

        System.out.println("EXPECTED: An empty event list.");
        System.out.println("ACTUAL: Number of events = " + loadedEvents.size());

        assertNotNull(loadedEvents);
        assertTrue(loadedEvents.isEmpty());

        System.out.println("RESULT: TEST PASSED");
    }
}
