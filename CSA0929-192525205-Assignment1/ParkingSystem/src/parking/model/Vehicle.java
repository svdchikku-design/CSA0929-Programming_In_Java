package parking.model;

import java.time.Duration;
import java.time.LocalDateTime;

/** Base type: fee = billable hours x hourly rate. */
public abstract class Vehicle {
    private final String regNo;

    protected Vehicle(String regNo) {
        if (regNo == null || regNo.isBlank()) throw new IllegalArgumentException("regNo required");
        this.regNo = regNo.trim().toUpperCase();
    }

    public final String regNo() { return regNo; }
    public abstract String type();
    protected abstract double ratePerHour();

    /** Integer ceiling of minutes/60. */
    public static int billableHours(int minutes) {
        if (minutes < 0) throw new IllegalArgumentException("Negative duration");
        return (minutes + 59) / 60;
    }

    public double calculateFee(int minutes) { return billableHours(minutes) * ratePerHour(); }

    public double calculateFee(LocalDateTime in, LocalDateTime out) {
        return calculateFee((int) Duration.between(in, out).toMinutes());
    }

    @Override public boolean equals(Object o) { return o instanceof Vehicle v && regNo.equals(v.regNo); }
    @Override public int hashCode() { return regNo.hashCode(); }
    @Override public String toString() { return type() + "[" + regNo + "]"; }
}
