package parking.model;

public class Car extends Vehicle {
    public Car(String regNo) { super(regNo); }
    @Override public String type() { return "CAR"; }
    @Override protected double ratePerHour() { return 30; }
}
