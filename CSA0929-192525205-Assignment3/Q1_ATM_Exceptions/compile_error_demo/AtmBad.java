import java.io.*;
public class AtmBad {
    public static void main(String[] args) {
        try {
            Integer.parseInt("abc");
        } catch (Exception e) {
            System.out.println("general");
        } catch (NumberFormatException e) {
            System.out.println("specific");
        }
    }
}
