package parking.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import parking.model.Chargeable;
import parking.model.Pass;
import parking.model.Vehicle;

public class ParkingFacility {
    private final String[][] slots;                       // jagged: regNo or null
    private final Map<String, ParkingTicket> active = new HashMap<>();
    private double total;

    /** One argument per floor = number of slots on it. */
    public ParkingFacility(int... slotsPerFloor) {
        slots = new String[slotsPerFloor.length][];
        for (int f = 0; f < slots.length; f++) slots[f] = new String[slotsPerFloor[f]];
    }

    public ParkingTicket enter(Vehicle v, LocalDateTime t, Pass p) {
        if (active.containsKey(v.regNo())) throw new IllegalStateException("Duplicate vehicle");
        for (int f = 0; f < slots.length; f++)
            for (int s = 0; s < slots[f].length; s++)
                if (slots[f][s] == null) {
                    slots[f][s] = v.regNo();
                    ParkingTicket tk = new ParkingTicket(v, f, s, t, p);
                    active.put(v.regNo(), tk);
                    return tk;
                }
        throw new IllegalStateException("Facility full");
    }

    public double exit(String regNo, LocalDateTime out, int kWh) {
        String key = regNo == null ? "" : regNo.trim().toUpperCase();
        ParkingTicket tk = active.get(key);
        if (tk == null) throw new IllegalArgumentException("Unknown vehicle: " + regNo);
        Vehicle v = tk.vehicle();
        double raw = v.calculateFee(tk.entry(), out) + (v instanceof Chargeable c ? c.chargingFee(kWh) : 0);
        double fee = tk.pass().apply(raw, out.toLocalDate());
        active.remove(key);                                // only after fee computed OK
        slots[tk.floor()][tk.slot()] = null;
        total += fee;
        return fee;
    }

    /** Frees a slot directly; false if invalid or already free. */
    public boolean release(int floor, int slot) {
        if (floor < 0 || floor >= slots.length || slot < 0 || slot >= slots[floor].length) return false;
        String reg = slots[floor][slot];
        if (reg == null) return false;
        slots[floor][slot] = null;
        active.remove(reg);
        return true;
    }

    public int capacity() { int n = 0; for (String[] f : slots) n += f.length; return n; }
    public double totalCollection() { return total; }
}
