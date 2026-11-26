package org.strathclyde.server;

import org.strathclyde.client.Client;
import org.strathclyde.client.NotificationListener;

import java.util.function.Consumer;

public class NotificationService implements Consumer<String> {
    private final ClientRegistry registry;

    public NotificationService(ClientRegistry registry){
        this.registry = registry;
    }

    @Override
    public void accept (String event){
        broadcastEvent(event);
    }

    public void broadcastEvent(String event){
        // listClients() returns a copy so clients can leave or connect during the loop
        for (Client c: registry.listClients()) {
            try{
                c.getHandler().sendNotification("NOTIF EVENT " + event);
            } catch (RuntimeException e){
                //one failing client mustn't stop the others, or the operation logging the event
                System.err.println("Notification to client " + c.getId() + " failed: " + e.getMessage());
            }
        }
    }
}
