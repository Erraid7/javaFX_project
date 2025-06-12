package transport.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import transport.core.AppState;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // Initialize the application state
            AppState.getInstance();

            // Load the login screen
            Parent root =  FXMLLoader.load(getClass().getResource("/transport/ui/dashboard.fxml"));
//            Parent root = loader.load();

            // Set up the scene
            Scene scene = new Scene(root);

            // In your App.java or controller classes
            scene.getStylesheets().add(getClass().getResource("/transport/ui/styles/main.css").toExternalForm());

            // Configure the stage
            primaryStage.setTitle("ESI-RUN Station Console");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(false);
            primaryStage.setWidth(1440);
            primaryStage.setHeight(800);
            primaryStage.show();
        } catch (Exception e) {
            System.err.println("Error loading application: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}