import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/** Text-file store from Task A1: add, find by pass ID, list by route; damaged lines are skipped. */
public class PassStoreFull {
    private final Path file;
    PassStoreFull(Path file) { this.file = file; }

    static String clean(String s) { return s.replace(',', ' ').replace('\n', ' ').replace('\r', ' ').trim(); }   // delimiter safety

    public void add(String id, String reg, String name, int route, String stop, String validTill, double amount) throws IOException {
        String line = String.join(",", clean(id), clean(reg), clean(name), String.valueOf(route), clean(stop), validTill, String.valueOf(amount));
        try (var w = Files.newBufferedWriter(file, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) { w.write(line); w.newLine(); }
    }
    /** null if the line is damaged, else its 7 fields. */
    static String[] parse(String line) {
        String[] f = line.split(",", -1);
        if (f.length != 7) return null;
        try { Integer.parseInt(f[3]); Double.parseDouble(f[6]); java.time.LocalDate.parse(f[5]); } catch (RuntimeException e) { return null; }
        return f;
    }
    public String[] find(String id) throws IOException {
        try (var r = Files.newBufferedReader(file)) { String l; while ((l = r.readLine()) != null) { String[] f = parse(l); if (f != null && f[0].equals(id)) return f; } }
        return null;
    }
    public List<String[]> listByRoute(int route) throws IOException {
        List<String[]> out = new ArrayList<>();
        try (var r = Files.newBufferedReader(file)) { String l; while ((l = r.readLine()) != null) { String[] f = parse(l); if (f != null && Integer.parseInt(f[3]) == route) out.add(f); } }
        return out;
    }
    public int damagedCount() throws IOException { int n = 0; try (var r = Files.newBufferedReader(file)) { String l; while ((l = r.readLine()) != null) if (parse(l) == null) n++; } return n; }

    public static void main(String[] a) throws Exception {
        Path f = Files.createTempFile("passes7", ".csv"); PassStoreFull s = new PassStoreFull(f);
        s.add("P1001", "192311001", "Arun", 12, "Gate 1", "2027-03-31", 4500);
        s.add("P1002", "192311002", "Ravi, K", 12, "Library", "2027-03-31", 4500);     // comma inside the name
        s.add("P1003", "192311003", "Divya", 7, "Hostel A", "2027-03-31", 3800);
        Files.writeString(f, "P1004,DAMAGED LINE\n", StandardOpenOption.APPEND);                  // wrong number of fields
        Files.writeString(f, "P1005,192311005,Meena,3,Gate 2,2027-03-31,abc\n", StandardOpenOption.APPEND);   // non-numeric amount
        Files.writeString(f, "\n", StandardOpenOption.APPEND);                                    // empty line
        s.add("P1006", "192311006", "Kumar", 12, "Main Block", "2027-03-31", 4500);
        System.out.println("T1 normal    find(P1003)      -> " + Arrays.toString(s.find("P1003")));
        System.out.println("T2 missing   find(P9999)      -> " + Arrays.toString(s.find("P9999")));
        System.out.println("T3 damaged   find(P1004)      -> " + Arrays.toString(s.find("P1004")) + "   find(P1006) after the damage -> " + Arrays.toString(s.find("P1006")));
        System.out.println("T4 list      route 12 (IDs)   -> " + s.listByRoute(12).stream().map(x -> x[0]).toList() + "   route 99 -> " + s.listByRoute(99).size() + " rows");
        System.out.println("T5 comma     name stored as   -> " + Arrays.toString(s.find("P1002")));
        System.out.println("T6 damaged lines counted      -> " + s.damagedCount() + "  (wrong fields, non-numeric amount, empty line)");
        try { new PassStoreFull(Path.of("no_such_file.csv")).find("P1"); } catch (NoSuchFileException e) { System.out.println("T7 missing file              -> NoSuchFileException: " + e.getMessage()); }
        System.out.println("-- raw file --"); Files.readAllLines(f).forEach(l -> System.out.println("   [" + l + "]"));
        Files.delete(f);
    }
}
