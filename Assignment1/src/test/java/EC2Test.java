import apps.Constants;
import com.amazonaws.services.ec2.model.Instance;
import handlers.EC2Handler;

import java.util.LinkedList;
import java.util.List;

public class EC2Test {

    public static void main(String[] args) throws Exception {

        runInstanceWithTag();
    }

    private static void runInstanceWithTag() throws Exception {
        System.out.println("Test run instances with tags");

        EC2Handler ec2 = new EC2Handler(true);
        List<Instance> managerInstances = new LinkedList<>();
        List<Instance> workersInstances = null;

        try {
            System.out.println("launch 1 manager");
            Instance manager = ec2.launchManager_EC2Instance(ec2.getRoleARN(Constants.MANAGER_ROLE));
            if (manager == null) {
                System.out.println("FAIL!! - failed to create a manager");
                return;
            }
            else
                managerInstances.add(manager);

            if (!ec2.isTagExists(Constants.INSTANCE_TAG.MANAGER))
                throw new Exception("FAIL!!");
            if (ec2.isTagExists(Constants.INSTANCE_TAG.WORKER))
                throw new Exception("FAIL!!");

            System.out.println("launch 1 workers");
            workersInstances = ec2.launchWorkers_EC2Instances(1, ec2.getRoleARN(Constants.WORKERS_ROLE));

            if (!ec2.isTagExists(Constants.INSTANCE_TAG.WORKER))
                throw new Exception("FAIL!!");
            if (!ec2.isTagExists(Constants.INSTANCE_TAG.MANAGER))
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
