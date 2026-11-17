package org.strathclyde.server;

import org.strathclyde.client.Client;
import org.strathclyde.common.Role;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientSessionHandler implements Runnable {
    private final Socket socket;
    private ClientRegistry registry;
    private PrintWriter out;
    private Role connectedUsersRole = null; // null until connection succeeds
    private Client client = null;

    public ClientSessionHandler(Socket socket, ClientRegistry registry) {
        this.socket = socket;
        this.registry = registry;
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
        switch (command) {
            case "CONNECT":
                handleConnect(parts);
                return true;
            case "QUIT":
                sendResponse("OK bye");
                return false;
            default:
                sendResponse("ERROR BAD_REQUEST unknown command");
                return true;
        }
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