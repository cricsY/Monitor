/**
 * Created by y50006079 on 2019/6/27.
 */
public class IpCount {
    private String Ip;
    private int port;
    private String Username;
    private String Password;
    private boolean Status;

    //不带端口号port的构造器，默认为23
    public IpCount(String Ip,String Username, String Password) {
        this.Ip = Ip;
        this.port = 23;
        this.Username = Username;
        this.Password = Password;
    }

    //带端口号port的构造器
    public IpCount(String Ip, int port, String Username, String Password) {
        this.Ip = Ip;
        this.port = port;
        this.Username = Username;
        this.Password = Password;
    }

    public String getIp() {
        return Ip;
    }

    public void setIp(String ip) {
        Ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }
}