// ===== FILE: src/transport/core/AppState.java =====
package transport.core;

import java.io.IOException;
import java.util.*;

public class AppState {
    private static final AppState instance = new AppState();
    public static AppState getInstance() { return instance; }

    private final List<Person> people;
    private final List<TransportPass> passes;
    private final List<TransportVehicle> vehicles;
    private final List<Station> stations;
    private final ComplaintService complaintService;

    private AppState() {
        try {
            people = CsvPersistence.loadUsers();
            passes = CsvPersistence.loadPasses(people);
            vehicles = CsvPersistence.loadVehicles();
            stations = CsvPersistence.loadStations();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        complaintService = new ComplaintService();
    }

    public List<Person> getPeople() { return people; }
    public List<TransportPass> getPasses() { return passes; }
    public ComplaintService getComplaintService() { return complaintService; }

    public void addPerson(Person p) {
        people.add(p);
    }
    public void addPass(TransportPass tp) {
        passes.add(tp);
    }

    public void saveUsers() {
        try { CsvPersistence.saveUsers(people); }
        catch (IOException e) { e.printStackTrace(); }
    }
    public void savePasses() {
        try { CsvPersistence.savePasses(passes); }
        catch (IOException e) { e.printStackTrace(); }
    }

    public List<TransportVehicle> getVehicles() {
        // load once or cache
        return vehicles;
    }

    public List<Station> getStations() {
        return stations;
    }
}

// ===== FILE: src/transport/core/CardType.java =====
package transport.core;

/**
 * Type of complaint.
 */
public enum CardType {
    REGULAR,
    STUDENT,
    SENIOR,
    EMPLOYEE
}

// ===== FILE: src/transport/core/Complaint.java =====
package transport.core;

import java.time.LocalDate;

/**
 * A complaint raised by a user or employee against a station or vehicle.
 */
public class Complaint implements Comparable<Complaint> {
    private static int counter = 1;
    private final int id;
    private final LocalDate date;
    private final Person reporter;
    private final ComplaintType type;
    private final Suspendable target;
    private final String description;
    private final GravityLevel gravity;

    public Complaint(Person reporter,
                     ComplaintType type,
                     Suspendable target,
                     String description,
                     GravityLevel gravity,
                     LocalDate date) {
        this.id = counter++;
        this.reporter = reporter;
        this.type = type;
        this.target = target;
        this.description = description;
        this.gravity = gravity;
        this.date = date;
    }

    public int getId() { return id; }
    public LocalDate getDate() { return date; }
    public Person getReporter() { return reporter; }
    public ComplaintType getType() { return type; }
    public Suspendable getTarget() { return target; }
    public String getDescription() { return description; }
    public GravityLevel getGravity() { return gravity; }

    @Override
    public int compareTo(Complaint o) {
        return Integer.compare(this.id, o.id);
    }

    @Override
    public String toString() {
        return String.format(
                "Complaint #%d [%s on %s]\n Reporter: %s\n Target: %s\n Gravity: %s\n Desc: %s\n",
                id, type, date, reporter, target, gravity, description);
    }
}

// ===== FILE: src/transport/core/ComplaintService.java =====
package transport.core;

import java.util.*;

/**
 * Manages submission, listing, and resolution of complaints.
 * Suspends any target that accumulates >3 complaints.
 */
public class ComplaintService {
    private static final int THRESHOLD = 3;
    private final List<Complaint> complaints = new ArrayList<>();

    /** Submit a new complaint. */
    public void submit(Complaint c) {
        complaints.add(c);
        long count = complaints.stream()
                .filter(comp -> comp.getTarget().equals(c.getTarget()))
                .count();
        if (count > THRESHOLD) {
            c.getTarget().suspend();
        }
    }

    /** Resolve a complaint: remove it and possibly reactivate the target. */
    public void resolve(Complaint c) {
        complaints.remove(c);
        long count = complaints.stream()
                .filter(comp -> comp.getTarget().equals(c.getTarget()))
                .count();
        if (count <= THRESHOLD) {
            c.getTarget().reactivate();
        }
    }

