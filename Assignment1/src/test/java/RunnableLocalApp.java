import apps.LocalApplication;
import apps.Manager;

public class RunnableLocalApp implements Runnable{
    @Override
    public void run() {
        String[] args = {"/home/yaniv/workSpace/dsps/reviews/test_json", "output1.html", "10" };
        try {
            LocalApplication.main(args);

        }catch(Exception e){
            System.out.println("Local App Thread interrupted..."+e);
            e.printStackTrace();
        }
    }
}
