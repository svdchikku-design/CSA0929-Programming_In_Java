import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.*;

public class LibrarySearch extends JFrame {
    public LibrarySearch() {
        super("Campus Library Search");
        setLayout(new BorderLayout());                                   // outer: title N, results C, bottom S

        JPanel top = new JPanel(new BorderLayout());                     // title above search row
        JLabel title = new JLabel("Campus Library", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(18f));
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT));  // keeps natural size
        searchRow.add(new JLabel("Search:")); searchRow.add(new JTextField(18));
        searchRow.add(new JComboBox<>(new String[]{"All", "Title", "Author", "Subject"}));
        searchRow.add(new JButton("Search"));
        top.add(title, BorderLayout.NORTH); top.add(searchRow, BorderLayout.SOUTH);

        JTextArea results = new JTextArea("1. Programming in Java\n2. Operating System Concepts\n3. Database Systems\n");
        results.setEditable(false);                                      // CENTER grows with the window

        JPanel bottom = new JPanel(new BorderLayout());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttons.add(new JButton("Prev")); buttons.add(new JButton("Next")); buttons.add(new JButton("Reserve"));
        bottom.add(buttons, BorderLayout.CENTER);
        bottom.add(new JLabel(" Ready"), BorderLayout.SOUTH);            // status message

        add(top, BorderLayout.NORTH); add(new JScrollPane(results), BorderLayout.CENTER); add(bottom, BorderLayout.SOUTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(560, 360);
    }
    public static void main(String[] a) { SwingUtilities.invokeLater(() -> new LibrarySearch().setVisible(true)); }
}
