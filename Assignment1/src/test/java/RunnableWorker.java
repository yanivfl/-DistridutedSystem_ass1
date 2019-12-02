import apps.MainWorkerClass;

public class RunnableWorker implements Runnable{
    @Override
    public void run() {
        String[] args = new String[0];
        try {
            MainWorkerClass.main(args);
        }catch(Exception e){
            System.out.println("Thread interrupted..."+e);
        }
    }
}