    /** List all complaints. */
    public List<Complaint> listAll() {
        return Collections.unmodifiableList(complaints);
    }

    /** List complaints by reporter. */
    public List<Complaint> listByReporter(Person p) {
        return complaints.stream()
                .filter(c -> c.getReporter().equals(p))
                .toList();
    }

    /** List complaints by target (station or vehicle). */
    public List<Complaint> listByTarget(Suspendable s) {
        return complaints.stream()
                .filter(c -> c.getTarget().equals(s))
                .toList();
    }
}

// ===== FILE: src/transport/core/ComplaintType.java =====
package transport.core;

/**
 * Type of complaint.
 */
public enum ComplaintType {
    TECHNICAL,
    PAYMENT,
    SERVICE
}

// ===== FILE: src/transport/core/CsvPersistence.java =====
package transport.core;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.*;

public class CsvPersistence {

    // Paths under src/main/resources when packaged, or a data/ folder next to the JAR
    private static final Path USERS_PATH  = Paths.get("data/users.csv");
    private static final Path PASSES_PATH = Paths.get("data/passes.csv");
    private static final Path VEHICLES_PATH = Paths.get("data/vehicles.csv");
    private static final Path STATIONS_PATH = Paths.get("data/stations.csv");

    /** Save users and employees to CSV. */
    public static void saveUsers(List<Person> people) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(USERS_PATH)) {
            writer.write("id,firstName,lastName,dateOfBirth,handicap,matricule,function");
            writer.newLine();
            int counter = 1;
            for (Person p : people) {
                String id       = (p instanceof Employee ? "E" : "U") + counter++;
                String fn       = p.getFirstName();
                String ln       = p.getLastName();
                String dob      = p.getDateOfBirth().toString();
                String hand     = Boolean.toString(p.hasHandicap());
                String matricule = p instanceof Employee ? ((Employee)p).getMatricule() : "";
                String func     = p instanceof Employee ? String.valueOf(((Employee)p).getFunction()) : "";
                writer.write(String.join(",",
                        id, fn, ln, dob, hand, matricule, func));
                writer.newLine();
            }
        }
    }

    /** Load users and employees from CSV. */
    public static List<Person> loadUsers() throws IOException {
        if (!Files.exists(USERS_PATH)) return new ArrayList<>();
        try (Stream<String> lines = Files.lines(USERS_PATH).skip(1)) {
            return lines.map(line -> {
                String[] cols = line.split(",");
                String id = cols[0];
                String fn = cols[1], ln = cols[2];
                LocalDate dob = LocalDate.parse(cols[3]);
                boolean hand = Boolean.parseBoolean(cols[4]);
                if (id.startsWith("E")) {
                    String matricule = cols[5];
                    FunctionType func = FunctionType.valueOf(cols[6]);
                    return (Person)new Employee(fn, ln, dob, hand, matricule, func);
                } else {
                    return (Person)new User(fn, ln, dob, hand);
                }
            }).collect(Collectors.toList());
        }
    }

    /** Save transport passes to CSV. */
    public static void savePasses(List<TransportPass> passes) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(PASSES_PATH)) {
            writer.write("id,type,purchaseDate,price,paymentMethod,ownerFullName,cardType,isEmployee");
            writer.newLine();
            for (TransportPass tp : passes) {
                String[] cols;
                if (tp instanceof Ticket) {
                    cols = new String[] {
                            String.valueOf(tp.getId()),
                            "TICKET",
                            tp.getPurchaseDate().toString(),
                            String.valueOf(tp.getPrice()),
                            tp.getPaymentMethod().name(),
                            "", "", ""
                    };
                } else {
                    PersonalCard pc = (PersonalCard)tp;
                    cols = new String[] {
                            String.valueOf(pc.getId()),
                            "PERS_CARD",
                            pc.getPurchaseDate().toString(),
                            String.valueOf(pc.getPrice()),
                            pc.getPaymentMethod().name(),
                            pc.getOwnerFullName(),
                            pc.getCardType().name(),
                            Boolean.toString(pc.isEmployee())
                    };
                }
                writer.write(String.join(",", cols));
                writer.newLine();
            }
        }
    }

    /** Load transport passes from CSV. */
    public static List<TransportPass> loadPasses(List<Person> people) throws IOException {
        if (!Files.exists(PASSES_PATH)) return new ArrayList<>();
        // Map ownerFullName -> Person to re-associate cards to users/employees if needed
        System.out.println("Mapping users to passes...");
        Map<String, Person> lookup = people.stream()
                .collect(Collectors.toMap(p -> p.getFirstName() + " " + p.getLastName(), p -> p));
        try (Stream<String> lines = Files.lines(PASSES_PATH).skip(1)) {
            return lines.map(line -> {
                String[] cols = line.split(",");
                String type = cols[1];
                LocalDate purchaseDate = LocalDate.parse(cols[2]);
                int id = Integer.parseInt(cols[0]);
                PaymentMethod pm = PaymentMethod.valueOf(cols[4]);
                if (type.equals("TICKET")) {
                    return (TransportPass)new Ticket(id, pm, purchaseDate);
                } else {
                    Person owner = lookup.get(cols[5]);
                    boolean isEmp = Boolean.parseBoolean(cols[7]);
                    return (TransportPass)new PersonalCard(
                           id, owner, isEmp, pm, purchaseDate);
                }
            }).collect(Collectors.toList());
        }
    }

    /** Load transport vehicles from CSV (id per line). */
    public static List<TransportVehicle> loadVehicles() throws IOException {
        if (!Files.exists(VEHICLES_PATH)) return new ArrayList<>();
        try (Stream<String> lines = Files.lines(VEHICLES_PATH)) {
            return lines
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .map(TransportVehicle::new)
                    .collect(Collectors.toList());
        }
    }

    /** Load stations from CSV (name per line). */
    public static List<Station> loadStations() throws IOException {
        if (!Files.exists(STATIONS_PATH)) return new ArrayList<>();
        try (Stream<String> lines = Files.lines(STATIONS_PATH)) {
            return lines
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .map(Station::new)
                    .collect(Collectors.toList());
        }
    }
}

