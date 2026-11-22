import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.concurrent.ExecutionException;
import javax.swing.*;

/** View only: collects input, shows results. One small handler per button. */
public class FeeWindow extends JFrame {
    private final JTextField regNo = new JTextField(12), amount = new JTextField(12);
    private final JButton pay = new JButton("Pay"), clear = new JButton("Clear"), receipt = new JButton("Receipt");
    private final JLabel status = new JLabel(" ");
    private final FeeService service;
    private String lastReceipt;

    public FeeWindow(FeeService service) {
        super("Fee Payment");
        this.service = service;
        JPanel form = new JPanel(new GridLayout(2, 2, 5, 5));
        form.add(new JLabel("Register No")); form.add(regNo);
        form.add(new JLabel("Amount")); form.add(amount);
        JPanel buttons = new JPanel(new FlowLayout());
        buttons.add(pay); buttons.add(clear); buttons.add(receipt);
        receipt.setEnabled(false);
        setLayout(new java.awt.BorderLayout());
        add(form, java.awt.BorderLayout.NORTH); add(buttons, java.awt.BorderLayout.CENTER); add(status, java.awt.BorderLayout.SOUTH);
        pay.addActionListener(e -> onPay());
        clear.addActionListener(e -> { regNo.setText(""); amount.setText(""); status.setText(" "); });
        receipt.addActionListener(e -> JOptionPane.showMessageDialog(this, lastReceipt, "Receipt", JOptionPane.INFORMATION_MESSAGE));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
    }

    private void onPay() {
        final String r = regNo.getText(), a = amount.getText();
        pay.setEnabled(false); status.setText("Processing...");
        new SwingWorker<String, Void>() {                       // keeps the event-dispatch thread free
            @Override protected String doInBackground() throws Exception { return service.pay(r, a); }
            @Override protected void done() {
                try { lastReceipt = get(); status.setText(lastReceipt); receipt.setEnabled(true); }
                catch (ExecutionException ex) { status.setText("Error: " + ex.getCause().getMessage()); }
                catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                finally { pay.setEnabled(true); }
            }
        }.execute();
    }
}
