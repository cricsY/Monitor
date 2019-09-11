import java.util.HashMap;
import java.util.Map;

/**
 * Created by y50006079 on 2019/6/28.
 */
public class ConstantParameter {
    //每次查询的间隔，单位毫秒
    public static double QUERYINTERVAL = 30 * 1000;

    //通过telnet查询的Ip地址存放log的根目录（第一步存储地址根目录）
    public static String UserIpPath = "../log/UserIpLog_01/" + Util.getDate() + "/";
    //真实用户log存放目录（第三步存储地址根目录）
    public static String UserNamePath = "../log/UserNameLog_03/" + Util.getDate() + "/";
    //用户电脑名称存放根目录（第二步存储地址根目录）
    public static String RemoteComputerPath = "../log/RemoteComputerLog_02/" + Util.getDate() + "/";
    //Mapping文件位置
    public static String ComputerNameAndUserMappingPath = "../Mapping.xml";
    //需要监控的Ip地址及密码
    public static String IpInformation = "../IpInformation.xml";
    //存放无法连接的路由器Ip地址的文件夹和文件名
    public static String ConnectionFailListFolder = "../log/";
    public static String ConnectionFailListfilename = "ConnectionFailList.xml";

    //用户与计算机名称的映射hashtable
    public static Map<String, String> ComputerNameAndUserMapping = new HashMap<String, String>();

    //telnet重连次数
    public static int TelnetRetryRounds = 1;
    //telnet每次查询消息等待时间
    public static double SendMessageWaitTime = 150;
    //telnet接收消息的等待时间
    public static double GetMessageWaitTime = 2000;
    //重连时间倍数
    public static double RetryMultiple = 1.5;
    //每次telnet连接后持续时间
    public static double TelnetConnectTimeout = 5 * 1000;
    //每次telnet会话持续时间，该值太大每轮查询耗时较长，该值太小可能查询不全
    public static double TelnetSessionTimeOut = 5 * 1000;
    //计算机名缓存有效回合数
    public static int CacheLiveRound = 5;

    //允许的连续密码错误最大次数
    public static int PasswordErrorTryTime = 5;

    //查询达到该次数时做一次统计
    public static int StatisticalRound = 1;
    //CMD调用允许的最长阻塞时间
    public static int CmdMaxTime = 70*1000;

}