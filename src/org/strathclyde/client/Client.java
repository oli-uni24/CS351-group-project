package org.strathclyde.client;

import org.strathclyde.common.Role;
import org.strathclyde.server.ClientSessionHandler;

public class Client {
    private final int id;
    private final Role role;
    private final ClientSessionHandler handler;

    public Client(int id, Role role, ClientSessionHandler handler) {
        this.id = id;
        this.role = role;
        this.handler = handler;
    }

    public int getId(){return id;}
    public Role getRole(){return role;}
    public ClientSessionHandler getHandler(){return handler;}

    @Override
    public String toString() {
        return "CLIENT " + id + " " + role;
    }
}
