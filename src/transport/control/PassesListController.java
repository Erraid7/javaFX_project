package transport.control;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import transport.core.*;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class PassesListController implements Initializable {

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> passTypeCombo;

    @FXML
    private TableView<TransportPass> passesTable;

    @FXML
    private TableColumn<TransportPass, Integer> idColumn;

    @FXML
    private TableColumn<TransportPass, String> typeColumn;

    @FXML
    private TableColumn<TransportPass, LocalDate> purchaseDateColumn;

    @FXML
    private TableColumn<TransportPass, Double> priceColumn;

    @FXML
    private TableColumn<TransportPass, PaymentMethod> paymentMethodColumn;

    @FXML
    private TableColumn<TransportPass, String> ownerColumn;

    @FXML
    private TableColumn<TransportPass, Void> actionsColumn;

    // Pagination removed

    private AppState appState;
    private ObservableList<TransportPass> allPasses;
    private FilteredList<TransportPass> filteredPasses;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get the application state
        appState = AppState.getInstance();

        // Initialize the table columns
        initializeTableColumns();

        // Load all passes
        loadPasses();

        // Set up search and filter functionality
        setupSearchAndFilter();
    }

    private void initializeTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        typeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue() instanceof Ticket ? "Ticket" : "Personal Card"));

        purchaseDateColumn.setCellValueFactory(new PropertyValueFactory<>("purchaseDate"));

        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceColumn.setCellFactory(column -> new TableCell<TransportPass, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f DA", item));
                }
            }
        });

        paymentMethodColumn.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));

        ownerColumn.setCellValueFactory(cellData -> {
            TransportPass pass = cellData.getValue();
            if (pass instanceof PersonalCard) {
                return new SimpleStringProperty(((PersonalCard) pass).getOwnerFullName());
            } else {
                return new SimpleStringProperty(((Ticket) pass).getOwnerFullName());
            }
        });

        // Set up the actions column with buttons
        actionsColumn.setCellFactory(createActionsColumnCellFactory());
    }

    private Callback<TableColumn<TransportPass, Void>, TableCell<TransportPass, Void>> createActionsColumnCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<TransportPass, Void> call(TableColumn<TransportPass, Void> param) {
                return new TableCell<>() {
                    private final Button viewButton = new Button("View");
                    private final Button validateButton = new Button("Validate");
                    private final HBox pane = new HBox(5, viewButton, validateButton);

                    {
                        viewButton.getStyleClass().addAll("btn", "btn-secondary");
                        viewButton.setOnAction(event -> {
                            TransportPass pass = getTableView().getItems().get(getIndex());
                            // Handle view action
                            showPassDetails(pass);
                        });

                        validateButton.getStyleClass().addAll("btn", "btn-primary");
                        validateButton.setOnAction(event -> {
                            TransportPass pass = getTableView().getItems().get(getIndex());
                            // Handle validate action
                            validatePass(pass);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : pane);
                    }
                };
            }
        };
    }

    private void loadPasses() {
        allPasses = FXCollections.observableArrayList(appState.getPasses());
        FXCollections.reverse(allPasses);
        filteredPasses = new FilteredList<>(allPasses);
        passesTable.setItems(filteredPasses);
    }

    private void setupSearchAndFilter() {
        // Set up search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });

        // Set up filter by pass type
        passTypeCombo.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase();
        String passType = passTypeCombo.getValue();

        filteredPasses.setPredicate(pass -> {
            boolean matchesSearch = searchText == null || searchText.isEmpty() ||
                    String.valueOf(pass.getId()).contains(searchText) ||
                    (pass instanceof PersonalCard && ((PersonalCard) pass).getOwnerFullName().toLowerCase().contains(searchText));

            boolean matchesType = passType == null || passType.equals("All Types") ||
                    (passType.equals("Ticket") && pass instanceof Ticket) ||
                    (passType.equals("Personal Card") && pass instanceof PersonalCard);

            return matchesSearch && matchesType;
        });
    }

    private void showPassDetails(TransportPass pass) {
        StringBuilder details = new StringBuilder();
        details.append("Pass ID: ").append(pass.getId()).append("\n");
        details.append("Type: ").append(pass instanceof Ticket ? "Ticket" : "Personal Card").append("\n");
        details.append("Purchase Date: ").append(pass.getPurchaseDate()).append("\n");
        details.append("Price: ").append(String.format("%.2f DA", pass.getPrice())).append("\n");
        details.append("Payment Method: ").append(pass.getPaymentMethod()).append("\n");

        if (pass instanceof PersonalCard) {
            PersonalCard card = (PersonalCard) pass;
            details.append("Owner: ").append(card.getOwnerFullName()).append("\n");
            details.append("Card Type: ").append(card.getCardType()).append("\n");
            details.append("Expiry Date: ").append(card.getExpiryDate()).append("\n");
            details.append("Employee Card: ").append(card.isEmployee() ? "Yes" : "No").append("\n");
        } else if (pass instanceof Ticket) {
            Ticket ticket = (Ticket) pass;
            details.append("Used: ").append(ticket.isUsed() ? "Yes" : "No").append("\n");
        }

        showAlert("Pass Details", details.toString());
    }

    private void validatePass(TransportPass pass) {
        try {
            boolean isValid = pass.isValid(LocalDate.now());
            if (isValid) {
                if (pass instanceof Ticket) {
                    ((Ticket) pass).markAsUsed();
                    appState.savePasses();
                    showAlert("Pass Validation", "The ticket is valid and has been marked as used.");
                } else {
                    showAlert("Pass Validation", "The personal card is valid.");
                }
            }
        } catch (InvalidPassException e) {
            showAlert("Pass Validation", "The pass is invalid: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        applyFilters();
    }

    @FXML
    private void handleFilterChange(ActionEvent event) {
        applyFilters();
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
        // Already on passes list, no need to navigate
    }

    @FXML
    private void navigateToComplaints(ActionEvent event) {
        navigateTo("complaints-list.fxml");
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
    private void logout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/transport/ui/login.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage) searchField.getScene().getWindow();
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
            Stage stage = (Stage) searchField.getScene().getWindow();
            stage.setScene(scene);
            stage.setWidth(1440);
            stage.setHeight(800);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}