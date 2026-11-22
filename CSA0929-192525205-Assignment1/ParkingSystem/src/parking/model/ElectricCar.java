package parking.model;

public class ElectricCar extends Car implements Chargeable {
    public ElectricCar(String regNo) { super(regNo); }
    @Override public String type() { return "EV"; }
    @Override public double chargingFee(int kWh) { return 12.0 * kWh; }
}
