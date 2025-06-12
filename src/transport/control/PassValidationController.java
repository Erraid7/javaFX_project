package transport.control;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import transport.core.*;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class PassValidationController implements Initializable {

    @FXML
    private TextField passIdField;

    @FXML
    private VBox validationForm;

    @FXML
    private VBox successResult;

    @FXML
    private Label validPassIdLabel;

    @FXML
    private Label validPassTypeLabel;

    @FXML
    private Label validUserNameLabel;

    @FXML
    private Label validIssueDateLabel;

    @FXML
    private Label validExpiryDateLabel;

    @FXML
    private VBox errorResult;

    @FXML
    private Label errorMessageLabel;

    @FXML
    private Label invalidPassIdLabel;

    @FXML
    private Label invalidPassTypeLabel;

    @FXML
    private Label invalidUserNameLabel;

    @FXML
    private Label invalidIssueDateLabel;

    @FXML
    private Label invalidExpiryDateLabel;

    private AppState appState;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get the application state
        appState = AppState.getInstance();

        // Hide result sections initially
        successResult.setVisible(false);
        successResult.setManaged(false);
        errorResult.setVisible(false);
        errorResult.setManaged(false);
    }

    @FXML
    private void handleValidatePass(ActionEvent event) {
        String passIdText = passIdField.getText().trim();

        if (passIdText.isEmpty()) {
            showAlert("Validation Error", "Please enter a pass ID.");
            return;
        }

        try {
            int passId = Integer.parseInt(passIdText);
            TransportPass pass = findPassById(passId);

            if (pass == null) {
                showAlert("Validation Error", "No pass found with ID: " + passId);
                return;
            }

            try {
                boolean isValid = pass.isValid(LocalDate.now());
                showValidResult(pass);

                // If it's a ticket, mark it as used
                if (pass instanceof Ticket) {
                    ((Ticket) pass).markAsUsed();
                    appState.savePasses();
                }
            } catch (InvalidPassException e) {
                showInvalidResult(pass, e.getMessage());
            }
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Please enter a valid numeric ID.");
        }
    }

    private TransportPass findPassById(int id) {
        for (TransportPass pass : appState.getPasses()) {
            if (pass.getId() == id) {
                return pass;
            }
        }
        return null;
    }

    private void showValidResult(TransportPass pass) {
        // Hide validation form and error result
        validationForm.setVisible(false);
        validationForm.setManaged(false);
        errorResult.setVisible(false);
        errorResult.setManaged(false);

        // Set valid pass details
        validPassIdLabel.setText(String.valueOf(pass.getId()));
        validPassTypeLabel.setText(pass instanceof Ticket ? "Ticket" : "Personal Card");

        if (pass instanceof PersonalCard) {
            PersonalCard card = (PersonalCard) pass;
            validUserNameLabel.setText(card.getOwnerFullName());
            validExpiryDateLabel.setText(card.getExpiryDate().format(dateFormatter));
        } else {
            validUserNameLabel.setText(((Ticket) pass).getOwnerFullName());
            validExpiryDateLabel.setText(pass.getPurchaseDate().plusDays(1).format(dateFormatter));
        }

        validIssueDateLabel.setText(pass.getPurchaseDate().format(dateFormatter));

        // Show success result
        successResult.setVisible(true);
        successResult.setManaged(true);
    }

    private void showInvalidResult(TransportPass pass, String errorMessage) {
        // Hide validation form and success result
        validationForm.setVisible(false);
        validationForm.setManaged(false);
        successResult.setVisible(false);
        successResult.setManaged(false);

        // Set error message
        errorMessageLabel.setText(errorMessage);

        // Set invalid pass details
        invalidPassIdLabel.setText(String.valueOf(pass.getId()));
        invalidPassTypeLabel.setText(pass instanceof Ticket ? "Ticket" : "Personal Card");

        if (pass instanceof PersonalCard) {
            PersonalCard card = (PersonalCard) pass;
            invalidUserNameLabel.setText(card.getOwnerFullName());
            invalidExpiryDateLabel.setText(card.getExpiryDate().format(dateFormatter));
        } else {
            invalidUserNameLabel.setText(((Ticket) pass).getOwnerFullName());
            invalidExpiryDateLabel.setText(pass.getPurchaseDate().plusDays(1).format(dateFormatter));
        }

        invalidIssueDateLabel.setText(pass.getPurchaseDate().format(dateFormatter));

        // Show error result
        errorResult.setVisible(true);
        errorResult.setManaged(true);
    }

    @FXML
    private void resetValidation(ActionEvent event) {
        // Clear pass ID field
        passIdField.clear();

        // Hide result sections
        successResult.setVisible(false);
        successResult.setManaged(false);
        errorResult.setVisible(false);
        errorResult.setManaged(false);

        // Show validation form
        validationForm.setVisible(true);
        validationForm.setManaged(true);
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
            Stage stage = (Stage) passIdField.getScene().getWindow();
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
            Stage stage = (Stage) passIdField.getScene().getWindow();
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