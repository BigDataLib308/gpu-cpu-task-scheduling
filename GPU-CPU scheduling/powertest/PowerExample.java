//package powertest;
//
//import org.cloudbus.cloudsim.*;
//import org.cloudbus.cloudsim.core.CloudSim;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.concurrent.ThreadPoolExecutor;
//
//public class PowerExample {
//    private static final int SCHEDULING_INTERVAL = 10;
//    private static final int HOSTS = 1;
//    private static final int HOST_PES = 4;
//
//    private static final int VMS = 1;
//    private static final int VM_PES = 2;
//
//    private static final int CLOUDLETS = 10;
//    private static final int CLOUDLET_PES = 2;
//    private static final int CLOUDLET_LENGTH = 50000;
//
//    /**
//     * Defines the power a Host uses, even if it's idle (in Watts).
//     */
//    private static final double STATIC_POWER = 35;
//
//    /**
//     * The max power a Host uses (in Watts).
//     */
//    private static final int MAX_POWER = 50;
//
//    private final CloudSim simulation;
//    private DatacenterBroker broker0;
//    private List<Vm> vmList;
//    private List<Cloudlet> cloudletList;
//    private Datacenter datacenter0;
//    private final List<Host> hostList;
//
//    public static void main(String[] args) {
//        new PowerExample();
//    }
//
//    private PowerExample(){
//
//        simulation = new CloudSim();
//        hostList = new ArrayList<>(HOSTS);
//        datacenter0 = createDatacenterSimple();
//    }
//
//    private Datacenter createDatacenterSimple() {
//        for(int i = 0; i < HOSTS; i++) {
//            Host host = createPowerHost(i);
//            hostList.add(host);
//        }
//        final Datacenter dc = new DatacenterSimple(simulation, hostList, new VmAllocationPolicySimple());
//        dc.setSchedulingInterval(SCHEDULING_INTERVAL);
//        return dc;
//   }
//    ThreadPoolExecutor
//    private Host createPowerHost(int i) {
//    }
//
//
//}
