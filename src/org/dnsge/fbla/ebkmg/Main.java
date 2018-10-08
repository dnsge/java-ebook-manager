package org.dnsge.fbla.ebkmg;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


import java.io.IOException;
import java.sql.SQLException;

public class Main extends Application {

    // todo: this location thing
    private final static String location = String.format("%s\\ebook-manager\\", System.getProperty("user.home"));

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("mainpage.fxml"));
        primaryStage.setTitle("Main Page");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        // Intercept all close requests
        primaryStage.setOnCloseRequest(event -> {
            // If we're still connected to the database, close it before exiting
            if (SQLiteConnector.getInstance().isConnected()) {
                try {
                    SQLiteConnector.getInstance().getConnectionSource().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Platform.exit();
        });
    }

    public static void main(String[] args) {
         launch(args);
    }

}
