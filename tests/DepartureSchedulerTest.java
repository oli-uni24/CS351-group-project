import org.junit.jupiter.api.Test;
import org.strathclyde.model.*;
import org.strathclyde.service.DepartureScheduler;
import org.strathclyde.service.EventLog;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class DepartureSchedulerTest {

    // Test ID 4
    @Test
    void dueReadyTransportDepartsAndItsBayIsReleased() throws InterruptedException {
        TransportRepository repository = new TransportRepository();
        EventLog eventLog = new EventLog();
        CountDownLatch departureRecorded = new CountDownLatch(1);
        eventLog.addListener(event -> {
            if (event.contains("TRANSPORT_DEPARTURE")) {
                departureRecorded.countDown();
            }
        });

        Transport transport = new Transport("T1", 100, 0, LocalDateTime.now().minusMinutes(1));
        DockingBay bay = new DockingBay("B1");
        bay.assignBay(transport); // WAITING -> DOCKED
        transport.setState(TransportState.SERVICING);
        transport.setState(TransportState.READY);
        repository.addTransport(transport);

        DepartureScheduler scheduler = new DepartureScheduler(repository, eventLog);
        try {
            scheduler.start();

            assertTrue(departureRecorded.await(3, TimeUnit.SECONDS),
                    "Expected scheduler to record a departure");
            assertEquals(TransportState.DEPARTED, transport.getState());
            assertNull(transport.getDockingBay());
            assertNull(bay.getAllocatedTransport());
            assertEquals(BayState.AVAILABLE, bay.getBayState());
            assertTrue(eventLog.getEvents().stream().anyMatch(e ->
                    e.contains("TRANSPORT_DEPARTURE") && e.contains("T1") && e.contains("B1")));
        } finally {
            scheduler.stop();
        }
    }

    // Test ID 5
    @Test
    void dueReadyTransportWithoutBayStillDeparts() throws InterruptedException {
        TransportRepository repository = new TransportRepository();
        EventLog eventLog = new EventLog();
        CountDownLatch departureRecorded = new CountDownLatch(1);
        eventLog.addListener(event -> {
            if (event.contains("TRANSPORT_DEPARTURE")) {
                departureRecorded.countDown();
            }
        });

        Transport transport = new Transport("T2", 100, 0, LocalDateTime.now().minusMinutes(1));
        transport.setState(TransportState.DOCKED);
        transport.setState(TransportState.SERVICING);
        transport.setState(TransportState.READY);
        repository.addTransport(transport);

        DepartureScheduler scheduler = new DepartureScheduler(repository, eventLog);
        try {
            scheduler.start();

            assertTrue(departureRecorded.await(3, TimeUnit.SECONDS));
            assertEquals(TransportState.DEPARTED, transport.getState());
            assertNull(transport.getDockingBay());
            assertTrue(eventLog.getEvents().get(0).contains("No bay allocated"));
        } finally {
            scheduler.stop();
        }
    }

    // Test ID 6
    @Test
    void futureDepartureIsNotProcessedImmediately() throws InterruptedException {
        TransportRepository repository = new TransportRepository();
        EventLog eventLog = new EventLog();
        Transport transport = new Transport("T3", 100, 0, LocalDateTime.now().plusHours(1));
        transport.setState(TransportState.DOCKED);
        transport.setState(TransportState.SERVICING);
        transport.setState(TransportState.READY);
        repository.addTransport(transport);

        DepartureScheduler scheduler = new DepartureScheduler(repository, eventLog);
        try {
            scheduler.start();
            Thread.sleep(250);

            assertEquals(TransportState.READY, transport.getState());
            assertTrue(eventLog.getEvents().isEmpty());
        } finally {
            scheduler.stop();
        }
    }
}

