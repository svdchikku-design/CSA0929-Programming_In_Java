import java.util.regex.Pattern;
public class ValidateBench {
    static long sink;
    static String escape(String s) { return s.replace("'", "''"); }
    public static void main(String[] a) {
        Pattern p = Pattern.compile("[A-Za-z0-9_]{1,20}");
        String[] ins = {"admin", "S3cret_pass99", "' OR '1'='1", "admin'-- "};
        int N = 2_000_000; double[] t = new double[3];
        for (int run = 0; run < 8; run++) {
            long a0 = System.nanoTime(); for (int i = 0; i < N; i++) sink += p.matcher(ins[i & 3]).matches() ? 1 : 0;
            long a1 = System.nanoTime(); for (int i = 0; i < N; i++) sink += escape(ins[i & 3]).length();
            long a2 = System.nanoTime(); StringBuilder sb = new StringBuilder(); for (int i = 0; i < N; i++) { sb.setLength(0); sb.append("SELECT * FROM users WHERE id=? AND pwd=?"); sink += sb.length(); }
            long a3 = System.nanoTime();
            if (run >= 3) { t[0] += a1 - a0; t[1] += a2 - a1; t[2] += a3 - a2; }
        }
        System.out.printf("regex validate: %.1f ns/input | quote escaping: %.1f ns/input | fixed '?' SQL text: %.1f ns/query%n", t[0] / 5 / N, t[1] / 5 / N, t[2] / 5 / N);
        for (String s : ins) System.out.println("   input [" + s + "]  regex accepts=" + p.matcher(s).matches() + "  escaped=[" + escape(s) + "]");
    }
}
