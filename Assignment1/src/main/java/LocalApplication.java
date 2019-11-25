import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.Instance;

import java.util.List;

public class LocalApplication {


    public static void main(String[] args) throws Exception {
        AmazonEC2 ec2= EC2Handler.connectEC2();
        List<Instance> myInstances = EC2Handler.launchEC2Instance(ec2);
        if(myInstances != null){
            String instanceIdToTerminate = myInstances.get(0).getInstanceId();
            EC2Handler.terminateEC2Instance(ec2, instanceIdToTerminate);
        }
    }


}
