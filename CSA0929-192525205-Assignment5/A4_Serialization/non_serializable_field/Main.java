import java.io.*;
import java.util.*;
public class Main {
    public static void main(String[] a) throws Exception {
        File f = new File(a[1]);
        if (a[0].equals("save")) {
            List<Resident> rooms = new ArrayList<>(List.of(new Resident("Arun", "192311001"), new Resident("Divya", "192311003")));
            try (ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream(f))) { o.writeObject(rooms); }
            System.out.println("saved " + rooms.size() + " residents, " + f.length() + " bytes");
        } else {
            try (ObjectInputStream i = new ObjectInputStream(new FileInputStream(f))) { List<?> l = (List<?>) i.readObject(); System.out.println("loaded: " + l); }
            catch (Exception e) { System.out.println("FAILED: " + e); }
        }
    }
}
