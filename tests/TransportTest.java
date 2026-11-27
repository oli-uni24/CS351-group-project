import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.strathclyde.model.Transport;
import org.strathclyde.model.TransportRepository;
import org.strathclyde.model.TransportState;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class TransportTest {

    TransportRepository transportRepository = new TransportRepository();

    //Test ID
//    @RepeatedTest(100)
//    void whenSetStateIsCalled_thenStateIsSetCorrectly() throws ExecutionException, InterruptedException, TimeoutException {       //come back to this test if there is time
//        Transport transport = transportRepository.createTransport(300, LocalDateTime.now());
//        int numberOfThreads = 100;
//        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
//        CountDownLatch countDownLatch = new CountDownLatch(1);
//        List<Future<Transport>> correctStateChangeFutures = new ArrayList<>();
//        boolean wasInterrupted = false;
//
//        for (int i = 0; i < numberOfThreads; i++) {
//            try {
//
//                correctStateChangeFutures.add(executor.submit(() ->
//                {
//
//
//                    countDownLatch.await();
//                    transport.setState(TransportState.DOCKED);
//                    return null;
//
//
//                }));
//            }   catch (Exception e) {
//                wasInterrupted = true;
//            }
//        }
//
//        List<Future<Transport>> incorrectStateChangeFutures = new ArrayList<>();
//        for (int i = 0; i < numberOfThreads; i++) {
//            incorrectStateChangeFutures.add(executor.submit(() ->
//            {
//                countDownLatch.await();
//                transport.setState(TransportState.SERVICING);
//                return null;
//            }));
//        }
//        countDownLatch.countDown();
//
//        for (Future<Transport> future : correctStateChangeFutures) {
//            future.get(3, TimeUnit.SECONDS);
//        }
//        for (Future<Transport> future : incorrectStateChangeFutures) {

    /// /            assertThrows(ExecutionException.class, () -> future.get(3, TimeUnit.SECONDS));
//            assertTrue(wasInterrupted);
//        }
//        executor.shutdown();
//        assertEquals(TransportState.SERVICING, transport.getState());
//    }


    //Test ID 5
    @Test
    public void whenEachUserThreadUpdatesState_thenAllThreadAreAwareOfTheTransportsLatestState() throws ExecutionException, InterruptedException, TimeoutException {
        Transport transport = transportRepository.createTransport(300, LocalDateTime.now());
        int numberOfThreads = 20;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        CountDownLatch countDownLatch2 = new CountDownLatch(10);
        CountDownLatch countDownLatch3 = new CountDownLatch(10);
        List<Future<TransportState>> dockedFutures = new ArrayList<>();
        List<Future<TransportState>> servicingFutures = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            dockedFutures.add(executor.submit(() ->
            {
                countDownLatch.await();
                transport.setState(TransportState.DOCKED);
                countDownLatch3.countDown();
                countDownLatch2.await();
                return transport.getState();
            }));
        }
        for (int i = 0; i < 10; i++) {
            servicingFutures.add(executor.submit(() ->
            {
                countDownLatch3.await();
                transport.setState(TransportState.SERVICING);
                countDownLatch2.countDown();
                return null;
            }));
        }
        countDownLatch.countDown();

        for (Future<TransportState> future : servicingFutures) {
            future.get(3, TimeUnit.SECONDS);
        }
        for (Future<TransportState> future : dockedFutures) {
            assertEquals(TransportState.SERVICING, future.get(3, TimeUnit.SECONDS));
        }
        executor.shutdown();
    }

    //Test ID 6
    @RepeatedTest(50)
    void whenMultipleThreadsUpdateTransportState_thenStateTransitionsAreCorrectlyChangedOrRejected() throws Exception {
        Transport transport = transportRepository.createTransport(300, LocalDateTime.now());

        int numberOfThreads = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> futures = new ArrayList<>();
        Map<TransportState, Integer> transportStates = new ConcurrentSkipListMap<>();
        transportStates.put(TransportState.DOCKED, 0);
        transportStates.put(TransportState.SERVICING, 1);
        transportStates.put(TransportState.READY, 2);
        transportStates.put(TransportState.DEPARTED, 3);

        for (int i = 0; i < numberOfThreads; i++) {
            futures.add(executor.submit(() -> {
                start.await();
                int theLastTransportStatePosition = -1;
                for (TransportState currentState : transportStates.keySet()) {
                    try {
                        transport.setState(currentState);
                    } catch (IllegalArgumentException expected) {
                    }
                    int theCurrentTransportStatePosition = transportStates.get(transport.getState());
                    if (theCurrentTransportStatePosition < theLastTransportStatePosition) {
                        return false;
                    }
                    theLastTransportStatePosition = theCurrentTransportStatePosition;
                }
                return true;
            }));
        }

        start.countDown();
        for (Future<Boolean> f : futures) {
            assertTrue(f.get(3, TimeUnit.SECONDS), "A thread tried to put the transport into an unacceptable state");
        }
        assertEquals(TransportState.DEPARTED, transport.getState());
    }


    //Test ID 7
    @Test
    public void whenThreadCallGetState_thenLatestStateIsReturned() throws InterruptedException {
        Transport transport = transportRepository.createTransport(300, LocalDateTime.now());
        ExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        Thread thread = new Thread(() -> {
            while (transport.getState() == TransportState.WAITING) {
                //do nothing
            }
        });

        thread.start();
        Thread.sleep(500);
        transport.setState(TransportState.DOCKED);

        thread.join(700);
        assertSame(TransportState.DOCKED, transport.getState());
        assertFalse(thread.isAlive());
        executor.shutdown();
    }


    //Test ID 7
    @Test
    public void whenThreadCallSetDockingBay_thenDockingBayIsSet() throws InterruptedException {
        Transport transport = transportRepository.createTransport(300, LocalDateTime.now());
        ExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        Thread thread = new Thread(() -> {
            while (transport.getState() == TransportState.WAITING) {
                //do nothing
            }
        });

        thread.start();
        Thread.sleep(500);
        transport.setState(TransportState.DOCKED);

        thread.join(700);
        assertSame(TransportState.DOCKED, transport.getState());
        assertFalse(thread.isAlive());
        executor.shutdown();
    }


    //NOTE - im skipping testing the basic synchronized setters and getters. I will come back to these if there is time but for now I will be testing the more complicated threading logic

    //Test ID 8
    @RepeatedTest(80)
    void whenMultipleThreadsAddCargo_thenTotalCargoIsAdded() throws ExecutionException, InterruptedException, TimeoutException {
        Transport transport = transportRepository.createTransport(300, LocalDateTime.now());
        transport.setState(TransportState.DOCKED);
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        List<Future<Integer>> futures = new ArrayList<>();
        int quantityOfCargoToBeAdded = 10;

        for (int i = 0; i < 10; i++) {
            futures.add(
                    executor.submit(() -> {
                        countDownLatch.await();
                        return transport.addCargo(quantityOfCargoToBeAdded);
                    }));
        }
        countDownLatch.countDown();

        for (Future<Integer> future : futures) {
            future.get(3, TimeUnit.SECONDS);
        }
        executor.shutdown();
        assertEquals(100, transport.getCurrentCargo());


    }


}
