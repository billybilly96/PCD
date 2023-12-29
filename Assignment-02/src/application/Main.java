package application;

import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {    	
    	FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/main-app.fxml"));
        Parent root = (Parent)loader.load();
        Scene scene = new Scene(root);
        primaryStage.setOnCloseRequest(event -> { Platform.exit(); System.exit(0); });
        primaryStage.setTitle("WORDS FINDER");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();            
    }

}