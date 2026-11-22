import java.util.*;

class Pt {
    final String name; final int sev; final long arr;
    Pt(String n, int s, long a) { name = n; sev = s; arr = a; }
    public String toString() { return name + "(S" + sev + ")"; }
}

public class Q6Exp {
    static long sink;
    static final int WARM = 8, RUNS = 7;
    static final Comparator<Pt> CMP = Comparator.comparingInt((Pt p) -> p.sev).thenComparingLong(p -> p.arr);

    interface Setup<T> { T get(); }
    interface Op<T> { void run(T t); }
    static <T> double us(Setup<T> setup, Op<T> op) {
        for (int i = 0; i < WARM; i++) op.run(setup.get());
        double s = 0;
        for (int i = 0; i < RUNS; i++) { T x = setup.get(); long t0 = System.nanoTime(); op.run(x); s += System.nanoTime() - t0; }
        return s / RUNS / 1e3;
    }
    static Pt[] gen(int n) {
        Random r = new Random(11); Pt[] p = new Pt[n];
        for (int i = 0; i < n; i++) p[i] = new Pt("P" + i, 1 + r.nextInt(5), i);
        return p;
    }
    static PriorityQueue<Pt> fillPQ(Pt[] ps) { PriorityQueue<Pt> q = new PriorityQueue<>(CMP); for (Pt p : ps) q.add(p); return q; }
    static LinkedList<Pt> fillLL(Pt[] ps) { LinkedList<Pt> l = new LinkedList<>(); for (Pt p : ps) llInsert(l, p); return l; }
    static void llInsert(LinkedList<Pt> l, Pt p) {
        ListIterator<Pt> it = l.listIterator();
        while (it.hasNext()) { if (CMP.compare(it.next(), p) > 0) { it.previous(); break; } }
        it.add(p);
    }
    @SuppressWarnings("unchecked")
    static ArrayDeque<Pt>[] fillQ5(Pt[] ps) { ArrayDeque<Pt>[] q = new ArrayDeque[5]; for (int i = 0; i < 5; i++) q[i] = new ArrayDeque<>(); for (Pt p : ps) q[p.sev - 1].addLast(p); return q; }
    static Pt serve5(ArrayDeque<Pt>[] q) { for (ArrayDeque<Pt> d : q) if (!d.isEmpty()) return d.pollFirst(); return null; }

    public static void main(String[] a) {
        System.out.println("== 8-patient hand-checkable example (name, severity, arrival order A..H) ==");
        String[] nm = {"A","B","C","D","E","F","G","H"}; int[] sv = {5,4,3,2,1,3,1,2};
        PriorityQueue<Pt> q = new PriorityQueue<>(CMP);
        for (int i = 0; i < 8; i++) q.add(new Pt(nm[i], sv[i], i));
        System.out.println("arrivals: A(S5) B(S4) C(S3) D(S2) E(S1) F(S3) G(S1) H(S2)");
        System.out.println("hand-derived treatment order: E G D H C F B A   (S1 by arrival, then S2, S3, S4, S5)");
        System.out.println("System.out.println(queue) [heap array order]: " + q);
        StringBuilder sb = new StringBuilder(); Pt p;
        while ((p = q.poll()) != null) sb.append(p.name).append(' ');
        System.out.println("poll() order: " + sb.toString().trim());
        System.out.println("matches hand order: " + sb.toString().trim().equals("E G D H C F B A"));

        System.out.println("\nwarm-up=" + WARM + " measured runs=" + RUNS + "; times in microseconds (avg) for whole batch; per-op in ns in brackets");
        System.out.printf("%n%-8s | %-26s %-26s%n", "N", "PriorityQueue add-all", "PriorityQueue poll-all");
        for (int n : new int[]{1000, 10000, 30000, 100000}) {
            Pt[] ps = gen(n);
            double add = us(() -> new PriorityQueue<Pt>(CMP), x -> { for (Pt pp : ps) x.add(pp); sink += x.size(); });
            double poll = us(() -> fillPQ(ps), x -> { Pt pp; while ((pp = x.poll()) != null) sink += pp.sev; });
            System.out.printf("%-8d | %10.1f [%7.1f ns/op] %10.1f [%7.1f ns/op]%n", n, add, add * 1000 / n, poll, poll * 1000 / n);
        }
        System.out.printf("%n%-8s | %-28s %-28s | %-28s %-28s%n", "N", "sorted LinkedList insert-all", "sorted LinkedList serve-all", "5 FIFO queues add-all", "5 FIFO queues serve-all");
        for (int n : new int[]{1000, 10000, 30000}) {
            Pt[] ps = gen(n);
            double lAdd = us(() -> new LinkedList<Pt>(), x -> { for (Pt pp : ps) llInsert(x, pp); sink += x.size(); });
            double lSrv = us(() -> fillLL(ps), x -> { Pt pp; while ((pp = x.pollFirst()) != null) sink += pp.sev; });
            double fAdd = us(() -> { @SuppressWarnings("unchecked") ArrayDeque<Pt>[] z = new ArrayDeque[5]; for (int i = 0; i < 5; i++) z[i] = new ArrayDeque<>(); return z; },
                x -> { for (Pt pp : ps) x[pp.sev - 1].addLast(pp); sink += x[0].size(); });
            double fSrv = us(() -> fillQ5(ps), x -> { Pt pp; while ((pp = serve5(x)) != null) sink += pp.sev; });
            System.out.printf("%-8d | %10.1f [%8.1f ns/op] %10.1f [%8.1f ns/op] | %10.1f [%6.1f ns/op] %10.1f [%6.1f ns/op]%n", n, lAdd, lAdd * 1000 / n, lSrv, lSrv * 1000 / n, fAdd, fAdd * 1000 / n, fSrv, fSrv * 1000 / n);
        }
        // order equivalence check
        Pt[] ps = gen(5000);
        PriorityQueue<Pt> pq = fillPQ(ps); LinkedList<Pt> ll = fillLL(ps); ArrayDeque<Pt>[] f5 = fillQ5(ps);
        boolean same = true; Pt x;
        while ((x = pq.poll()) != null) { if (ll.pollFirst() != x || serve5(f5) != x) { same = false; break; } }
        System.out.println("\nservice order identical across PQ / sorted LinkedList / 5 FIFO queues (N=5000): " + same);
        System.out.println("sink=" + sink);
    }
}
