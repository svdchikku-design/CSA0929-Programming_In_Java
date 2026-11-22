import java.util.*;
import java.util.function.*;

public class Q2 {
    static long sink;
    static final int WARM = 10, RUNS = 10;

    static <T> double measure(Supplier<T> setup, Consumer<T> op) {
        for (int i = 0; i < WARM; i++) op.accept(setup.get());
        double sum = 0;
        for (int r = 0; r < RUNS; r++) {
            T s = setup.get();
            long t0 = System.nanoTime();
            op.accept(s);
            sum += System.nanoTime() - t0;
        }
        return sum / RUNS / 1e3; // microseconds
    }

    static Collection<Integer> make(String kind) {
        switch (kind) {
            case "ArrayList": return new ArrayList<>();
            case "LinkedList": return new LinkedList<>();
            default: return new ArrayDeque<>();
        }
    }
    static Collection<Integer> filled(String kind, int n) {
        Collection<Integer> c = make(kind);
        for (int i = 0; i < n; i++) c.add(i);
        return c;
    }

    public static void main(String[] args) {
        System.out.println("warm-up=" + WARM + " measured runs=" + RUNS + "; all times in microseconds (avg), total for the batch");
        System.out.println("add-end: N adds from empty; front/middle: 1000 inserts into prefilled list of N; get: 1000 random get(i); iterate: full for-each sum");
        String[] kinds = {"ArrayList", "LinkedList", "ArrayDeque"};
        int[] sizes = {1000, 10000, 100000};
        for (int n : sizes) {
            Random rnd = new Random(1);
            int[] idx = new int[1000];
            for (int i = 0; i < 1000; i++) idx[i] = rnd.nextInt(n);
            System.out.println("\nN = " + n);
            System.out.printf("%-11s %12s %14s %16s %14s %12s%n", "structure", "add-end(N)", "front x1000", "middle x1000", "get-kth x1000", "iterate");
            for (String k : kinds) {
                double addEnd = measure(() -> make(k), c -> { for (int i = 0; i < n; i++) c.add(i); sink += c.size(); });
                double front, mid, get;
                if (k.equals("ArrayDeque")) {
                    front = measure(() -> (ArrayDeque<Integer>) filled(k, n), c -> { for (int i = 0; i < 1000; i++) c.addFirst(i); sink += c.size(); });
                    mid = Double.NaN; get = Double.NaN;
                } else {
                    front = measure(() -> (List<Integer>) filled(k, n), c -> { for (int i = 0; i < 1000; i++) c.add(0, i); sink += c.size(); });
                    mid = measure(() -> (List<Integer>) filled(k, n), c -> { for (int i = 0; i < 1000; i++) c.add(c.size() / 2, i); sink += c.size(); });
                    get = measure(() -> (List<Integer>) filled(k, n), c -> { long s = 0; for (int i : idx) s += c.get(i); sink += s; });
                }
                double it = measure(() -> filled(k, n), c -> { long s = 0; for (int x : c) s += x; sink += s; });
                System.out.printf("%-11s %12.1f %14.1f %16s %14s %12.1f%n", k, addEnd, front,
                    Double.isNaN(mid) ? "N/A" : String.format("%.1f", mid), Double.isNaN(get) ? "N/A" : String.format("%.1f", get), it);
            }
        }
        System.out.println("sink=" + sink);
    }
}
