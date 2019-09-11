import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by y50006079 on 2019/7/2.
 */

public class ThreadRunnable implements Runnable {

    private IpCount ipCount;

    public ThreadRunnable(IpCount ipCount) {
        this.ipCount = ipCount;
    }

    public static Map<String, Integer> passwordErrorTime = new HashMap<>();

    public void run() {
        try {
            //存放IP连接失败的Map  {Ip地址,连接失败次数}
            String ip = ipCount.getIp();
            if (passwordErrorTime.containsKey(ip)) {
                if (passwordErrorTime.get(ip) >= ConstantParameter.PasswordErrorTryTime) {
                    Util.setIpInfomationPasswordError(ip);
                }
            } else {
                passwordErrorTime.put(ip, 0);
            }

            List<String> usersList = new Telnet(ipCount).getUsers();
            if (usersList != null) {
                Cmd cmd = new Cmd();
                String RemoteComputerName = cmd.getRemoteComputerName(usersList, ip);

                RealUser realUser = new RealUser(RemoteComputerName, ip);
                realUser.recordUserName();
            }
        } catch (IOException e) {
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}