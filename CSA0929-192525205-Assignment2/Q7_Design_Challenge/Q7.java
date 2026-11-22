import java.util.*;

public class Q7 {
    static final int T = 500, SWEEP_EVERY = 10;
    static final int WARM = 3, RUNS = 7;

    static final class Pat {
        final int arrival, origSev; int sev; int lastBump; final long key;
        Pat(int a, int s) { arrival = a; origSev = s; sev = s; lastBump = a; key = (long) a + (long) (s - 1) * T; }
    }
    static final class Res {
        long offers, polls, sweepVisits, rebuildInserts, comparisons, sweeps;
        int served, leftover, s5Arrived, s5Served, s5Unserved, maxWaitS5 = -1, maxWaitAll = -1;
    }
    enum D { ORIGINAL_AGING_SWEEP, REDESIGN_TIME_INVARIANT_KEY, BASELINE_STRICT_NO_AGING }

    static Res run(D d, int n, boolean count) {
        Random rnd = new Random(42);
        Res r = new Res();
        Comparator<Pat> base;
        if (d == D.REDESIGN_TIME_INVARIANT_KEY)
            base = (x, y) -> { int c = Long.compare(x.key, y.key); return c != 0 ? c : Integer.compare(x.arrival, y.arrival); };
        else
            base = (x, y) -> { int c = Integer.compare(x.sev, y.sev); return c != 0 ? c : Integer.compare(x.arrival, y.arrival); };
        Comparator<Pat> cmp = count ? (x, y) -> { r.comparisons++; return base.compare(x, y); } : base;
        PriorityQueue<Pat> pq = new PriorityQueue<>(cmp);
        int admissions = 0;
        for (int t = 0; t < n; t++) {
            Pat p = new Pat(t, 1 + rnd.nextInt(5));
            if (p.origSev == 5) r.s5Arrived++;
            pq.offer(p); r.offers++; admissions++;
            if (d == D.ORIGINAL_AGING_SWEEP && admissions % SWEEP_EVERY == 0) {
                r.sweeps++;
                List<Pat> all = new ArrayList<>(pq);
                r.sweepVisits += all.size();
                for (Pat q : all) if (q.sev > 1 && t - q.lastBump >= T) { q.sev--; q.lastBump = t; }
                pq.clear();
                for (Pat q : all) pq.offer(q);   // rebuild
                r.rebuildInserts += all.size();
            }
            if ((t + 1) % 2 == 0) {
                Pat s = pq.poll();
                if (s != null) {
                    r.polls++; r.served++;
                    int w = t - s.arrival;
                    if (w > r.maxWaitAll) r.maxWaitAll = w;
                    if (s.origSev == 5) { r.s5Served++; if (w > r.maxWaitS5) r.maxWaitS5 = w; }
                }
            }
        }
        r.leftover = pq.size();
        for (Pat q : pq) if (q.origSev == 5) r.s5Unserved++;
        return r;
    }

    public static void main(String[] a) {
        System.out.println("T=" + T + ", sweep every " + SWEEP_EVERY + " admissions, 1 arrival per time unit, 1 patient served after every 2nd arrival, Random(42) severities 1..5");
        System.out.println("warm-up=" + WARM + " measured runs=" + RUNS + " (avg ms). counts from a separate instrumented run. visits = patients scanned by sweeps; rebuild = re-inserts after sweep");
        for (int n : new int[]{5000, 10000, 20000, 40000}) {
            System.out.println("\nN = " + n + " arrivals");
            System.out.printf("%-30s %10s %10s %10s %12s %12s %12s %12s | %8s %9s %8s %9s %8s%n", "design", "avg ms", "min ms", "offers", "polls", "sweepVisits", "rebuildIns", "comparisons", "S5 arr", "S5 served", "S5 never", "maxWaitS5", "maxWaitAll");
            for (D d : D.values()) {
                for (int i = 0; i < WARM; i++) run(d, n, false);
                double sum = 0, min = Double.MAX_VALUE;
                for (int i = 0; i < RUNS; i++) { long t0 = System.nanoTime(); run(d, n, false); long dt = System.nanoTime() - t0; sum += dt; min = Math.min(min, dt); }
                Res r = run(d, n, true);
                System.out.printf("%-30s %10.3f %10.3f %10d %12d %12d %12d %12d | %8d %9d %8d %9s %8d   (served=%d leftover=%d sweeps=%d)%n", d, sum / RUNS / 1e6, min / 1e6, r.offers, r.polls, r.sweepVisits, r.rebuildInserts, r.comparisons,
                    r.s5Arrived, r.s5Served, r.s5Unserved, r.maxWaitS5 < 0 ? "none" : String.valueOf(r.maxWaitS5), r.maxWaitAll, r.served, r.leftover, r.sweeps);
            }
        }
    }
}
