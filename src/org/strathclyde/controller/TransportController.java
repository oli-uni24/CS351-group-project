package org.strathclyde.controller;

import org.strathclyde.common.ServerResponse;

//TODO: remove not implemented method when implemented

public class TransportController {

    public ServerResponse addTransport(int maxCapacity, String departureTime) {
        return notImplemented();
    }

    //one transport's details
    //message has one item per line
    public ServerResponse viewTransport(){
        return notImplemented();
    }

    public ServerResponse assignTeam(String transportId, String teamId){
        return notImplemented();
    }

    public ServerResponse completeMaintenance(String transportId){
        return notImplemented();
    }

    private ServerResponse notImplemented() {
        return new ServerResponse(false, "Nothing here yet");
    }
}
