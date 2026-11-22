package parking.ext;

import parking.model.TwoWheeler;

/** Flat Rs15 regardless of duration. */
public class EBike extends TwoWheeler {
    public EBike(String regNo) { super(regNo); }
    @Override public String type() { return "EBIKE"; }
    @Override public double calculateFee(int minutes) { return 15; }
}
