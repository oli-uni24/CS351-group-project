package org.strathclyde.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

//This class is for the shared storage of every transport in the spaceport
//Many client threads use it at the same time, so it uses a thread-safe map
//The map is keyed by transport id and keeps transports in a stable id order.
public class TransportRepository {

    private final ConcurrentSkipListMap<String,Transport> transports = new ConcurrentSkipListMap<>();

    //Used by createTransport to hand out ids. For example, T1, T2, T3, etc.
    private final AtomicInteger nextNumber = new AtomicInteger(1);

    //Add existing transport, Ids must be unique. putIfAbsent checks and adds in one step so
    //two threads adding the same id at the same moment cannot both succeed
    public void addTransport(Transport transport) {
        if (transport == null){
            throw new IllegalArgumentException("Transport is required");
        }
        if (transports.putIfAbsent(transport.getId(), transport) != null){
            throw new IllegalArgumentException("Transport " + transport.getId() + " already exists");
        }
    }

    //creates new empty transport with a unique generated id and stores it
    public Transport createTransport(int maxCapacity, LocalDateTime scheduledDeparture) {
        while (true){
            String id = "T" + nextNumber.getAndIncrement();
            Transport transport = new Transport(id, maxCapacity, 0, scheduledDeparture);

            //if this id is already used then it will try the next
            if ( transports.putIfAbsent(id, transport) == null){
                return transport;
            }
        }
    }

    //Returns the transport with this id or null
    public Transport findById(String id) {
        if (id == null){
            return null;
        }
        return  transports.get(id);
    }

    //Returns a list of all transports
    public List<Transport> getAllTransports(){
        return new ArrayList<>(transports.values());
    }
}
