import java.awt.FlowLayout;
import java.awt.event.*;
import javax.swing.*;

public class D extends JFrame {
    static String convert(String text) {
        try { return String.format("%.2f USD", Double.parseDouble(text.trim()) * 0.012); }
        catch (NumberFormatException e) { return "Invalid amount"; }
    }
    static final WindowAdapter closer = new WindowAdapter() { @Override public void windowClosing(WindowEvent e) { System.exit(0); } };
    final JTextField in = new JTextField(8); final JButton go = new JButton("Convert"); final JLabel out = new JLabel(" ");
    D() { setLayout(new FlowLayout()); add(in); add(go); add(out);
        ActionListener l = e -> { out.setText(convert(in.getText())); JOptionPane.showMessageDialog(this, out.getText()); };
        go.addActionListener(l); in.addActionListener(l); addWindowListener(closer); pack(); }
    public static void main(String[] a) { SwingUtilities.invokeLater(() -> new D().setVisible(true)); }
}
