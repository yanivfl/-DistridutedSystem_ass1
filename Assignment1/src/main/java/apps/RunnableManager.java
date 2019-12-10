package apps;

public class RunnableManager implements Runnable{
    @Override
    public void run() {
        String[] args = new String[0];
        try {
            Constants.printDEBUG("Manager: started running");
            Manager.main(args);
        }catch(Exception e){
            Constants.printDEBUG("Manager Thread interrupted..."+e);
            e.printStackTrace();
        }
    }
}
