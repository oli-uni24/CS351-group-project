package org.strathclyde.service;

import org.strathclyde.model.DockingBay;
import org.strathclyde.model.Transport;
import org.strathclyde.model.TransportState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DepartureScheduler {
    private final ScheduledExecutorService scheduler;
    private final List<Transport> transports;

    public DepartureScheduler(List<Transport> transports) {
        //using single thread executor for this background task
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.transports = transports;
    }

    public void start() {
        // Runs the checkDepartures method every 1 second
        scheduler.scheduleAtFixedRate(this::checkDepartures, 0, 1, TimeUnit.SECONDS);
        System.out.println("Departure Scheduler started.");
    }

    private void checkDepartures() {
        LocalDateTime now = LocalDateTime.now();

        for (Transport transport : transports) {
            // lock the transport while checking it.
            // This prevents a client from changing its state to SERVICING while we are trying to depart it.
            synchronized (transport) {
                if (transport.getState() == TransportState.READY &&
                        (transport.getScheduledDepartureTime().isBefore(now) || transport.getScheduledDepartureTime().isEqual(now))) {

                    // 1. Change state
                    transport.setState(TransportState.DEPARTED);

                    // 2. Release Bay
                    DockingBay bay = transport.getDockingBay();
                    if (bay != null) {
                        bay.releaseBay();
                        transport.setDockingBay(null);
                    }

                    // 3. Log Event (will add the real logging later)
                    System.out.println("[BACKGROUND THREAD] Transport " + transport.getId() + " has DEPARTED. Bay released.");
                }
            }
        }
    }

    public void stop() {
        scheduler.shutdown();
    }
}