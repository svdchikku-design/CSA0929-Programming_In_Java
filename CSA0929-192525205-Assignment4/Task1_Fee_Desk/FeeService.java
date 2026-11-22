import java.sql.SQLException;

/** Business logic only: no AWT/Swing here, so it can be unit-tested without a window. */
public class FeeService {
    public interface PaymentGateway { void save(String regNo, double amount) throws SQLException; }
    public static class ValidationException extends Exception {
        public ValidationException(String m) { super(m); }
    }
    private final PaymentGateway dao;
    public FeeService(PaymentGateway dao) { this.dao = dao; }

    public String pay(String regNo, String amountText) throws ValidationException, SQLException {
        if (regNo == null || !regNo.trim().matches("\\d{9}"))
            throw new ValidationException("Register number must be 9 digits");
        double amount;
        try { amount = Double.parseDouble(amountText.trim()); }
        catch (NumberFormatException | NullPointerException e) { throw new ValidationException("Amount must be a number"); }
        if (amount <= 0) throw new ValidationException("Amount must be greater than zero");
        dao.save(regNo.trim(), amount);        // DAO uses PreparedStatement + try-with-resources
        return "Paid Rs " + amount + " for " + regNo.trim();
    }
}
