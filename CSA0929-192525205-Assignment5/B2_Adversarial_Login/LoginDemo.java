import java.sql.*;

public class LoginDemo {
    static final String URL = "jdbc:mysql://localhost:3306/buspass_test", USER = "root", PWD = "your_password";

    /** UNSAFE: user input is concatenated into the SQL text. */
    static boolean unsafeLogin(Connection c, String id, String pwd) throws SQLException {
        String sql = "SELECT * FROM users WHERE id='" + id + "' AND pwd='" + pwd + "'";
        System.out.println("   SQL sent: " + sql);
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) { return rs.next(); }
    }
    /** SAFE: the SQL structure is fixed; input is bound as data. */
    static boolean safeLogin(Connection c, String id, String pwd) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT * FROM users WHERE id=? AND pwd=?")) {
            ps.setString(1, id); ps.setString(2, pwd);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }
    public static void main(String[] args) throws SQLException {
        try (Connection c = DriverManager.getConnection(URL, USER, PWD)) {
            String[][] tests = {{"admin", "S3cret!"}, {"admin", "wrong"}, {"admin", "' OR '1'='1"}, {"admin'-- ", "x"}};
            for (String[] t : tests) {
                System.out.println("id=" + t[0] + "  pwd=" + t[1]);
                System.out.println("   unsafe -> " + (unsafeLogin(c, t[0], t[1]) ? "LOGIN ACCEPTED" : "rejected"));
                System.out.println("   safe   -> " + (safeLogin(c, t[0], t[1]) ? "LOGIN ACCEPTED" : "rejected"));
            }
        }
    }
}
