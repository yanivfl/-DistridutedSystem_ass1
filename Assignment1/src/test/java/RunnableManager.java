import apps.Manager;

public class RunnableManager implements Runnable{
    @Override
    public void run() {
        String[] args = {"local"};
        try {
            Manager.main(args);
        }catch(Exception e){
            System.out.println("Thread interrupted..."+e);
        }
    }
}
