package org.strathclyde.common;

public enum Action {
    ADD_TRANSPORT,
    VIEW_TRANSPORT,
    LOAD_CARGO,
    UNLOAD_CARGO,
    TRANSFER_CARGO,
    OPEN_AND_CLOSE_BAY,
    ALLOCATE_BAY,
    EXCHANGE_BAY,
    VIEW_EVENT_LOG,
    VIEW_RESOURCES,
    VIEW_CONNECTED_CLIENTS,
    ASSIGN_MAINTENANCE_TEAM,
    COMPLETE_MAINTENANCE,
    VIEW_BAY,
    VIEW_MAINTENANCE_TEAMS, //can view the maintenance teams and see which ones are in use and which ones are available
    VIEW_CARGO_QUANTITIES
}