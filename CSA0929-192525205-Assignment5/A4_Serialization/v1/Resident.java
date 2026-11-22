import java.io.*;
public class Resident implements Serializable {

    String name, regNo;
    transient String password = "secret";

    transient java.sql.Connection con;
    Resident(String n, String r) { name = n; regNo = r; }
    public String toString() { return "Resident[" + name + ", " + regNo + ", password=" + password + "con=" + con + "]"; }
}
