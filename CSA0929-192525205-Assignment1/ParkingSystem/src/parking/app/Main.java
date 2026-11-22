package parking.app;

import java.time.*;
import java.util.function.Supplier;
import parking.ext.*;
import parking.model.*;
import parking.service.*;

public class Main {
    static int pass, fail;
    static final LocalDate D = LocalDate.of(2026, 11, 17);

    static LocalDateTime at(int h, int m) { return D.atTime(h, m); }

    static void row(String id, String name, Object exp, Object act) {
        boolean ok = String.valueOf(exp).equals(String.valueOf(act));
        if (ok) pass++; else fail++;
        System.out.printf("%-5s | %-44s | %-26s | %-26s | %s%n", id, name, exp, act, ok ? "PASS" : "FAIL");
    }

    /** Returns the exception message (or "no exception"). */
    static String thrown(Supplier<?> s) {
        try { s.get(); return "no exception"; }
        catch (RuntimeException e) { return e.getClass().getSimpleName() + ": " + e.getMessage(); }
    }

    static double fee(ParkingFacility f, Vehicle v, LocalDateTime in, LocalDateTime out, int kWh, Pass p) {
        f.enter(v, in, p);
        return f.exit(v.regNo(), out, kWh);
    }

    static void sectionA(String p) {
        System.out.println("--- Section A (" + p + "): baseline ---");
        ParkingFacility f = new ParkingFacility(3, 3);
        ParkingTicket t1 = f.enter(new TwoWheeler("tn01a1"), at(9, 0), Pass.NONE);
        ParkingTicket t2 = f.enter(new Car("tn01b2"), at(8, 0), Pass.NONE);
        ParkingTicket t3 = f.enter(new ElectricCar("tn01c3"), at(10, 0), Pass.NONE);
        ParkingTicket t4 = f.enter(new Car("tn01d4"), at(8, 0), new MonthlyPass(D));
        row(p + "1", "Normal: TwoWheeler 09:00-10:30", 20.0, f.exit("TN01A1", at(10, 30), 0));
        row(p + "2", "Normal: Car 08:00-11:00", 90.0, f.exit("TN01B2", at(11, 0), 0));
        row(p + "3", "Normal: EV 10:00-12:05 + 8 kWh", 186.0, f.exit("TN01C3", at(12, 5), 8));
        row(p + "4", "Normal: Car with MonthlyPass", 0.0, f.exit("TN01D4", at(11, 0), 0));
        row(p + "5", "Day total (hand-calculated 296)", 296.0, f.totalCollection());
        row(p + "6", "Ticket numbers unique and increasing", true,
            t1.ticketNo() < t2.ticketNo() && t2.ticketNo() < t3.ticketNo() && t3.ticketNo() < t4.ticketNo());

        ParkingFacility one = new ParkingFacility(1);
        one.enter(new Car("X1"), at(9, 0), Pass.NONE);
        row(p + "7", "Boundary: last slot taken, next enter", "IllegalStateException: Facility full",
            thrown(() -> one.enter(new Car("X2"), at(9, 0), Pass.NONE)));
        Car c = new Car("Z9");
        row(p + "8", "Boundary: 0-minute car", 0.0, c.calculateFee(0));
        row(p + "9", "Boundary: 60-minute car", 30.0, c.calculateFee(60));
        row(p + "10", "Boundary: 61-minute car", 60.0, c.calculateFee(61));
        row(p + "11", "Invalid: factory type BUS", "IllegalArgumentException: Unknown vehicle type: BUS",
            thrown(() -> VehicleFactory.create("BUS", "B1")));
        row(p + "12", "Invalid: duplicate regNo", "IllegalStateException: Duplicate vehicle",
            thrown(() -> one.enter(new Car("x1"), at(9, 5), Pass.NONE)));
        row(p + "13", "Invalid: exit unknown regNo", "IllegalArgumentException",
            thrown(() -> f.exit("NOPE", at(9, 0), 0)).split(":")[0]);
        row(p + "14", "Invalid: release(5,0)", false, f.release(5, 0));
        row(p + "15", "Invalid: release already-free slot", false, f.release(0, 0));
    }

    static void sectionB() {
        System.out.println("--- Section B: Task 12 extension ---");
        VehicleFactory.register("EBIKE", EBike::new);
        VehicleFactory.register("VAN", DeliveryVan::new);
        ParkingFacility f = new ParkingFacility(10);
        row("B1", "EBike 5h", 15.0, fee(f, VehicleFactory.create("EBIKE", "e1"), at(8, 0), at(13, 0), 0, Pass.NONE));
        row("B2", "EBike 0 min", 15.0, fee(f, VehicleFactory.create("EBIKE", "e2"), at(8, 0), at(8, 0), 0, Pass.NONE));
        row("B3", "Van 10:00-12:00 (peak)", 120.0, fee(f, VehicleFactory.create("VAN", "v1"), at(10, 0), at(12, 0), 0, Pass.NONE));
        row("B4", "Van 16:30-18:00 (mixed)", 90.0, fee(f, VehicleFactory.create("VAN", "v2"), at(16, 30), at(18, 0), 0, Pass.NONE));
        row("B5", "Van 07:00-08:00 (off-peak)", 30.0, fee(f, VehicleFactory.create("VAN", "v3"), at(7, 0), at(8, 0), 0, Pass.NONE));
        row("B6", "Visitor Car 10h + DayPass (cap 60)", 60.0, fee(f, new Car("c1"), at(8, 0), at(18, 0), 0, new DayPass(D)));
        row("B7", "Visitor Car 1h + DayPass", 30.0, fee(f, new Car("c2"), at(8, 0), at(9, 0), 0, new DayPass(D)));
        row("B8", "DayPass of another day -> raw fee", 300.0,
            fee(f, new Car("c3"), at(8, 0), at(18, 0), 0, new DayPass(D.minusDays(1))));

        ParkingFacility big = new ParkingFacility(20, 35, 50, 60, 80);
        row("B9", "Jagged {20,35,50,60,80} capacity", 245, big.capacity());
        ParkingTicket first = big.enter(new Car("f0"), at(9, 0), Pass.NONE);
        ParkingTicket last = first;
        for (int i = 1; i < 21; i++) last = big.enter(new Car("f" + i), at(9, 0), Pass.NONE);
        row("B10", "First vehicle floor/slot", "0/0", first.floor() + "/" + first.slot());
        row("B11", "20th vehicle still floor 0 (slot 19)", "0/19", ticketAt(big, 19));
        row("B12", "21st vehicle spills to floor 1 slot 0", "1/0", last.floor() + "/" + last.slot());
    }

    // helper: the 20th entry was on floor 0; recompute via a fresh facility
    static String ticketAt(ParkingFacility ignored, int n) {
        ParkingFacility t = new ParkingFacility(20, 35);
        ParkingTicket k = null;
        for (int i = 0; i <= n; i++) k = t.enter(new Car("t" + i), at(9, 0), Pass.NONE);
        return k.floor() + "/" + k.slot();
    }

    public static void main(String[] args) {
        System.out.printf("%-5s | %-44s | %-26s | %-26s | %s%n", "ID", "Test", "Expected", "Actual", "Result");
        sectionA("A");
        sectionB();
        sectionA("R");   // regression re-run after extension
        System.out.println("TOTAL: " + (pass + fail) + " tests, " + pass + " PASS, " + fail + " FAIL");
    }
}
