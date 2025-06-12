// src/test/java/transport/test/MainTest.java
package test;

import transport.core.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class MainTest {
    public static void main(String[] args) {
        try {
            // 1. Create sample users and employees
            List<Person> people = new ArrayList<>();
            people.add(new User("Alice", "Young", LocalDate.of(2005, 6, 15), false));    // junior
            people.add(new User("Bob", "Elder", LocalDate.of(1950, 1, 20), false));    // senior
            people.add(new User("Cara", "Solid", LocalDate.of(1990, 3, 10), true));    // solidarity
            people.add(new Employee("Dave", "Partner", LocalDate.of(1985, 9, 5), false,
                    "EMP1", FunctionType.AGENT));                      // partner

            System.out.println("== Original People ==");
            people.forEach(p -> System.out.println(" - " + p));

            // 2. Persist users
            CsvPersistence.saveUsers(people);
            System.out.println("Users saved to CSV.");

            // 3. Reload users
            List<Person> loadedPeople = CsvPersistence.loadUsers();
            System.out.println("== Loaded People ==");
            loadedPeople.forEach(p -> System.out.println(" - " + p));
            System.out.println("User count matches: " + (loadedPeople.size() == people.size()));

            // 4. Create sample passes
            List<TransportPass> passes = new ArrayList<>();
            // Ticket purchased by cash
            passes.add(new Ticket(PaymentMethod.CASH, people.get(0)));
            // Junior card by Dahabia
            passes.add(new PersonalCard(people.get(0), false, PaymentMethod.DAHABIA));
            // Senior card by BaridiMob
            passes.add(new PersonalCard(people.get(1), false, PaymentMethod.BARIDIMOB));
            // Solidarity card paid in cash
            passes.add(new PersonalCard(people.get(2), false, PaymentMethod.CASH));
            // Partner card paid in Dahabia
            passes.add(new PersonalCard(people.get(3), true, PaymentMethod.DAHABIA));

            System.out.println("\n== Created Passes ==");
            passes.forEach(tp -> System.out.println(" - " + tp));

            // 5. Persist passes
            CsvPersistence.savePasses(passes);
            System.out.println("Passes saved to CSV.");

            // 6. Reload passes
            List<TransportPass> loadedPasses = CsvPersistence.loadPasses(loadedPeople);
            System.out.println("\n== Loaded Passes ==");
            loadedPasses.forEach(tp -> System.out.println(" - " + tp));
            System.out.println("Pass count matches: " + (loadedPasses.size() == passes.size()));

            // 7. Test validity
            LocalDate today = LocalDate.now();
            System.out.println("\n== Validity Checks ==");
            for (TransportPass tp : loadedPasses) {
                try {
                    boolean valid = tp.isValid(today);
                    System.out.printf("Pass #%d valid today? %s%n", tp.getId(), valid);
                } catch (InvalidPassException ex) {
                    System.out.printf("Pass #%d invalid: %s%n", tp.getId(), ex.getMessage());
                }
            }

            // 8. Test expired date for card
            LocalDate nextYear = today.plusYears(2);
            TransportPass card = loadedPasses.stream()
                    .filter(tp -> tp instanceof PersonalCard)
                    .findFirst().orElseThrow();
            try {
                card.isValid(nextYear);
            } catch (InvalidPassException ex) {
                System.out.println("Expected expiration: " + ex.getMessage());
            }

            System.out.println("\nAll tests completed.");

        } catch (IOException e) {
            System.err.println("IO error during CSV operations: " + e.getMessage());
        } catch (ReductionImpossibleException e) {
            System.err.println("Unexpected discount error: " + e.getMessage());
        }
    }
}
