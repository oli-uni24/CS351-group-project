package org.strathclyde.common;

import java.util.ArrayList;
import java.util.List;

//should this be a singleton class? If so, how would that affect threading?
public class CheckRole {

    private static ArrayList<Action> clientActions;
    private static ArrayList<Action> adminActions;


    //need to clarify with Warren if an admin can perform admin only operations or if an admin can perform admin operations and operator operations. At this moment in time I would assume that the admins cannot perform client actions

    public CheckRole() {
        clientActions = new ArrayList<>(List.of(
                Action.VIEW_TRANSPORT,
                Action.VIEW_BAY,
                Action.VIEW_MAINTENANCE_TEAMS,
                Action.VIEW_CARGO_QUANTITIES,
                Action.ALLOCATE_BAY,
                Action.EXCHANGE_BAY,
                Action.ASSIGN_MAINTENANCE_TEAM,
                Action.COMPLETE_MAINTENANCE,
                Action.LOAD_CARGO,
                Action.UNLOAD_CARGO,
                Action.TRANSFER_CARGO
        ));

        adminActions = new ArrayList<>(List.of(
                Action.OPEN_AND_CLOSE_BAY,
                Action.VIEW_TRANSPORT,
                Action.VIEW_RESOURCES,
                Action.ADD_TRANSPORT,
                Action.VIEW_CONNECTED_CLIENTS,
                Action.VIEW_EVENT_LOG
        ));
    }


    public static boolean checkActionAgainstRole(Role role, Action action) {
        if (role == Role.CLIENT && clientActions.contains(action)) {
            return true;
        } else return role == Role.ADMIN && adminActions.contains(action);

    }


}
