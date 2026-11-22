import java.sql.*;

public class MetaDemo {
    public static void main(String[] args) throws SQLException {
        try (Connection c = DriverManager.getConnection(args[0], args[1], args[2])) {
            DatabaseMetaData m = c.getMetaData();
            System.out.println("Product : " + m.getDatabaseProductName() + " " + m.getDatabaseProductVersion());
            System.out.println("Driver  : " + m.getDriverName() + " " + m.getDriverVersion());
            System.out.println("Tables  :");
            try (ResultSet rs = m.getTables(c.getCatalog(), null, "%", new String[]{"TABLE"})) { while (rs.next()) System.out.println("   " + rs.getString("TABLE_NAME")); }
        }
    }
}
