package org.strathclyde.service;

import org.strathclyde.model.DockingBay;
import org.strathclyde.model.MaintenanceTeam;
import org.strathclyde.model.Transport;
import org.strathclyde.model.TransportState;

import java.io.*;
import java.time.LocalDateTime;
import java.util.List;

public class PersistenceManager {

    private static final String FILE_NAME = "spaceport_data.txt";

    public void saveData(List<Transport> transports,
                         List<DockingBay> bays,
                         List<MaintenanceTeam> teams) {

        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {

            // Save transport information
            for (Transport t : transports) {

                String departure = t.getScheduledDeparture() == null
                        ? ""
                        : t.getScheduledDeparture().toString();

                writer.println("TRANSPORT,"
                        + t.getId() + ","
                        + t.getMaxCapacity() + ","
                        + t.getCurrentCargo() + ","
                        + departure + ","
                        + t.getState());
            }

            // Save docking bay information
            for (DockingBay b : bays) {
                writer.println("BAY," + b.getId() + "," + b.getState());
            }

            // Save maintenance team information
            for (MaintenanceTeam m : teams) {
                writer.println("TEAM," + m.getId() + "," + m.getState());
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

            while ((line = reader.readLine()) != null) {

                String[] parts = line.split(",", -1);

                String type = parts[0];

                if (type.equals("TRANSPORT")) {

                    // Transport records now contain six fields
                    if (parts.length != 6) {
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
                            departure
                    );

                    t.setState(TransportState.valueOf(parts[5]));

                    transports.add(t);

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

            System.out.println("Success: Data loaded from " + FILE_NAME);

        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Error loading data: " + e.getMessage());
        }
    }
}

