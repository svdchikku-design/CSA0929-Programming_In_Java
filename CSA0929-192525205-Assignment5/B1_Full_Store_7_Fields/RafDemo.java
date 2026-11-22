import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class RafDemo {
    static final int REC = 81;   // passId 8 | regNo 9 | name 30 | route 2 | stop 20 | valid yyyymmdd 4 | amount 8
    static final String[] NAMES = {"Arun", "Divya", "Meena", "Ravi Kumar", "Saai Vardhan", "Lakshmi Priya", "Karthik", "Pooja", "Mohammed Irfan", "Anitha Devi"};
    static final String[] STOPS = {"Gate 1", "Library", "Hostel A", "Main Block", "Bus Stand", "Canteen Road"};
    static void put(RandomAccessFile f, String s, int len) throws IOException { byte[] b = new byte[len]; Arrays.fill(b, (byte) ' '); byte[] x = s.getBytes(StandardCharsets.US_ASCII); System.arraycopy(x, 0, b, 0, Math.min(len, x.length)); f.write(b); }
    static String get(RandomAccessFile f, int len) throws IOException { byte[] b = new byte[len]; f.readFully(b); return new String(b, StandardCharsets.US_ASCII).trim(); }
    static void writeRec(RandomAccessFile f, int i) throws IOException {
        put(f, "P" + (1000 + i), 8); put(f, String.valueOf(192300000 + i), 9); put(f, NAMES[i % NAMES.length], 30); f.writeShort(i % 20 + 1); put(f, STOPS[i % STOPS.length], 20); f.writeInt(20270331); f.writeDouble(4500.0); }
    static String[] readRec(RandomAccessFile f) throws IOException { return new String[]{get(f, 8), get(f, 9), get(f, 30), String.valueOf(f.readShort()), get(f, 20), String.valueOf(f.readInt()), String.valueOf(f.readDouble())}; }
    public static void main(String[] a) throws Exception {
        System.out.println("n | text file KB (7 fields) | fixed-record file KB | padding bytes/record (avg) | find last: text scan ms | find last: RAF seek ms | find last: RAF scan of records ms");
        for (int n : new int[]{1000, 10000, 100000}) {
            Path t = Files.createTempFile("t", ".csv"), r = Files.createTempFile("r", ".dat");
            long actual = 0;
            try (BufferedWriter w = Files.newBufferedWriter(t); RandomAccessFile f = new RandomAccessFile(r.toFile(), "rw")) {
                for (int i = 0; i < n; i++) { String line = String.join(",", "P" + (1000 + i), String.valueOf(192300000 + i), NAMES[i % NAMES.length], String.valueOf(i % 20 + 1), STOPS[i % STOPS.length], "2027-03-31", "4500.0");
                    w.write(line); w.newLine(); writeRec(f, i);
                    actual += ("P" + (1000 + i)).length() + 9 + NAMES[i % NAMES.length].length() + STOPS[i % STOPS.length].length() + 2 + 4 + 8; }
            }
            PassStoreFull ps = new PassStoreFull(t); String target = "P" + (1000 + n - 1);
            double tt = 0, tr = 0, ts = 0;
            try (RandomAccessFile f = new RandomAccessFile(r.toFile(), "r")) {
                for (int run = 0; run < 25; run++) {
                    long a0 = System.nanoTime(); ps.find(target); long a1 = System.nanoTime();
                    f.seek((long) (n - 1) * REC); String[] x = readRec(f); long a2 = System.nanoTime();
                    f.seek(0); String[] y = null; for (int i = 0; i < n; i++) { y = readRec(f); if (y[0].equals(target)) break; } long a3 = System.nanoTime();
                    if (run >= 5) { tt += a1 - a0; tr += a2 - a1; ts += a3 - a2; }
                }
            }
            System.out.printf("%d | %d | %d | %.0f | %.3f | %.4f | %.2f%n", n, Files.size(t) / 1024, Files.size(r) / 1024, (double) ((long) REC * n - actual) / n, tt / 20 / 1e6, tr / 20 / 1e6, ts / 20 / 1e6);
            Files.delete(t); Files.delete(r);
        }
    }
}
