import org.junit.jupiter.api.Test;
import org.strathclyde.service.EventLog;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class EventLogTest {

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

    @Test
    void testMultipleEventsAreRecorded() {
        EventLog eventLog = new EventLog();

        eventLog.recordEvent("ARRIVAL", "T1", "B1", "Arrived");
        eventLog.recordEvent("DEPARTURE", "T2", "B2", "Departed");

        assertEquals(2, eventLog.getEvents().size());
    }

    @Test
    void testListenerReceivesEvent() {
        EventLog eventLog = new EventLog();
        AtomicReference<String> receivedEvent = new AtomicReference<>();

        eventLog.addListener(receivedEvent::set);

        eventLog.recordEvent("ARRIVAL", "T1", "B1", "Arrived");

        assertNotNull(receivedEvent.get());
        assertTrue(receivedEvent.get().contains("ARRIVAL"));
    }

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

    @Test
    void testEmptyEventLog() {
        EventLog eventLog = new EventLog();

        assertTrue(eventLog.getEvents().isEmpty());
    }
}