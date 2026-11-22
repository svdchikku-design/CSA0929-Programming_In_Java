package parking.service;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import parking.model.*;

public final class VehicleFactory {
    private static final Map<String, Function<String, Vehicle>> registry = new HashMap<>();
    static {
        register("TWOWHEELER", TwoWheeler::new);
        register("CAR", Car::new);
        register("EV", ElectricCar::new);
    }

    private VehicleFactory() {}

    public static void register(String type, Function<String, Vehicle> maker) {
        registry.put(type.trim().toUpperCase(), maker);
    }

    public static Vehicle create(String type, String regNo) {
        Function<String, Vehicle> f = type == null ? null : registry.get(type.trim().toUpperCase());
        if (f == null) throw new IllegalArgumentException("Unknown vehicle type: " + type);
        return f.apply(regNo);
    }
}
