package test;



import java.util.Random;
import java.util.Scanner;

public class Simulation3 {

    public static void main(String[] args) throws Exception{
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter the Number of times you want to compare results of PSO");
        int n = sc.nextInt();
        double arrres[][] = new double[n][1];
        FileReader3 file = new FileReader3("E:\\研究生\\cloudsim\\PSO-cloudsim-HK202-master01\\dataset.xlsx");
        file.readFile();
        float[] cloudletIntensity = file.getCloudletIntensity();
        for (int i=0;i<n;i++){
            file.readFile();
            int[] tasklength = file.getRunTime();
            int[] pesNumber = file.getPesNumber();//分配的处理器数量
            Random rand = new Random();
            int[] outputfilesize = new int[Constants3.NoOfTasks];
            for (int j=0;j<Constants3.NoOfTasks;j++){
                outputfilesize[j] = rand.nextInt((1000 - 100) + 1) + 100;
            }
            //虚拟机处理能力
            int mips[] = {50,80,20,30,25,60,40,30,30,30,30,30,30,30};
            double execcost[] = {6, 10, 2, 0.5, 0.5, 4.5, 2, 7,7,7,7,7,7,7};
            double waitcost[] = {6, 10, 2, 0.5, 0.5, 4.5, 2, 7,7,7,7,7,7,7};

            int[] precedingJob = file.getPrecedingJob();//PrecedingJob前工作数目
            int[][] graph = new int[Constants3.NoOfTasks][Constants3.NoOfTasks];
            //初始化一个矩阵graph20x20
            for (int j = 0; j < Constants3.NoOfTasks; j++)
                for (int k = 0; k < Constants3.NoOfTasks; k++)
                    graph[j][k] = 0;
            //这是一个搜索矩阵？数据为1的列数代表任务id；行数代表该任务前工作数目
            for (int j = 0; j < Constants3.NoOfTasks; j++) {
                if (precedingJob[j] != -1)
                    graph[precedingJob[j] - 1][j] = 1;
            }
//            for (int j = 0; j < Constants.NoOfTasks; j++){
//                System.out.println();
//                for (int k = 0; k < Constants.NoOfTasks; k++)
//                    System.out.print(graph[j][k]);
//            }




            TaskScheduler3 obj = new TaskScheduler3();
            double ans[] = obj.func(tasklength, outputfilesize, mips, execcost, waitcost, graph,
                    pesNumber);
            arrres[i][0] = ans[0];
            System.out.println("Cost of PSO Based Scheduling:" + ans[0]);
        }
        System.out.println("------------------------------------------------------------------------------------------");
        System.out.println("PSO");
        for (int i = 0; i < n; i++) {
            System.out.println(arrres[i][0]);
        }
    }
}