// ===== FILE: src/transport/core/Employee.java =====
package transport.core;

import java.time.LocalDate;

/**
 * Represents an employee of the transport system.
 */
public class Employee extends Person {
    private String matricule;
    private FunctionType function;

    public Employee(String firstName, String lastName, LocalDate dateOfBirth, boolean handicap,
                    String matricule, FunctionType function) {
        super(firstName, lastName, dateOfBirth, handicap);
        this.matricule = matricule;
        this.function = function;
    }

    public String getMatricule() {
        return matricule;
    }

    public FunctionType getFunction() {
        return function;
    }
}

// ===== FILE: src/transport/core/FunctionType.java =====
package transport.core;

/**
 * Type of employee function.
 */
public enum FunctionType {
    DRIVER,
    CONTROLLER,
    STATION_AGENT,
    ADMIN
}

// ===== FILE: src/transport/core/GravityLevel.java =====
package transport.core;

/**
 * Severity level of a complaint.
 */
public enum GravityLevel {
    LOW,
    MEDIUM,
    HIGH
}

// ===== FILE: src/transport/core/InvalidPassException.java =====
package transport.core;

/**
 * Exception thrown when a pass is invalid.
 */
public class InvalidPassException extends Exception {
    public InvalidPassException(String message) {
        super(message);
    }
}

// ===== FILE: src/transport/core/PaymentMethod.java =====
package transport.core;

/**
 * Payment methods for transport passes.
 */
public enum PaymentMethod {
    CASH,
    CREDIT_CARD,
    DEBIT_CARD,
    MOBILE_PAYMENT
}

// ===== FILE: src/transport/core/Person.java =====
// src/transport/core/Person.java
package transport.core;

import java.time.LocalDate;

/**
 * Abstract base class for all persons in the system.
 */
public abstract class Person {
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private boolean handicap;

