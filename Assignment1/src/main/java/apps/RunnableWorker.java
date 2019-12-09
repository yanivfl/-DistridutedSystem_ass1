package apps;

public class RunnableWorker implements Runnable{
    @Override
    public void run() {
        String[] args = new String[0];
        try {
            System.out.println("Worker: started running");
            MainWorkerClass.main(args);
        }catch(Exception e){
            System.out.println("Thread interrupted..."+e);
        }
    }
}
