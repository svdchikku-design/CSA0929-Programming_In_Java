import java.util.*;
public class TicketFind {
    record T(String reg, int no) {}
    public static void main(String[] x) {
        System.out.println("n | array scan ms | ArrayList scan ms | HashMap ms   (2,000 random exit look-ups, avg of 5 after warm-up)");
        for (int n : new int[]{1000, 10000, 100000}) {
            T[] arr = new T[n]; List<T> list = new ArrayList<>(); Map<String, T> map = new HashMap<>();
            for (int i = 0; i < n; i++) { T t = new T("TN" + i, i); arr[i] = t; list.add(t); map.put(t.reg(), t); }
            Random r = new Random(7); String[] q = new String[2000]; for (int i = 0; i < q.length; i++) q[i] = "TN" + r.nextInt(n);
            double[] tm = new double[3]; long sink = 0;
            for (int run = 0; run < 7; run++) {
                long a = System.nanoTime();
                for (String s : q) for (int i = 0; i < n; i++) if (arr[i].reg().equals(s)) { sink += arr[i].no(); break; }
                long b = System.nanoTime();
                for (String s : q) for (T t : list) if (t.reg().equals(s)) { sink += t.no(); break; }
                long c = System.nanoTime();
                for (String s : q) sink += map.get(s).no();
                long d = System.nanoTime();
                if (run >= 2) { tm[0] += b - a; tm[1] += c - b; tm[2] += d - c; }
            }
            System.out.printf("%d | %.2f | %.2f | %.3f%n", n, tm[0] / 5e6, tm[1] / 5e6, tm[2] / 5e6);
        }
    }
}
