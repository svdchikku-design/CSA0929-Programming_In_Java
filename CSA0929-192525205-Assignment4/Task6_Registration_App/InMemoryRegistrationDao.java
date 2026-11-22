import java.util.ArrayList;
import java.util.List;

/** Stand-in DAO used to test the service and screens without a database. */
public class InMemoryRegistrationDao implements RegistrationDao {
    private final List<Registration> rows = new ArrayList<>();
    @Override public boolean exists(String sid, String event) { return rows.stream().anyMatch(r -> r.studentId.equals(sid) && r.event.equals(event)); }
    @Override public void insert(Registration r) { rows.add(r); }
    private List<Registration> filtered(String event) { return rows.stream().filter(r -> event.equals("All") || r.event.equals(event)).toList(); }
    @Override public List<Registration> page(String event, int limit, int offset) {
        List<Registration> f = filtered(event);
        return f.subList(Math.min(offset, f.size()), Math.min(offset + limit, f.size()));
    }
    @Override public int count(String event) { return filtered(event).size(); }
}
