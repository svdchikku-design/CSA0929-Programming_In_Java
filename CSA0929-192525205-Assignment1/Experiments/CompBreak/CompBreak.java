import java.util.*;
public class CompBreak {
    record Team(String name, int points, int gd) {}
    public static void main(String[] a) {
        Comparator<Team> faulty = (x, y) -> y.points() - x.points();      // subtraction: overflows
        List<Team> l = new ArrayList<>(List.of(new Team("A", Integer.MAX_VALUE, 0), new Team("B", -2, 0)));
        l.sort(faulty);  System.out.println("subtraction comparator: " + l + "   (MAX_VALUE should rank first)");
        Comparator<Team> noTie = Comparator.comparingInt((Team t) -> t.points()).reversed();
        List<Team> p = new ArrayList<>(List.of(new Team("Panthers", 9, 4), new Team("Bisons", 9, 4)));
        List<Team> q = new ArrayList<>(List.of(new Team("Bisons", 9, 4), new Team("Panthers", 9, 4)));
        p.sort(noTie); q.sort(noTie);
        System.out.println("no tie-breaker, input order 1: " + p); System.out.println("no tie-breaker, input order 2: " + q + "  (result depends on input order)");
        Comparator<Team> good = Comparator.comparingInt((Team t) -> t.points()).reversed().thenComparing(Comparator.comparingInt((Team t) -> t.gd()).reversed()).thenComparing(Team::name);
        p.sort(good); q.sort(good);
        System.out.println("full comparator, order 1: " + p); System.out.println("full comparator, order 2: " + q);
        Integer i1 = 127, i2 = 127, j1 = 1000, j2 = 1000;
        System.out.println("Integer 127 == 127: " + (i1 == i2) + " ; 1000 == 1000: " + (j1 == j2) + " ; 1000 equals 1000: " + j1.equals(j2));
    }
}
