package org.strathclyde.service;

import org.strathclyde.model.DockingBay;
import org.strathclyde.model.Transport;
import org.strathclyde.model.TransportRepository;
import org.strathclyde.model.TransportState;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DepartureScheduler {
    private final ScheduledExecutorService scheduler;
    private final TransportRepository transportRepository;
    private final EventLog eventLog;

    public DepartureScheduler(TransportRepository transportRepository, EventLog eventLog) {
        //using single thread executor for this background task
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.transportRepository = transportRepository;
        this.eventLog = eventLog;
    }

    public void start() {
        // Runs the checkDepartures method every 1 second
        scheduler.scheduleAtFixedRate(this::checkDepartures, 0, 1, TimeUnit.SECONDS);
        System.out.println("Departure Scheduler started.");
    }

    private void checkDepartures() {
        try {
            LocalDateTime now = LocalDateTime.now();

            for (Transport transport : transportRepository.getAllTransports()) {

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

                            bay.releaseBay(transport);
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
        } catch (RuntimeException e){
            //keep scheduler alive so later checks still run
            System.err.println("Departure check failed: " + e.getMessage());
        }
    }


    public void stop() {
        scheduler.shutdown();
    }
}