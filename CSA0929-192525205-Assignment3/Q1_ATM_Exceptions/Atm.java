import java.io.*;
import java.util.Scanner;

public class Atm {
    // Reads balance from card-reader file, parses account+amount text, withdraws.
    static double withdraw(String cardFile, String accText, String amtText)
            throws FileNotFoundException, InsufficientFundsException {
        Scanner card = new Scanner(new File(cardFile));          // FileNotFoundException (checked)
        try {
            double balance = Double.parseDouble(card.nextLine().trim());
            int acc = Integer.parseInt(accText.trim());          // NumberFormatException (unchecked)
            double amt = Double.parseDouble(amtText.trim());     // NumberFormatException (unchecked)
            if (amt < 0) throw new IllegalArgumentException("Amount must not be negative: " + amt);
            if (amt > balance) throw new InsufficientFundsException("Balance " + balance + " < requested " + amt);
            System.out.println("   dispensed " + amt + " from account " + acc);
            return balance - amt;
        } finally {
            card.close();
        }
    }

    static void run(int n, String desc, String cardFile, String acc, String amt) {
        System.out.println("Test " + n + ": " + desc + "  [card=" + cardFile + ", acc=\"" + acc + "\", amt=\"" + amt + "\"]");
        try {
            double nb = withdraw(cardFile, acc, amt);
            System.out.println("   CATCH: none (success), new balance " + nb);
        } catch (NumberFormatException e) {
            System.out.println("   CATCH: NumberFormatException -> " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("   CATCH: IllegalArgumentException -> " + e.getMessage());
        } catch (InsufficientFundsException | FileNotFoundException e) {
            System.out.println("   CATCH: multi-catch (" + e.getClass().getSimpleName() + ") -> " + e.getMessage());
        } finally {
            System.out.println("   FINALLY: executed");
        }
    }

    public static void main(String[] args) throws Exception {
        try (PrintWriter w = new PrintWriter("card.txt")) { w.println("5000.0"); }
        run(1, "valid withdrawal", "card.txt", "12345", "2000");
        run(2, "non-numeric account", "card.txt", "12A45", "2000");
        run(3, "non-numeric amount", "card.txt", "12345", "two thousand");
        run(4, "negative amount", "card.txt", "12345", "-500");
        run(5, "amount > balance", "card.txt", "12345", "9000");
        run(6, "missing card-reader file", "nocard.txt", "12345", "100");
    }
}
