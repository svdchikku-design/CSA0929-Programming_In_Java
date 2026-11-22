package parking.ext;

import java.time.Duration;
import java.time.LocalDateTime;
import parking.model.Vehicle;

/** Rs60 per started hour starting 09:00-16:59, else Rs30. */
public class DeliveryVan extends Vehicle {
    public DeliveryVan(String regNo) { super(regNo); }
    @Override public String type() { return "VAN"; }
    @Override protected double ratePerHour() { return 30; }

    @Override public double calculateFee(LocalDateTime in, LocalDateTime out) {
        int hours = billableHours((int) Duration.between(in, out).toMinutes());
        double fee = 0;
        for (int h = 0; h < hours; h++) {
            int hod = in.plusHours(h).getHour();
            fee += (hod >= 9 && hod < 17) ? 60 : 30;
        }
        return fee;
    }
}
