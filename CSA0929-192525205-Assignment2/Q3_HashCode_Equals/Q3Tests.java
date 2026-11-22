import java.util.*;

class StudentBad { // equals only (hashCode NOT overridden)
    final String regNo, name;
    StudentBad(String r, String n) { regNo = r; name = n; }
    @Override public boolean equals(Object o) { return o instanceof StudentBad && regNo.equals(((StudentBad) o).regNo); }
}
class StudentGood {
    final String regNo, name;
    StudentGood(String r, String n) { regNo = r; name = n; }
    @Override public boolean equals(Object o) { return o instanceof StudentGood && regNo.equals(((StudentGood) o).regNo); }
    @Override public int hashCode() { return regNo.hashCode(); }
}
class StudentMut { // mutable regNo used in hashCode/equals
    String regNo; final String name;
    StudentMut(String r, String n) { regNo = r; name = n; }
    @Override public boolean equals(Object o) { return o instanceof StudentMut && regNo.equals(((StudentMut) o).regNo); }
    @Override public int hashCode() { return regNo.hashCode(); }
}

public class Q3Tests {
    public static void main(String[] a) {
        System.out.println("Test 1: StudentBad, 10 all-distinct students scanned once each");
        Set<StudentBad> s1 = new HashSet<>();
        for (int i = 0; i < 10; i++) s1.add(new StudentBad("19231" + (1000 + i), "S" + i));
        System.out.println("  distinct students=10 set size=" + s1.size());

        System.out.println("Test 2: StudentBad, 10 distinct students, each scanned 3 times (30 scans)");
        for (int rep = 0; rep < 5; rep++) {
            Set<StudentBad> s2 = new HashSet<>();
            for (int k = 0; k < 3; k++) for (int i = 0; i < 10; i++) s2.add(new StudentBad("19231" + (1000 + i), "S" + i));
            System.out.println("  trial " + (rep + 1) + ": unique=10 scans=30 set size=" + s2.size());
        }
        Set<StudentGood> s2g = new HashSet<>();
        for (int k = 0; k < 3; k++) for (int i = 0; i < 10; i++) s2g.add(new StudentGood("19231" + (1000 + i), "S" + i));
        System.out.println("  same scans with StudentGood (hashCode fixed): set size=" + s2g.size());
        Set<StudentBad> s2c = new HashSet<>();
        StudentBad x = new StudentBad("192311001", "Arun");
        s2c.add(x); s2c.add(new StudentBad("192311001", "Arun"));
        System.out.println("  StudentBad contains(same object)=" + s2c.contains(x) + ", contains(equal new object)=" + s2c.contains(new StudentBad("192311001", "Arun")) + ", size=" + s2c.size());

        System.out.println("Test 3: StudentMut (hashCode uses mutable regNo), regNo changed after insertion");
        Set<StudentMut> s3 = new HashSet<>();
        StudentMut m = new StudentMut("192311001", "Arun");
        s3.add(m);
        System.out.println("  before mutation: size=" + s3.size() + " contains(m)=" + s3.contains(m));
        m.regNo = "192399999";
        System.out.println("  after  mutation: size=" + s3.size() + " contains(m)=" + s3.contains(m)
            + " contains(new 192311001)=" + s3.contains(new StudentMut("192311001", "Arun"))
            + " contains(new 192399999)=" + s3.contains(new StudentMut("192399999", "Arun")));
        s3.add(m);
        System.out.println("  after re-adding the same object m: size=" + s3.size());
        int found = 0; for (StudentMut e : s3) if (e == m) found++;
        System.out.println("  iteration sees m " + found + " time(s); remove(m)=" + s3.remove(m) + " size=" + s3.size());
    }
}
