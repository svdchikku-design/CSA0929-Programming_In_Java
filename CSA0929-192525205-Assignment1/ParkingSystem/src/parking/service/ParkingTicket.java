package parking.service;

import java.time.LocalDateTime;
import parking.model.Pass;
import parking.model.Vehicle;

public final class ParkingTicket {
    private static int nextNo = 1000;   // shared counter
    private final int ticketNo;
    private final Vehicle vehicle;
    private final int floor, slot;
    private final LocalDateTime entry;
    private final Pass pass;

    public ParkingTicket(Vehicle v, int floor, int slot, LocalDateTime entry) {
        this(v, floor, slot, entry, Pass.NONE);
    }

    public ParkingTicket(Vehicle v, int floor, int slot, LocalDateTime entry, Pass pass) {
        this.ticketNo = nextNo++;
        this.vehicle = v; this.floor = floor; this.slot = slot; this.entry = entry; this.pass = pass;
    }

    /** Copy constructor: re-issues a duplicate ticket with the SAME number (counter not touched). */
    public ParkingTicket(ParkingTicket old) {
        this.ticketNo = old.ticketNo; this.vehicle = old.vehicle; this.floor = old.floor;
        this.slot = old.slot; this.entry = old.entry; this.pass = old.pass;
    }
    public ParkingTicket duplicate() { return new ParkingTicket(this); }

    public int ticketNo() { return ticketNo; }
    public Vehicle vehicle() { return vehicle; }
    public int floor() { return floor; }
    public int slot() { return slot; }
    public LocalDateTime entry() { return entry; }
    public Pass pass() { return pass; }
    @Override public String toString() { return "#" + ticketNo + " " + vehicle + " F" + floor + "/S" + slot; }
}
