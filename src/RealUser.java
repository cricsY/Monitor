import java.util.Date;

/**
 * Created by y50006079 on 2019/7/1.
 */
public class RealUser {
    private String nameStr;
    private String ip;

    public RealUser(String nameStr, String ip) {
        this.nameStr = nameStr;
        this.ip = ip;
    }

    public void recordUserName() {
        //System.out.println("待解析计算机名:" + nameStr);

        if (nameStr == null||nameStr.equals("")) {
            Util.record(ConstantParameter.UserNamePath, ip + ".xml", new Date().toString() + ":0" + " users!\r\n");
            return;
        }

        //文件配置
        StringBuilder msg = new StringBuilder();
        String[] nameNum = nameStr.split("[ ]{1,}");
        msg.append(new Date().toString() + ":" + nameNum.length + " users!");


        //逐个解析计算机名
        for (String str : nameNum) {
            if (str.matches("(.*)[a-zA-Z][0-9]{8}(.*)") && str.endsWith(">")) {                     //云主机、正则表达式匹配倒数第14位为字母，倒数第15位到倒数第6位为数字,用来处理工号类型
                msg.append("<" + str.substring(str.length() - 14, str.length() - 5) + ">");
            } else {                                                                                 //计算机名、用来处理映射类型
                if (str.endsWith(">")) {
                    int a = str.indexOf('<');
                    str = str.substring(0, a);
                }
                String value = ConstantParameter.ComputerNameAndUserMapping.get(str);
                if (value == null) {                                                                 //判断是否在映射文件中找到相应的映射，找到则输出用户名称到日志，未找到则输出<!计算机名称!>到日志
                    msg.append("<!" + str + "!>");
                } else
                    msg.append("<" + value + ">");
            }
        }
        msg.append("\r\n");
        Util.record(ConstantParameter.UserNamePath, ip + ".xml", msg.toString());

    }
}