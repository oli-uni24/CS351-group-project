package org.strathclyde.client;

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

        try {
            socket = new Socket(host, PORT);

            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            //Reader thread prints everything recieved from server as it is recieved
            Thread reader = new Thread(new NotificationListener(serverIn));
            reader.setDaemon(true);
            reader.start();

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
        } finally {
            try {
                if (socket != null) socket.close();
            } catch (IOException ignore){}
        }
    }
}