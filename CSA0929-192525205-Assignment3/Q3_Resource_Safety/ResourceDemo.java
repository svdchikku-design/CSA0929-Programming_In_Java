import java.io.*;
import java.nio.file.*;
import java.util.*;

class TrackedResource implements AutoCloseable {
    static int opened = 0, closed = 0;
    final String name; final BufferedReader in; boolean isClosed = false;
    final boolean failOnClose;
    TrackedResource(String path, boolean failOnClose) throws IOException {
        this.name = Path.of(path).getFileName().toString();
        this.in = new BufferedReader(new FileReader(path));
        this.failOnClose = failOnClose;
        opened++;
        System.out.println("    open  " + name + "  (opened=" + opened + ", closed=" + closed + ")");
    }
    TrackedResource(String path) throws IOException { this(path, false); }
    String readLine() throws IOException { return in.readLine(); }
    @Override public void close() throws IOException {
        if (isClosed) return;
        isClosed = true; in.close(); closed++;
        System.out.println("    close " + name + "  (opened=" + opened + ", closed=" + closed + ")");
        if (failOnClose) throw new IOException("close() failed for " + name);
    }
    static void reset() { opened = 0; closed = 0; }
    static int openHandles() { return opened - closed; }
}

public class ResourceDemo {
    static final int F = 5;
    static List<String> files = new ArrayList<>();

    static long parse(String file, String line) {
        try { return Long.parseLong(line.trim()); }
        catch (NumberFormatException e) {
            System.out.println("    LOG: failure caused by file " + file + " line=\"" + line + "\"");
            throw e;
        }
    }

    // (a) manual close at the end of try, no finally
    static long reportA() throws IOException {
        long total = 0;
        for (String f : files) {
            TrackedResource r = new TrackedResource(f);
            total += parse(f, r.readLine());
            r.close();             // skipped when parse() throws
        }
        return total;
    }
    // (b) manual close in finally
    static long reportB() throws IOException {
        long total = 0;
        for (String f : files) {
            TrackedResource r = null;
            try {
                r = new TrackedResource(f);
                total += parse(f, r.readLine());
            } finally {
                if (r != null) r.close();
            }
        }
        return total;
    }
    // (c) try-with-resources
    static long reportC() throws IOException {
        long total = 0;
        for (String f : files) {
            try (TrackedResource r = new TrackedResource(f)) {
                total += parse(f, r.readLine());
            }
        }
        return total;
    }

    interface Report { long run() throws IOException; }
    static void exercise(String label, Report r) {
        TrackedResource.reset();
        System.out.println("--- " + label + " ---");
        try { System.out.println("    total = " + r.run()); }
        catch (Exception e) { System.out.println("    FAILED: " + e); }
        System.out.println("    RESULT: opened=" + TrackedResource.opened + " closed=" + TrackedResource.closed
                + " open handles left=" + TrackedResource.openHandles());
    }

    static void makeStore(Path dir, int badIndex) throws IOException {
        files.clear();
        for (int i = 1; i <= F; i++) {
            Path p = dir.resolve("store" + i + ".txt");
            Files.writeString(p, (i == badIndex ? "ERR#" : String.valueOf(i * 100)) + "\n");
            files.add(p.toString());
        }
    }

    public static void main(String[] args) throws Exception {
        Path dir = Files.createTempDirectory("stores");
        System.out.println("##### GOOD INPUT (all 5 files valid): same report from all versions? #####");
        makeStore(dir, -1);
        exercise("(a) manual close, no finally", ResourceDemo::reportA);
        exercise("(b) manual close in finally", ResourceDemo::reportB);
        exercise("(c) try-with-resources", ResourceDemo::reportC);

        System.out.println("\n##### FAILING INPUT: store3.txt corrupt (f=5), failure part-way #####");
        makeStore(dir, 3);
        exercise("(a) manual close, no finally", ResourceDemo::reportA);
        exercise("(b) manual close in finally", ResourceDemo::reportB);
        exercise("(c) try-with-resources", ResourceDemo::reportC);

        System.out.println("\n##### Repeated failing run x1000 (accumulated leaked handles) #####");
        PrintStream real = System.out;
        for (String label : new String[]{"a","b","c"}) {
            TrackedResource.reset();
            System.setOut(new PrintStream(OutputStream.nullOutputStream()));
            for (int i = 0; i < 1000; i++) {
                try { switch (label) { case "a" -> reportA(); case "b" -> reportB(); default -> reportC(); } }
                catch (Exception e) { }
            }
            System.setOut(real);
            System.out.println("version (" + label + "): opened=" + TrackedResource.opened + " closed=" + TrackedResource.closed + " leaked=" + TrackedResource.openHandles());
        }

        System.out.println("\n##### Closing order in try-with-resources (declared r1, r2 -> closed r2, r1) #####");
        TrackedResource.reset();
        try (TrackedResource r1 = new TrackedResource(files.get(0)); TrackedResource r2 = new TrackedResource(files.get(1))) {
            System.out.println("    body runs");
        }

        System.out.println("\n##### Suppressed exception demo: body throws AND close() throws #####");
        TrackedResource.reset();
        try (TrackedResource r = new TrackedResource(files.get(0), true)) {
            throw new IllegalStateException("body failed");
        } catch (Exception e) {
            System.out.println("    primary   : " + e);
            for (Throwable s : e.getSuppressed()) System.out.println("    suppressed: " + s);
            System.out.println("    getSuppressed().length = " + e.getSuppressed().length);
        }
        System.out.println("  -- same with manual finally (close exception REPLACES body exception) --");
        TrackedResource.reset();
        try {
            TrackedResource r = new TrackedResource(files.get(0), true);
            try { throw new IllegalStateException("body failed"); }
            finally { r.close(); }
        } catch (Exception e) {
            System.out.println("    caught    : " + e);
            System.out.println("    getSuppressed().length = " + e.getSuppressed().length + "  (body exception lost)");
        }
    }
}
