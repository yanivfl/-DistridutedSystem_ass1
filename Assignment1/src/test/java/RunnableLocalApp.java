import apps.LocalApplication;
import apps.Manager;

public class RunnableLocalApp implements Runnable{
    private String[] args;

    public RunnableLocalApp(String[] args) {
        this.args = args;
    }

    @Override
    public void run() {
        try {
            LocalApplication.main(args);

        }catch(Exception e){
            System.out.println("Local App Thread interrupted..."+e);
            e.printStackTrace();
        }
    }
}
