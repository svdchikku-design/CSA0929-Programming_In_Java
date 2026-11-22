import java.util.List;

/** Data-access layer contract (the only layer that knows SQL). */
public interface RegistrationDao {
    class DaoException extends Exception { public DaoException(String m, Throwable c) { super(m, c); } }
    boolean exists(String studentId, String event) throws DaoException;
    void insert(Registration r) throws DaoException;
    List<Registration> page(String event, int limit, int offset) throws DaoException;
    int count(String event) throws DaoException;
}
