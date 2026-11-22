import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;

public class Bench {
    static final int SEATS = 1000, BOOKINGS = 10_000;
    interface Booker { void book(int seat); int sold(); }
    static volatile long sink; static int PREP = 200;

    // non-critical work done before the critical check-then-set (validation + receipt number)
    static long prep(int seat) { long h = seat; for (int i = 0; i < PREP; i++) h = h * 31 + (h >>> 7) + i; return h; }

    static class SyncMethod implements Booker {
        boolean[] b = new boolean[SEATS]; int sold = 0;
        public synchronized void book(int seat) { sink = prep(seat); if (!b[seat]) { b[seat] = true; sold++; } }
        public int sold() { return sold; }
    }
    static class SyncBlock implements Booker {
        boolean[] b = new boolean[SEATS]; int sold = 0;
        public void book(int seat) { sink = prep(seat); synchronized (this) { if (!b[seat]) { b[seat] = true; sold++; } } }
        public int sold() { return sold; }
    }
    static class Reentrant implements Booker {
        boolean[] b = new boolean[SEATS]; int sold = 0; final ReentrantLock lock = new ReentrantLock();
        public void book(int seat) { sink = prep(seat); lock.lock(); try { if (!b[seat]) { b[seat] = true; sold++; } } finally { lock.unlock(); } }
        public int sold() { return sold; }
    }
    static class Atomic implements Booker {
        AtomicBoolean[] b = new AtomicBoolean[SEATS]; AtomicInteger sold = new AtomicInteger();
        Atomic() { for (int i = 0; i < SEATS; i++) b[i] = new AtomicBoolean(); }
        public void book(int seat) { sink = prep(seat); if (b[seat].compareAndSet(false, true)) sold.incrementAndGet(); }
        public int sold() { return sold.get(); }
    }

    static double runOnce(Booker bk, int nThreads) throws Exception {
        Thread[] t = new Thread[nThreads];
        int per = BOOKINGS / nThreads;
        for (int k = 0; k < nThreads; k++) {
            final int start = k * per;
            t[k] = new Thread(() -> { for (int i = 0; i < per; i++) bk.book((start + i) % SEATS); });
        }
        long t0 = System.nanoTime();
        for (Thread x : t) x.start(); for (Thread x : t) x.join();
        double ms = (System.nanoTime() - t0) / 1e6;
        if (bk.sold() != SEATS) throw new IllegalStateException("WRONG sold=" + bk.sold());
        return ms;
    }

    public static void main(String[] a) throws Exception {
        if (a.length > 0) PREP = Integer.parseInt(a[0]);
        System.out.println("non-critical prep loop iterations per booking = " + PREP);
        String[] names = {"synchronized method", "synchronized block", "ReentrantLock", "AtomicBoolean[] CAS"};
        for (int nThreads : new int[]{1, 8}) {
            System.out.println("=== " + BOOKINGS + " bookings over " + SEATS + " seats, " + nThreads + " thread(s); 3 warm-up runs discarded, 7 timed runs ===");
            for (int v = 0; v < 4; v++) {
                for (int w = 0; w < 3; w++) runOnce(make(v), nThreads);
                double sum = 0, min = 1e9, max = 0; StringBuilder sb = new StringBuilder();
                for (int r = 0; r < 7; r++) { double ms = runOnce(make(v), nThreads); sum += ms; min = Math.min(min, ms); max = Math.max(max, ms); sb.append(String.format("%.2f ", ms)); }
                System.out.printf("%-22s avg=%.2f ms  min=%.2f max=%.2f  runs: %s%n", names[v], sum / 7, min, max, sb);
            }
        }
    }
    static Booker make(int v) { return switch (v) { case 0 -> new SyncMethod(); case 1 -> new SyncBlock(); case 2 -> new Reentrant(); default -> new Atomic(); }; }
}
