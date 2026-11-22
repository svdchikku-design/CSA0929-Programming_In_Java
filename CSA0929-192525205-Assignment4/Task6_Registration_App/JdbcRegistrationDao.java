import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcRegistrationDao implements RegistrationDao {
    private final String url, user, pwd;
    public JdbcRegistrationDao(String url, String user, String pwd) { this.url = url; this.user = user; this.pwd = pwd; }
    private Connection open() throws SQLException { return DriverManager.getConnection(url, user, pwd); }

    @Override public boolean exists(String sid, String event) throws DaoException {
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement("SELECT 1 FROM registration WHERE student_id=? AND event_name=?")) {
            ps.setString(1, sid); ps.setString(2, event);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { throw new DaoException("Could not check registration", e); }
    }
    @Override public void insert(Registration r) throws DaoException {
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement("INSERT INTO registration(student_id,name,event_name) VALUES(?,?,?)")) {
            ps.setString(1, r.studentId); ps.setString(2, r.name); ps.setString(3, r.event);
            ps.executeUpdate();
        } catch (SQLException e) { throw new DaoException("Could not save registration", e); }
    }
    @Override public List<Registration> page(String event, int limit, int offset) throws DaoException {
        String sql = "SELECT student_id,name,event_name FROM registration WHERE (?='All' OR event_name=?) ORDER BY id LIMIT ? OFFSET ?";
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, event); ps.setString(2, event); ps.setInt(3, limit); ps.setInt(4, offset);
            List<Registration> out = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) out.add(new Registration(rs.getString(1), rs.getString(2), rs.getString(3))); }
            return out;
        } catch (SQLException e) { throw new DaoException("Could not load registrations", e); }
    }
    @Override public int count(String event) throws DaoException {
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM registration WHERE (?='All' OR event_name=?)")) {
            ps.setString(1, event); ps.setString(2, event);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1); }
        } catch (SQLException e) { throw new DaoException("Could not count registrations", e); }
    }
}
