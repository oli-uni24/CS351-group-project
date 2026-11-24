package org.strathclyde.server;

import org.strathclyde.client.Client;
import org.strathclyde.common.Role;
import org.strathclyde.controller.CargoManagementController;
import org.strathclyde.controller.DockingBayController;
import org.strathclyde.controller.PortController;
import org.strathclyde.controller.TransportController;
import org.strathclyde.common.ServerResponse;
import org.strathclyde.common.CheckRole;
import org.strathclyde.common.Action;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientSessionHandler implements Runnable {
    private final Socket socket;
    private final ClientRegistry registry;
    private final PortController portController;
    private final DockingBayController bayController;
    private final TransportController transportController;
    private final CargoManagementController cargoController;

    private PrintWriter out;
    private Role connectedUsersRole = null; // null until connection succeeds
    private Client client = null;

    public ClientSessionHandler(Socket socket, ClientRegistry registry, PortController portController, DockingBayController bayController, TransportController transportController, CargoManagementController cargoController) {
        this.socket = socket;
        this.registry = registry;
        this.portController = portController;
        this.bayController = bayController;
        this.transportController = transportController;
        this.cargoController = cargoController;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            handleClient(in);
        } catch (IOException e) {
            System.out.println("Client connection lost: " + e.getMessage());
        } finally {
            if(client != null){
                registry.removeClient(client.getId());
            }
            try{ socket.close();} catch (IOException ignore) {}
        }
    }

    private void handleClient(BufferedReader in) throws IOException{
        String line;
        while ((line = in.readLine()) != null) {
            if (!processRequest(line.trim())) {
                break; // quit
            }
        }
    }

    // return false when client should disconnect
    private boolean processRequest(String line){
       System.out.println("RECIEVED " + line);
        if (line.isEmpty()){
           sendResponse("ERROR empty command");
           return true;
       }

        String[] parts = line.split("\\s+");
        String command = parts[0].toUpperCase();

        //only able to connect or quit before connecting
        if (connectedUsersRole == null && !command.equals("CONNECT") && !command.equals("QUIT")){
            sendResponse("ERROR not connected, send CONNECT client or admin first");
            return true;
        }
        if(command.equals("QUIT")){
            sendResponse("OK bye");
            return false;
        }
        if(command.equals("CONNECT")){
            handleConnect(parts);
            return true;
        }

        // get the action requested
        Action action = toAction(command);
        if(action==null){
            sendResponse("ERROR unknown command");
            return true;
        }
        // check if the connected role can do this action
        if(!CheckRole.checkActionAgainstRole(connectedUsersRole, action)){
            sendResponse("ERROR your role is not allowed to do " + command);
            return true;
        }

        try{
            handleCommand(command, parts);
        } catch (RuntimeException e) {
            //one bad request must not kill this thread
            sendResponse("ERROR request failed: " + e.getMessage());
        }
        return true;
    }

    // turns a command into the Action used for the role check
    private Action toAction(String command) {
        switch (command) {
            case "ADD_TRANSPORT":
                return Action.ADD_TRANSPORT;
            case "VIEW_TRANSPORT":
                return Action.VIEW_TRANSPORT;
            case "LOAD_CARGO":
                return Action.LOAD_CARGO;
            case "UNLOAD_CARGO":
                return Action.UNLOAD_CARGO;
            case "TRANSFER_CARGO":
                return Action.TRANSFER_CARGO;
            case "OPEN_BAY":
                return Action.OPEN_BAY;
            case "CLOSE_BAY":
                return Action.CLOSE_BAY;
            case "ALLOCATE_BAY":
                return Action.ALLOCATE_BAY;
            case "EXCHANGE_BAY":
                return Action.EXCHANGE_BAY;
            case "VIEW_EVENT_LOG":
                return Action.VIEW_EVENT_LOG;
            case "VIEW_RESOURCES":
                return Action.VIEW_RESOURCES;
            case "VIEW_CONNECTED_CLIENTS":
                return Action.VIEW_CONNECTED_CLIENTS;
            case "ASSIGN_MAINTENANCE_TEAM":
                return Action.ASSIGN_MAINTENANCE_TEAM;
            case "COMPLETE_MAINTENANCE":
                return Action.COMPLETE_MAINTENANCE;
            case "VIEW_BAY":
                return Action.VIEW_BAY;
            case "VIEW_MAINTENANCE_TEAMS":
                return Action.VIEW_MAINTENANCE_TEAMS;
            default:
                return null;
        }
    }

    private void handleCommand(String command, String[] parts) {
        switch (command) {
            case "VIEW_RESOURCES":
                if (needArgs(parts, 0, "VIEW_RESOURCES")) sendView(portController.viewResources());
                break;
            case "VIEW_EVENT_LOG":
                if (needArgs(parts, 0, "VIEW_EVENT_LOG")) sendView(portController.viewEventLog());
                break;
            case "VIEW_CONNECTED_CLIENTS":
                if (needArgs(parts, 0, "VIEW_CONNECTED_CLIENTS")) {
                    sendResponse("OK");
                    for (Client c : registry.listClients()) {
                        sendResponse(c.toString());
                    }
                    sendResponse("END");
                }
                break;
            case "ALLOCATE_BAY":
                if (needArgs(parts, 2, "ALLOCATE_BAY transportId bayId")) {
                    sendResult(bayController.allocateBay(parts[1], parts[2]));
                }
                break;
            case "EXCHANGE_BAY":
                if (needArgs(parts, 2, "EXCHANGE_BAY transportIdA transportIdB")) {
                    sendResult(bayController.exchangeBays(parts[1], parts[2]));
                }
                break;
            case "OPEN_BAY":
                if (needArgs(parts, 1, "OPEN_BAY bayId")) {
                    sendResult(bayController.openBay(parts[1]));
                }
                break;
            case "CLOSE_BAY":
                if (needArgs(parts, 1, "CLOSE_BAY bayId")) {
                    sendResult(bayController.closeBay(parts[1]));
                }
                break;
            case "ASSIGN_MAINTENANCE_TEAM":
                if(needArgs(parts, 2, "ASSIGN_MAINTENANCE_TEAM transportId teamId")) {
                    sendResult(transportController.assignTeam(parts[1], parts[2]));
                }
                break;
            case "COMPLETE_MAINTENANCE":
                if(needArgs(parts, 1, "COMPLETE_MAINTENANCE transportId")) {
                    sendResult(transportController.completeMaintenance(parts[1]));
                }
                break;
            case "ADD_TRANSPORT":
                if (needArgs(parts, 2, "ADD_TRANSPORT maxCapacity departureTime")) {
                    Integer capacity = parseWholeNumber(parts[1], "maxCapacity");
                    if (capacity != null) sendResult(transportController.addTransport(capacity, parts[2]));
                }
                break;
            case "LOAD_CARGO":
                if (needArgs(parts, 2, "LOAD_CARGO transportId units")){
                    Integer units = parseWholeNumber(parts[2], "units");
                    if (units != null) sendResult(cargoController.loadCargo(parts[1], units));
                }
                break;
            case "UNLOAD_CARGO":
                if(needArgs(parts, 2, "UNLOAD_CARGO transportId units")) {
                    Integer units = parseWholeNumber(parts[2], "units");
                    if (units != null) sendResult(cargoController.unloadCargo(parts[1], units));
                }
                break;
            case "TRANSFER_CARGO":
                if(needArgs(parts, 3, "TRANSFER_CARGO fromId toId units")){
                    Integer units = parseWholeNumber(parts[3], "units");
                    if (units != null) sendResult(cargoController.transferCargo(parts[1], parts[2], units));
                }
                break;
                //TODO: add VIEW_TRANSPORT once TransportController has viewTransport(transportId)
            default:
                sendResponse("ERROR " + command + " is not available yet");
        }
    }

    //true if the right number of arguements was given, otherwise returns error
    private boolean needArgs(String[] parts, int expected, String usage){
        if (parts.length - 1 != expected){
            sendResponse("ERROR usage: " + usage);
            return false;
        }
        return true;
    }

    private Integer parseWholeNumber(String text, String name){
        try{
            return Integer.parseInt(text);
        } catch(NumberFormatException e){
            sendResponse("ERROR " + name + " must be a whole number");
            return null;
        }
    }

    // single line result
    private void sendResult(ServerResponse response){
        sendResponse((response.isSuccess() ? "OK " : "ERROR ") + response.getMessage());
    }

    //multi-line result
    private void sendView(ServerResponse response){
        if (!response.isSuccess()){
            sendResponse("ERROR " + response.getMessage());
            return;
        }
        sendResponse("OK");
        for (String viewLine : response.getMessage().split("\n")) {
            sendResponse(viewLine);
        }
        sendResponse("END");
    }

    private void handleConnect(String[] parts){
        if (connectedUsersRole != null) {
            sendResponse("ERROR already connected");
            return;
        }
        if (parts.length !=2) {
            sendResponse("ERROR usage: CONNECT client or admin");
            return;
        }
        String requestedRole = parts[1].toUpperCase();
        if(requestedRole.equals("CLIENT")) {
            connectedUsersRole = Role.CLIENT;
        } else if (requestedRole.equals("ADMIN")) {
            connectedUsersRole = Role.ADMIN;
        } else {
            sendResponse("ERROR role must be client or admin");
            return;
        }
        client = registry.addClient(connectedUsersRole, this);
        sendResponse("OK connected");
    }

    private void sendResponse(String message){
        out.println(message);
    }
}