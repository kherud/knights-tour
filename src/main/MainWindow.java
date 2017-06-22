package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class MainWindow extends Application {


    @Override
    public void start(Stage primaryStage) {
        try {
            AnchorPane page = FXMLLoader.load(MainWindow.class.getResource("mainframe.fxml"));
            Scene scene = new Scene(page);
            scene.getStylesheets().add(MainWindow.class.getResource("chessBoardStyle.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.setTitle("Knight's Tour (9348226)");
            primaryStage.setResizable(false);
            // primaryStage.setMaximized(true);
            primaryStage.show();
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
