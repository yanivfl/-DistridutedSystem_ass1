package apps;

import org.omg.PortableServer.THREAD_POLICY_ID;

import java.lang.reflect.Array;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class Manager {

    public static void main(String[] args) {

        AtomicInteger workersCount = new AtomicInteger(0);
        ReentrantLock workersCountLock = new ReentrantLock();


        // TODO: deal with a duplicated response from the workers on the same job

        // TODO: for each local app the bucket will serve the manager as as appID


        // TODO: remove all execptions from the project!


        // TODO: when terminates, delete all queues


    }
}
