import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.strathclyde.controller.CargoManagementController;
import org.strathclyde.controller.DockingBayController;
import org.strathclyde.controller.PortController;
import org.strathclyde.controller.TransportController;
import org.strathclyde.model.TransportRepository;
import org.strathclyde.server.ClientRegistry;
import org.strathclyde.server.ClientSessionHandler;
import org.strathclyde.server.NotificationService;
import org.strathclyde.service.EventLog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class ClientSessionHandlerTest {
    private EventLog eventLog;
    private ClientRegistry registry;
    private PortController portController;
    private DockingBayController bayController;
    private TransportController transportController;
    private CargoManagementController cargoController;

    private ServerSocket serverSocket;
    private ExecutorService pool;
    private List<Session> sessions;

    //Test terminal - socket plus reader and writer
    private static class Session{
        final Socket socket;
        final BufferedReader in;
        final PrintWriter out;

        Session(Socket socket) throws IOException {
            this.socket = socket;
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);
        }

        //sends one command and returns first reply line
        String send(String command) throws IOException{
            out.println(command);
            return in.readLine();
        }

        //reads the data lines of a multi-line reply, up to END
        List<String> readUntilEnd() throws IOException{
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = in.readLine()) != null && !line.equals("END")){
                lines.add(line);
            }
            return lines;
        }
    }

    @BeforeEach
    void setUp() throws IOException {
        eventLog = new EventLog();
        registry = new ClientRegistry();
        TransportRepository repository = new TransportRepository();
        portController = new PortController(eventLog, repository);
        bayController = new DockingBayController(eventLog, portController);
        transportController = new TransportController(eventLog, repository, portController);
        cargoController = new CargoManagementController(repository, eventLog, portController);

        serverSocket = new ServerSocket(0); //any free port
        pool = Executors.newCachedThreadPool();
        sessions = new ArrayList<>();
    }

    @AfterEach
    void tearDown() throws IOException {
        for (Session s : sessions) {
            s.socket.close();
        }
        serverSocket.close();
        pool.shutdownNow();
    }

    //opens a new terminal and starts a handler for it (like the real server)
    private Session openSession() throws IOException{
        Socket clientSide = new Socket("localhost", serverSocket.getLocalPort());
        clientSide.setSoTimeout(3000); //missing reply fails test
        Socket serverSide = serverSocket.accept();
        pool.execute(new ClientSessionHandler(serverSide, registry, portController, bayController, transportController, cargoController));
        Session session = new Session(clientSide);
        sessions.add(session);
        return session;
    }

    private Session connectedAs(String role) throws IOException{
        Session session = openSession();
        assertEquals("OK connected", session.send("CONNECTED " + role));
        return session;
    }

    private void waitForClientCount(int expected) throws InterruptedException{
        long end = System.currentTimeMillis() + 2000;
        while (registry.listClients().size() != expected && System.currentTimeMillis() < end){
            Thread.sleep(20);
        }
        assertEquals(expected, registry.listClients().size());
    }

    // -----connecting-----
    @Test
    void commandBeforeConnectIsRejected() throws Exception{
        Session s = openSession();
        assertTrue(s.send("VIEW_RESOURCES").startsWith("ERROR not connected"));
    }

    @Test
    void connectSucceedsAndRegistersClient() throws Exception {
        connectedAs("client");
        assertEquals(1, registry.listClients().size());
    }

    @Test
    void connectTwiceIsRejected() throws Exception{
        Session s = connectedAs("client");
        assertEquals("ERROR already connected", s.send("CONNECT admin"));
    }

    @Test
    void connectWithInvalidRoleIsRejected() throws Exception {
        Session s = openSession();
        assertTrue(s.send("CONNECT boss").startsWith("ERROR role must be"));
        assertEquals(0, registry.listClients().size());
    }

    @Test
    void connectWithoutRoleShowsUsage() throws Exception {
        Session s = openSession();
        assertTrue(s.send("CONNECT").startsWith("ERROR usage"));
    }

    //-----command parsing

    @Test
    void unknownCommandIsRejected() throws Exception{
        Session s = connectedAs("client");
        assertEquals("ERROR unknown command", s.send("BLAH"));
    }

    @Test
    void commandsAreNotCaseSensitive() throws Exception {
        Session s = connectedAs("client");
        assertEquals("OK", s.send("view_resources"));
    }

    @Test
    void wrongNumberOfArgumentsShowsUsage() throws Exception {
        Session s = connectedAs("client");
        assertTrue(s.send("ALLOCATE_BAY T1").startsWith("ERROR usage: ALLOCATE_BAY"));
    }

    //-----roles
    @Test
    void clientCannotUseAdminCommands() throws Exception{
        Session s = connectedAs("client");
        assertTrue(s.send("ADD_TRANSPORT 100 2030-01-01T110:00").startsWith("ERROR your role is not allowed"));
        assertTrue(s.send("CLOSE_BAY B1").startsWith("ERROR your role is not allowed"));
        assertTrue(s.send("VIEW_CONNECTED_CLIENT").startsWith("ERROR your role is not allowed"));
    }

    @Test
    void adminCanAddTransport() throws Exception{
        Session s = connectedAs("admin");
        assertTrue(s.send("ADD_TRANSPORT 100 2030-01-01T10:00").startsWith("OK"));
    }

    @Test
    void adminSeesConnectedClients() throws Exception {
        Session admin = connectedAs("admin");
        connectedAs("client");

        assertEquals("OK", admin.send("VIEW_CONNECTED_CLIENT"));
        assertEquals(2, admin.readUntilEnd().size());
    }

    //-----quitting and disconnects
    @Test
    void quitClosesConnectionAndRemovesClient() throws Exception {
        Session s = connectedAs("client");

        assertEquals("OK bye", s.send("QUIT"));
        assertNull(s.in.readLine()); //server closed connection
    }

    @Test
    void droppedClientIsRemovedFromRegistry() throws Exception{
        Session s = connectedAs("client");
        waitForClientCount(1);

        s.socket.close(); //no QUIT, just disappears

        waitForClientCount(0);
    }

    // ----- notifications
    @Test
    void everyConnectedTerminalReceivesNotification() throws Exception{
        eventLog.addListener(new NotificationService(registry));
        Session a = connectedAs("client");
        Session b = connectedAs("admin");

        eventLog.recordEvent("TEST_EVENT", "T1", "B1", "hello");

        assertTrue(a.in.readLine().startsWith("NOTIF EVENT"));
        assertTrue(b.in.readLine().startsWith("NOTIF EVENT"));
    }

    @Test
    void terminalStillAcceptsCommandsAfterNotification() throws Exception{
        eventLog.addListener(new NotificationService(registry));
        Session a = connectedAs("client");

        eventLog.recordEvent("TEST_EVENT", "T1", "B1", "hello");
        assertTrue(a.in.readLine().startsWith("NOTIF EVENT"));

        assertEquals("OK", a.send("VIEW_RESOURCES"));
    }

    @Test
    void deadClientDoesNotStopOtherNotifications() throws Exception{
        eventLog.addListener(new NotificationService(registry));
        Session a = connectedAs("client");
        Session b = connectedAs("client");

        b.socket.close(); // b disappears without QUIT

        try {
            eventLog.recordEvent("TEST_EVENT", "T1", "B1", "after b left");
        } catch (RuntimeException e) {
            fail("A dead client must not make recordEvent fail: " + e.getMessage());
        }
        assertTrue(a.in.readLine().startsWith("NOTIF EVENT"));
    }

    @Test
    void manyTerminalsConnectingAtOnceAllRegister() throws Exception {
        int n = 10;
        List<Session> all = new ArrayList<>();
        for (int i = 0; i < n; i++){
            all.add(openSession());
        }

        ExecutorService threads = Executors.newFixedThreadPool(n);
        final CountDownLatch start = new CountDownLatch(1);
        List<Future<String>> replies = new ArrayList<>();
        for (final Session s : all){
            replies.add(threads.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    start.await();
                    return s.send("CONNECT CLIENT");
                }
            }));
        }
        start.countDown();

        for(Future<String> reply : replies){
            //assertEquals("OK connected", reply.get(5, TimeUnits.SECONDS));
        }

    }
}
