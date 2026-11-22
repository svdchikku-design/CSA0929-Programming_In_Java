import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

public class MockTest {
    static List<String> log = new ArrayList<>();
    static Connection mockCon(int[] state) {
        InvocationHandler psH = (p, m, a) -> {
            switch (m.getName()) {
                case "addBatch": state[0]++; return null;
                case "executeBatch": log.add("executeBatch(pending=" + state[0] + ")"); state[1] += state[0]; state[0] = 0; return new int[0];
                case "close": return null;
            }
            return null;
        };
        PreparedStatement ps = (PreparedStatement) Proxy.newProxyInstance(MockTest.class.getClassLoader(), new Class[]{PreparedStatement.class}, psH);
        boolean[] auto = {true};
        InvocationHandler cH = (p, m, a) -> {
            switch (m.getName()) {
                case "getAutoCommit": return auto[0];
                case "setAutoCommit": auto[0] = (Boolean) a[0]; log.add("setAutoCommit(" + a[0] + ")"); return null;
                case "prepareStatement": return ps;
                case "commit": log.add("COMMIT"); return null;
                case "rollback": log.add("ROLLBACK"); return null;
            }
            return null;
        };
        return (Connection) Proxy.newProxyInstance(MockTest.class.getClassLoader(), new Class[]{Connection.class}, cH);
    }
    static List<String[]> rows(int n, int badAt) {
        List<String[]> l = new ArrayList<>();
        for (int i = 0; i < n; i++) l.add(new String[]{"P" + i, "192" + String.format("%06d", i), "N" + i, "5", i == badAt ? "abc" : "4500.0"});
        return l;
    }
    public static void main(String[] a) throws Exception {
        log.clear();
        int n = PassLoader.loadAll(mockCon(new int[2]), rows(2500, -1));
        System.out.println("Test 1: 2,500 good rows -> returned " + n + "\n  " + log);
        log.clear();
        try { PassLoader.loadAll(mockCon(new int[2]), rows(2500, 1500)); }
        catch (Exception e) { System.out.println("Test 2: bad amount at row 1500 -> " + e.getClass().getSimpleName() + ": " + e.getMessage() + "\n  " + log); }
    }
}
