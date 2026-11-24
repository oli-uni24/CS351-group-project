package org.strathclyde.client;

import org.strathclyde.common.Role;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ControlTerminal {
    private static final int PORT = 50000;

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "127.0.0.1";
        Socket socket = null;
        Role userRole;

        try {
            socket = new Socket(host, PORT);

            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            //Reader thread prints everything recieved from server as it is recieved
            Thread reader = new Thread(new NotificationListener(serverIn));
            reader.setDaemon(true);
            reader.start();

            //get the users role and send it to the server so that server knows what operations the user is allowed to perform before the user is able to perform operations
            userRole = getUserRole(keyboard);
            out.println("CONNECT " + userRole);
            Thread.sleep(10);
            System.out.println();

            //Main thread that sends what user types
            String line;
            while ((line = keyboard.readLine()) != null) {
                out.println(line);
                if (line.trim().equalsIgnoreCase("QUIT")){
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Could not connect: " + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("Interrupted: " + e.getMessage());;
        } finally {
            try {
                if (socket != null) socket.close();
            } catch (IOException ignore){}
        }
    }

    private static Role getUserRole(BufferedReader keyboard) {
        System.out.println("Select a role option below:");
        System.out.println("Client");
        System.out.println("Admin\n");

        while (true) {
            try {
                String line = keyboard.readLine();
                if (line.equalsIgnoreCase("client")) {
                    return Role.CLIENT;
                } else if (line.equalsIgnoreCase("admin")) {
                    return Role.ADMIN;
                }
                System.out.println("Please enter Client or Admin and press enter.");
            } catch (IOException e) {
                System.out.println("Please select a role.");
            }
        }
    }
}