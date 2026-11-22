import java.util.*;
import java.util.function.*;

public class Q4 {
    static long sink;
    static final int WARM = 8, RUNS = 7;

    static double ms(Runnable r) {
        for (int i = 0; i < WARM; i++) r.run();
        double s = 0;
        for (int i = 0; i < RUNS; i++) { long t0 = System.nanoTime(); r.run(); s += System.nanoTime() - t0; }
        return s / RUNS / 1e6;
    }

    static Map<String,Integer> make(String k) {
        return k.equals("HashMap") ? new HashMap<>() : k.equals("TreeMap") ? new TreeMap<>() : new LinkedHashMap<>();
    }

    static String codeOrderReport(Map<String,Integer> m) {
        StringBuilder sb = new StringBuilder();
        if (m instanceof TreeMap) { for (Map.Entry<String,Integer> e : m.entrySet()) sb.append(e.getKey()).append(':').append(e.getValue()).append('\n'); }
        else {
            List<String> keys = new ArrayList<>(m.keySet());
            Collections.sort(keys);
            for (String k : keys) sb.append(k).append(':').append(m.get(k)).append('\n');
        }
        return sb.toString();
    }
    static String natural(Map<String,Integer> m) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String,Integer> e : m.entrySet()) sb.append(e.getKey()).append(':').append(e.getValue()).append('\n');
        return sb.toString();
    }

    public static void main(String[] a) {
        System.out.println("warm-up=" + WARM + " measured runs=" + RUNS + "; times in ms (avg). Keys=String codes MEDxxxxxx inserted in seeded-random order; 100,000 random successful lookups");
        String[] kinds = {"HashMap", "TreeMap", "LinkedHashMap"};
        for (int n : new int[]{1000, 10000, 100000}) {
            List<Integer> perm = new ArrayList<>();
            for (int i = 0; i < n; i++) perm.add(i);
            Collections.shuffle(perm, new Random(7));
            String[] keys = new String[n];
            for (int i = 0; i < n; i++) keys[i] = String.format("MED%06d", perm.get(i));
            Random rnd = new Random(3);
            String[] look = new String[100000];
            for (int i = 0; i < look.length; i++) look[i] = keys[rnd.nextInt(n)];
            String[] sorted = keys.clone(); Arrays.sort(sorted);
            StringBuilder eCode = new StringBuilder(), eIns = new StringBuilder();
            for (String k : sorted) eCode.append(k).append(':').append(Integer.parseInt(k.substring(3)) % 500).append('\n');
            for (String k : keys) eIns.append(k).append(':').append(Integer.parseInt(k.substring(3)) % 500).append('\n');

            System.out.println("\nN = " + n + " medicine codes");
            System.out.printf("%-14s %10s %14s %10s %16s %18s | natural iteration == code order? == insertion order? | code-report correct? | insertion-report%n",
                "map", "put-all", "100k lookups", "iterate", "code-order rpt", "insertion-order rpt");
            for (String k : kinds) {
                double put = ms(() -> { Map<String,Integer> m = make(k); for (String s : keys) m.put(s, Integer.parseInt(s.substring(3)) % 500); sink += m.size(); });
                Map<String,Integer> m = make(k);
                for (String s : keys) m.put(s, Integer.parseInt(s.substring(3)) % 500);
                double look100k = ms(() -> { long s = 0; for (String s2 : look) s += m.get(s2); sink += s; });
                double it = ms(() -> { long s = 0; for (Map.Entry<String,Integer> e : m.entrySet()) s += e.getValue(); sink += s; });
                double code = ms(() -> sink += codeOrderReport(m).length());
                String insRpt; String insStr;
                if (k.equals("LinkedHashMap")) { insRpt = String.format("%.3f", ms(() -> sink += natural(m).length())); }
                else insRpt = "N/A(native)";
                String nat = natural(m);
                boolean codeOk = codeOrderReport(m).contentEquals(eCode);
                boolean natCode = nat.contentEquals(eCode), natIns = nat.contentEquals(eIns);
                System.out.printf("%-14s %10.3f %14.3f %10.3f %16.3f %18s | %s / %s | %s | %s%n", k, put, look100k, it, code, insRpt, natCode, natIns, codeOk,
                    k.equals("LinkedHashMap") ? ("correct=" + natIns) : "needs extra structure");
            }
        }
        System.out.println("sink=" + sink);
    }
}
