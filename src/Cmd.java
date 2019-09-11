import java.io.*;
import java.util.*;

/**
 * Created by y50006079 on 2019/7/1.
 */
public class Cmd {

    //暂存所有的计算机名称
    private List<String> computerNameList = new ArrayList<>();

    public String getRemoteComputerName(List<String> Ips, String ip) {
        if (Ips == null) {
            Util.record(ConstantParameter.RemoteComputerPath, ip + ".xml", new Date().toString() + ":0" + " users!");
            return null;
        }
        String returnString = new String();
        StringBuilder msg = new StringBuilder();
        msg.append(new Date().toString() + ":" + Ips.size() + " users!");
        //用来存储所有查询线程
        List<Thread> threads = new ArrayList<>();
        //多线程查询计算机名称
        for (String s : Ips) {
            String temp = Main.IpAndComputerNameCache.get(s);
            if (temp != null) {                                                                 //调试信息
                System.out.println(s + "->" + temp + "缓存命中");                               //调试信息
                computerNameList.add(temp);
            }                                                                                    //调试信息
            if (temp == null) {                                                                  //没有缓存则调用命令行查询
                cmdQueryRunnable runnable = new cmdQueryRunnable(s, Ips);
                Thread thread = new Thread(runnable);
                thread.start();

                threads.add(thread);
            }
        }
        try {
            //同步所有查询线程
            for (Thread s : threads) {
                s.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (String s : computerNameList) {
            returnString = returnString + s.toUpperCase() + " ";                   //转换成大写
            msg.append("<" + s + "> ");
        }

        msg.append("\r\n");
        Util.record(ConstantParameter.RemoteComputerPath, ip + ".xml", msg.toString());
        return returnString;
    }

    private class cmdQueryRunnable implements Runnable {
        private String ipElement;
        private List<String> ipList;

        cmdQueryRunnable(String string, List<String> ipList) {
            ipElement = string;
            this.ipList = ipList;
        }

        @Override
        public void run() {
            try {
                String command = "nbtstat -a " + ipElement;
                String line = null, temp = ipElement, computerName = null;
                Runtime runtime = Runtime.getRuntime();
                Process process = runtime.exec(command);
                InputStream inputStream = process.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                //用来打断卡死进程
                protectRunnable timerRunnable = new protectRunnable(process, Thread.currentThread());
                Thread timerThread = new Thread(timerRunnable);
                timerThread.start();

                while ((line = bufferedReader.readLine()) != null) {
                    if (line.trim().startsWith("---")) {
                        int count = 0;
                        //在该while循环中取得计算机名并赋值给computerName，如果computerName为null，则未查询到计算机名
                        while ((line = bufferedReader.readLine().trim()) != null) {
                            temp = line.split("[ ]{1,}")[0];
                            if (++count > 30 || temp.startsWith("MAC") || temp == null) {
                                System.out.println(ipElement + "未找到");
                                break;
                            }
                            if (temp.startsWith("CHINA")) {
                                continue;
                            }
                            computerName = temp;
                            System.out.println(ipElement + "读取" + count + "行");
                            break;
                        }
                        break;
                    }
                }

                bufferedReader.close();
                inputStreamReader.close();
                inputStream.close();
                //根据不同情况将计算机名或IP地址填入数据
                if (computerName == null) {
                    computerNameList.add(ipElement);
                    Main.IpAndComputerNameCache.put(ipElement, ipElement);
                } else {
                    computerNameList.add(computerName);
                    Main.IpAndComputerNameCache.put(ipElement, computerName);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class protectRunnable implements Runnable {
        private Thread thread;
        private Process process;

        protectRunnable(Process process, Thread thread) {
            this.process = process;
            this.thread = thread;
        }

        @Override
        public void run() {
            try {
                Thread.currentThread().sleep(ConstantParameter.CmdMaxTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (thread.isAlive()) {
                System.out.println(thread.getName() + "强制打断！");
                process.destroy();
            }
        }
    }
}