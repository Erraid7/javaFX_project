package transport.control;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import transport.core.*;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class UserAddController implements Initializable {

    @FXML
    private RadioButton regularUserRadio;

    @FXML
    private RadioButton employeeRadio;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private DatePicker dobPicker;

    @FXML
    private CheckBox handicapCheckbox;

    @FXML
    private VBox employeeSection;

    @FXML
    private TextField matriculeField;

    @FXML
    private ComboBox<String> functionCombo;

    @FXML
    private HBox successMessage;

    private AppState appState;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get the application state
        appState = AppState.getInstance();

        // Set up the user type toggle group listener
        employeeRadio.selectedProperty().addListener((observable, oldValue, newValue) -> {
            employeeSection.setVisible(newValue);
            employeeSection.setManaged(newValue);
        });

        // Initialize the function combo box
        functionCombo.getItems().clear();
        for (FunctionType function : FunctionType.values()) {
            functionCombo.getItems().add(function.name());
        }

        // Hide success message initially
        successMessage.setVisible(false);
        successMessage.setManaged(false);
    }

    @FXML
    private void handleSaveUser(ActionEvent event) {
        if (validateForm()) {
            // Get form data
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            LocalDate dob = dobPicker.getValue();
            boolean handicap = handicapCheckbox.isSelected();

            Person newPerson;

            if (employeeRadio.isSelected()) {
                // Create an employee
                String matricule = matriculeField.getText().trim();
                FunctionType function = FunctionType.valueOf(functionCombo.getValue());
                newPerson = new Employee(firstName, lastName, dob, handicap, matricule, function);
            } else {
                // Create a regular user
                newPerson = new User(firstName, lastName, dob, handicap);
            }

            // Add the person to the application state
            appState.addPerson(newPerson);
            appState.saveUsers();

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
        // Validate first name
        if (firstNameField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Please enter a first name.");
            return false;
        }

        // Validate last name
        if (lastNameField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Please enter a last name.");
            return false;
        }

        // Validate date of birth
        if (dobPicker.getValue() == null) {
            showAlert("Validation Error", "Please select a date of birth.");
            return false;
        }

        // Validate employee-specific fields if employee is selected
        if (employeeRadio.isSelected()) {
            // Validate matricule
            if (matriculeField.getText().trim().isEmpty()) {
                showAlert("Validation Error", "Please enter a matricule for the employee.");
                return false;
            }

            // Validate function
            if (functionCombo.getValue() == null) {
                showAlert("Validation Error", "Please select a function for the employee.");
                return false;
            }
        }

        return true;
    }

    private void clearForm() {
        regularUserRadio.setSelected(true);
        employeeRadio.setSelected(false);
        firstNameField.clear();
        lastNameField.clear();
        dobPicker.setValue(null);
        handicapCheckbox.setSelected(false);
        matriculeField.clear();
        functionCombo.setValue(null);
        employeeSection.setVisible(false);
        employeeSection.setManaged(false);
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        navigateToUsers(event);
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
            Stage stage = (Stage) firstNameField.getScene().getWindow();
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
            Stage stage = (Stage) firstNameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setWidth(1440);
            stage.setHeight(800);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}