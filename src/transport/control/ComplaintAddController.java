package transport.control;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import transport.core.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class ComplaintAddController implements Initializable {

    @FXML
    private TextField reporterSearchField;

    @FXML
    private ListView<String> searchResultsList;

    @FXML
    private HBox selectedReporterCard;

    @FXML
    private Label reporterInitialsLabel;

    @FXML
    private Label selectedReporterNameLabel;

    @FXML
    private Label selectedReporterEmailLabel;

    @FXML
    private ComboBox<String> targetTypeCombo;

    @FXML
    private ComboBox<Suspendable> targetCombo;

    @FXML
    private ComboBox<ComplaintType> complaintTypeCombo;

    @FXML
    private ComboBox<GravityLevel> gravityLevelCombo;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private TextField documentPathField;

    @FXML
    private HBox successMessage;

    private AppState appState;
    private Person selectedReporter;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get the application state
        appState = AppState.getInstance();

        // Initialize complaint types
        complaintTypeCombo.getItems().setAll(ComplaintType.values());

        // Initialize gravity levels
        gravityLevelCombo.getItems().setAll(GravityLevel.values());

        // Set up target type change listener
        targetTypeCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateTargetCombo(newVal);
        });

        // Hide search results and selected reporter card initially
        searchResultsList.setVisible(false);
        searchResultsList.setManaged(false);
        selectedReporterCard.setVisible(false);
        selectedReporterCard.setManaged(false);

        // Hide success message initially
        successMessage.setVisible(false);
        successMessage.setManaged(false);
    }

    @FXML
    private void handleReporterSearch(KeyEvent event) {
        String searchText = reporterSearchField.getText().trim().toLowerCase();

        if (searchText.isEmpty()) {
            searchResultsList.setVisible(false);
            searchResultsList.setManaged(false);
            return;
        }

        ObservableList<String> filteredResults = FXCollections.observableArrayList();

        for (Person person : appState.getPeople()) {
            if (person.getFirstName().toLowerCase().contains(searchText) ||
                    person.getLastName().toLowerCase().contains(searchText)) {

                filteredResults.add(person.getFirstName() + " " + person.getLastName());
            }
        }

        searchResultsList.setItems(filteredResults);
        searchResultsList.setVisible(!filteredResults.isEmpty());
        searchResultsList.setManaged(!filteredResults.isEmpty());
    }

    @FXML
    private void handleReporterSelection(MouseEvent event) {
        String selected = searchResultsList.getSelectionModel().getSelectedItem();

        if (selected != null) {
            // Find the person
            for (Person person : appState.getPeople()) {
                if ((person.getFirstName() + " " + person.getLastName()).equals(selected)) {
                    selectedReporter = person;
                    break;
                }
            }

            if (selectedReporter != null) {
                // Update the selected reporter card
                selectedReporterNameLabel.setText(selectedReporter.getFirstName() + " " + selectedReporter.getLastName());
                selectedReporterEmailLabel.setText(selectedReporter instanceof Employee ?
                        ((Employee) selectedReporter).getMatricule() : "Regular User");

                // Set initials
                String[] nameParts = selectedReporter.getFirstName().split(" ");
                String initials = nameParts.length > 0 ? String.valueOf(nameParts[0].charAt(0)) : "";
                nameParts = selectedReporter.getLastName().split(" ");
                initials += nameParts.length > 0 ? String.valueOf(nameParts[0].charAt(0)) : "";
                reporterInitialsLabel.setText(initials);

                // Show the selected reporter card and hide search results
                selectedReporterCard.setVisible(true);
                selectedReporterCard.setManaged(true);
                searchResultsList.setVisible(false);
                searchResultsList.setManaged(false);
            }
        }
    }

    @FXML
    private void handleTargetTypeChange(ActionEvent event) {
        String targetType = targetTypeCombo.getValue();
        updateTargetCombo(targetType);
    }

    private void updateTargetCombo(String targetType) {
        if (targetType == null) {
            targetCombo.getItems().clear();
            return;
        }

        if (targetType.equals("Station")) {
            targetCombo.setItems(FXCollections.observableArrayList(appState.getStations()));
        } else if (targetType.equals("Vehicle")) {
            targetCombo.setItems(FXCollections.observableArrayList(appState.getVehicles()));
        }
    }

    @FXML
    private void handleBrowseDocument(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Supporting Document");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(documentPathField.getScene().getWindow());
        if (selectedFile != null) {
            documentPathField.setText(selectedFile.getAbsolutePath());
        }
    }

    // Add this method to your existing ComplaintAddController class
    @FXML
    private void handleSubmitComplaint(ActionEvent event) {
        if (validateForm()) {
            // Create and submit the complaint
            Complaint complaint = new Complaint(
                    selectedReporter,
                    complaintTypeCombo.getValue(),
                    targetCombo.getValue(),
                    descriptionArea.getText().trim(),
                    gravityLevelCombo.getValue(),
                    LocalDate.now()
            );

            // Submit to service (this will also save to CSV)
            appState.getComplaintService().submit(complaint);

            // Show success message
            successMessage.setVisible(true);
            successMessage.setManaged(true);

            // Clear the form after a delay
            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                    javafx.application.Platform.runLater(() -> {
                        successMessage.setVisible(false);
                        successMessage.setManaged(false);
                        clearForm();
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private boolean validateForm() {
        if (selectedReporter == null) {
            showAlert("Please select a reporter");
            return false;
        }

        if (targetTypeCombo.getValue() == null) {
            showAlert("Please select a target type");
            return false;
        }

        if (targetCombo.getValue() == null) {
            showAlert("Please select a target");
            return false;
        }

        if (complaintTypeCombo.getValue() == null) {
            showAlert("Please select a complaint type");
            return false;
        }

        if (gravityLevelCombo.getValue() == null) {
            showAlert("Please select a gravity level");
            return false;
        }

        if (descriptionArea.getText().trim().isEmpty()) {
            showAlert("Please enter a description");
            return false;
        }

        return true;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearForm() {
        reporterSearchField.clear();
        selectedReporter = null;
        selectedReporterCard.setVisible(false);
        selectedReporterCard.setManaged(false);
        targetTypeCombo.setValue(null);
        targetCombo.getItems().clear();
        complaintTypeCombo.setValue(null);
        gravityLevelCombo.setValue(null);
        descriptionArea.clear();
        documentPathField.clear();
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        navigateToComplaints(event);
    }

    @FXML
    private void navigateToDashboard(ActionEvent event) {
        navigateTo("dashboard.fxml");
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
    private void logout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/transport/ui/login.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage) reporterSearchField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("ESI-RUN Station Console - Login");
            stage.setWidth(1440);
            stage.setHeight(800);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void navigateTo(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/transport/ui/" + fxml));
            Scene scene = new Scene(root);
            Stage stage = (Stage) reporterSearchField.getScene().getWindow();
            stage.setScene(scene);
            stage.setWidth(1440);
            stage.setHeight(800);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}