import java.io.BufferedReader;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

/** Reads the old pass file, validates each line, loads valid rows in batches inside ONE transaction, reports failures. */
public class MigrationTool {
    static String validate(String line) {
        String[] f = line.split(",", -1);
        if (f.length != 5) return "expected 5 fields, found " + f.length;
        if (!f[1].matches("\\d{9}")) return "bad register number";
        try { Integer.parseInt(f[3]); } catch (NumberFormatException e) { return "route is not an integer"; }
        try { if (Double.parseDouble(f[4]) < 0) return "negative amount"; } catch (NumberFormatException e) { return "amount is not a number"; }
        return null;
    }
    public static void main(String[] args) throws Exception {
        Path file = Path.of(args[0]);
        List<String[]> good = new ArrayList<>(); List<String> failed = new ArrayList<>();
        try (BufferedReader r = Files.newBufferedReader(file)) {
            String line; int no = 0;
            while ((line = r.readLine()) != null) {
                no++; String why = validate(line);
                if (why == null) good.add(line.split(",", -1)); else failed.add("line " + no + ": " + why + "  [" + line + "]");
            }
        }
        String sql = "INSERT INTO bus_pass(pass_id, reg_no, name, route_no, amount) VALUES (?,?,?,?,?)";
        try (Connection con = DriverManager.getConnection(args[1], args[2], args[3])) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                int n = 0;
                for (String[] p : good) {
                    ps.setString(1, p[0]); ps.setString(2, p[1]); ps.setString(3, p[2]);
                    ps.setInt(4, Integer.parseInt(p[3])); ps.setDouble(5, Double.parseDouble(p[4]));
                    ps.addBatch();
                    if (++n % 1000 == 0) ps.executeBatch();
                }
                ps.executeBatch();
                con.commit();
                System.out.println("Loaded " + good.size() + " rows; " + failed.size() + " rejected");
            } catch (SQLException e) { con.rollback(); System.out.println("Rolled back: " + e.getMessage()); throw e; }
        }
        failed.forEach(System.out::println);
    }
}
