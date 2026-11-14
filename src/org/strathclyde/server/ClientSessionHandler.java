package org.strathclyde.server;

import org.strathclyde.common.Role;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientSessionHandler implements Runnable {
    private final Socket socket;
    private PrintWriter out;
    private Role connectedUsersRole;

    public ClientSessionHandler(Socket socket) {
        this.socket = socket;
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
            //TODO: remove client from client registry here
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
    //TODO: update with real values
    private boolean processRequest(String line){
        String[] parts = line.split("\\s+");
        String command = parts[0].toUpperCase();

        switch (command) {
            //assign user role
            case "CLIENT":
                connectedUsersRole = Role.CLIENT;           //confirm with Olivia if this fits wit her implementation else move it
                System.out.println("DEBUG: The terminal is a client");
                sendResponse("You have successfully connected to the server as a client");
                return true;
            case "ADMIN":
                connectedUsersRole = Role.ADMIN;
                System.out.println("DEBUG: The terminal is an admin");
                sendResponse("You have successfully connected to the server as an admin");
                return true;

            //user operations
            case "HELLO":
                sendResponse("OK WORLD");
                return true;
            case "QUIT":
                sendResponse("OK bye");
                return false;
            default:
                sendResponse("ERROR BAD_REQUEST unknown command");
                return true;
        }
    }

    private void sendResponse(String message){
        out.println(message);
    }
}