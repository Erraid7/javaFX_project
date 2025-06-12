package transport.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        // Initialize collectionsfff
        people = new ArrayList<>();
        passes = new ArrayList<>();
        vehicles = new ArrayList<>();
        stations = new ArrayList<>();
        complaintService = new ComplaintService();

        // Disable auto-save during initialization
        complaintService.setAutoSave(false);

        try {
            // Ensure data directory exists
            Path dataDir = Paths.get("data");
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
            }

            // Load data
            loadAllData();

            // Enable auto-save after initialization
            complaintService.setAutoSave(true);
        } catch (IOException e) {
            System.err.println("Error initializing AppState: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadAllData() throws IOException {
        // Load users
        people.clear();
        people.addAll(CsvPersistence.loadUsers());

        // Load passes
        passes.clear();
        passes.addAll(CsvPersistence.loadPasses(people));

        // Load vehicles
        vehicles.clear();
        vehicles.addAll(CsvPersistence.loadVehicles());

        // Load stations
        stations.clear();
        stations.addAll(CsvPersistence.loadStations());

        // Load complaints
        try {
            List<Complaint> complaints = CsvPersistence.loadComplaints(people, vehicles, stations);
            complaintService.addAll(complaints);
        } catch (Exception e) {
            System.err.println("Error loading complaints: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Person> getPeople() { return people; }
    public List<TransportPass> getPasses() { return passes; }
    public ComplaintService getComplaintService() { return complaintService; }

    public void addPerson(Person p) {
        people.add(p);
    }
    public void addPass(TransportPass tp) {
        System.out.println("Adding pass " + tp);
        passes.add(tp);
    }

    public void saveUsers() {
        try {
            CsvPersistence.saveUsers(people);
        }
        catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void savePasses() {
        try {
            CsvPersistence.savePasses(passes);
        }
        catch (IOException e) {
            System.err.println("Error saving passes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void saveComplaints() {
        try {
            CsvPersistence.saveComplaints(
                    complaintService.listAll(),
                    people,
                    vehicles,
                    stations
            );
        }
        catch (IOException e) {
            System.err.println("Error saving complaints: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<TransportVehicle> getVehicles() {
        return vehicles;
    }

    public List<Station> getStations() {
        return stations;
    }

    // For convenience, save all data at once
    public void saveAllData() {
        saveUsers();
        savePasses();
        saveComplaints();
    }
}