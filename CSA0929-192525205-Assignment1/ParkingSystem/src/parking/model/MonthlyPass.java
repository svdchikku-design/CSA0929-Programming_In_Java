package parking.model;

import java.time.LocalDate;

public class MonthlyPass implements Pass {
    private final LocalDate validTill;
    public MonthlyPass(LocalDate validTill) { this.validTill = validTill; }
    @Override public double apply(double rawFee, LocalDate day) { return day.isAfter(validTill) ? rawFee : 0; }
}
