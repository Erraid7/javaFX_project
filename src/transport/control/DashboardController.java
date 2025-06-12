package transport.control;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import transport.core.AppState;
import transport.core.Complaint;
import transport.core.TransportPass;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML
    private Label userCountLabel;

    @FXML
    private Label passCountLabel;

    @FXML
    private Label complaintCountLabel;

    @FXML
    private Label vehicleCountLabel;

    // If your FXML doesn't have these table elements, comment them out
    // or update the fx:ids to match what's in your FXML
    /*
    @FXML
    private TableView<ActivityItem> recentActivityTable;

    @FXML
    private TableColumn<ActivityItem, String> activityDateColumn;

    @FXML
    private TableColumn<ActivityItem, String> activityTypeColumn;

    @FXML
    private TableColumn<ActivityItem, String> activityDescriptionColumn;

    @FXML
    private TableColumn<ActivityItem, String> activityStatusColumn;
    */

    private AppState appState;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get the application state
        appState = AppState.getInstance();

        // Initialize the dashboard statistics
        updateStatistics();

        // Only initialize the activity table if it exists in your FXML
        // If it doesn't exist, comment this out
        // initializeActivityTable();
        // loadRecentActivity();
    }

    private void updateStatistics() {
        try {
            // Update user count
            if (userCountLabel != null) {
                userCountLabel.setText(String.valueOf(appState.getPeople().size()));
            }

            // Update pass count
            if (passCountLabel != null) {
                long activePassCount = appState.getPasses().stream()
                        .filter(pass -> {
                            try {
                                return pass.isValid(LocalDate.now());
                            } catch (Exception e) {
                                return false;
                            }
                        })
                        .count();
                passCountLabel.setText(String.valueOf(activePassCount));
            }

            // Update complaint count
            if (complaintCountLabel != null && appState.getComplaintService() != null) {
                complaintCountLabel.setText(String.valueOf(appState.getComplaintService().listAll().size()));
            }

            // Update vehicle count
            if (vehicleCountLabel != null) {
                long activeVehicleCount = appState.getVehicles().stream()
                        .filter(vehicle -> !vehicle.isSuspended())
                        .count();
                vehicleCountLabel.setText(String.valueOf(activeVehicleCount));
            }
        } catch (Exception e) {
            System.err.println("Error updating statistics: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Only uncomment this if your FXML has the activity table
    /*
    private void initializeActivityTable() {
        if (activityDateColumn != null) {
            activityDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        }
        if (activityTypeColumn != null) {
            activityTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        }
        if (activityDescriptionColumn != null) {
            activityDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        }
        if (activityStatusColumn != null) {
            activityStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        }
    }

    private void loadRecentActivity() {
        if (recentActivityTable == null) return;

        ObservableList<ActivityItem> activityItems = FXCollections.observableArrayList();

        // Add recent complaints as activity items
        if (appState.getComplaintService() != null) {
            appState.getComplaintService().listAll().stream()
                    .sorted((c1, c2) -> c2.getDate().compareTo(c1.getDate())) // Sort by date descending
                    .limit(5) // Get only the 5 most recent
                    .forEach(complaint -> {
                        activityItems.add(new ActivityItem(
                                complaint.getDate().toString(),
                                "Complaint",
                                "Complaint #" + complaint.getId() + " - " + complaint.getType(),
                                complaint.getGravity().toString()
                        ));
                    });
        }

        // Add recent passes as activity items
        appState.getPasses().stream()
                .sorted((p1, p2) -> p2.getPurchaseDate().compareTo(p1.getPurchaseDate())) // Sort by date descending
                .limit(5) // Get only the 5 most recent
                .forEach(pass -> {
                    String type = pass instanceof transport.core.Ticket ? "Ticket" : "Personal Card";
                    String status;
                    try {
                        status = pass.isValid(LocalDate.now()) ? "Valid" : "Invalid";
                    } catch (Exception e) {
                        status = "Invalid";
                    }
                    activityItems.add(new ActivityItem(
                            pass.getPurchaseDate().toString(),
                            "Pass Issued",
                            type + " #" + pass.getId(),
                            status
                    ));
                });

        // Sort all activity items by date descending
        activityItems.sort((a1, a2) -> a2.getDate().compareTo(a1.getDate()));

        // Set the items to the table
        recentActivityTable.setItems(activityItems);
    }
    */

    @FXML
    private void navigateToDashboard(ActionEvent event) {
        // Already on dashboard, no need to navigate
    }

    @FXML
    private void navigateToUsers(ActionEvent event) {
        navigateTo("users-list.fxml");
    }

    @FXML
    private void navigateToPasses(ActionEvent event) {
        navigateTo("passes-list.fxml");
    }

    @FXML
    private void navigateToComplaints(ActionEvent event) {
        navigateTo("complaints-list.fxml");
    }

    @FXML
    private void navigateToAddUser(ActionEvent event) {
        navigateTo("users-add.fxml");
    }

    @FXML
    private void navigateToAddPass(ActionEvent event) {
        navigateTo("passes-add.fxml");
    }

    @FXML
    private void navigateToValidatePass(ActionEvent event) {
        navigateTo("pass-validation.fxml");
    }

    @FXML
    private void navigateToAddComplaint(ActionEvent event) {
        navigateTo("complaints-add.fxml");
    }

    @FXML
    private void logout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/transport/ui/login.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage) (userCountLabel != null ? userCountLabel.getScene().getWindow() : null);
            if (stage != null) {
                stage.setScene(scene);
                stage.setTitle("ESI-RUN Station Console - Login");
                stage.setMaximized(false);
                stage.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void navigateTo(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/transport/ui/" + fxml));
            Scene scene = new Scene(root);
            Stage stage = (Stage) (userCountLabel != null ? userCountLabel.getScene().getWindow() : null);
            if (stage != null) {
                stage.setScene(scene);
                stage.setWidth(1440);
                stage.setHeight(800);
                stage.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Only uncomment this if your FXML has the activity table
    /*
    // Inner class for activity items
    public static class ActivityItem {
        private final String date;
        private final String type;
        private final String description;
        private final String status;

        public ActivityItem(String date, String type, String description, String status) {
            this.date = date;
            this.type = type;
            this.description = description;
            this.status = status;
        }

        public String getDate() {
            return date;
        }

        public String getType() {
            return type;
        }

        public String getDescription() {
            return description;
        }

        public String getStatus() {
            return status;
        }
    }
    */
}