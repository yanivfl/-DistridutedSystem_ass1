package apps;

public class Constants {

    public static final String AMI = "ami-b66ed3de";
    public static final String TAG_MANAGER = "TAG_MANAGER";
    public static final String TAG_WORKER = "TAG_WORKER";
    public static final String REVIEWS= "reviews";
    public static final String TEXT= "text";

    public static final String CLIENTS_TO_MANAGER_QUEUE_BUCKET = "ClientsToManagerQueueBucket";
    public static final String CLIENTS_TO_MANAGER_QUEUE_KEY = "ClientsToManagerQueueKey.txt";

    public static final String MANAGER_TO_CLIENTS_QUEUE_BUCKET = "ManagerToClientsQueueBucket";
    public static final String MANAGER_TO_CLIENTS_QUEUE_KEY = "ManagerToClientsQueueKey.txt";

    public static final String WORKERS_TO_MANAGER_QUEUE_BUCKET = "WorkersToManagerQueueBucket";
    public static final String WORKERS_TO_MANAGER_QUEUE_KEY = "WorkersToManagerQueueKey.txt";

    public static final String MANAGER_TO_WORKERS_QUEUE_BUCKET = "ManagerToWorkersQueueBucket";
    public static final String MANAGER_TO_WORKERS_QUEUE_KEY = "ManagerToWorkersQueueKey.txt";

    public enum TAGS {
        CLIENT_2_MANAGER, MANAGER_2_CLIENT, MANAGER_2_WORKER, WORKER_2_MANAGER;
    }

    public static final String[] HTML_COLORS = new String[]{"dark red", "red", "black", "light green", "dark green"};




}


