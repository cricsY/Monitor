import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Util {
    public static void record(String path, String filename, String msg) {
        File folder = new File(path), file = new File(path + "/" + filename);
        try {
            if (!folder.exists()) {
                folder.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            BufferedWriter out = new BufferedWriter (new OutputStreamWriter (new FileOutputStream (file,true),"UTF-8"));
            out.write(msg);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getDate() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }

    public static synchronized void setIpInfomationPasswordError(String ip) {
        try {
            RandomAccessFile file = new RandomAccessFile(ConstantParameter.IpInformation, "rw");
            String atLine;
            String info = " password error";
            String[] buffer = new String[300];
            while ((atLine = file.readLine()) != null) {
                if (atLine.startsWith(ip+" ")) {
                    long pos = file.getFilePointer() - 2;
                    file.seek(pos);
                    int i = 0;
                    while ((buffer[i++] = file.readLine()) != null) ;

                    file.seek(pos);
                    file.write(info.getBytes());

                    i = 1;
                    while (buffer[i] != null) {
                        file.writeBytes("\r\n");
                        file.writeBytes(buffer[i]);
                        i++;
                    }
                    break;
                }
            }
            file.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static void statistics() {
        //// TODO: 2019/7/9

        //删除以前的统计文件
        File oldFile = new File(ConstantParameter.UserNamePath + "statistics.xml");
        if (oldFile.exists()) {
            oldFile.delete();
        }
        record(ConstantParameter.UserNamePath, "statistics.xml", "--IP地址------最高同时在线人数--------今日访问用户---------------\r\n");

        File file = new File(ConstantParameter.UserNamePath);
        File[] fileList = file.listFiles();
        for (File eachFile : fileList) {                                                    //依次处理每个文件
            Set<String> Views = new HashSet<>();                                            //存放ip及今天所有访问过的人数
            int maximumOnline = 0;                                                          //记录今天最高同时在线人数
            String Ip = eachFile.getName().substring(0, eachFile.getName().lastIndexOf('.'));
            if (!Ip.matches("([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}")) {
                continue;
            }

            BufferedReader reader = null;
            try {
                FileInputStream fis = new FileInputStream(eachFile);
                InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
                reader = new BufferedReader(isr);

                String line = null;
                while ((line = reader.readLine()) != null) {                                //依次读取文件每一行
                    List<String> list = Util.getIpFromLine(line);
                    if (list.size() > maximumOnline) {                                               //
                        maximumOnline = list.size();
                    }
                    for (String i : list) {
                        Views.add(i);
                    }
                }


                StringBuilder msg = new StringBuilder();
                msg.append(Ip);
                msg.append(" total " + maximumOnline + " users!");
                for (String i : Views) {
                    msg.append(i);
                }
                msg.append("\r\n");
                record(ConstantParameter.UserNamePath, "statistics.xml", msg.toString());
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e1) {
                    }
                }
            }
        }
    }

    public static List getIpFromLine(String str) {
        if (str.equals(" ") || str == null) {
            return new ArrayList();
        }

        List<String> modelList = new ArrayList<>();
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '<') {
                int j = i;
                while (j < str.length() && str.charAt(++j) != '>') ;
                modelList.add(str.substring(i, j + 1));
                i = j - 1;
            }
        }
        return modelList;
    }
}
