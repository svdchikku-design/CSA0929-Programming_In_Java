import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class RegistrationApp extends JFrame {
    static final String[] EVENTS = {"Dance", "Music", "Quiz", "Hackathon"};
    static final int PAGE = 50;
    private final RegistrationService service;
    private final JTextField sid = new JTextField(9), name = new JTextField(14);
    private final JComboBox<String> event = new JComboBox<>(EVENTS);
    private final JComboBox<String> filter = new JComboBox<>(new String[]{"All", "Dance", "Music", "Quiz", "Hackathon"});
    private final DefaultTableModel model = new DefaultTableModel(new String[]{"Student ID", "Name", "Event"}, 0);
    private final JLabel status = new JLabel(" Ready"), pageLabel = new JLabel("Page 1");
    private int pageNo = 0;

    public RegistrationApp(RegistrationService service) {
        super("Campus Event Registration");
        this.service = service;
        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("File"); JMenuItem export = new JMenuItem("Export CSV..."), exit = new JMenuItem("Exit");
        file.add(export); file.addSeparator(); file.add(exit); bar.add(file); setJMenuBar(bar);
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton register = new JButton("Register");
        form.add(new JLabel("Student ID")); form.add(sid); form.add(new JLabel("Name")); form.add(name); form.add(event); form.add(register);
        JPanel nav = new JPanel(new FlowLayout());
        JButton prev = new JButton("Prev"), next = new JButton("Next");
        nav.add(new JLabel("Filter")); nav.add(filter); nav.add(prev); nav.add(pageLabel); nav.add(next);
        JPanel south = new JPanel(new BorderLayout()); south.add(nav, BorderLayout.CENTER); south.add(status, BorderLayout.SOUTH);
        add(form, BorderLayout.NORTH); add(new JScrollPane(new JTable(model)), BorderLayout.CENTER); add(south, BorderLayout.SOUTH);

        register.addActionListener(e -> onRegister());
        filter.addActionListener(e -> { pageNo = 0; load(); });
        prev.addActionListener(e -> { if (pageNo > 0) { pageNo--; load(); } });
        next.addActionListener(e -> { pageNo++; load(); });
        exit.addActionListener(e -> dispose());
        export.addActionListener(e -> onExport());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(720, 420);
        load();
    }

    private void onRegister() {
        final String s = sid.getText(), n = name.getText(), ev = (String) event.getSelectedItem();
        new SwingWorker<Void, Void>() {                                  // DB work off the event-dispatch thread
            @Override protected Void doInBackground() throws Exception { service.register(s, n, ev); return null; }
            @Override protected void done() {
                try { get(); status.setText(" Registered " + s + " for " + ev); sid.setText(""); name.setText(""); load(); }
                catch (ExecutionException ex) { status.setText(" Error: " + ex.getCause().getMessage()); }
                catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            }
        }.execute();
    }

    private void load() {
        final String f = (String) filter.getSelectedItem(); final int p = pageNo;
        new SwingWorker<List<Registration>, Void>() {
            @Override protected List<Registration> doInBackground() throws Exception { return service.page(f, p, PAGE); }
            @Override protected void done() {
                try {
                    model.setRowCount(0);
                    for (Registration r : get()) model.addRow(new Object[]{r.studentId, r.name, r.event});
                    pageLabel.setText("Page " + (p + 1));
                } catch (ExecutionException ex) { status.setText(" Error: " + ex.getCause().getMessage()); }
                catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            }
        }.execute();
    }

    private void onExport() {
        JFileChooser fc = new JFileChooser();
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        final java.io.File out = fc.getSelectedFile(); final String f = (String) filter.getSelectedItem();
        new SwingWorker<Integer, Void>() {
            @Override protected Integer doInBackground() throws Exception {
                int n = 0;
                try (PrintWriter w = new PrintWriter(out)) {
                    for (int p = 0; ; p++) { List<Registration> rows = service.page(f, p, 1000); if (rows.isEmpty()) break;
                        for (Registration r : rows) { w.println(r.studentId + "," + r.name + "," + r.event); n++; } }
                }
                return n;
            }
            @Override protected void done() { try { status.setText(" Exported " + get() + " rows"); } catch (Exception ex) { status.setText(" Export failed"); } }
        }.execute();
    }

    public static void main(String[] args) {
        RegistrationDao dao = new JdbcRegistrationDao("jdbc:mysql://localhost:3306/campus_events", "root", "your_password");
        SwingUtilities.invokeLater(() -> new RegistrationApp(new RegistrationService(dao)).setVisible(true));
    }
}
