package test;

import transport.core.*;
import java.time.LocalDate;
import java.util.List;

public class ComplaintTest {
    public static void main(String[] args) throws Exception {
        // Load stations & vehicles
        var stations = CsvPersistence.loadStations();
        var vehicles = CsvPersistence.loadVehicles();

        System.out.println("Stations: " + stations);
        System.out.println("Vehicles: " + vehicles);

        // Create reporters
        Person u = new User("Eva", "Doe", LocalDate.of(1995,5,1), false);
        Person e = new Employee("Frank", "Agent", LocalDate.of(1980,3,2), false, "EMP2", FunctionType.AGENT);

        // Pick a target
        Suspendable bus = vehicles.get(0);
        Suspendable station = stations.get(0);

        // Set up service
        ComplaintService service = new ComplaintService();

        // Submit 4 complaints against the bus
        for (int i = 1; i <= 4; i++) {
            Complaint c = new Complaint(u, ComplaintType.TECHNICAL, bus,
                    "Issue #" + i, GravityLevel.MEDIUM,
                    LocalDate.now().plusDays(i));
            service.submit(c);
            System.out.printf("Submitted %s | Bus state: %s%n", c, bus.getState());
        }

        // Expect bus to be suspended after 4th
        System.out.println("Final bus state: " + bus.getState());

        // List all complaints
        List<Complaint> all = service.listAll();
        System.out.println("\nAll complaints:");
        all.forEach(System.out::println);

        // Resolve one complaint and check reactivation
        Complaint first = all.get(0);
        service.resolve(first);
        System.out.printf("After resolving #%d, bus state: %s%n", first.getId(), bus.getState());
    }
}
