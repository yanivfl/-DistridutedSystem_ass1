import apps.Constants;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.Instance;
import handlers.EC2Handler;

import java.util.List;

public class EC2Test {

    public static void main(String[] args) throws Exception {

        runInstanceWithTag();
    }

    public static void runInstanceWithTag() throws Exception {
        System.out.println("Test run instances with tags");

        EC2Handler ec2 = new EC2Handler();
        List<Instance> managerInstances = null;
        List<Instance> workersInstances = null;

        try {
            System.out.println("launch 1 manager");
            managerInstances = ec2.launchEC2Instances(1, Constants.INSTANCE_TAG.TAG_MANAGER);

            if (!ec2.isTagExists(Constants.INSTANCE_TAG.TAG_MANAGER))
                throw new Exception("FAIL!!");
            if (ec2.isTagExists(Constants.INSTANCE_TAG.TAG_WORKER))
                throw new Exception("FAIL!!");

            System.out.println("launch 1 workers");
            workersInstances = ec2.launchEC2Instances(1, Constants.INSTANCE_TAG.TAG_WORKER);

            if (!ec2.isTagExists(Constants.INSTANCE_TAG.TAG_WORKER))
                throw new Exception("FAIL!!");
            if (!ec2.isTagExists(Constants.INSTANCE_TAG.TAG_MANAGER))
                throw new Exception("FAIL!!");
        }
        finally {
            System.out.println("terminate all instances");
            terminateInstList(managerInstances, ec2);
            terminateInstList(workersInstances, ec2);
        }
    }

    private static void terminateInstList(List<Instance> list, EC2Handler ec2) {
        if (list != null) {
            for (Instance inst : list) {
                ec2.terminateEC2Instance(inst.getInstanceId());
            }
        }
    }
}
