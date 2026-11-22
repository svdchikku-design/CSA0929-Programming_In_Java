package parking.model;

import java.time.LocalDate;

/** Fee adjustment applied on exit. */
public interface Pass {
    Pass NONE = (raw, day) -> raw;
    double apply(double rawFee, LocalDate day);
}
