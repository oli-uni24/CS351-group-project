
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
            "VIEW_BAY",
            "VIEW_MAINTENANCE_TEAMS",
            "VIEW_CARGO_QUANTITIES",
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
            "OPEN_AND_CLOSE_BAY",
            "VIEW_RESOURCES",
            "ADD_TRANSPORT",
            "VIEW_CONNECTED_CLIENTS",
            "VIEW_EVENT_LOG"})
    void whenRoleIsAdminThenOnlyAdminCanPerformAdminActions(Action action) {
        Role adminRole = Role.ADMIN;
        Role clientRole = Role.CLIENT;

        assertTrue(CheckRole.checkActionAgainstRole(adminRole, action));
        assertFalse(CheckRole.checkActionAgainstRole(clientRole, action));
    }

    @Test
    void ClientAndAdminCanBothViewTransport() {
        Action viewTransport = Action.VIEW_TRANSPORT;
        Role adminRole = Role.ADMIN;
        Role clientRole = Role.CLIENT;

        assertTrue(CheckRole.checkActionAgainstRole(adminRole, viewTransport));
        assertTrue(CheckRole.checkActionAgainstRole(clientRole, viewTransport));
    }
}
