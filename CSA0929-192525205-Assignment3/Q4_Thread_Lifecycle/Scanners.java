import java.util.*;
import java.util.concurrent.*;

public class Scanners {
    static final int SCRIPTS = 5;
    static final List<String> order = Collections.synchronizedList(new ArrayList<>());
    static final int[] scanned = new int[4];

    static void scanBatch(int id, long perScriptMs) {
        int n = 0;
        for (int i = 0; i < SCRIPTS; i++) {
            try { Thread.sleep(perScriptMs); } catch (InterruptedException e) { return; }
            n++;
        }
        scanned[id] = n;
        synchronized (order) { order.add("S" + (id + 1));
        System.out.println("Station S" + (id + 1) + " finished: " + n + " scripts (thread " + Thread.currentThread().getName() + ")"); }
    }

    static class StationThread extends Thread {
        final int id; final long ms;
        StationThread(int id, long ms) { super("Station-" + (id + 1)); this.id = id; this.ms = ms; }
        @Override public void run() { scanBatch(id, ms); }
    }
    static class StationTask implements Runnable {
        final int id; final long ms;
        StationTask(int id, long ms) { this.id = id; this.ms = ms; }
        @Override public void run() { scanBatch(id, ms); }
    }

    static void report(String tag, boolean verbose) {
        int total = 0; for (int x : scanned) total += x;
        System.out.println(tag + " ORDER=" + String.join(">", order) + " TOTAL=" + total);
    }

    public static void main(String[] a) throws Exception {
        String mode = a[0]; boolean verbose = a.length > 1 && a[1].equals("v");
        Thread[] t = new Thread[4];
        for (int i = 0; i < 4; i++) t[i] = mode.equals("thread") ? new StationThread(i, 20) : new Thread(new StationTask(i, 20), "Station-" + (i + 1));

        for (Thread x : t) { if (verbose) System.out.println(x.getName() + " state before start: " + x.getState() + " alive=" + x.isAlive()); }
        for (Thread x : t) x.start();
        if (verbose) { Thread.sleep(30); for (Thread x : t) System.out.println(x.getName() + " state after start (~30ms): " + x.getState() + " alive=" + x.isAlive()); }
        for (Thread x : t) x.join();
        if (verbose) for (Thread x : t) System.out.println(x.getName() + " after join: " + x.getState() + " alive=" + x.isAlive());
        report(mode, verbose);
    }
}
