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

public class ComplaintsListController implements Initializable {

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> complaintTypeCombo;

    @FXML
    private ComboBox<String> gravityCombo;

    @FXML
    private TableView<Complaint> complaintsTable;

    @FXML
    private TableColumn<Complaint, Integer> idColumn;

    @FXML
    private TableColumn<Complaint, LocalDate> dateColumn;

    @FXML
    private TableColumn<Complaint, String> reporterColumn;

    @FXML
    private TableColumn<Complaint, ComplaintType> typeColumn;

    @FXML
    private TableColumn<Complaint, String> targetColumn;

    @FXML
    private TableColumn<Complaint, GravityLevel> gravityColumn;

    @FXML
    private TableColumn<Complaint, String> descriptionColumn;

    @FXML
    private TableColumn<Complaint, Void> actionsColumn;

    // Pagination removed

    private AppState appState;
    private ObservableList<Complaint> allComplaints;
    private FilteredList<Complaint> filteredComplaints;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get the application state
        appState = AppState.getInstance();

        // Initialize the table columns
        initializeTableColumns();

        // Load all complaints
        loadComplaints();

        // Set up search and filter functionality
        setupSearchAndFilter();
    }

    private void initializeTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        reporterColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getReporter().toString()));

        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));

        targetColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTarget().toString()));

        gravityColumn.setCellValueFactory(new PropertyValueFactory<>("gravity"));

        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionColumn.setCellFactory(column -> {
            TableCell<Complaint, String> cell = new TableCell<Complaint, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.length() > 50 ? item.substring(0, 47) + "..." : item);
                    }
                }
            };
            return cell;
        });

        // Set up the actions column with buttons
        actionsColumn.setCellFactory(createActionsColumnCellFactory());
    }

    private Callback<TableColumn<Complaint, Void>, TableCell<Complaint, Void>> createActionsColumnCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<Complaint, Void> call(TableColumn<Complaint, Void> param) {
                return new TableCell<>() {
                    private final Button viewButton = new Button("View");
                    private final Button resolveButton = new Button("Resolve");
                    private final HBox pane = new HBox(5, viewButton, resolveButton);

                    {
                        viewButton.getStyleClass().addAll("btn", "btn-secondary");
                        viewButton.setOnAction(event -> {
                            Complaint complaint = getTableView().getItems().get(getIndex());
                            // Handle view action
                            showComplaintDetails(complaint);
                        });

                        resolveButton.getStyleClass().addAll("btn", "btn-primary");
                        resolveButton.setOnAction(event -> {
                            Complaint complaint = getTableView().getItems().get(getIndex());
                            // Handle resolve action
                            resolveComplaint(complaint);
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

    private void loadComplaints() {
        allComplaints = FXCollections.observableArrayList(appState.getComplaintService().listAll());
        filteredComplaints = new FilteredList<>(allComplaints);
        complaintsTable.setItems(filteredComplaints);
    }

    private void setupSearchAndFilter() {
        // Set up search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });

        // Set up filter by complaint type
        complaintTypeCombo.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });

        // Set up filter by gravity level
        gravityCombo.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase();
        String complaintType = complaintTypeCombo.getValue();
        String gravityLevel = gravityCombo.getValue();

        filteredComplaints.setPredicate(complaint -> {
            boolean matchesSearch = searchText == null || searchText.isEmpty() ||
                    String.valueOf(complaint.getId()).contains(searchText) ||
                    complaint.getReporter().toString().toLowerCase().contains(searchText) ||
                    complaint.getTarget().toString().toLowerCase().contains(searchText) ||
                    complaint.getDescription().toLowerCase().contains(searchText);

            boolean matchesType = complaintType == null || complaintType.equals("All Types") ||
                    complaint.getType().name().equals(complaintType);

            boolean matchesGravity = gravityLevel == null || gravityLevel.equals("All Gravity Levels") ||
                    complaint.getGravity().name().equals(gravityLevel);

            return matchesSearch && matchesType && matchesGravity;
        });
    }

    private void showComplaintDetails(Complaint complaint) {
        StringBuilder details = new StringBuilder();
        details.append("Complaint ID: ").append(complaint.getId()).append("\n");
        details.append("Date: ").append(complaint.getDate()).append("\n");
        details.append("Reporter: ").append(complaint.getReporter()).append("\n");
        details.append("Type: ").append(complaint.getType()).append("\n");
        details.append("Target: ").append(complaint.getTarget()).append("\n");
        details.append("Gravity: ").append(complaint.getGravity()).append("\n");
        details.append("Description: ").append(complaint.getDescription()).append("\n");

        showAlert("Complaint Details", details.toString());
    }

    // Update this method in your existing ComplaintsListController class
    private void resolveComplaint(Complaint complaint) {
        if (confirmResolve("Resolve Complaint", "Are you sure you want to resolve this complaint?")) {
            appState.getComplaintService().resolve(complaint);

            // Explicitly save complaints
            appState.saveComplaints();

            loadComplaints(); // Reload the table
            applyFilters();
        }
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        applyFilters();
    }

    @FXML
    private void handleTypeChange(ActionEvent event) {
        applyFilters();
    }

    @FXML
    private void handleGravityChange(ActionEvent event) {
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
        navigateTo("passes-list.fxml");
    }

    @FXML
    private void navigateToComplaints(ActionEvent event) {
        // Already on complaints list, no need to navigate
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

    private boolean confirmResolve(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
}