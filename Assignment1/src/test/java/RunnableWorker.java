import apps.MainWorkerClass;

public class RunnableWorker implements Runnable{
    @Override
    public void run() {
        String[] args = {"local"};
        try {
            System.out.println("WORKER: started runnig");
            MainWorkerClass.main(args);
        }catch(Exception e){
            System.out.println("WORKER: Thread interrupted..."+e);
        }
    }
}
