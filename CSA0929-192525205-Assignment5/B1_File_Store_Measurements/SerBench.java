import java.io.*;
import java.nio.file.*;
import java.util.*;
public class SerBench {
    static class Resident implements Serializable {
        private static final long serialVersionUID = 1L;
        String name, regNo; transient String password = "secret"; int room;
        Resident(String n, String r, int room) { name = n; regNo = r; this.room = room; }
    }
    public static void main(String[] x) throws Exception {
        System.out.println("n | serial size KB | serial save ms | serial load ms | CSV size KB | CSV save ms | CSV load ms  (avg of 5 after 2 warm-ups)");
        for (int n : new int[]{1000, 10000, 100000}) {
            List<Resident> list = new ArrayList<>(); for (int i = 0; i < n; i++) list.add(new Resident("Student" + i, "19231" + i, i % 400));
            Path s = Files.createTempFile("res", ".ser"), c = Files.createTempFile("res", ".csv");
            double[] t = new double[4];
            for (int r = 0; r < 7; r++) {
                long a = System.nanoTime();
                try (ObjectOutputStream o = new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(s)))) { o.writeObject(list); }
                long b = System.nanoTime();
                try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(s)))) { in.readObject(); }
                long cc = System.nanoTime();
                try (BufferedWriter w = Files.newBufferedWriter(c)) { for (Resident q : list) { w.write(q.name + "," + q.regNo + "," + q.room); w.newLine(); } }
                long d = System.nanoTime();
                List<Resident> back = new ArrayList<>();
                try (BufferedReader rd = Files.newBufferedReader(c)) { String l; while ((l = rd.readLine()) != null) { String[] p = l.split(","); back.add(new Resident(p[0], p[1], Integer.parseInt(p[2]))); } }
                long e = System.nanoTime();
                if (r >= 2) { t[0] += b - a; t[1] += cc - b; t[2] += d - cc; t[3] += e - d; }
            }
            System.out.printf("%d | %d | %.1f | %.1f | %d | %.1f | %.1f%n", n, Files.size(s) / 1024, t[0] / 5e6, t[1] / 5e6, Files.size(c) / 1024, t[2] / 5e6, t[3] / 5e6);
            Files.delete(s); Files.delete(c);
        }
    }
}
