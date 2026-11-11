package org.strathclyde.service;

import org.strathclyde.model.BayState;
import org.strathclyde.model.DockingBay;
import org.strathclyde.model.MaintenanceTeam;
import org.strathclyde.model.TeamState;
import org.strathclyde.model.Transport;
import org.strathclyde.model.TransportState;

import java.io.*;
import java.util.List;

public class PersistenceManager {

    private static final String FILE_NAME = "spaceport_data.txt";

    public void saveData(List<Transport> transports, List<DockingBay> bays, List<MaintenanceTeam> teams) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Transport t : transports) {
                writer.println("TRANSPORT," + t.getId() + "," + t.getState());
            }
            for (DockingBay b : bays) {
                writer.println("BAY," + b.getId() + "," + b.getState());
            }
            for (MaintenanceTeam m : teams) {
                writer.println("TEAM," + m.getId() + "," + m.getState());
            }
            System.out.println("Success: Data saved to " + FILE_NAME);
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }

    public void loadData(List<Transport> transports, List<DockingBay> bays, List<MaintenanceTeam> teams) {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            System.out.println("No save file found. Starting fresh.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 3) continue;

                String type = parts[0];
                String id = parts[1];
                String state = parts[2];

                if (type.equals("TRANSPORT")) {
                    Transport t = new Transport(id);
                    t.setState(TransportState.valueOf(state));
                    transports.add(t);
                }
                else if (type.equals("BAY")) {
                    DockingBay b = new DockingBay(id);
                    // Note: To set a bay to CLOSED, DockingBay class needs a setState() method
                    // For now, just adding it as a new bay
                    bays.add(b);
                }
                else if (type.equals("TEAM")) {
                    MaintenanceTeam m = new MaintenanceTeam(id);
                    teams.add(m);
                }
            }
            System.out.println("Success: Data loaded from " + FILE_NAME);
        } catch (IOException e) {
            System.err.println("Error loading data: " + e.getMessage());
        }
    }
}
