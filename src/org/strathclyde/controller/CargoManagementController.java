package org.strathclyde.controller;

import org.strathclyde.common.ServerResponse;

//TODO: remove not implemented method when implemented

public class CargoManagementController {


    public ServerResponse loadCargo(String transportId, int units) {
        return notImplemented();
    }

    public ServerResponse unloadCargo(String transportId, int units) {
        return notImplemented();
    }

    public ServerResponse transferCargo(String fromId, String toId, int units) {
        return notImplemented();
    }

    private ServerResponse notImplemented() {
        return new ServerResponse(false, "Nothing here yet");
    }
}
