package org.strathclyde.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PortWanderServer {

    private final static int PORT = 50000;
    private final static int MAX_CLIENTS = 20; //TODO: fixed threadpool cap?

    private final ExecutorService threadPool = Executors.newFixedThreadPool(MAX_CLIENTS);
    private ServerSocket serverSocket = null; // listens for incoming connections

    public void startServer() throws IOException {
        // create the server socket to listen to incoming requests
        serverSocket = new ServerSocket(PORT);
        System.out.println("Server listening on port " + PORT);
        acceptConnections();
    }

    private void acceptConnections() {
        while (!serverSocket.isClosed()) {
            try {
                Socket socket = serverSocket.accept(); // blocks until a client connects
                threadPool.execute(new ClientSessionHandler(socket));
            } catch (IOException e) {
                if (!serverSocket.isClosed()) { //if its not running, socket was closed on purpose
                    System.err.println("Accept connection failed: " + e.getMessage());
                }
            }
        }
    }

    public void shutdown() {
        try {
            if (serverSocket != null) serverSocket.close(); //accept() will throw and close loop
        } catch (IOException e) {
        } //ignored
        threadPool.shutdown();
        //TODO: add persistence saving data here
    }

    //TODO: move into its own class once pulling other's work
    public static void main(String[] args) throws IOException {
        PortWanderServer server = new PortWanderServer();
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
        server.startServer();
    }

}

