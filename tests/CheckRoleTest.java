
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.strathclyde.common.Action;
import org.strathclyde.common.CheckRole;
import org.strathclyde.common.Role;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class CheckRoleTest {

    @ParameterizedTest
    @EnumSource(value = Action.class, names = {
            "ALLOCATE_BAY",
            "EXCHANGE_BAY",
            "ASSIGN_MAINTENANCE_TEAM",
            "COMPLETE_MAINTENANCE",
            "LOAD_CARGO",
            "UNLOAD_CARGO",
            "TRANSFER_CARGO"})
    void whenRoleIsClientThenOnlyClientCanPerformClientActions(Action action) {
        Role clientRole = Role.CLIENT;
        Role adminRole = Role.ADMIN;

        assertTrue(CheckRole.checkActionAgainstRole(clientRole, action));
        assertFalse(CheckRole.checkActionAgainstRole(adminRole, action));
    }

    @ParameterizedTest
    @EnumSource(value = Action.class, names = {
            "OPEN_BAY",
            "CLOSE_BAY",
            "ADD_TRANSPORT",
            "VIEW_CONNECTED_CLIENTS",
            })
    void whenRoleIsAdminThenOnlyAdminCanPerformAdminActions(Action action) {
        Role adminRole = Role.ADMIN;
        Role clientRole = Role.CLIENT;

        assertTrue(CheckRole.checkActionAgainstRole(adminRole, action));
        assertFalse(CheckRole.checkActionAgainstRole(clientRole, action));
    }

    @ParameterizedTest
    @EnumSource(value = Action.class, names = {
            "VIEW_TRANSPORT",
            "VIEW_BAY",
            "VIEW_MAINTENANCE_TEAMS",
            "VIEW_CARGO_QUANTITIES",
            "VIEW_EVENT_LOG",
            "VIEW_RESOURCES"})
    void ClientAndAdminCanBothViewTransport(Action action) {
        Role adminRole = Role.ADMIN;
        Role clientRole = Role.CLIENT;

        assertTrue(CheckRole.checkActionAgainstRole(adminRole, action));
        assertTrue(CheckRole.checkActionAgainstRole(clientRole, action));
    }
}
