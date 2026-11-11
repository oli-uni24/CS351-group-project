package org.strathclyde.controller;

import org.strathclyde.common.ServerResponse;

//TODO: remove not implemented method when implemented

public class PortController {
    //all transports, bays, teams and cargo
    // message holds one item per line
    public ServerResponse viewResources(){
        return notImplemented();
    }

    //event log
    // message holds one event per line
    public ServerResponse viewEventLog() {
        return notImplemented();
    }

    private ServerResponse notImplemented() {
        return new ServerResponse(false, "Nothing here yet");
    }
}
