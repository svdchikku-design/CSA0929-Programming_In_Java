import java.lang.management.ManagementFactory;

public class Q1 {
    static final com.sun.management.ThreadMXBean TB =
        (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
    static long sink;
    static final int WARM = 5, RUNS = 7;

    static int plus(String[] f) { String s = ""; for (String x : f) s = s + x + ","; return s.length(); }
    static int sb(String[] f) { StringBuilder b = new StringBuilder(); for (String x : f) b.append(x).append(','); return b.toString().length(); }
    static int sbuf(String[] f) { StringBuffer b = new StringBuffer(); for (String x : f) b.append(x).append(','); return b.toString().length(); }

    interface Fn { int run(String[] f); }

    // returns {avg ns per run, min ns, avg bytes per run}
    static double[] measure(Fn fn, String[] f, int ticketsPerRun) {
        long tid = Thread.currentThread().threadId();
        for (int i = 0; i < WARM; i++) for (int t = 0; t < ticketsPerRun; t++) sink += fn.run(f);
        double sumT = 0, sumB = 0, min = Double.MAX_VALUE;
        for (int r = 0; r < RUNS; r++) {
            System.gc();
            long b0 = TB.getThreadAllocatedBytes(tid);
            long t0 = System.nanoTime();
            for (int t = 0; t < ticketsPerRun; t++) sink += fn.run(f);
            long t1 = System.nanoTime();
            long b1 = TB.getThreadAllocatedBytes(tid);
            sumT += (t1 - t0); sumB += (b1 - b0); min = Math.min(min, t1 - t0);
        }
        return new double[]{sumT / RUNS, min, sumB / RUNS};
    }

    static String[] fields(int n) {
        String[] f = new String[n];
        for (int i = 0; i < n; i++) f[i] = "field" + i;
        return f;
    }

    public static void main(String[] a) {
        System.out.println("warm-up runs=" + WARM + ", measured runs=" + RUNS + " (averaged)");
        String[] names = {"String +", "StringBuilder", "StringBuffer"};
        Fn[] fns = {Q1::plus, Q1::sb, Q1::sbuf};
        int[][] wl = {{10, 100000}, {50, 10000}, {10000, 1}};
        String[] wn = {"(a) 1 ticket x 10 fields [run = 100,000 repeats]", "(b) 10,000 tickets x 50 fields [run = 10,000 tickets]", "(c) 1 ticket x 10,000 fields [run = 1 ticket]"};
        for (int w = 0; w < 3; w++) {
            String[] f = fields(wl[w][0]);
            int tickets = wl[w][1];
            int len = plus(f);
            System.out.println("\nWorkload " + wn[w] + "  ticket length=" + len + " chars");
            System.out.printf("%-14s %14s %14s %16s %16s%n", "approach", "avg ms/run", "min ms/run", "avg bytes/run", "avg ns/ticket");
            for (int i = 0; i < 3; i++) {
                double[] m = measure(fns[i], f, tickets);
                System.out.printf("%-14s %14.4f %14.4f %16.0f %16.1f   bytes/ticket=%.1f%n", names[i], m[0] / 1e6, m[1] / 1e6, m[2], m[0] / tickets, m[2] / tickets);
            }
        }
        System.out.println("sink=" + sink);
    }
}
