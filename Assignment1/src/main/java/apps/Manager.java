package apps;

import org.omg.PortableServer.THREAD_POLICY_ID;

import java.lang.reflect.Array;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class Manager {

    public static void main(String[] args) {

        AtomicInteger workersCount = new AtomicInteger(0);
        ReentrantLock workersCountLock = new ReentrantLock();
        AtomicBoolean terminate = new AtomicBoolean(false);


        // TODO: deal with a duplicated response from the workers on the same job

        // TODO: for each local app the bucket will serve the manager as as appID

        // TODO: when terminates, delete all queues

        // TODO: when sending a done message to a client, remove it from the client info map

        // TODO: add termination process:
        // TODO: serve the local application that sent the termination message.
        // TODO: Waits for all the workers to finish their job, and then terminates them.
        // TODO: Creates response messages for the jobs, if needed.
        // TODO: Terminates.

    }
}
