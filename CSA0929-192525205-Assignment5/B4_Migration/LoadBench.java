import java.sql.*;
import java.util.*;

/** B3.2: times Statement vs PreparedStatement vs batch for n rows. Run on YOUR MySQL (add ?rewriteBatchedStatements=true to the URL for the batch case). */
public class LoadBench {
    static List<String[]> gen(int n) { List<String[]> l = new ArrayList<>(); for (int i = 0; i < n; i++) l.add(new String[]{"P" + i, String.format("%09d", i), "Name" + i, String.valueOf(i % 20 + 1), "4500.0"}); return l; }
    public static void main(String[] a) throws Exception {
        try (Connection c = DriverManager.getConnection(a[0], a[1], a[2])) {
            for (int n : new int[]{1000, 10000, 100000}) {
                List<String[]> rows = gen(n);
                c.createStatement().execute("TRUNCATE TABLE bus_pass");
                long t0 = System.nanoTime();
                try (Statement st = c.createStatement()) { for (String[] p : rows) st.executeUpdate("INSERT INTO bus_pass VALUES('" + p[0] + "','" + p[1] + "','" + p[2] + "'," + p[3] + "," + p[4] + ")"); }
                double tStmt = (System.nanoTime() - t0) / 1e9;
                c.createStatement().execute("TRUNCATE TABLE bus_pass"); t0 = System.nanoTime();
                try (PreparedStatement ps = c.prepareStatement("INSERT INTO bus_pass VALUES(?,?,?,?,?)")) {
                    for (String[] p : rows) { ps.setString(1, p[0]); ps.setString(2, p[1]); ps.setString(3, p[2]); ps.setInt(4, Integer.parseInt(p[3])); ps.setDouble(5, Double.parseDouble(p[4])); ps.executeUpdate(); } }
                double tPrep = (System.nanoTime() - t0) / 1e9;
                c.createStatement().execute("TRUNCATE TABLE bus_pass"); t0 = System.nanoTime();
                PassLoader.loadAll(c, rows);
                double tBatch = (System.nanoTime() - t0) / 1e9;
                System.out.printf("%d rows | Statement %.2f s | PreparedStatement %.2f s | batch+transaction %.2f s%n", n, tStmt, tPrep, tBatch);
            }
        }
    }
}
