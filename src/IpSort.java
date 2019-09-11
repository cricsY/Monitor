import java.util.*;


public class IpSort {
    public static List<String> IpSetSort(List<String> ipSet) {
        if (ipSet.size() <= 1)
            return ipSet;

        for (int i = 0; i < ipSet.size() - 1; i++) {
            for (int j = 0; j < ipSet.size() - i - 1; j++) {
                String a = ipSet.get(j), b = ipSet.get(j + 1);
                if (compare(a, b)) {
                    ipSet.set(j, b);
                    ipSet.set(j + 1, a);
                }
            }
        }
        return ipSet;
    }

    //Ip地址比较
    private static boolean compare(String a, String b) {
        String[] strA = a.split("\\."), strB = b.split("\\.");
        int[] numA = new int[4], numB = new int[4];

        for (int i = 0; i < 4; i++) {
            numA[i] = Integer.parseInt(strA[i]);
            numB[i] = Integer.parseInt(strB[i]);
        }
        if (numA[0] > numB[0] || (numA[0] == numB[0] && numA[1] > numB[1]) || (numA[0] == numB[0] && numA[1] == numB[1] && numA[2] > numB[2]) || ((numA[0] == numB[0] && numA[1] == numB[1] && numA[2] == numB[2] && numA[3] >= numB[3])))
            return true;
        return false;
    }
}