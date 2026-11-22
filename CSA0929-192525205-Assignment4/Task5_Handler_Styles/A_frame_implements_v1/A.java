import java.awt.FlowLayout;
import java.awt.event.*;
import javax.swing.*;

public class A extends JFrame implements ActionListener {
    static String convert(String text) {
        try { return String.format("%.2f USD", Double.parseDouble(text.trim()) * 0.012); }
        catch (NumberFormatException e) { return "Invalid amount"; }
    }
    static final WindowAdapter closer = new WindowAdapter() { @Override public void windowClosing(WindowEvent e) { System.exit(0); } };
    final JTextField in = new JTextField(8); final JButton go = new JButton("Convert"); final JLabel out = new JLabel(" ");
    A() { setLayout(new FlowLayout()); add(in); add(go); add(out); go.addActionListener(this); addWindowListener(closer); pack(); }
    @Override public void actionPerformed(ActionEvent e) { out.setText(convert(in.getText())); }
    public static void main(String[] a) { SwingUtilities.invokeLater(() -> new A().setVisible(true)); }
}
