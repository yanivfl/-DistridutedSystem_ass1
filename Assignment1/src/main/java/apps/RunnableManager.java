package apps;

public class RunnableManager implements Runnable{
    @Override
    public void run() {
        String[] args = new String[0];
        try {
            System.out.println("Manager: started running");
            Manager.main(args);
        }catch(Exception e){
            System.out.println("Manager Thread interrupted..."+e);
            e.printStackTrace();
        }
    }
}
