package org.strathclyde.service;

import org.strathclyde.model.DockingBay;
import org.strathclyde.model.MaintenanceTeam;
import org.strathclyde.model.Transport;
import org.strathclyde.model.TransportState;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersistenceManager {

    private static final String FILE_NAME = "spaceport_data.txt";
    private static final String EVENT_FILE_NAME= "spaceport_events.txt";

    public static void saveData(List<Transport> transports,
                         List<DockingBay> bays,
                         List<MaintenanceTeam> teams) {

        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {

            // Save transport information
            for (Transport t : transports) {

                String departure = t.getScheduledDeparture() == null
                        ? ""
                        : t.getScheduledDeparture().toString();

                DockingBay bay;
                MaintenanceTeam team;
                synchronized (t){
                    bay = t.getDockingBay();
                    team = t.getMaintenanceTeam();
                }

                writer.println("TRANSPORT,"
                        + t.getId() + ","
                        + t.getMaxCapacity() + ","
                        + t.getCurrentCargo() + ","
                        + departure + ","
                        + t.getState() + ","
                        + (bay==null ? "none" : bay.getId())+ ","
                        + (team ==null ? "none" : team.getId()));
            }

            // Save docking bay information
            for (DockingBay b : bays) {
                writer.println("BAY," + b.getId() + "," + b.getState());
            }

            // Save maintenance team information
            for (MaintenanceTeam m : teams) {
                writer.println("TEAM," + m.getId() + "," + m.getTeamState());
            }

            System.out.println("Success: Data saved to " + FILE_NAME);

        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }

    public void loadData(List<Transport> transports,
                         List<DockingBay> bays,
                         List<MaintenanceTeam> teams) {

        File file = new File(FILE_NAME);

        if (!file.exists()) {
            System.out.println("No save file found. Starting fresh.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

            String line;

            Map<String, String> bayLinks = new HashMap<>(); //transportId -> bayId
            Map<String, String> teamLinks = new HashMap<>(); //transportId -> teamId

            while ((line = reader.readLine()) != null) {

                String[] parts = line.split(",", -1);

                String type = parts[0];

                if (type.equals("TRANSPORT")) {

                    // Transport records now contain six fields
                    if (parts.length != 6 && parts.length !=8) {
                        continue;
                    }

                    String id = parts[1];
                    int maxCapacity = Integer.parseInt(parts[2]);
                    int currentCargo = Integer.parseInt(parts[3]);

                    LocalDateTime departure = parts[4].isEmpty()
                            ? null
                            : LocalDateTime.parse(parts[4]);

                    Transport t = new Transport(
                            id,
                            maxCapacity,
                            currentCargo,
                            departure,
                            TransportState.valueOf(parts[5])
                    );

                    transports.add(t);

                    if (parts.length ==8){
                        if(!parts[6].equals("none")) bayLinks.put(id, parts[6]);
                        if(!parts[7].equals("none")) teamLinks.put(id, parts[7]);
                    }

                } else if (type.equals("BAY")) {

                    if (parts.length != 3) {
                        continue;
                    }

                    String id = parts[1];
                    DockingBay b = new DockingBay(id);

                    // Restore CLOSED bays
                    if (parts[2].equals("CLOSED")) {
                        b.closeBay();
                    }

                    bays.add(b);

                } else if (type.equals("TEAM")) {

                    if (parts.length != 3) {
                        continue;
                    }

                    String id = parts[1];
                    MaintenanceTeam m = new MaintenanceTeam(id);

                    teams.add(m);
                }
            }

            for (Transport t : transports){
                String bayId = bayLinks.get(t.getId());
                if (bayId != null){
                    DockingBay found = null;
                    for (DockingBay b : bays) {
                        if (b.getId().equals(bayId)) found = b;
                    }
                    if(found != null){
                        found.restoreTransport(t);
                    } else {
                        System.err.println("Saved bay " + bayId + " for " + t.getId() + " not found");
                    }
                }

                String teamId = teamLinks.get(t.getId());
                if (teamId != null){
                    MaintenanceTeam found = null;
                    for(MaintenanceTeam m : teams){
                        if (m.getId().equals(teamId)) found = m;
                    }
                    if (found != null){
                        found.restoreTransport(t);
                    } else {
                        System.err.println("Saved team " + teamId + " for " + t.getId() + " not found");
                    }
                }
            }

            System.out.println("Success: Data loaded from " + FILE_NAME);

        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Error loading data: " + e.getMessage());
        }
    }

    public static void saveEvents(List<String> events) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(EVENT_FILE_NAME))){
            for (String event : events) {
                writer.println(event);
            }
            System.out.println("Success: Event log saved to " + EVENT_FILE_NAME);
        } catch (IOException e){
            System.err.println("Error saving event log: " + e.getMessage());
        }
    }

    public List<String> loadEvents(){
        List<String> events = new ArrayList<>();
        File file = new File(EVENT_FILE_NAME);

        if (!file.exists()){
            System.out.println("No event log file found. Starting with empty log.");
            return events;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))){
            String line;
            while ((line = reader.readLine()) != null){
                if(!line.isBlank()){
                    events.add(line);
                }
            }
            System.out.println("Success: Event log loaded from " + EVENT_FILE_NAME);
        } catch (IOException e){
            System.err.println("Error loading event log: " + e.getMessage());
        }
        return events;
    }
}

