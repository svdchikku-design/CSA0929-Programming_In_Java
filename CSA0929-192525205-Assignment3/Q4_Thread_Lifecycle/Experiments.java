import java.util.*;

public class Experiments {
    static final List<String> order = Collections.synchronizedList(new ArrayList<>());
    static Runnable work(String name, int scripts, long ms) {
        return () -> {
            try { for (int i = 0; i < scripts; i++) Thread.sleep(ms); } catch (InterruptedException e) { }
            order.add(name);
        };
    }
    // CPU-bound variant, to make priorities matter if they ever could
    static Runnable cpu(String name) {
        return () -> { long x = 0; for (long i = 0; i < 400_000_000L; i++) x += i ^ (i >> 3); if (x == 42) System.out.println(); order.add(name); };
    }
    public static void main(String[] a) throws Exception {
        switch (a[0]) {
            case "prio" -> {   // S1 MAX, S2 NORM, S3 NORM, S4 MIN ; sleep-based
                Thread[] t = new Thread[4]; int[] p = {Thread.MAX_PRIORITY, 5, 5, Thread.MIN_PRIORITY};
                for (int i = 0; i < 4; i++) { t[i] = new Thread(work("S" + (i + 1), 5, 20), "S" + (i + 1)); t[i].setPriority(p[i]); }
                for (Thread x : t) x.start(); for (Thread x : t) x.join();
                System.out.println("prio-sleep (S1=10,S2=5,S3=5,S4=1) ORDER=" + String.join(">", order));
            }
            case "prioInv" -> { // reverse priorities, sleep-based
                Thread[] t = new Thread[4]; int[] p = {Thread.MIN_PRIORITY, 5, 5, Thread.MAX_PRIORITY};
                for (int i = 0; i < 4; i++) { t[i] = new Thread(work("S" + (i + 1), 5, 20), "S" + (i + 1)); t[i].setPriority(p[i]); }
                for (Thread x : t) x.start(); for (Thread x : t) x.join();
                System.out.println("prio-sleep (S1=1,S2=5,S3=5,S4=10) ORDER=" + String.join(">", order));
            }
            case "prioCpu" -> {
                Thread[] t = new Thread[4]; int[] p = {Thread.MAX_PRIORITY, 5, 5, Thread.MIN_PRIORITY};
                for (int i = 0; i < 4; i++) { t[i] = new Thread(cpu("S" + (i + 1)), "S" + (i + 1)); t[i].setPriority(p[i]); }
                long t0 = System.nanoTime();
                for (Thread x : t) x.start(); for (Thread x : t) x.join();
                System.out.println("prio-cpu (S1=10,S2=5,S3=5,S4=1) ORDER=" + String.join(">", order) + " ms=" + (System.nanoTime() - t0) / 1_000_000);
            }
            case "daemon" -> {
                Thread d = new Thread(() -> {
                    try { for (int i = 1; i <= 5; i++) { Thread.sleep(200); System.out.println("  daemon S4 scanned script " + i); } }
                    catch (InterruptedException e) { }
                    System.out.println("  daemon S4 FINISHED all 5 scripts");
                }, "S4-daemon");
                d.setDaemon(a.length > 1 && a[1].equals("daemon"));
                System.out.println("S4 isDaemon=" + d.isDaemon());
                d.start();
                Thread.sleep(500);
                System.out.println("main() ends now (S4 alive=" + d.isAlive() + ")");
            }
            case "runVsStart" -> {
                Runnable r = () -> System.out.println("  task body runs on thread: " + Thread.currentThread().getName() + " (id " + Thread.currentThread().threadId() + ")");
                System.out.println("main thread: " + Thread.currentThread().getName() + " (id " + Thread.currentThread().threadId() + ")");
                Thread t1 = new Thread(r, "Station-A");
                System.out.println("calling t1.run():");  t1.run();
                System.out.println("  t1.getState() after run() = " + t1.getState() + ", isAlive=" + t1.isAlive());
                Thread t2 = new Thread(r, "Station-B");
                System.out.println("calling t2.start():"); t2.start(); t2.join();
                System.out.println("  t2.getState() after join = " + t2.getState());
                System.out.println("calling t2.start() a second time:");
                try { t2.start(); } catch (IllegalThreadStateException e) { System.out.println("  " + e); }
                // run() blocks caller: timing
                Runnable slow = () -> { try { Thread.sleep(300); } catch (InterruptedException e) { } };
                long s = System.nanoTime(); for (int i = 0; i < 4; i++) new Thread(slow).run();
                System.out.println("4 x run() of 300ms task took ~" + (System.nanoTime() - s) / 1_000_000 + " ms (sequential)");
                s = System.nanoTime(); Thread[] ts = new Thread[4]; for (int i = 0; i < 4; i++) { ts[i] = new Thread(slow); ts[i].start(); } for (Thread x : ts) x.join();
                System.out.println("4 x start() of 300ms task took ~" + (System.nanoTime() - s) / 1_000_000 + " ms (parallel)");
            }
        }
    }
}
