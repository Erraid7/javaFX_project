package test;

import transport.core.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

public class IntegrationTest {
    public static void main(String[] args) {
        try {
            System.out.println("=== 1. USER MANAGEMENT & PERSISTENCE ===");
            List<Person> people = new ArrayList<>();
            people.add(new User("Alice", "Young", LocalDate.of(2005, 6, 15), false));    // JUNIOR
            people.add(new User("Bob",   "Senior",LocalDate.of(1950, 1, 20), false));    // SENIOR
            people.add(new User("Cara",  "Solid", LocalDate.of(1990, 3, 10), true));     // SOLIDARITY
            people.add(new Employee("Dave","Partner",LocalDate.of(1985,9,5), false,
                    "EMP1", FunctionType.AGENT));                     // PARTNER

            System.out.println("-- Original People --");
            people.forEach(p -> System.out.println("   " + p));

            CsvPersistence.saveUsers(people);
            System.out.println("Saved users.csv");

            List<Person> loadedPeople = CsvPersistence.loadUsers();
            System.out.println("-- Loaded People --");
            loadedPeople.forEach(p -> System.out.println("   " + p));
            assert loadedPeople.size() == people.size();

            System.out.println("\n=== 2. PASS MANAGEMENT & PERSISTENCE ===");
            List<TransportPass> passes = new ArrayList<>();
            passes.add(new Ticket(PaymentMethod.CASH, people.get(0)));
            passes.add(new PersonalCard(loadedPeople.get(0), false, PaymentMethod.DAHABIA));
            passes.add(new PersonalCard(loadedPeople.get(1), false, PaymentMethod.BARIDIMOB));
            passes.add(new PersonalCard(loadedPeople.get(2), false, PaymentMethod.CASH));
            passes.add(new PersonalCard(loadedPeople.get(3), true,  PaymentMethod.DAHABIA));

            System.out.println("-- Created Passes --");
            passes.forEach(tp -> System.out.println("   " + tp));

            CsvPersistence.savePasses(passes);
            System.out.println("Saved passes.csv");

            List<TransportPass> loadedPasses = CsvPersistence.loadPasses(loadedPeople);
            System.out.println("-- Loaded Passes --");
            loadedPasses.forEach(tp -> System.out.println("   " + tp));
            assert loadedPasses.size() == passes.size();

            // Validity checks for today
            LocalDate today = LocalDate.now();
            System.out.println("\n-- Validity Today --");
            for (TransportPass tp : loadedPasses) {
                try {
                    System.out.printf("   Pass #%d: valid today? %s%n",
                            tp.getId(), tp.isValid(today));
                } catch (InvalidPassException e) {
                    System.out.printf("   Pass #%d invalid: %s%n",
                            tp.getId(), e.getMessage());
                }
            }

            // Test expiration (pick first PersonalCard)
            LocalDate future = today.plusYears(2);
            TransportPass card = loadedPasses.stream()
                    .filter(tp -> tp instanceof PersonalCard).findFirst().get();
            try {
                card.isValid(future);
            } catch (InvalidPassException e) {
                System.out.println("Expected expired: " + e.getMessage());
            }

            System.out.println("\n=== 3. LOAD VEHICLES & STATIONS ===");
            List<TransportVehicle> vehicles = CsvPersistence.loadVehicles();
            List<Station> stations       = CsvPersistence.loadStations();

            System.out.println("-- Vehicles --");
            vehicles.forEach(v -> System.out.println("   " + v));
            System.out.println("-- Stations --");
            stations.forEach(s -> System.out.println("   " + s));

            System.out.println("\n=== 4. COMPLAINT MANAGEMENT ===");
            ComplaintService service = new ComplaintService();
            // pick reporter and targets
            Person reporter1 = loadedPeople.get(0);
            Person reporter2 = loadedPeople.get(3);
            Suspendable targetV = vehicles.get(0);
            Suspendable targetS = stations.get(0);

            // Submit complaints on vehicle
            System.out.println("-- Submitting complaints on vehicle --");
            for (int i = 1; i <= 4; i++) {
                Complaint c = new Complaint(
                        reporter1,
                        ComplaintType.TECHNICAL,
                        targetV,
                        "Issue #" + i,
                        GravityLevel.MEDIUM,
                        today.plusDays(i)
                );
                service.submit(c);
                System.out.println("   Submitted " + c.getType() +
                        " -> " + targetV + " | State: " + targetV.getState());
            }
            assert targetV.isSuspended();

            // Submit complaints on station
            System.out.println("-- Submitting complaints on station --");
            for (int i = 1; i <= 2; i++) {
                Complaint c = new Complaint(
                        reporter2,
                        ComplaintType.SERVICE,
                        targetS,
                        "Service issue #" + i,
                        (i==2? GravityLevel.HIGH: GravityLevel.LOW),
                        today.plusDays(i)
                );
                service.submit(c);
                System.out.println("   Submitted " + c.getType() +
                        " -> " + targetS + " | State: " + targetS.getState());
            }
            assert !targetS.isSuspended();

            // List complaints by reporter1
            System.out.println("\n-- Complaints by " + reporter1 + " --");
            service.listByReporter(reporter1)
                    .forEach(c -> System.out.println("   " + c));

            // List complaints for vehicle
            System.out.println("\n-- Complaints for " + targetV + " --");
            service.listByTarget(targetV)
                    .forEach(c -> System.out.println("   " + c));

            // Resolve first complaint on vehicle
            Complaint first = service.listByTarget(targetV).get(0);
            service.resolve(first);
            System.out.println("After resolving #" + first.getId() +
                    ", vehicle state: " + targetV.getState());
            assert !targetV.isSuspended();

            System.out.println("\n=== ALL TESTS COMPLETED SUCCESSFULLY ===");

        } catch (IOException ioe) {
            System.err.println("CSV IO error: " + ioe.getMessage());
        } catch (ReductionImpossibleException dne) {
            System.err.println("Discount error: " + dne.getMessage());
        }
    }
}
