package org.strathclyde.controller;

import org.strathclyde.common.ServerResponse;

//TODO: remove not implemented method when implemented

public class DockingBayController {


    public ServerResponse allocateBay(String transportId, String bayId) {
        return notImplemented();
    }

    public ServerResponse exchangeBays(String transportId, String bayId) {
        return notImplemented();
    }

    public ServerResponse openBay(String bayId) {
        return notImplemented();
    }

    public ServerResponse closeBay(String bayId) {
        return notImplemented();
    }


    private ServerResponse notImplemented() {
        return new ServerResponse(false, "Nothing here yet");
    }
}
