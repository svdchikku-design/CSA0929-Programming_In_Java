import java.awt.FlowLayout;
import java.awt.event.*;
import javax.swing.*;

public class B extends JFrame {
    static String convert(String text) {
        try { return String.format("%.2f USD", Double.parseDouble(text.trim()) * 0.012); }
        catch (NumberFormatException e) { return "Invalid amount"; }
    }
    static final WindowAdapter closer = new WindowAdapter() { @Override public void windowClosing(WindowEvent e) { System.exit(0); } };
    final JTextField in = new JTextField(8); final JButton go = new JButton("Convert"); final JLabel out = new JLabel(" ");
    class ConvertListener implements ActionListener { @Override public void actionPerformed(ActionEvent e) { out.setText(convert(in.getText())); } }
    B() { setLayout(new FlowLayout()); add(in); add(go); add(out); go.addActionListener(new ConvertListener()); addWindowListener(closer); pack(); }
    public static void main(String[] a) { SwingUtilities.invokeLater(() -> new B().setVisible(true)); }
}
