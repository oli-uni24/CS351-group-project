import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.strathclyde.client.Client;
import org.strathclyde.common.Role;
import org.strathclyde.server.ClientRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ClientRegistryTest {
    private ClientRegistry registry;

    @BeforeEach
    void setUp(){
        registry = new ClientRegistry();
    }
    @Test
    void addClientAssignsDifferentIds(){
        Client a = registry.addClient(Role.CLIENT, null);
        Client b = registry.addClient(Role.ADMIN, null);

        assertNotEquals(a.getId(), b.getId());
        assertNotEquals(2, registry.listClients().size());
    }

    @Test
    void removeClientOnlyRemovesThatClient(){
        Client a = registry.addClient(Role.CLIENT, null);
        Client b = registry.addClient(Role.CLIENT, null);

        registry.removeClient(a.getId());

        List<Client> remaining = registry.listClients();
        assertEquals(1, remaining.size());
        assertEquals(b.getId(), remaining.get(0).getId());
    }

    @Test
    void listClientsReturnsACopy(){
        registry.addClient(Role.CLIENT, null);

        List<Client> copy = registry.listClients();
        copy.clear();

        assertEquals(1, registry.listClients().size());
    }

    @Test
    void concurrentAddsGiveUniqueIds() throws Exception{

    }

    @Test
    void concurrentAddRemoveAndListDoNotFail() throws Exception{

    }


}
