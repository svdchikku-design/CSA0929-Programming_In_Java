public class Uncaught {
    static int parseAccount(String s) { return Integer.parseInt(s); }
    static void withdraw(String acc) { System.out.println(parseAccount(acc)); }
    public static void main(String[] args) {
        withdraw("12A45");
    }
}
