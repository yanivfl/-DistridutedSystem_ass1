package apps;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Tag;
import handlers.EC2Handler;
import handlers.S3Handler;
import handlers.SQSHandler;
import org.omg.PortableServer.THREAD_POLICY_ID;

import java.lang.reflect.Array;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class Manager {

    private static EC2Handler ec2;
    private static S3Handler s3;
    private static SQSHandler sqs;

    private static final int MAX_THREADS_PER_GROUP = 10;

    private static ConcurrentMap<String, ClientInfo> clientsInfo;
    private static AtomicInteger clientsCount;
    private static AtomicInteger workersCount;
    private static ReentrantLock workersCountLock;
    private static AtomicBoolean terminate;

    private static LinkedList<Thread> clientsThreads;
    private static LinkedList<Thread> workersThreads;
    private static int clientsThreadCount;
    private static int workersThreadCount;

    private static final Object waitingObject = new Object();


    public static void initialConfigurations() {

        // initial configurations
        ec2 = new EC2Handler();
        s3 = new S3Handler(ec2);
        sqs = new SQSHandler(ec2.getCredentials());     // TODO: change this!

        clientsCount = new AtomicInteger(0);
        workersCount = new AtomicInteger(0);
        workersCountLock = new ReentrantLock();
        terminate = new AtomicBoolean(false);

        clientsThreads = new LinkedList<>();
        workersThreads = new LinkedList<>();
        clientsThreadCount = 0;
        workersThreadCount = 0;

    }

    public static void main(String[] args) throws InterruptedException {

        initialConfigurations();

        // start the first threads (1 for workers and 1 for clients)
        Thread workersThread = new Thread(new ManageWorkers(clientsInfo, ec2, s3, sqs));
        Thread clientsThread = new Thread(new ManageClients(clientsInfo, workersCount, workersCountLock, terminate, ec2, s3, sqs));

        workersThreads.add(workersThread);
        clientsThreads.add(clientsThread);

        workersThreadCount++;
        clientsThreadCount++;

        workersThread.start();
        clientsThread.start();

        // wait for some thread to add workers or add clients


        while (!terminate.get()) {

            synchronized (waitingObject) {

                // TODO:
                // wakes up when:
                // 1. new worker
                // 2. new client
                // 3. client is done
                // 4. worker is done    // TODO
                waitingObject.wait();

                // clientsCount can increase only on waitingObject synchronization
                if (clientsThreadCount < clientsCount.get() && clientsThreadCount < MAX_THREADS_PER_GROUP) {
                    clientsThread = new Thread(new ManageClients(clientsInfo, workersCount, workersCountLock, terminate, ec2, s3, sqs));
                    clientsThreads.add(clientsThread);
                    clientsThreadCount++;
                    clientsThread.start();
                }

                // workersCount can increase only on waitingObject synchronization
                if (workersThreadCount < workersCount.get() && workersThreadCount < MAX_THREADS_PER_GROUP) {
                    workersThread = new Thread(new ManageWorkers(clientsInfo, ec2, s3, sqs));
                    workersThreads.add(workersThread);
                    workersThreadCount++;
                    workersThread.start();
                }

                // clientsCount can decrease only on waitingObject synchronization
                if (clientsThreadCount < clientsCount.get() && clientsThreadCount > 1) {
                    Thread toInterrupt = clientsThreads.poll();
                    toInterrupt.interrupt();
                }

                // workersCount can decrease only on waitingObject synchronization
                if (workersThreadCount < workersCount.get() && workersThreadCount > 1) {
                    Thread toInterrupt = clientsThreads.poll();
                    toInterrupt.interrupt();
                }
            }
        }

        // wait for all clients to be serves (and the workers to finish their jobs)
        for (Thread thread: clientsThreads) {
            thread.join();
        }
        for (Thread thread: workersThreads) {
            thread.join();
        }

        // all workers are finished, terminate them
        List<Instance> instances = ec2.listInstances(false);
        Instance managerInstance = null;
        for (Instance instance: instances) {

            for (Tag tag: instance.getTags()) {
                if (tag.getValue().equals(Constants.INSTANCE_TAG.TAG_WORKER.toString())) {
                    ec2.terminateEC2Instance(instance.getInstanceId());
                }
                else {
                    if (tag.getValue().equals(Constants.INSTANCE_TAG.TAG_MANAGER.toString())) {
                        managerInstance = instance;
                    }
                }
            }
        }

        // get queues (URLs)
        String M2C_QueueURL = sqs.getURL(Constants.MANAGER_TO_CLIENTS_QUEUE);
        String C2M_QueueURL = sqs.getURL(Constants.CLIENTS_TO_MANAGER_QUEUE);
        String W2M_QueueURL = sqs.getURL(Constants.WORKERS_TO_MANAGER_QUEUE);
        String M2W_QueueURL = sqs.getURL(Constants.MANAGER_TO_WORKERS_QUEUE);

        // busy wait on clients queue (by their expected emptying order)
        // Note: We don't care about (Clients -> Manager) queue, because new clients can always try to connect
        // 1. (Manager -> Workers)
        // 2. (Workers -> Manager)
        // 3. (Manager -> Clients)
        while (!sqs.receiveMessages(M2W_QueueURL, true, true).isEmpty());
        while (!sqs.receiveMessages(W2M_QueueURL, true, true).isEmpty());
        while (!sqs.receiveMessages(M2C_QueueURL, true, true).isEmpty());

        // delete all queues
        sqs.deleteQueue(M2C_QueueURL);
        sqs.deleteQueue(C2M_QueueURL);
        sqs.deleteQueue(W2M_QueueURL);
        sqs.deleteQueue(M2W_QueueURL);

        // terminate - after this the program must end!
        ec2.terminateEC2Instance(managerInstance.getInstanceId());



        // TODO: deal with adding workers and closing them

        // TODO: add support in manageClients and managerWorkers to interrupt

        // TODO: wake up when need to close/add clients or workers

        // TODO: deal with a duplicated response from the workers on the same job

        // TODO: when sending a done message to a client, remove it from the client info map

        // TODO: order the short and long polling and visibillity timeout

    }
}
