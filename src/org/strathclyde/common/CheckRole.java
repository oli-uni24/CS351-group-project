package org.strathclyde.common;

import java.util.ArrayList;
import java.util.List;

//should this be a singleton class? If so, how would that affect threading?
public class CheckRole {

    private static final ArrayList<Action> clientActions;
    private static final ArrayList<Action> adminActions;

    //need to clarify with Warren if an admin can perform admin only operations or if an admin can perform admin operations and operator operations. At this moment in time I would assume that the admins cannot perform client actions

    static {
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
                Action.TRANSFER_CARGO,
                Action.VIEW_RESOURCES,
                Action.VIEW_EVENT_LOG
                ));

        adminActions = new ArrayList<>(List.of(
                Action.OPEN_BAY,
                Action.CLOSE_BAY,
                Action.VIEW_TRANSPORT,
                Action.VIEW_RESOURCES,
                Action.ADD_TRANSPORT,
                Action.VIEW_CONNECTED_CLIENTS,
                Action.VIEW_EVENT_LOG,
                Action.VIEW_BAY,
                Action.VIEW_MAINTENANCE_TEAMS,
                Action.VIEW_CARGO_QUANTITIES
        ));
    }


    public static boolean checkActionAgainstRole(Role role, Action action) {    //add this check to the controllers. If is returns true then a controller can call the corresponding method else block the
        if (role == Role.CLIENT && clientActions.contains(action)) {            // method call and return a message to the client informing them that they cannot perform that operation with their role
            return true;
        } else return role == Role.ADMIN && adminActions.contains(action);
    }
}
