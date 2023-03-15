package test;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.gpu.*;
import org.cloudbus.cloudsim.gpu.allocation.VideoCardAllocationPolicy;
import org.cloudbus.cloudsim.gpu.allocation.VideoCardAllocationPolicyDepthFirst;
import org.cloudbus.cloudsim.gpu.hardware_assisted.grid.GridVgpuSchedulerFairShareEx;
import org.cloudbus.cloudsim.gpu.hardware_assisted.grid.GridVgpuTags;
import org.cloudbus.cloudsim.gpu.hardware_assisted.grid.GridVideoCardTags;
import org.cloudbus.cloudsim.gpu.performance.models.PerformanceModelGpuNull;
import org.cloudbus.cloudsim.gpu.provisioners.GpuBwProvisionerShared;
import org.cloudbus.cloudsim.gpu.provisioners.GpuGddramProvisionerSimple;
import org.cloudbus.cloudsim.gpu.provisioners.VideoCardBwProvisioner;
import org.cloudbus.cloudsim.gpu.provisioners.VideoCardBwProvisionerShared;
import org.cloudbus.cloudsim.gpu.selection.PgpuSelectionPolicy;
import org.cloudbus.cloudsim.gpu.selection.PgpuSelectionPolicyDepthFirst;
import org.cloudbus.cloudsim.lists.VmList;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import test1.Processors1;
import de.vandermeer.asciitable.AsciiTable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class TaskScheduler3 {
    private static List<GpuCloudlet> cloudletList;

    private static List<GpuVm> vmList;
    /*
     * mapping: represents the mapping of cloudlets to vm based on MultiSwarmPSO mapping2: represents
     * the mapping of cloudlets to vm based on Random Scheduling resultcost: stores the cost of
     * cloudlet execution based on the mappings other parameters are defined in FitnessFunction
     * 映射：表示cloudlets到基于MultiWarmPSO的vm的映射   mapping2：表示基于随机调度的Cloudlet到vm的映射   resultcost：根据FitnessFunction中定义的映射存储cloudlet执行的成本
     */
    public static double mapping[];
    public static double[][] executiontimematrix;  //执行时间矩阵
    public static double[][] communicationtimematrix;  //通信时间矩阵
    public static double[][] datatransfermatrix;  //数据传输矩阵
    public static double[][] taskoutputfilematrix;   //任务输出文件矩阵
    public static double[][] commcost;  //通信成本
    public static double[] mapping2 = new double[Constants3.NoOfTasks];
    public static int depgraph[][] = new int[Constants3.NoOfTasks][Constants3.NoOfTasks];//分布图

    public static double[] resultcost = new double[1];

    /**
     * Creates main() to run this example
     * 创建main()来运行此示例
     */
    public double[] getPSOMapping() {
        return mapping;
    }

    /*
     * This function is used for the simulation of Cloud Scenarios using CloudSim.
     * 该功能用于使用CloudSim模拟云场景。
     */

    public static double[] func(int[] tasklength, int[] outputfilesize, int[] mips, double[] execcost,
                                double[] waitcost, int[][] graph, int[] pesNumber) throws Exception {
        /*
         * Depgraph denotes that a task requires output files from which tasks
         * Depgraph表示任务需要输出文件，这些文件来自
         */
        for (int j = 0; j < Constants3.NoOfTasks; j++) {
            for (int k = 0; k < Constants3.NoOfTasks; k++) {
                depgraph[k][j] = graph[j][k];
            }
        }
        /*
         * Run the PSO to obtain the mapping of cloudlets to VM
         * 运行PSO以获得cloudlets到VM的映射
         */
        PSO3 PSOScheduler = new PSO3(tasklength, outputfilesize, mips, execcost, waitcost, graph);   //得到粒子的速度和位置
        //得到最好的位置
        mapping = PSOScheduler.run();
        //该矩阵表示在vm上运行cloudlet所需的执行时间
        executiontimematrix = PSOScheduler.getexecutiontimematrix();
        //该矩阵表示输出文件从一个任务到另一个任务的通信时间
        communicationtimematrix = PSOScheduler.getcommunicationtimematrix();
        //这个矩阵表示从一个cloudlet到另一个cloudlet的数据传输速度
        datatransfermatrix = PSOScheduler.getdatatransfermatrix();
        //这是单位时间内从一个任务到另一个任务的结果的通信成本
        commcost = PSOScheduler.getcommcost();
        //该矩阵表示从一个cloudlet传输到另一个cloudlet的数据量
        taskoutputfilematrix = PSOScheduler.gettaskoutputfilematrix();
        Log.printLine("Le Gia Huy (1910202) - Ngo Le Quoc Dung (1910101)");
        //正在启动TaskScheduler，其中有一个具有不同虚拟机的数据中心。。。
        Log.printLine(" Starting TaskScheduler having 1 datacenter with different VMs...");


        try {
            // First step: Initialize the CloudSim package. It should be called
            // before creating any entities.
            int num_user = 1; // number of cloud users
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false; // mean trace events

            // Initialize the CloudSim library
            CloudSim.init(num_user, calendar, trace_flag);

            // Second step: Create Datacenters
            // Datacenters are the resource providers in CloudSim. We need at list one of them to run a
            // CloudSim simulation
            @SuppressWarnings("unused")
            GpuDatacenter datacenter0 = createDatacenter("Datacenter_0");

            // Third step: Create Broker
            MyDataCenterBroker3 broker = createBroker();//???没有改
            //MyDataCenterBroker broker = createBroker();
            int brokerId = broker.getId();


            // //submit vm list to the broker
            vmList = createVm(mips, brokerId);
            broker.submitVmList(vmList);

            // Fifth step: Create two Cloudlets
            cloudletList = new ArrayList<GpuCloudlet>();

            cloudletList = createCloudLets(tasklength, pesNumber, outputfilesize, brokerId);
            // submit cloudlet list to the broker
            broker.submitCloudletList(cloudletList);
            //延迟
            double delay[] = new double[Constants3.NoOfTasks];
            delay[0] = 0;
            for (int i = 0; i < Constants3.NoOfTasks; i++) {
                for (int j = i + 1; j < Constants3.NoOfTasks; j++) {
                    if (taskoutputfilematrix[i][j] != 0) {
                        delay[j] = Math.max(delay[j], delay[i] + executiontimematrix[i][(int) mapping[i]]
                                + communicationtimematrix[i][j]);
                    }
                }
            }

            broker.submitMapping(mapping);
            broker.submitDelay(delay);
            System.out.println("+++++++++++++++++++++++++++++++++++++++++++");
            for (int i=0;i<Constants3.NoOfTasks;i++){
                System.out.println(mapping[i]);
            }


            // Sixth step: Starts the simulation
            CloudSim.startSimulation();

            // Final step: Print results when simulation is over
            List<Cloudlet> newList = broker.getCloudletReceivedList();



            CloudSim.stopSimulation();

            printCloudletList(newList);
//            printVmList(vmList);

            resultcost[0] = PSOScheduler.printBestFitness();

            Log.printLine("Simulation of Task Scheduler using PSO is finished!");
//      }
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }
        return resultcost;
    }

//    private static void printVmList(List<Vm> newvmList) {
//        int size = newvmList.size();
//        Vm vm;
//        System.out.println("==========虚拟机mips输出==========");
//        System.out.println(size);
//        for (int i=0;i<size;i++){
//
//        }
//        vm = newvmList.get(0);
//        System.out.println("获取当前分配的mips:"+vm.getCurrentAllocatedMips());
////        System.out.println("获取当前请求的mips:"+vm.getCurrentRequestedMips());
//        System.out.println("获取所有虚拟pe中当前请求的最大mips:"+vm.getCurrentRequestedMaxMips());
//        System.out.println("获取当前请求的总mips:"+vm.getCurrentRequestedTotalMips());
//
//    }

    private static GpuDatacenter createDatacenter(String name) {
        // Here are the steps needed to create a PowerDatacenter:

        // 我们需要创建一个列表来存储我们的机器
        List<GpuHost> hostList = new ArrayList<GpuHost>();
        // 主机显卡数
        int numVideoCards = Constants3.DUAL_INTEL_XEON_E5_2620_V3_NUM_VIDEO_CARDS;
        // 手持显卡
        List<VideoCard> videoCards = new ArrayList<VideoCard>(numVideoCards);
        for (int videoCardId =0;videoCardId<numVideoCards;videoCardId++){
            List<Pgpu> pgpus = new ArrayList<Pgpu>();
            //添加NVIDIA K1卡
            double mips = GridVideoCardTags.NVIDIA_K1_CARD_PE_MIPS;
            int gddram = GridVideoCardTags.NVIDIA_K1_CARD_GPU_MEM;
            long bw = GridVideoCardTags.NVIDIA_K1_CARD_BW_PER_BUS;
            for (int pgpuId = 0;pgpuId<GridVideoCardTags.NVIDIA_K1_CARD_GPUS;pgpuId++){
                List<Pe> pes = new ArrayList<Pe>();
                for (int peId = 0;peId<GridVideoCardTags.NVIDIA_K1_CARD_GPU_PES;peId++){
                    pes.add(new Pe(peId,new PeProvisionerSimple(mips)));
                }
                pgpus.add(new Pgpu(pgpuId, GridVideoCardTags.NVIDIA_K1_GPU_TYPE, pes,
                        new GpuGddramProvisionerSimple(gddram), new GpuBwProvisionerShared(bw)));
            }
            // Pgpu选择策略
            PgpuSelectionPolicy pgpuSelectionPolicy = new PgpuSelectionPolicyDepthFirst();
            // Vgpu调度器
            VgpuScheduler vgpuScheduler = new GridVgpuSchedulerFairShareEx(GridVideoCardTags.NVIDIA_K1_CARD, pgpus,
                    pgpuSelectionPolicy, new PerformanceModelGpuNull(), GridVideoCardTags.K1_VGPUS);
            // PCI Express Bus Bw Provisioner
            VideoCardBwProvisioner videoCardBwProvisioner = new VideoCardBwProvisionerShared(BusTags.PCI_E_3_X16_BW);
            // 创建一个显卡
            VideoCard videoCard = new VideoCard(videoCardId, GridVideoCardTags.NVIDIA_K1_CARD, vgpuScheduler,
                    videoCardBwProvisioner);
            videoCards.add(videoCard);
        }
        // 2. create hosts, where Every Machine contains one or more PEs or CPUs/Cores

        // ((( Host 1
        // )))--------------------------------------------------------------------------------------------------------
        // 一台机器包含一个或多个pe或cpu /核。
        List<Pe> Host_1_peList = new ArrayList<Pe>();

        // 获取所选处理器的MIPS值
        int Host_1_mips = Processors1.Intel.Core_2_Extreme_X6800.mips;
        // get processor's number of cores
        int Host_1_cores = Processors1.Intel.Core_2_Extreme_X6800.cores;

        // 创建pe并将其添加到列表中。
        for (int i = 0; i < Host_1_cores; i++) {
            // mips/cores => mips值是所有核的累积值，因此我们将mips值分配给所有核
            // of the cores
            Host_1_peList.add(new Pe(i, new PeProvisionerSimple(Host_1_mips / Host_1_cores))); // need to
            // store Pe
            // id and
            // MIPS
            // Rating
        }

        // 4. Create Host with its id and list of PEs and add them to the list of machines
        int host_1_ID = 0;
        int host_1_ram = 20480; // host memory (MB)
        long host_1_storage = 1048570; // host storage in MBs
        int host_1_bw = 102400; // bandwidth in MB/s
        // 显卡选择策略
        VideoCardAllocationPolicy videoCardAllocationPolicy = new VideoCardAllocationPolicyDepthFirst(videoCards);
        GpuHost newHost1 = new GpuHost(host_1_ID,GpuHostTags.DUAL_INTEL_XEON_E5_2620_V3,new RamProvisionerSimple(host_1_ram),
                new BwProvisionerSimple(host_1_bw),host_1_storage, host_1_storage,Host_1_peList,new VmSchedulerTimeShared(Host_1_peList),videoCardAllocationPolicy);

        hostList.add(newHost1);
        System.out.println("第一个主机添加成功！！！");
        // ((( \Host 1
        // )))--------------------------------------------------------------------------------------------------------

        // ((( Host 2
        // )))--------------------------------------------------------------------------------------------------------

        List<Pe> Host_2_peList = new ArrayList<Pe>();

        // get the mips value of the selected processor
        int Host_2_mips = Processors1.Intel.Core_i7_Extreme_Edition_3960X.mips;
        // get processor's number of cores
        int Host_2_cores = Processors1.Intel.Core_i7_Extreme_Edition_3960X.cores;

        // 3. Create PEs and add these into a list.
        for (int i = 0; i < Host_2_cores; i++) {
            // mips/cores => MIPS value is cumulative for all cores so we divide the MIPS value among all
            // of the cores
            Host_2_peList.add(new Pe(i, new PeProvisionerSimple(Host_2_mips / Host_2_cores))); // need to
            // store Pe
            // id and
            // MIPS
            // Rating
        }

        // 4. Create Host with its id and list of PEs and add them to the list of machines
        int host_2_id = 1;
        int host_2_ram = 20480; // host memory (MB)
        long host_2_storage = 1048570; // host storage in MBs
        int host_2_bw = 10240; // bandwidth in MB/s
        GpuHost newHost2 = new GpuHost(host_2_id,GpuHostTags.DUAL_INTEL_XEON_E5_2620_V3,new RamProvisionerSimple(host_2_ram),
                new BwProvisionerSimple(host_2_bw),host_2_storage, host_2_storage, Host_2_peList,new VmSchedulerTimeShared(Host_2_peList),videoCardAllocationPolicy);

        hostList.add(newHost2);
        System.out.println("第二个主机添加成功！！！");

        // ((( \Host 2
        // )))--------------------------------------------------------------------------------------------------------

        // 5. Create a DatacenterCharacteristics object that stores the
        // properties of a data center: architecture, OS, list of
        // Machines, allocation policy: time- or space-shared, time zone
        // and its price (G$/Pe time unit).
        String arch = "x86"; // system architecture
        String os = "Linux"; // operating system
        String vmm = "Xen";
        double time_zone = 10.0; // time zone this resource located
        double cost = 3.0; // the cost of using processing in this resource
        double costPerMem = 0.05; // the cost of using memory in this resource
        double costPerStorage = 0.001; // the cost of using storage in this resource
        double costPerBw = 0.0; // the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are not adding SAN devices by
        // now

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(arch, os, vmm,
                hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);

        // 6. Finally, we need to create a PowerDatacenter object.
        GpuDatacenter datacenter = null;
        try {
            datacenter = new GpuDatacenter(name, characteristics, new GpuVmAllocationPolicySimple(hostList),
                    storageList, 0);//???
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }

    private static MyDataCenterBroker3 createBroker() {
        MyDataCenterBroker3 broker = null;
        try {
            broker = new MyDataCenterBroker3("Broker");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return broker;
    }

//    private static GpuDatacenterBroker createBroker(String name) {
//        GpuDatacenterBroker broker = null;
//        try {
//            broker = new GpuDatacenterBroker(name);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//        return broker;
//    }

    private static List<GpuVm> createVm(int[] mips, int brokerId) {
        // 创建一个虚拟机
        List<GpuVm> vmlist = new ArrayList<GpuVm>();

        // VM description
        int vmid = 0;
        long size = 10000; // image size (MB)
        int ram = 256; // vm memory (MB)
        long bw = 1000;
        // int pesNumber = 1; //number of cpus
        int pesNumber = 500000;
        String vmm = "Xen"; // VMM name
        //创建GpuTask调度器
        GpuTaskSchedulerLeftover gpuTaskScheduler = new GpuTaskSchedulerLeftover();
        // 创建了虚拟机
        for (int i = 0; i < Constants3.NoOfVMs; i++) {
            GpuVm vm =new GpuVm(vmid,brokerId,mips[i],pesNumber,ram,bw,size,vmm,"Custom",
                    new GpuCloudletSchedulerTimeShared());
            vmid++;
            //将虚拟机添加到vmList中
            vmlist.add(vm);

            //创建一个Vgpu
            Vgpu vgpu = GridVgpuTags.getK180Q(i,gpuTaskScheduler);
            vm.setVgpu(vgpu);


        }
        return vmlist;
    }

    private static List<GpuCloudlet> createCloudLets(int[] tasklength, int[] pesNumber, int[] outputSize,
                                                  int brokerId) {
        List<GpuCloudlet> cloudletList = new ArrayList<GpuCloudlet>();
        //Cloudlet任务属性
        long fileSize = 300;
        UtilizationModel cpuUtilizationModel = new UtilizationModelFull();
        UtilizationModel ramUtilizationModel = new UtilizationModelFull();
        UtilizationModel bwUtilizationModel = new UtilizationModelFull();
        //Gpu任务属性  用的固定值，以后改
        long taskLength = (long)(GridVideoCardTags.NVIDIA_K1_CARD_PE_MIPS * 150);
        long taskInputSize = 128;
        long taskOutputSize = 128;
        long requestedGddramSize = 4 * 1024;
        int numberOfBlocks = 2;
        UtilizationModel gpuUtilizationModel = new UtilizationModelFull();
        UtilizationModel gddramUtilizationModel = new UtilizationModelFull();
        UtilizationModel gddramBwUtilizationModel = new UtilizationModelFull();
        for (int id = 0; id < Constants3.NoOfTasks; id++) {

            GpuTask gpuTask = new GpuTask(id,taskLength,numberOfBlocks,taskInputSize,taskOutputSize,
                    requestedGddramSize,gpuUtilizationModel,gddramUtilizationModel,gddramBwUtilizationModel);

            GpuCloudlet gpuCloudlet = new GpuCloudlet(id,tasklength[id],pesNumber[id],fileSize,outputSize[id],
                    cpuUtilizationModel,ramUtilizationModel,bwUtilizationModel,gpuTask,false);

            gpuCloudlet.setUserId(brokerId);
            cloudletList.add(gpuCloudlet);
        }
        return cloudletList;
    }

    private static void printCloudletList(List<Cloudlet> list) {
        int size = list.size();

        Cloudlet cloudlet;
        double exetime = 0;


        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID"
                + indent + "Time" + indent + "Start Time" + indent + "Finish Time");

        DecimalFormat dft = new DecimalFormat("###.##");
        for (GpuCloudlet gpuCloudlet : (List<GpuCloudlet>)(List<?>)list){
            //Cloudlet
            AsciiTable at = new AsciiTable();
            at.addRule();
            at.addRow("Cloudlet ID", "Status", "Datacenter ID", "VM ID", "Time", "Start Time", "Finish Time");
            at.addRule();
            if (gpuCloudlet.getStatus() ==Cloudlet.SUCCESS){
                at.addRow(gpuCloudlet.getCloudletId(),"SUCCESS",gpuCloudlet.getResourceId(),gpuCloudlet.getVmId(),
                        dft.format(gpuCloudlet.getActualCPUTime()).toString(),
                        dft.format(gpuCloudlet.getExecStartTime()).toString(),
                        dft.format(gpuCloudlet.getFinishTime()).toString());
                at.addRule();
            }
            //GpuTask
            GpuTask gpuTask = gpuCloudlet.getGpuTask();
            at.addRow("Task ID", "Cloudlet ID", "Status", "vGPU Profile", "Time", "Start Time", "Finish Time");
            at.addRule();
            if (gpuTask.getTaskStatus() == GpuTask.SUCCESS){
                at.addRow(gpuTask.getTaskId(),gpuTask.getCloudlet().getCloudletId(),"SUCCESS",
                        ((GpuVm) VmList.getById(vmList,gpuTask.getCloudlet().getVmId())).getVgpu().getType(),
                        dft.format(gpuTask.getActualGPUTime()).toString(),
                        dft.format(gpuTask.getExecStartTime()).toString(),
                        dft.format(gpuTask.getFinishTime()).toString());
                at.addRule();
            }
            Log.printLine(at.render());
            exetime += gpuCloudlet.getActualCPUTime();
        }
//        for (int i = 0; i < size; i++) {
//            cloudlet = list.get(i);
//            Log.print(indent + cloudlet.getCloudletId() + indent + indent);
//
//            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
//                Log.print("SUCCESS");
//
//                Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + indent
//                        + cloudlet.getVmId() + indent + indent + dft.format(cloudlet.getActualCPUTime())
//                        + indent + indent + dft.format(cloudlet.getExecStartTime()) + indent + indent
//                        + dft.format(cloudlet.getFinishTime()));
//
//
//            }
//            exetime += cloudlet.getActualCPUTime();
//        }


        Log.printLine("+++++++"+dft.format(exetime));
    }

}
