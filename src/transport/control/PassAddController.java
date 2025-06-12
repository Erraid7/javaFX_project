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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import transport.core.*;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class PassAddController implements Initializable {

    @FXML
    private RadioButton ticketRadio;

    @FXML
    private RadioButton personalCardRadio;

    @FXML
    private VBox ownerSection;

    @FXML
    private TextField userSearchField;

    @FXML
    private ListView<String> searchResultsList;

    @FXML
    private HBox selectedUserCard;

    @FXML
    private Label userInitialsLabel;

    @FXML
    private Label selectedUserNameLabel;

    @FXML
    private Label selectedUserTypeLabel;

    @FXML
    private Label selectedUserDobLabel;

    @FXML
    private ComboBox<String> paymentMethodCombo;

    @FXML
    private Label basePriceLabel;

    @FXML
    private Label discountLabel;

    @FXML
    private Label totalPriceLabel;

    @FXML
    private HBox successMessage;

    private AppState appState;
    private Person selectedUser;
    private double basePrice = 50.0; // Default for ticket
    private double discount = 0.0;
    private double totalPrice = 50.0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get the application state
        appState = AppState.getInstance();

        // Set up the pass type toggle group listener
        personalCardRadio.selectedProperty().addListener((observable, oldValue, newValue) -> {
            // Always show owner section, but update price calculation
            updatePriceCalculation();
        });

        ticketRadio.selectedProperty().addListener((observable, oldValue, newValue) -> {
            // Always show owner section, but update price calculation
            updatePriceCalculation();
        });

        // Initialize the payment method combo box
        paymentMethodCombo.getItems().clear();
        for (PaymentMethod method : PaymentMethod.values()) {
            paymentMethodCombo.getItems().add(method.name());
        }

        // Always show owner section
        ownerSection.setVisible(true);
        ownerSection.setManaged(true);

        // Hide search results and selected user card initially
        searchResultsList.setVisible(false);
        searchResultsList.setManaged(false);
        selectedUserCard.setVisible(false);
        selectedUserCard.setManaged(false);

        // Hide success message initially
        successMessage.setVisible(false);
        successMessage.setManaged(false);

        // Update price calculation
        updatePriceCalculation();
    }

    @FXML
    private void handleUserSearch(KeyEvent event) {
        String searchText = userSearchField.getText().trim().toLowerCase();

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
    private void handleUserSelection(MouseEvent event) {
        String selected = searchResultsList.getSelectionModel().getSelectedItem();

        if (selected != null) {
            // Find the person
            for (Person person : appState.getPeople()) {
                if ((person.getFirstName() + " " + person.getLastName()).equals(selected)) {
                    selectedUser = person;
                    break;
                }
            }

            if (selectedUser != null) {
                // Update the selected user card
                selectedUserNameLabel.setText(selectedUser.getFirstName() + " " + selectedUser.getLastName());
                selectedUserTypeLabel.setText(selectedUser instanceof Employee ? "Employee" : "User");
                selectedUserDobLabel.setText("DOB: " + selectedUser.getDateOfBirth().format(DateTimeFormatter.ISO_DATE));

                // Set initials
                String[] nameParts = selectedUser.getFirstName().split(" ");
                String initials = nameParts.length > 0 ? String.valueOf(nameParts[0].charAt(0)) : "";
                nameParts = selectedUser.getLastName().split(" ");
                initials += nameParts.length > 0 ? String.valueOf(nameParts[0].charAt(0)) : "";
                userInitialsLabel.setText(initials);

                // Show the selected user card and hide search results
                selectedUserCard.setVisible(true);
                selectedUserCard.setManaged(true);
                searchResultsList.setVisible(false);
                searchResultsList.setManaged(false);

                // Update price calculation
                updatePriceCalculation();
            }
        }
    }

    private void updatePriceCalculation() {
        if (selectedUser == null) {
            // No user selected yet
            if (ticketRadio.isSelected()) {
                basePrice = 50.0;
                discount = 0.0;
            } else {
                basePrice = 5000.0;
                discount = 0.0;
            }
            totalPrice = basePrice;
        } else {
            boolean isEmployee = selectedUser instanceof Employee;
            int age = Period.between(selectedUser.getDateOfBirth(), LocalDate.now()).getYears();
            boolean hasHandicap = selectedUser.hasHandicap();

            if (ticketRadio.isSelected()) {
                // Ticket price is always 50 DA
                basePrice = 50.0;
                discount = 0.0;
                totalPrice = basePrice;
            } else {
                // Personal card with base price of 5000 DA
                basePrice = 5000.0;

                // Calculate discount based on user profile
                if (hasHandicap) {
                    // Solidarity card: 50% reduction
                    discount = basePrice * 0.5;
                } else if (isEmployee) {
                    // Partner card: 40% reduction
                    discount = basePrice * 0.4;
                } else if (age < 25) {
                    // Junior card: 30% reduction
                    discount = basePrice * 0.3;
                } else if (age > 65) {
                    // Senior card: 25% reduction
                    discount = basePrice * 0.25;
                } else {
                    // No reduction applies
                    discount = 0.0;
                }

                totalPrice = basePrice - discount;
            }
        }

        // Update the UI
        basePriceLabel.setText(String.format("%.2f DA", basePrice));
        discountLabel.setText(String.format("%.2f DA", discount));
        totalPriceLabel.setText(String.format("%.2f DA", totalPrice));
    }

    @FXML
    private void handleIssuePass(ActionEvent event) {
        if (validateForm()) {
            PaymentMethod paymentMethod = PaymentMethod.valueOf(paymentMethodCombo.getValue());
            TransportPass newPass;

            if (ticketRadio.isSelected()) {
                // Create a ticket
                newPass = new Ticket(paymentMethod, selectedUser);
            } else {
                // Create a personal card
                // The isEmployee flag is automatically determined from the selectedUser type
                boolean isEmployee = selectedUser instanceof Employee;
                try {
                    newPass = new PersonalCard(selectedUser, isEmployee, paymentMethod);
                } catch (ReductionImpossibleException e) {
                    showAlert("Validation Error", "No discount applies for this user. Cannot issue personal card.");
                    return;
                }
            }

            System.out.println(newPass);

            // Add the pass to the application state
            appState.addPass(newPass);
            appState.savePasses();

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
        // Validate user selection
        if (selectedUser == null) {
            showAlert("Validation Error", "Please select a user for the pass.");
            return false;
        }

        // Validate payment method
        if (paymentMethodCombo.getValue() == null) {
            showAlert("Validation Error", "Please select a payment method.");
            return false;
        }

        // For personal cards, validate that a discount applies
        if (personalCardRadio.isSelected()) {
            boolean isEmployee = selectedUser instanceof Employee;
            int age = Period.between(selectedUser.getDateOfBirth(), LocalDate.now()).getYears();
            boolean hasHandicap = selectedUser.hasHandicap();

            if (!hasHandicap && !isEmployee && age >= 25 && age <= 65) {
                showAlert("Validation Error", "No discount applies for this user. Cannot issue personal card.");
                return false;
            }
        }

        return true;
    }

    private void clearForm() {
        ticketRadio.setSelected(true);
        personalCardRadio.setSelected(false);
        userSearchField.clear();
        selectedUser = null;
        selectedUserCard.setVisible(false);
        selectedUserCard.setManaged(false);
        paymentMethodCombo.setValue(null);
        updatePriceCalculation();
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        navigateToPasses(event);
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
            Stage stage = (Stage) paymentMethodCombo.getScene().getWindow();
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
            Stage stage = (Stage) paymentMethodCombo.getScene().getWindow();
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