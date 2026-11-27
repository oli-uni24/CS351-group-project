import org.junit.jupiter.api.Test;
import org.strathclyde.service.EventLog;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class EventLogTest {

    // Test ID 7
    @Test
    void testRecordEvent() {
        EventLog eventLog = new EventLog();

        eventLog.recordEvent(
                "TRANSPORT_DEPARTURE",
                "T1",
                "B1",
                "Transport departed successfully"
        );

        List<String> events = eventLog.getEvents();

        assertEquals(1, events.size());
        assertTrue(events.get(0).contains("TRANSPORT_DEPARTURE"));
        assertTrue(events.get(0).contains("T1"));
        assertTrue(events.get(0).contains("B1"));
        assertTrue(events.get(0).contains("Transport departed successfully"));
    }

    // Test ID 8
    @Test
    void testMultipleEventsAreRecorded() {
        EventLog eventLog = new EventLog();

        eventLog.recordEvent("ARRIVAL", "T1", "B1", "Arrived");
        eventLog.recordEvent("DEPARTURE", "T2", "B2", "Departed");

        assertEquals(2, eventLog.getEvents().size());
    }

    // Test ID 9
    @Test
    void testListenerReceivesEvent() {
        EventLog eventLog = new EventLog();
        AtomicReference<String> receivedEvent = new AtomicReference<>();

        eventLog.addListener(receivedEvent::set);

        eventLog.recordEvent("ARRIVAL", "T1", "B1", "Arrived");

        assertNotNull(receivedEvent.get());
        assertTrue(receivedEvent.get().contains("ARRIVAL"));
    }

    // Test ID 10
    @Test
    void testGetEventsReturnsUnmodifiableList() {
        EventLog eventLog = new EventLog();

        eventLog.recordEvent("ARRIVAL", "T1", "B1", "Arrived");

        List<String> events = eventLog.getEvents();

        assertThrows(
                UnsupportedOperationException.class,
                () -> events.add("Fake event")
        );
    }

    // Test ID 11
    @Test
    void testEmptyEventLog() {
        EventLog eventLog = new EventLog();

        assertTrue(eventLog.getEvents().isEmpty());
    }
}