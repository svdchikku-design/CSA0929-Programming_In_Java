import java.awt.FlowLayout;
import java.awt.event.*;
import javax.swing.*;

/** The same Convert action written in the four styles of Task 5.1 (each handles window closing with a WindowAdapter). */
public class ConverterStyles {
    static String convert(String text) {
        try { return String.format("%.2f USD", Double.parseDouble(text.trim()) * 0.012); }
        catch (NumberFormatException e) { return "Invalid amount"; }
    }
    static WindowAdapter closer = new WindowAdapter() { @Override public void windowClosing(WindowEvent e) { System.exit(0); } };

    // (a) the frame itself implements ActionListener
    static class A extends JFrame implements ActionListener {
        JTextField in = new JTextField(8); JButton go = new JButton("Convert"); JLabel out = new JLabel(" ");
        A() { setLayout(new FlowLayout()); add(in); add(go); add(out); go.addActionListener(this); addWindowListener(closer); pack(); }
        @Override public void actionPerformed(ActionEvent e) { out.setText(convert(in.getText())); }
    }
    // (b) named inner class
    static class B extends JFrame {
        JTextField in = new JTextField(8); JButton go = new JButton("Convert"); JLabel out = new JLabel(" ");
        class ConvertListener implements ActionListener { @Override public void actionPerformed(ActionEvent e) { out.setText(convert(in.getText())); } }
        B() { setLayout(new FlowLayout()); add(in); add(go); add(out); go.addActionListener(new ConvertListener()); addWindowListener(closer); pack(); }
    }
    // (c) anonymous inner class
    static class C extends JFrame {
        JTextField in = new JTextField(8); JButton go = new JButton("Convert"); JLabel out = new JLabel(" ");
        C() { setLayout(new FlowLayout()); add(in); add(go); add(out);
              go.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { out.setText(convert(in.getText())); } });
              addWindowListener(closer); pack(); }
    }
    // (d) lambda expression
    static class D extends JFrame {
        JTextField in = new JTextField(8); JButton go = new JButton("Convert"); JLabel out = new JLabel(" ");
        D() { setLayout(new FlowLayout()); add(in); add(go); add(out); go.addActionListener(e -> out.setText(convert(in.getText()))); addWindowListener(closer); pack(); }
    }
}
