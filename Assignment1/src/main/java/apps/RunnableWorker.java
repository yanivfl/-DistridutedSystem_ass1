package apps;

public class RunnableWorker implements Runnable{
    @Override
    public void run() {
        String[] args = new String[0];
        try {
            Constants.printDEBUG("Worker: started running");
            MainWorkerClass.main(args);
        }catch(Exception e){
            Constants.printDEBUG("Worker Thread interrupted..."+e);
            e.printStackTrace();
        }
    }
}