    public Person(String firstName, String lastName, LocalDate dateOfBirth, boolean handicap) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.handicap = handicap;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public boolean hasHandicap() {
        return handicap;
    }

    @Override
    public String toString() {
        return String.format("%s %s", firstName, lastName);
    }
}

// ===== FILE: src/transport/core/PersonalCard.java =====
package transport.core;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * A personal transport card that can be used for multiple trips.
 */
public class PersonalCard extends TransportPass {
    private final Person owner;
    private final boolean isEmployee;
    private final CardType cardType;
    private LocalDate expiryDate;

    public PersonalCard(int id, Person owner, boolean isEmployee, PaymentMethod paymentMethod, LocalDate purchaseDate) {
        super(id, paymentMethod, purchaseDate);
        this.owner = owner;
        this.isEmployee = isEmployee;
        
        // Determine card type based on owner
        if (isEmployee) {
            this.cardType = CardType.EMPLOYEE;
            this.price = 0.0; // Free for employees
        } else if (owner.getDateOfBirth().until(LocalDate.now(), ChronoUnit.YEARS) >= 65) {
            this.cardType = CardType.SENIOR;
            this.price = 2000.0; // Reduced price for seniors
        } else if (owner.getDateOfBirth().until(LocalDate.now(), ChronoUnit.YEARS) <= 25) {
            this.cardType = CardType.STUDENT;
            this.price = 1500.0; // Student price
        } else {
            this.cardType = CardType.REGULAR;
            this.price = 3000.0; // Regular price
        }
        
        // Set expiry date to 1 year from purchase
        this.expiryDate = purchaseDate.plusYears(1);
    }
    
    public PersonalCard(Person owner, boolean isEmployee, PaymentMethod paymentMethod) {
        super(paymentMethod);
        this.owner = owner;
        this.isEmployee = isEmployee;
        
        // Determine card type based on owner
        if (isEmployee) {
            this.cardType = CardType.EMPLOYEE;
            this.price = 0.0; // Free for employees
        } else if (owner.getDateOfBirth().until(LocalDate.now(), ChronoUnit.YEARS) >= 65) {
            this.cardType = CardType.SENIOR;
            this.price = 2000.0; // Reduced price for seniors
        } else if (owner.getDateOfBirth().until(LocalDate.now(), ChronoUnit.YEARS) <= 25) {
            this.cardType = CardType.STUDENT;
            this.price = 1500.0; // Student price
        } else {
            this.cardType = CardType.REGULAR;
            this.price = 3000.0; // Regular price
        }
        
        // Set expiry date to 1 year from purchase
        this.expiryDate = purchaseDate.plusYears(1);
    }

    public Person getOwner() {
        return owner;
    }
    
    public String getOwnerFullName() {
        return owner.getFirstName() + " " + owner.getLastName();
    }

    public boolean isEmployee() {
        return isEmployee;
    }

    public CardType getCardType() {
        return cardType;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    @Override
    public boolean isValid(LocalDate date) throws InvalidPassException {
        if (date.isAfter(expiryDate)) {
            throw new InvalidPassException("Card expired on " + expiryDate);
        }
        return true;
    }
    
    @Override
    public String toString() {
        return super.toString() + " | Type: " + cardType + " | Owner: " + owner + " | Expires: " + expiryDate;
    }
}

// ===== FILE: src/transport/core/ReductionImpossibleException.java =====
package transport.core;

/**
 * Exception thrown when a price reduction is not possible.
 */
public class ReductionImpossibleException extends Exception {
    public ReductionImpossibleException(String message) {
        super(message);
    }
}

// ===== FILE: src/transport/core/Station.java =====
package transport.core;

/**
 * Represents a transport station.
 */
public class Station implements Suspendable {
    private final String name;
    private boolean suspended;

    public Station(String name) {
        this.name = name;
        this.suspended = false;
    }

    public String getName() {
        return name;
    }

    @Override
    public void suspend() {
        this.suspended = true;
    }

    @Override
    public void reactivate() {
        this.suspended = false;
    }

