import java.nio.file.*;
import java.util.Arrays;
public class AdversarialFile {
    public static void main(String[] a) throws Exception {
        Path f = Files.createTempFile("bad", ".csv");
        Files.writeString(f, "P1,192311001,Arun,12,4500.0\n"          // valid
                + "P2,192311002,Divya,7\n"                           // wrong number of fields (4)
                + "P3,192311003,Kumar,5,abc\n"                       // non-numeric amount
                + "\n"                                               // empty line
                + "P5,192311005,Meena,3,3800.0\n");                  // valid after the damage
        PassStore s = new PassStore(f);
        for (String id : new String[]{"P1", "P2", "P3", "P5", "P9"}) System.out.println(id + " -> " + Arrays.toString(s.find(id)));
        Files.delete(f);
    }
}
