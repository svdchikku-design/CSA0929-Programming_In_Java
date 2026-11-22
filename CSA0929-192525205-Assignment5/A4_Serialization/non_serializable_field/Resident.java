import java.io.*;
public class Resident implements Serializable {

    String name, regNo;
    transient String password = "secret";

    java.sql.Connection con = (java.sql.Connection) java.lang.reflect.Proxy.newProxyInstance(Resident.class.getClassLoader(), new Class<?>[]{java.sql.Connection.class}, (p, m, a) -> null);
    Resident(String n, String r) { name = n; regNo = r; }
    public String toString() { return "Resident[" + name + ", " + regNo + ", password=" + password + "con=" + con + "]"; }
}
