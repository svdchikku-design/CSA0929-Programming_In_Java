import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class Bench4 {
    static long sink;
    static long usedHeap() { System.gc(); System.gc(); Runtime r = Runtime.getRuntime(); return r.totalMemory() - r.freeMemory(); }

    // ---- T1: dispatch for k buttons: if-else on action command vs per-button listener
    interface L { void act(); }
    static void t1() {
        System.out.println("## T1 dispatch of one event, k buttons (ns per dispatch, worst case = last button; avg of 5 after warm-up)");
        System.out.println("k | if-else equals() chain | direct listener call | k listener objects (bytes, measured)");
        for (int k : new int[]{3, 10, 50, 200}) {
            String[] cmds = new String[k]; for (int i = 0; i < k; i++) cmds[i] = "Button" + i;
            String target = new String("Button" + (k - 1));
            int N = 2_000_000; double a = 0, b = 0;
            L[] ls = new L[k]; for (int i = 0; i < k; i++) { final int id = i; ls[i] = () -> sink += id; }
            for (int run = 0; run < 8; run++) {
                long t0 = System.nanoTime();
                for (int n = 0; n < N; n++) { for (int i = 0; i < k; i++) { if (cmds[i].equals(target)) { sink += i; break; } } }
                long t1 = System.nanoTime();
                L l = ls[k - 1]; for (int n = 0; n < N; n++) l.act();
                long t2 = System.nanoTime();
                if (run >= 3) { a += (t1 - t0); b += (t2 - t1); }
            }
            long before = usedHeap(); L[] objs = new L[k]; for (int i = 0; i < k; i++) { final int id = i; objs[i] = new L() { long x = id; public void act() { sink += x; } }; }
            long after = usedHeap(); sink += objs.length;
            System.out.printf("%d | %.1f | %.2f | ~%d%n", k, a / 5 / N, b / 5 / N, Math.max(0, (after - before)));
        }
    }
    // ---- T3: layout computation (validate) for 400 components
    static void t3() throws Exception {
        System.out.println("## T3 layout time (validate/doLayout) and container count for 400 components in a 800x600 frame; avg of 30 after 10 warm-ups, ms");
        String[] names = {"nested BorderLayout+FlowLayout", "GridBagLayout", "null layout + setBounds"};
        for (int d = 0; d < 3; d++) {
            final int design = d; final JFrame[] fr = new JFrame[1]; final int[] containers = {0};
            SwingUtilities.invokeAndWait(() -> {
                JFrame f = new JFrame(); f.setSize(800, 600); JPanel root = new JPanel();
                if (design == 0) {
                    root.setLayout(new BorderLayout()); JPanel top = new JPanel(new FlowLayout()); JPanel mid = new JPanel(new FlowLayout()); JPanel bot = new JPanel(new FlowLayout());
                    for (int i = 0; i < 400; i++) (i < 100 ? top : i < 300 ? mid : bot).add(new JButton("b" + i));
                    root.add(top, BorderLayout.NORTH); root.add(mid, BorderLayout.CENTER); root.add(bot, BorderLayout.SOUTH); containers[0] = 4;
                } else if (design == 1) {
                    root.setLayout(new GridBagLayout()); GridBagConstraints c = new GridBagConstraints(); c.fill = GridBagConstraints.BOTH; c.weightx = 1; c.weighty = 1;
                    for (int i = 0; i < 400; i++) { c.gridx = i % 20; c.gridy = i / 20; root.add(new JButton("b" + i), c); } containers[0] = 1;
                } else {
                    root.setLayout(null); for (int i = 0; i < 400; i++) { JButton b = new JButton("b" + i); b.setBounds((i % 20) * 38, (i / 20) * 28, 36, 26); root.add(b); } containers[0] = 1;
                }
                f.setContentPane(root); f.setVisible(true); fr[0] = f;
            });
            Thread.sleep(500); double tot = 0;
            for (int run = 0; run < 40; run++) {
                final long[] t = new long[1];
                SwingUtilities.invokeAndWait(() -> { fr[0].getContentPane().invalidate(); long a = System.nanoTime(); fr[0].getContentPane().validate(); t[0] = System.nanoTime() - a; });
                if (run >= 10) tot += t[0];
            }
            System.out.printf("%s | containers=%d | %.3f ms%n", names[d], containers[0], tot / 30 / 1e6);
            SwingUtilities.invokeAndWait(fr[0]::dispose);
        }
    }
    // ---- T4: repaint with n stored points vs one off-screen image
    static void t4() {
        System.out.println("## T4 repaint cost: redraw n points (6x6 ovals) vs copy one 400x300 BufferedImage; avg of 20 after 10 warm-ups, us");
        System.out.println("n | redraw n points | drawImage of 400x300 | memory: points (bytes, measured) | memory image (bytes)");
        BufferedImage screen = new BufferedImage(400, 300, BufferedImage.TYPE_INT_RGB); Graphics2D g = screen.createGraphics();
        BufferedImage off = new BufferedImage(400, 300, BufferedImage.TYPE_INT_RGB);
        for (int n : new int[]{100, 1000, 10000, 100000}) {
            long before = usedHeap(); List<Point> pts = new ArrayList<>(); Random r = new Random(1); for (int i = 0; i < n; i++) pts.add(new Point(r.nextInt(400), r.nextInt(300)));
            long after = usedHeap();
            Graphics2D og = off.createGraphics(); for (Point p : pts) og.fillOval(p.x - 3, p.y - 3, 6, 6); og.dispose();
            double a = 0, b = 0;
            for (int run = 0; run < 30; run++) {
                long t0 = System.nanoTime(); for (Point p : pts) g.fillOval(p.x - 3, p.y - 3, 6, 6); long t1 = System.nanoTime();
                g.drawImage(off, 0, 0, null); long t2 = System.nanoTime();
                if (run >= 10) { a += t1 - t0; b += t2 - t1; }
            }
            System.out.printf("%d | %.0f | %.0f | ~%d | %d%n", n, a / 20 / 1e3, b / 20 / 1e3, after - before, 400 * 300 * 4);
            sink += pts.size();
        }
    }
    // ---- T6: load all 100,000 rows in a JTable model vs one page of 50
    static void t6() {
        System.out.println("## T6 JTable model: add 100,000 rows at once vs a 50-row page (in-memory; database latency NOT included); avg of 5 after 2 warm-ups");
        for (int[] cfg : new int[][]{{100000}, {50}}) {
            int n = cfg[0]; double tot = 0; long mem = 0;
            for (int run = 0; run < 7; run++) {
                long before = usedHeap(); long t0 = System.nanoTime();
                DefaultTableModel m = new DefaultTableModel(new String[]{"Student ID", "Name", "Event"}, 0);
                for (int i = 0; i < n; i++) m.addRow(new Object[]{String.format("%09d", i), "Student " + i, "Quiz"});
                long t1 = System.nanoTime(); long after = usedHeap();
                if (run >= 2) { tot += t1 - t0; mem += after - before; } sink += m.getRowCount();
            }
            System.out.printf("%d rows | build model %.2f ms | heap ~%.1f MB%n", n, tot / 5 / 1e6, mem / 5 / 1048576.0);
        }
    }
    public static void main(String[] a) throws Exception { t1(); t3(); t4(); t6(); }
}
