import java.io.*;
import java.util.*;

/**
 * Created by y50006079 on 2019/6/27.
 */
public class Main {
    //用来存储连接失败的IP地址
    public static List<String> connectionFailList = new ArrayList<>();              //存放每次连接失败的Ip集合
    public static List<String> errorInformationList = new ArrayList<>();            //存放每次能连通但无法连接上的集合
    public static Map<String, String> IpAndComputerNameCache = new HashMap<String, String>();


    public static void main(String[] args) throws IOException {
        int atCacheRound = 1;                 //  当前缓存回合数
        int atStaticalRound = 1;

        while (true) {
            setMapping();
            setDirDate();
            FileInputStream fir = new FileInputStream(ConstantParameter.IpInformation);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fir));
            String line = null;
            System.out.println(new Date().toString() + "----------------该轮查询开始----------------");
            //用来存储所有线程
            List<Thread> threads = new ArrayList<Thread>();
            //每有一个IP开一个线程去查询
            try {
                while ((line = bufferedReader.readLine()) != null) {
                    String[] strNums = line.split("[ ]{1,}");
                    if (strNums.length < 3 || !strNums[0].matches("([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}")) {
                        System.out.println(line + " Ip信息有误！");
                        continue;
                    }
                    if (strNums.length > 3) {                                                      //  strNums[3]存放错误信息，有误则strNums[3]不存在，存在则有误，不查询该行
                        System.out.println(strNums[0] + ": password error!不再查询该Ip！检查账号密码并删除错误信息后方可重连");
                        continue;
                    }
                    //启动多线程
                    ThreadRunnable task = new ThreadRunnable(new IpCount(strNums[0], strNums[1], strNums[2]));
                    Thread th = new Thread(task, strNums[0]);
                    th.start();
                    threads.add(th);
                }
                for (Thread list : threads) {
                    list.join();                                               //// TODO: 2019/7/17 改变了值
                }
                System.out.println("所有结果查询完毕");
                bufferedReader.close();
                fir.close();
                //recordFailConnection();
                connectionFailList.clear();
                errorInformationList.clear();

                //到达回合数清除缓存
                if (atCacheRound++ % (ConstantParameter.CacheLiveRound + 1) == 0) {
                    IpAndComputerNameCache.clear();
                    atCacheRound = 1;
                    System.out.println("缓存清除！");
                }
                if (atStaticalRound++ % (ConstantParameter.StatisticalRound) == 0) {
                    atStaticalRound = 1;
                    System.out.println("开始自动统计！");//// TODO: 2019/7/9
                    Util.statistics();
                }
                System.out.println(new Date().toString() + "--------------该轮查询执行完毕--------------");
                System.out.println(new Date().toString() + "--------------所有结果查看文件--------------\n");
                Thread.currentThread().sleep((int) ConstantParameter.QUERYINTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //根据mapping文件将计算机名和相应人物的映射对存在内存里，方便查找
    private static void setMapping() throws IOException {
        if (!new File(ConstantParameter.ComputerNameAndUserMappingPath).exists()) {
            return;
        }
        FileInputStream fileInputStream = new FileInputStream(ConstantParameter.ComputerNameAndUserMappingPath);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream, "GBK"));

        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            String[] temp = line.split("[ ]{1,}");
            if (temp.length != 2) {
                System.out.println("error:" + line);
                continue;
            }
            ConstantParameter.ComputerNameAndUserMapping.put(temp[0], temp[1]);
        }
        fileInputStream.close();
        bufferedReader.close();
    }

    //所有线程第一步执行完后，根据connectionFailList输出连接失败的Ip地址到相应文档(总文件)
    public static void recordFailConnection() {
        StringBuilder msg = new StringBuilder();                                                //采用msg拼接信息
        msg.append(new Date().toString() + ":");
        Main.connectionFailList = IpSort.IpSetSort(Main.connectionFailList);                    //对输出排序
        for (String s : Main.connectionFailList) {                                              //该循环将所有IP拼接
            msg.append("<" + s + ">");
        }
        msg.append("\n\r");
        Util.record(ConstantParameter.ConnectionFailListFolder, ConstantParameter.ConnectionFailListfilename, msg.toString());
    }

    //更新文件夹日期
    public static void setDirDate() {
        ConstantParameter.UserIpPath = "log/UserIpLog_01/" + Util.getDate() + "/";
        ConstantParameter.UserNamePath = "log/UserNameLog_03/" + Util.getDate() + "/";
        ConstantParameter.RemoteComputerPath = "log/RemoteComputerLog_02/" + Util.getDate() + "/";
    }
}
