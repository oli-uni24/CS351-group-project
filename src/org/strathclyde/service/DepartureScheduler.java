package org.strathclyde.service;

import org.strathclyde.model.DockingBay;
import org.strathclyde.model.Transport;
import org.strathclyde.model.TransportState;
import org.strathclyde.service.EventLog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DepartureScheduler {
    private final ScheduledExecutorService scheduler;
    private final List<Transport> transports;
    private final EventLog eventLog;

    public DepartureScheduler(List<Transport> transports, EventLog eventLog) {
        //using single thread executor for this background task
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.transports = transports;
        this.eventLog = eventLog;
    }

    public void start() {
        // Runs the checkDepartures method every 1 second
        scheduler.scheduleAtFixedRate(this::checkDepartures, 0, 1, TimeUnit.SECONDS);
        System.out.println("Departure Scheduler started.");
    }

//    private void checkDepartures() {
//        LocalDateTime now = LocalDateTime.now();
//
//        for (Transport transport : transports) {
//            // lock the transport while checking it.
//            // This prevents a client from changing its state to SERVICING while we are trying to depart it.
//            synchronized (transport) {
//                if (transport.getState() == TransportState.READY &&
//                        (transport.getScheduledDeparture().isBefore(now) || transport.getScheduledDeparture().isEqual(now))) {
//
//                    // 1. Change state
//                    transport.setState(TransportState.DEPARTED);
//
//                    // 2. Release Bay
//                    DockingBay bay = transport.getDockingBay();
//                    if (bay != null) {
//                        bay.releaseBay();
//                        transport.setDockingBay(null);
//                    }
//
//                    // 3. Log Event (will add the real logging later)
//                    System.out.println("[BACKGROUND THREAD] Transport " + transport.getId() + " has DEPARTED. Bay released.");
//                }
//            }
//        }
//    }

    private void checkDepartures() {

        LocalDateTime now = LocalDateTime.now();

        for (Transport transport : transports) {

            synchronized (transport) {

                if (transport.getState() == TransportState.READY &&
                        transport.getScheduledDeparture() != null &&
                        !transport.getScheduledDeparture().isAfter(now)) {

                    // 1. Change transport state
                    transport.setState(TransportState.DEPARTED);

                    // 2. Get the allocated bay
                    DockingBay bay = transport.getDockingBay();

                    String bayId = "No bay allocated";

                    // 3. Release the bay
                    if (bay != null) {
                        bayId = bay.getId();

                        bay.releaseBay();
                        transport.setDockingBay(null);
                    }

                    // 4. Record the departure event
                    eventLog.recordEvent(
                            "TRANSPORT_DEPARTURE",
                            transport.getId(),
                            bayId,
                            "Transport departed successfully and docking bay released"
                    );

                    System.out.println(
                            "[BACKGROUND THREAD] Transport "
                                    + transport.getId()
                                    + " has DEPARTED. Bay released."
                    );
                }
            }
        }
    }


    public void stop() {
        scheduler.shutdown();
    }
}