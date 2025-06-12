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
    private static final Path COMPLAINTS_PATH = Paths.get("data/complaints.csv");
    private static final TransportPass TP = new TransportPass(PaymentMethod.CASH) {
        @Override
        public boolean isValid(LocalDate date) {
            return false;
        }
    };


    public int current_users_id;
    public static int current_passes_id;
    public static int current_complaints_id;


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
                System.out.println("Loading user " + fn + " " + ln + " (id=" + id + ")" + hand + "...");
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
                            ((Ticket)tp).getOwnerFullName(),
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
                if (id >= TransportPass.nextId) {
                    TransportPass.nextId = id + 1;
                }
                PaymentMethod pm = PaymentMethod.valueOf(cols[4]);
                Person owner = lookup.get(cols[5]);
                if (type.equals("TICKET")) {
                    return (TransportPass)new Ticket(id, pm, purchaseDate, owner);
                } else {
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

    /** Save complaints to CSV. */
    public static void saveComplaints(List<Complaint> complaints, List<Person> people,
                                      List<TransportVehicle> vehicles, List<Station> stations) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(COMPLAINTS_PATH)) {
            writer.write("id,date,reporterFullName,type,targetType,targetId,description,gravity");
            writer.newLine();

            for (Complaint complaint : complaints) {
                String id = String.valueOf(complaint.getId());
                String date = complaint.getDate().toString();
                String reporterFullName = complaint.getReporter().getFirstName() + " " + complaint.getReporter().getLastName();
                String type = complaint.getType().name();

                // Determine target type and ID
                String targetType;
                String targetId;
                Suspendable target = complaint.getTarget();

                if (target instanceof Station) {
                    targetType = "STATION";
                    targetId = ((Station) target).getName();
                } else if (target instanceof TransportVehicle) {
                    targetType = "VEHICLE";
                    targetId = ((TransportVehicle) target).getId();
                } else {
                    // Default case if we have other Suspendable types in the future
                    targetType = "UNKNOWN";
                    targetId = target.toString();
                }

                // Escape commas in description by wrapping in quotes
                String description = "\"" + complaint.getDescription().replace("\"", "\"\"") + "\"";
                String gravity = complaint.getGravity().name();

                writer.write(String.join(",",
                        id, date, reporterFullName, type, targetType, targetId, description, gravity));
                writer.newLine();
            }
        }
    }

    /** Load complaints from CSV. */
    public static List<Complaint> loadComplaints(List<Person> people,
                                                 List<TransportVehicle> vehicles,
                                                 List<Station> stations) throws IOException {
        if (!Files.exists(COMPLAINTS_PATH)) return new ArrayList<>();

        // Create lookup maps for reporters and targets
        Map<String, Person> personLookup = people.stream()
                .collect(Collectors.toMap(p -> p.getFirstName() + " " + p.getLastName(), p -> p));

        Map<String, TransportVehicle> vehicleLookup = vehicles.stream()
                .collect(Collectors.toMap(TransportVehicle::getId, v -> v));

        Map<String, Station> stationLookup = stations.stream()
                .collect(Collectors.toMap(Station::getName, s -> s));

        List<Complaint> complaints = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(COMPLAINTS_PATH)) {
            // Skip header
            reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {
                // Handle quoted fields (for description that might contain commas)
                List<String> cols = parseCSVLine(line);

                if (cols.size() < 8) continue; // Skip malformed lines

                int id = Integer.parseInt(cols.get(0));
                if (id >= Complaint.counter) {
                    Complaint.counter = id + 1;
                }
                LocalDate date = LocalDate.parse(cols.get(1));
                String reporterFullName = cols.get(2);
                ComplaintType type = ComplaintType.valueOf(cols.get(3));
                String targetType = cols.get(4);
                String targetId = cols.get(5);
                String description = cols.get(6);
                GravityLevel gravity = GravityLevel.valueOf(cols.get(7));

                // Find reporter
                Person reporter = personLookup.get(reporterFullName);
                if (reporter == null) continue; // Skip if reporter not found

                // Find target
                Suspendable target = null;
                if ("STATION".equals(targetType)) {
                    target = stationLookup.get(targetId);
                } else if ("VEHICLE".equals(targetType)) {
                    target = vehicleLookup.get(targetId);
                }

                if (target == null) continue; // Skip if target not found

                // Create complaint with the existing ID
                Complaint complaint = new Complaint(id ,reporter, type, target, description, gravity, date);

                // Add to list
                complaints.add(complaint);
            }
        }

        return complaints;
    }

    /**
     * Parse a CSV line handling quoted fields
     */
    private static List<String> parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder field = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                // If we see a quote inside quotes, and the next char is also a quote, it's an escaped quote
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    field.append('"');
                    i++; // Skip the next quote
                } else {
                    // Toggle the inQuotes flag
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                // End of field
                result.add(field.toString());
                field = new StringBuilder();
            } else {
                field.append(c);
            }
        }

        // Add the last field
        result.add(field.toString());

        return result;
    }
}