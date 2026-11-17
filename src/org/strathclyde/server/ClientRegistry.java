package org.strathclyde.server;

import org.strathclyde.client.Client;
import org.strathclyde.common.Role;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClientRegistry {

    private HashMap<Integer, Client> clients = new HashMap<>();
    private int nextId = 1;

    public synchronized Client addClient(Role role, ClientSessionHandler handler){
        Client client = new Client(nextId++, role, handler);
        clients.put(client.getId(), client);
        return client;
    }

    public synchronized void removeClient(int id){
        clients.remove(id);
    }

    //returns a copy so callers can loop over it while others connect/disconnect
    public synchronized List<Client> listClients(){
        return new ArrayList<>(clients.values());
    }
}