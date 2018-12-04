package org.dnsge.fbla.ebkmg;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.dnsge.fbla.ebkmg.db.SQLiteConnector;
import org.dnsge.fbla.ebkmg.popup.AlertCreator;
import org.dnsge.fbla.ebkmg.util.ErrorLog;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * Main class and program entry point
 *
 * @author Daniel Sage
 * @since 0.0
 * @version 0.4
 */
public class Main extends Application {

    public final static String VERSION = "1.0.0-SNAPSHOT";

    final static File HOME_DIRECTORY = new File(System.getProperty("user.home"));
    final static File EBOOK_DIRECTORY = new File(HOME_DIRECTORY, "EbookManagerData");
    final static File REPORTS_DIRECTORY = new File(EBOOK_DIRECTORY, "reports");
    public final static File LOGS_DIRECTORY = new File(EBOOK_DIRECTORY, "logs");

    public final static SimpleDateFormat ERROR_LOG_DATE_FORMAT = new SimpleDateFormat("YYYY-MM-dd kk.mm.ss");
    final static SimpleDateFormat CSV_FILE_DATE_FORMAT = new SimpleDateFormat("YYYY-MM-dd kk.mm.ss");

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler(Main::showError);

        // Setup working directories
        if (!EBOOK_DIRECTORY.exists() && !EBOOK_DIRECTORY.mkdirs()) {
            System.out.println("Unable to create " + EBOOK_DIRECTORY.getAbsolutePath());
        }
        if (!REPORTS_DIRECTORY.exists() && !REPORTS_DIRECTORY.mkdirs()) {
            System.out.println("Unable to create " + REPORTS_DIRECTORY.getAbsolutePath());
        }
        if (!LOGS_DIRECTORY.exists() && !LOGS_DIRECTORY.mkdirs()) {
            System.out.println("Unable to create " + LOGS_DIRECTORY.getAbsolutePath());
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/mainpage.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Main Page");
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(700);
        primaryStage.setMinHeight(400);
        MainPageController controller = loader.getController();
        controller.setWindow(primaryStage);

        // Intercept all close requests
        Platform.setImplicitExit(false);
        primaryStage.setOnCloseRequest(event -> {
            if (controller.unsavedChanges() && !AlertCreator.askYesOrNo("You have unsaved changes, are you sure you want to exit?")) {
                event.consume();
                return;
            }

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

        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/bookicon.png")));
        primaryStage.show();

    }

    /**
     * Handles exceptions that aren't handled in this jfx program
     * <p>
     * Creates a log file and shows a popup before exiting the applciation
     *
     * @param t Thread the exception occured in
     * @param exception {@code Throwalbe} that was thrown
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void showError(Thread t, Throwable exception) {
        if (Platform.isFxApplicationThread()) {
            File logFile = ErrorLog.createErrorLog(exception);
            ErrorLog.showLogCreationPopup(logFile);
            Platform.exit();
        } else {
            System.err.println("An unexpected error occurred in " + t);
        }
    }

}