    @Override
    public boolean isSuspended() {
        return suspended;
    }

    @Override
    public String getState() {
        return suspended ? "suspended" : "active";
    }

    @Override
    public String toString() {
        return "Station: " + name + " (" + getState() + ")";
    }
}

// ===== FILE: src/transport/core/Suspendable.java =====
package transport.core;

/**
 * Common interface for entities that can be suspended/reactivated.
 */
public interface Suspendable {
    void suspend();
    void reactivate();
    boolean isSuspended();
    String getState();  // "active" or "suspended"
}

// ===== FILE: src/transport/core/Ticket.java =====
package transport.core;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * A single-use transport ticket.
 */
public class Ticket extends TransportPass {
    private boolean used;
    private LocalDate validUntil;

    public Ticket(int id, PaymentMethod paymentMethod, LocalDate purchaseDate) {
        super(id, paymentMethod, purchaseDate);
        this.price = 100.0; // Base price for a ticket
        this.used = false;
        this.validUntil = purchaseDate.plusDays(1); // Valid for 1 day
    }
    
    public Ticket(PaymentMethod paymentMethod) {
        super(paymentMethod);
        this.price = 100.0; // Base price for a ticket
        this.used = false;
        this.validUntil = purchaseDate.plusDays(1); // Valid for 1 day
    }

    public boolean isUsed() {
        return used;
    }

    public void markAsUsed() {
        this.used = true;
    }

    @Override
    public boolean isValid(LocalDate date) throws InvalidPassException {
        if (used) {
            throw new InvalidPassException("Ticket already used");
        }
        if (date.isAfter(validUntil)) {
            throw new InvalidPassException("Ticket expired on " + validUntil);
        }
        return true;
    }
    
    @Override
    public String toString() {
        return super.toString() + " | Valid until: " + validUntil + " | Used: " + used;
    }
}

// ===== FILE: src/transport/core/TransportPass.java =====
package transport.core;

import java.io.Serializable;
import java.time.LocalDate;

public abstract class TransportPass implements Serializable {
    private static int nextId = 1;
    protected final int id;
    protected final LocalDate purchaseDate;
    protected double price;
    protected PaymentMethod paymentMethod;

    protected TransportPass (int id, PaymentMethod paymentMethod, LocalDate purchaseDate) {
        this.id = id;
        this.purchaseDate = purchaseDate;
        this.paymentMethod = paymentMethod;
    }

    public TransportPass(PaymentMethod paymentMethod) {
        this.id = nextId++;
        this.purchaseDate = LocalDate.now();
        this.paymentMethod = paymentMethod;
    }

    public int getId() { return id; }
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public double getPrice() { return price; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }

    public abstract boolean isValid(LocalDate date) throws InvalidPassException;

    @Override
    public String toString() {
        return "Pass #" + id + " | Purchased: " + purchaseDate + " | Price: " + price + " DA | Paid by: " + paymentMethod;
    }
}

// ===== FILE: src/transport/core/TransportVehicle.java =====
package transport.core;

/**
 * Represents a transport vehicle (bus, tram, etc.).
 */
public class TransportVehicle implements Suspendable {
    private final String id;
    private boolean suspended;

    public TransportVehicle(String id) {
        this.id = id;
        this.suspended = false;
    }

    public String getId() {
        return id;
    }

    @Override
    public void suspend() {
        this.suspended = true;
    }

    @Override
    public void reactivate() {
        this.suspended = false;
    }

    @Override
    public boolean isSuspended() {
        return suspended;
    }

    @Override
    public String getState() {
        return suspended ? "suspended" : "active";
    }

    @Override
    public String toString() {
        return "Vehicle: " + id + " (" + getState() + ")";
    }
}

// ===== FILE: src/transport/core/User.java =====
// src/transport/core/User.java
package transport.core;

import java.time.LocalDate;

/**
 * Represents a passenger user.
 */
public class User extends Person {
    public User(String firstName, String lastName, LocalDate dateOfBirth, boolean handicap) {
        super(firstName, lastName, dateOfBirth, handicap);
    }
}
