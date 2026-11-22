package parking.ext;

import java.time.LocalDate;
import parking.model.Pass;

/** Visitor day pass: fee capped at Rs60 on its day. */
public class DayPass implements Pass {
    private final LocalDate day;
    public DayPass(LocalDate day) { this.day = day; }
    @Override public double apply(double rawFee, LocalDate d) { return d.equals(day) ? Math.min(rawFee, 60) : rawFee; }
}
