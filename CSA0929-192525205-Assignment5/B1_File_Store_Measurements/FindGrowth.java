import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FindGrowth {
    public static void main(String[] args) throws Exception {
        System.out.println("n | find(last id) file scan ms (avg 20) | HashMap load-once ms | 1000 lookups in map ms");
        for (int n : new int[]{1000, 10000, 100000}) {
            Path f = Files.createTempFile("passes" + n, ".csv");
            try (BufferedWriter w = Files.newBufferedWriter(f)) {
                for (int i = 0; i < n; i++) { w.write(String.join(",", "P" + (1000 + i), "19231" + i, "Name" + i, String.valueOf(i % 20 + 1), "4500.0")); w.newLine(); }
            }
            PassStore st = new PassStore(f);
            String target = "P" + (1000 + n - 1);
            for (int i = 0; i < 5; i++) st.find(target);
            long t = 0; for (int i = 0; i < 20; i++) { long a = System.nanoTime(); st.find(target); t += System.nanoTime() - a; }
            long a = System.nanoTime(); Map<String, String[]> m = new HashMap<>();
            try (BufferedReader r = Files.newBufferedReader(f)) { String line; while ((line = r.readLine()) != null) { String[] x = line.split(",", -1); if (x.length == 5) m.put(x[0], x); } }
            double load = (System.nanoTime() - a) / 1e6;
            Random rnd = new Random(1); long s = 0; a = System.nanoTime();
            for (int i = 0; i < 1000; i++) if (m.get("P" + (1000 + rnd.nextInt(n))) != null) s++;
            double look = (System.nanoTime() - a) / 1e6;
            System.out.printf("%d | %.3f | %.2f | %.3f%n", n, t / 20.0 / 1e6, load, look);
            Files.delete(f);
        }
    }
}
