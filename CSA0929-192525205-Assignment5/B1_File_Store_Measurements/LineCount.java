import java.io.*;
import java.lang.management.ManagementFactory;
import java.nio.file.*;
import java.util.*;

public class LineCount {
    static long unbuf(Path p) throws IOException { long n = 0; try (FileInputStream in = new FileInputStream(p.toFile())) { int b; while ((b = in.read()) != -1) if (b == '\n') n++; } return n; }
    static long buf(Path p) throws IOException { long n = 0; try (BufferedReader r = Files.newBufferedReader(p)) { while (r.readLine() != null) n++; } return n; }
    static long arr(Path p) throws IOException { long n = 0; byte[] bb = new byte[8192]; try (FileInputStream in = new FileInputStream(p.toFile())) { int k; while ((k = in.read(bb)) > 0) for (int i = 0; i < k; i++) if (bb[i] == '\n') n++; } return n; }
    static long all(Path p) throws IOException { return Files.readAllLines(p).size(); }
    interface C { long run(Path p) throws IOException; }
    static double ms(C c, Path p, int runs, boolean warm) throws IOException {
        if (warm) c.run(p);
        long t = 0; for (int i = 0; i < runs; i++) { long a = System.nanoTime(); c.run(p); t += System.nanoTime() - a; }
        return t / (double) runs / 1e6;
    }
    static long alloc(C c, Path p) throws IOException {
        com.sun.management.ThreadMXBean m = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
        long a = m.getThreadAllocatedBytes(Thread.currentThread().threadId()); c.run(p);
        return m.getThreadAllocatedBytes(Thread.currentThread().threadId()) - a;
    }
    public static void main(String[] args) throws Exception {
        System.out.println("size_MB | lines | read() unbuffered ms (runs) | BufferedReader ms | byte[8192] ms | readAllLines ms | alloc BufferedReader | alloc readAllLines");
        for (int mb : new int[]{1, 10, 50}) {
            Path f = Files.createTempFile("log" + mb, ".txt");
            try (BufferedWriter w = Files.newBufferedWriter(f)) {
                long bytes = 0; int i = 0;
                while (bytes < mb * 1_048_576L) { String line = "2026-11-17 10:" + (i % 60) + ":00,ID" + i + ",GATE-" + (i % 5) + ",IN"; w.write(line); w.newLine(); bytes += line.length() + 1; i++; }
            }
            long lines = buf(f);
            int ur = mb == 50 ? 1 : 3;
            double u = ms(LineCount::unbuf, f, ur, mb != 50);
            double b = ms(LineCount::buf, f, 5, true);
            double a = ms(LineCount::arr, f, 5, true);
            double r = ms(LineCount::all, f, 3, true);
            System.out.printf("%d | %d | %.1f (%d run%s) | %.1f | %.1f | %.1f | %d | %d%n", mb, lines, u, ur, ur > 1 ? "s" : "", b, a, r, alloc(LineCount::buf, f), alloc(LineCount::all, f));
            System.out.flush();
            Files.delete(f);
        }
    }
}
