import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ResultProcessor {
    static class CorruptFileException extends Exception {
        CorruptFileException(String m, Throwable c) { super(m, c); }
    }
    /** One task = one score file. Simulates 1 ms of disk/network latency per file. */
    static class ScoreTask implements Callable<Integer> {
        final Path file;
        ScoreTask(Path f) { file = f; }
        public Integer call() throws Exception {
            Thread.sleep(1);                                   // simulated I/O latency
            try {
                String[] p = Files.readString(file).trim().split(",");
                int mark = Integer.parseInt(p[1].trim());
                if (mark < 0 || mark > 100) throw new IllegalArgumentException("mark out of range");
                return mark;
            } catch (NoSuchFileException e) {
                throw e;                                       // missing: propagate as is
            } catch (IOException e) {
                throw e;
            } catch (RuntimeException e) {                     // NumberFormat / ArrayIndex / range
                throw new CorruptFileException("corrupt " + file.getFileName(), e);
            }
        }
    }
    static class Summary {
        final ConcurrentHashMap<String, Integer> marks = new ConcurrentHashMap<>();
        final ConcurrentLinkedQueue<String> failures = new ConcurrentLinkedQueue<>();
        final AtomicInteger missing = new AtomicInteger(), corrupt = new AtomicInteger(), other = new AtomicInteger();
        String line() {
            return "ok=" + marks.size() + " corrupt=" + corrupt + " missing=" + missing + " other=" + other
                    + " total=" + (marks.size() + corrupt.get() + missing.get() + other.get());
        }
    }
    static void record(Summary s, Path f, Throwable t) {
        Throwable c = t instanceof ExecutionException ? t.getCause() : t;
        if (c instanceof NoSuchFileException) s.missing.incrementAndGet();
        else if (c instanceof CorruptFileException) s.corrupt.incrementAndGet();
        else s.other.incrementAndGet();
        s.failures.add(f.getFileName() + " -> " + c.getClass().getSimpleName());
    }
    static Summary runPool(List<Path> files, int threads) throws Exception {
        ExecutorService ex = Executors.newFixedThreadPool(threads);
        Summary s = new Summary();
        List<Future<Integer>> fs = new ArrayList<>();
        for (Path f : files) fs.add(ex.submit(new ScoreTask(f)));
        for (int i = 0; i < files.size(); i++) {
            try { s.marks.put(files.get(i).getFileName().toString(), fs.get(i).get()); }
            catch (ExecutionException e) { record(s, files.get(i), e); }   // one failure never stops the run
        }
        ex.shutdown();
        return s;
    }
    static Summary runParallelStream(List<Path> files) {
        Summary s = new Summary();
        files.parallelStream().forEach(f -> {
            try { s.marks.put(f.getFileName().toString(), new ScoreTask(f).call()); }
            catch (Exception e) { record(s, f, e); }
        });
        return s;
    }
    static Summary runThreadPerFile(List<Path> files) throws Exception {
        Summary s = new Summary();
        List<Thread> ts = new ArrayList<>();
        for (Path f : files) {
            Thread t = new Thread(() -> {
                try { s.marks.put(f.getFileName().toString(), new ScoreTask(f).call()); }
                catch (Exception e) { record(s, f, e); }
            });
            ts.add(t); t.start();
        }
        for (Thread t : ts) t.join();
        return s;
    }
    static List<Path> generate(Path dir, int n, int corruptMod, int corruptRem, int missingMod, int missingRem) throws IOException {
        List<Path> list = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Path p = dir.resolve("s" + i + ".txt");
            list.add(p);
            if (i % missingMod == missingRem) continue;                      // missing file
            Files.writeString(p, i % corruptMod == corruptRem ? "REG" + i + ",abc" : "REG" + i + "," + (i % 101));
        }
        return list;
    }
    interface R { Summary run() throws Exception; }
    static double avgMs(R r, int runs) throws Exception {
        r.run(); long t = 0; Summary last = null;
        for (int i = 0; i < runs; i++) { long a = System.nanoTime(); last = r.run(); t += System.nanoTime() - a; }
        System.out.println("      last summary: " + last.line());
        return t / (double) runs / 1e6;
    }
    public static void main(String[] a) throws Exception {
        Path small = Files.createTempDirectory("small");
        List<Path> sf = generate(small, 20, 7, 3, 10, 4);   // corrupt: i=3,10,17 ; missing: i=4,14
        Summary hs = runPool(sf, 4);
        System.out.println("HAND-CHECKED SET (20 files; expected ok=15 corrupt=3 missing=2): " + hs.line());
        System.out.println("  failures: " + new TreeSet<>(hs.failures));
        int expectedSum = 0; for (int i = 0; i < 20; i++) if (!(i % 10 == 4) && !(i % 7 == 3)) expectedSum += i % 101;
        int got = hs.marks.values().stream().mapToInt(Integer::intValue).sum();
        System.out.println("  sum of marks expected=" + expectedSum + " got=" + got);

        Path big = Files.createTempDirectory("big");
        List<Path> files = generate(big, 10000, 50, 7, 100, 13);   // 200 corrupt, 100 missing, 9700 ok
        System.out.println("\nTIMING: 10,000 files (200 corrupt, 100 missing), 1 ms simulated latency per file, 1 warm-up + 3 timed runs");
        for (int p : new int[]{1, 2, 4, 8, 16, 64}) {
            final int pp = p;
            double ms = avgMs(() -> runPool(files, pp), 3);
            System.out.printf("  pool=%-3d avg=%.0f ms%n", p, ms);
        }
        double ps = avgMs(() -> runParallelStream(files), 3);
        System.out.printf("  parallelStream (common pool, parallelism=%d) avg=%.0f ms%n", ForkJoinPool.commonPool().getParallelism(), ps);
        long t0 = System.nanoTime(); Summary s = runThreadPerFile(files); double tp = (System.nanoTime() - t0) / 1e6;
        System.out.printf("  thread-per-file (10,000 threads) 1 run=%.0f ms  %s%n", tp, s.line());
        System.out.println("cores=" + Runtime.getRuntime().availableProcessors());
    }
}
