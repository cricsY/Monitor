/**
 * Created by y50006079 on 2019/6/27.
 */

import java.io.*;
import java.util.*;

import org.apache.commons.net.telnet.TelnetClient;

public class Telnet {
    private TelnetClient telnetClient;                                           //指明Telnet终端类型，否则返回来的数据中文会乱码
    private InputStream inputStream;                                             //读取命令的流
    private PrintStream pStream;                                                 //写命令的流
    private double sessionTimeout = ConstantParameter.TelnetSessionTimeOut;     //超时时间
    private double connectTimeout = ConstantParameter.TelnetConnectTimeout;     //连接超时时间
    private BufferedReader in;                                                    //+++++++++++++++++++++
    private Set<String> users = new HashSet<String>();
    private IpCount ipCount;
    private String MyIp;
    private String errorInfo = null;                                               // 相关错误信息，紧当flagOfStatus==3时有效
    private List<String> modelList = null;


    //当前重试次数
    private int flagOfRetry = 0;
    //取两次发送消息之间的等待时间存为本地值
    private double LocalSendMessageWaitTime = ConstantParameter.SendMessageWaitTime;
    //取接收消息的等待时间为本地值
    private double LocalGetMessageWaitTime = ConstantParameter.GetMessageWaitTime;

    public Telnet(IpCount ipCount) {
        this.ipCount = ipCount;
    }

    //查询结果的标志位:  1代表查询成功 ;  2代表无法连接 ;3代表密码错误
    public int flagOfStatus = 1;

    //整个流程为:先建立连接，远端发送请求Username的命令，发送Username，远端请求Password，发送Password，远端反馈信息，发送"dis users"指令，接受数据并解析
    public List<String> getUsers() throws IOException, InterruptedException {
        try {
            telnetClient = new TelnetClient("vt100");
            telnetClient.setDefaultTimeout((int) sessionTimeout);                                                               //设置会话超时时间
            telnetClient.setConnectTimeout((int) connectTimeout);
            while (!telnetClient.isConnected() && flagOfRetry <= ConstantParameter.TelnetRetryRounds) {
                try {
                    telnetClient.connect(ipCount.getIp());                                                                      //建立一个连接,目标IP地址以及端口号,连接不上跳到异常处理
                } catch (IOException e) {
                }
                if (flagOfRetry != 0) {
                    flagOfRetry++;
                    System.out.println(ipCount.getIp() + " 连接超时,尝试第" + flagOfRetry + "次连接...");
                    connectTimeout = ConstantParameter.TelnetConnectTimeout * ConstantParameter.RetryMultiple;                  //增加连接时间
                } else {
                    flagOfRetry++;
                }
            }
            if (!telnetClient.isConnected()) {
                System.out.println(ipCount.getIp() + " 无法连接");
                Main.connectionFailList.add(ipCount.getIp());
                flagOfStatus = 2;
                return null;
            }
            inputStream = telnetClient.getInputStream();                                //输入流
            pStream = new PrintStream(telnetClient.getOutputStream());                  //输出流
            in = new BufferedReader(new InputStreamReader(inputStream));                //+++++
            Thread.sleep((int) LocalSendMessageWaitTime);
            sendMessage(ipCount.getUsername());                                           //发送Username
            Thread.sleep((int) LocalSendMessageWaitTime);
            sendMessage(ipCount.getPassword());                                          //发送password
            Thread.sleep((int) LocalSendMessageWaitTime);
            sendMessage("N");                                                            //对于某些IP比如"8.32.149.48"多出的指令
            Thread.sleep((int) LocalSendMessageWaitTime);
            sendMessage("dis users");
            Thread.sleep((int) LocalGetMessageWaitTime);
            getMessage();                                                                //接收信息
        } catch (IOException e) {                                                      //以time out的方式抛出异常来退出telnet连接，下策，但暂未找到更好的办法
        } finally {
            if (users.contains(MyIp)) {
                users.remove(MyIp);
            }
            telnetClient.disconnect();
            switch (flagOfStatus) {
                case 1: {
                    modelList = IpSort.IpSetSort(new ArrayList<>(users));                 //set去重后转list进行排序
                    recordStatusOfSuccess(modelList);
                    ThreadRunnable.passwordErrorTime.put(ipCount.getIp(),0);
                    break;
                }
                case 2: {
                    recordStatusOfPingFail();
                    modelList=null;
                    break;
                }
                case 3: {
                    recordStatusOfError();
                    modelList=null;
                    ThreadRunnable.passwordErrorTime.put(ipCount.getIp(),ThreadRunnable.passwordErrorTime.get(ipCount.getIp())+1);
                    break;
                }
            }
        }
        return modelList;
    }

    private void getMessage() throws IOException {
        StringBuffer sBuffer = new StringBuffer(300);
        String str;
        while ((str = in.readLine()) != null) {
            getIp(str);                                              // 先捕获错误再提取IP
            //System.out.println(str);                              //输出接收到的信息
        }
    }

    private void sendMessage(String msg) {
        //System.out.println("上传:" + msg);                       //输出上传的信息，方便调试
        pStream.println(msg);                                    //写命令
        pStream.flush();                                         //将命令发送到telnet Server
    }

    private void getIp(String str) throws IOException {
        if (str.startsWith("Error") && !str.startsWith("Error: Unrecognized command found")) {
            flagOfStatus = 3;
            Main.errorInformationList.add(ipCount.getIp());
            errorInfo = str;
            return;
        }
        String strNums[] = str.split("[ ]{1,}");
        for (String s : strNums) {
            if (s.matches("([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}")) {
                users.add(s);
            }
            if (strNums[0].equals("+") && s.matches("([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}")) {
                MyIp = s;
            }
        }
    }

    //生成记录在线IP的log
    private void recordStatusOfSuccess(List<String> usersSet) throws IOException {
        StringBuilder msg = new StringBuilder();                                    //采用msg拼接信息
        msg.append(new Date().toString() + ":" + usersSet.size() + " users!");
        if (Main.connectionFailList.contains(ipCount.getIp())) {
            msg.append(" Error: Ping fail！");
        } else {
            for (String s : usersSet) {
                msg.append(" <" + s + ">");
            }
        }
        msg.append("\r\n");
        Util.record(ConstantParameter.UserIpPath, ipCount.getIp() + ".xml", msg.toString());
    }

    private void recordStatusOfPingFail() throws IOException {
        StringBuilder msg = new StringBuilder();                                    //采用msg拼接信息
        msg.append(new Date().toString() + ":0 users! Error: ping fail!\r\n");
        Util.record(ConstantParameter.UserIpPath, ipCount.getIp() + ".xml", msg.toString());
        Util.record(ConstantParameter.RemoteComputerPath, ipCount.getIp() + ".xml", msg.toString());
        Util.record(ConstantParameter.UserNamePath, ipCount.getIp() + ".xml", msg.toString());
    }

    private void recordStatusOfError() throws IOException {
        StringBuilder msg = new StringBuilder();                                    //采用msg拼接信息
        msg.append(new Date().toString() + " " + errorInfo + "\r\n");
        Util.record(ConstantParameter.UserIpPath, ipCount.getIp() + ".xml", msg.toString());
        Util.record(ConstantParameter.RemoteComputerPath, ipCount.getIp() + ".xml", msg.toString());
        Util.record(ConstantParameter.UserNamePath, ipCount.getIp() + ".xml", msg.toString());
    }
}
