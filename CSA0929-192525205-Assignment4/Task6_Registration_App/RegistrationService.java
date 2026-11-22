import java.util.List;

/** Validation + rules. No Swing, no SQL. */
public class RegistrationService {
    public static class ValidationException extends Exception { public ValidationException(String m) { super(m); } }
    private final RegistrationDao dao;
    public RegistrationService(RegistrationDao dao) { this.dao = dao; }

    public void register(String studentId, String name, String event) throws ValidationException, RegistrationDao.DaoException {
        if (studentId == null || !studentId.trim().matches("\\d{9}")) throw new ValidationException("Student ID must be 9 digits");
        if (name == null || !name.trim().matches("[A-Za-z ]{2,60}")) throw new ValidationException("Name must be 2-60 letters");
        if (event == null || event.isBlank() || event.equals("All")) throw new ValidationException("Select an event");
        if (dao.exists(studentId.trim(), event)) throw new ValidationException("Already registered for " + event);
        dao.insert(new Registration(studentId.trim(), name.trim(), event));
    }
    public List<Registration> page(String event, int pageNo, int size) throws RegistrationDao.DaoException { return dao.page(event, size, pageNo * size); }
    public int count(String event) throws RegistrationDao.DaoException { return dao.count(event); }
}
