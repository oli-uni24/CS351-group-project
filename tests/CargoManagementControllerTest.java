import org.junit.jupiter.api.RepeatedTest;
import org.strathclyde.controller.CargoManagementController;
import org.strathclyde.controller.PortController;
import org.strathclyde.model.Transport;
import org.strathclyde.model.TransportRepository;
import org.strathclyde.model.TransportState;
import org.strathclyde.service.EventLog;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CargoManagementControllerTest {

    TransportRepository transportRepository = new TransportRepository();
    EventLog eventLog = new EventLog();
    PortController portController = new PortController(eventLog, transportRepository);
    CargoManagementController cargoManagementController = new CargoManagementController(transportRepository, eventLog, portController);


    //Test ID 1
    @RepeatedTest(20)
    void whenMultipleThreadsCallLoadCargo_thenTransportCargoIsUpdatedToTheRightAmount() throws ExecutionException, InterruptedException, TimeoutException {

        Transport transport = transportRepository.createTransport(300, LocalDateTime.now());
        transport.setState(TransportState.DOCKED);
        String transportId = transport.getId();
        int numberOfThreads = 100;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        List<Future<Transport>> futures = new ArrayList<>();

        for (int i = 0; i < numberOfThreads; i++) {
            futures.add(executor.submit(() ->
            {
                countDownLatch.await();
                cargoManagementController.loadCargo(transportId, 2);
                return null;
            }));
        }
        countDownLatch.countDown();

        for (Future<Transport> future : futures) {
            future.get(3, TimeUnit.SECONDS);
        }
        executor.shutdown();
        assertEquals(200, transport.getCurrentCargo());
    }

    //Test ID 2
    @RepeatedTest(20)
    void whenMultipleThreadsCallUnloadCargo_thenTransportCargoIsUpdatedToTheRightAmount() throws ExecutionException, InterruptedException, TimeoutException {

        Transport transport = transportRepository.createTransport(300, LocalDateTime.now());
        transport.setState(TransportState.DOCKED);
        transport.addCargo(200);

        String transportId = transport.getId();
        int numberOfThreads = 100;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        List<Future<Transport>> futures = new ArrayList<>();

        for (int i = 0; i < numberOfThreads; i++) {
            futures.add(executor.submit(() ->
            {
                countDownLatch.await();
                cargoManagementController.unloadCargo(transportId, 2);
                return null;
            }));
        }
        countDownLatch.countDown();

        for (Future<Transport> future : futures) {
            future.get(3, TimeUnit.SECONDS);
        }
        executor.shutdown();
        assertEquals(0, transport.getCurrentCargo());
    }


    //Test ID 3
    @RepeatedTest(20)
    void whenMultipleThreadsCallTransferCargo_thenTransportCargoIsRemovedAndAddedToTheOtherTransport() throws ExecutionException, InterruptedException, TimeoutException {

        Transport origionalTransport = transportRepository.createTransport(1600, LocalDateTime.now());
        origionalTransport.setState(TransportState.DOCKED);
        origionalTransport.addCargo(1600);

        Transport newTransport = transportRepository.createTransport(1600, LocalDateTime.now());
        newTransport.setState(TransportState.DOCKED);

        String originalTransportId = origionalTransport.getId();
        String newTransportId = newTransport.getId();

        int numberOfThreads = 800;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        List<Future<Transport>> futures = new ArrayList<>();

        for (int i = 0; i < numberOfThreads; i++) {
            futures.add(executor.submit(() ->
            {
                countDownLatch.await();
                cargoManagementController.transferCargo(originalTransportId, newTransportId, 1);
                return null;
            }));
        }
        countDownLatch.countDown();

        for (Future<Transport> future : futures) {
            future.get(3, TimeUnit.SECONDS);
        }
        executor.shutdown();
        assertEquals(800, origionalTransport.getCurrentCargo());
        assertEquals(800, newTransport.getCurrentCargo());
    }


}
