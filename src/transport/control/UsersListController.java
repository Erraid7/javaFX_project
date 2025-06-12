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
import transport.core.AppState;
import transport.core.Employee;
import transport.core.Person;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class UsersListController implements Initializable {

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> userTypeCombo;

    @FXML
    private TableView<Person> usersTable;

    @FXML
    private TableColumn<Person, String> firstNameColumn;

    @FXML
    private TableColumn<Person, String> lastNameColumn;

    @FXML
    private TableColumn<Person, LocalDate> dobColumn;

    @FXML
    private TableColumn<Person, String> typeColumn;

    @FXML
    private TableColumn<Person, Boolean> handicapColumn;

    @FXML
    private TableColumn<Person, Void> actionsColumn;

    // Pagination removed

    private AppState appState;
    private ObservableList<Person> allUsers;
    private FilteredList<Person> filteredUsers;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get the application state
        appState = AppState.getInstance();

        // Initialize the table columns
        initializeTableColumns();

        // Load all users
        loadUsers();

        // Set up search and filter functionality
        setupSearchAndFilter();
    }

    // Update this method in your UsersListController class
    private void initializeTableColumns() {
        firstNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFirstName()));
        lastNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLastName()));
        dobColumn.setCellValueFactory(new PropertyValueFactory<>("dateOfBirth"));

        typeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue() instanceof Employee ? "Employee" : "User"));

        // Fix the handicap column by using a custom cell value factory instead of PropertyValueFactory
        handicapColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleBooleanProperty(cellData.getValue().hasHandicap()));

        handicapColumn.setCellFactory(column -> new TableCell<Person, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "Yes" : "No");
                }
            }
        });

        // Set up the actions column with buttons
        actionsColumn.setCellFactory(createActionsColumnCellFactory());
    }

    private Callback<TableColumn<Person, Void>, TableCell<Person, Void>> createActionsColumnCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<Person, Void> call(TableColumn<Person, Void> param) {
                return new TableCell<>() {
                    private final Button viewButton = new Button("View");
                    private final Button editButton = new Button("Edit");
                    private final Button deleteButton = new Button("Delete");
                    private final HBox pane = new HBox(5, viewButton, editButton, deleteButton);

                    {
                        viewButton.getStyleClass().addAll("btn", "btn-secondary");
                        viewButton.setOnAction(event -> {
                            Person person = getTableView().getItems().get(getIndex());
                            // Handle view action
                            showAlert("View User", "Viewing user: " + person.getFirstName() + " " + person.getLastName());
                        });

                        editButton.getStyleClass().addAll("btn", "btn-secondary");
                        editButton.setOnAction(event -> {
                            Person person = getTableView().getItems().get(getIndex());
                            // Handle edit action
                            showAlert("Edit User", "Editing user: " + person.getFirstName() + " " + person.getLastName());
                        });

                        deleteButton.getStyleClass().addAll("btn", "btn-danger");
                        deleteButton.setOnAction(event -> {
                            Person person = getTableView().getItems().get(getIndex());
                            // Handle delete action
                            if (confirmDelete("Delete User", "Are you sure you want to delete " + person.getFirstName() + " " + person.getLastName() + "?")) {
                                appState.getPeople().remove(person);
                                appState.saveUsers();
                                loadUsers(); // Reload the table
                                appState.getPasses().removeIf(pass -> pass.getOwnerFullName().equals(person.getFirstName()+ " " + person.getLastName()));
                                appState.savePasses();
                                appState.getComplaintService().listAll().removeIf(complaint -> complaint.getReporterFullName().equals(person.getFirstName() + " " + person.getLastName()));
                                appState.saveComplaints();
                            }
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

    private void loadUsers() {
        allUsers = FXCollections.observableArrayList(appState.getPeople());
        FXCollections.reverse(allUsers);
        filteredUsers = new FilteredList<>(allUsers);
        usersTable.setItems(filteredUsers);
    }

    private void setupSearchAndFilter() {
        // Set up search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });

        // Set up filter by user type
        userTypeCombo.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase();
        String userType = userTypeCombo.getValue();

        filteredUsers.setPredicate(person -> {
            boolean matchesSearch = searchText == null || searchText.isEmpty() ||
                    person.getFirstName().toLowerCase().contains(searchText) ||
                    person.getLastName().toLowerCase().contains(searchText);

            boolean matchesType = userType == null || userType.equals("All Types") ||
                    (userType.equals("Users") && !(person instanceof Employee)) ||
                    (userType.equals("Employees") && person instanceof Employee);

            return matchesSearch && matchesType;
        });
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
        // Already on users list, no need to navigate
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

    private boolean confirmDelete(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
}