public class Task6Demo {
    public static void main(String[] a) throws Exception {
        RegistrationService s = new RegistrationService(new InMemoryRegistrationDao());
        String[][] cases = {
            {"normal", "192525205", "Saai Vardhan", "Quiz"},
            {"normal (other event)", "192525205", "Saai Vardhan", "Music"},
            {"invalid id", "12345", "Arun", "Quiz"},
            {"invalid name", "192525206", "R2D2!!", "Quiz"},
            {"no event", "192525206", "Arun Kumar", "All"},
            {"DUPLICATE", "192525205", "Saai Vardhan", "Quiz"}};
        for (String[] c : cases) {
            try { s.register(c[1], c[2], c[3]); System.out.println(c[0] + " -> ACCEPTED"); }
            catch (RegistrationService.ValidationException e) { System.out.println(c[0] + " -> REJECTED: " + e.getMessage()); }
        }
        System.out.println("rows stored (All) = " + s.count("All") + ", Quiz = " + s.count("Quiz"));
    }
}
