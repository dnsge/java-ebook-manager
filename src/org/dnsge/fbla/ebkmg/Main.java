package org.dnsge.fbla.ebkmg;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.dnsge.fbla.ebkmg.db.SQLiteConnector;
import org.dnsge.fbla.ebkmg.popup.AlertCreator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Main class & program entry point
 *
 * @author Daniel Sage
 * @since 0.0
 * @version 0.4
 */
public class Main extends Application {

    public final static String VERSION = "0.4";

    final static File HOME_DIRECTORY = new File(System.getProperty("user.home"));
    final static File EBOOK_DIRECTORY = new File(HOME_DIRECTORY, "EbookManagerData");
    final static File REPORTS_DIRECTORY = new File(EBOOK_DIRECTORY, "reports");
    private final static File LOGS_DIRECTORY = new File(EBOOK_DIRECTORY, "logs");
    private final static SimpleDateFormat ERROR_LOG_DATE_FORMAT = new SimpleDateFormat("YYYY-MM-dd kk.mm.ss");

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler(Main::showError);

        if (!EBOOK_DIRECTORY.exists() && !EBOOK_DIRECTORY.mkdirs()) {
            System.out.println("Unable to create " + EBOOK_DIRECTORY.getAbsolutePath());
        }
        if (!REPORTS_DIRECTORY.exists() && !REPORTS_DIRECTORY.mkdirs()) {
            System.out.println("Unable to create " + REPORTS_DIRECTORY.getAbsolutePath());
        }
        if (!LOGS_DIRECTORY.exists() && !LOGS_DIRECTORY.mkdirs()) {
            System.out.println("Unable to create " + LOGS_DIRECTORY.getAbsolutePath());
        }

        Parent root = FXMLLoader.load(getClass().getResource("mainpage.fxml"));
        primaryStage.setTitle("Main Page");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        primaryStage.setResizable(false);

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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void showError(Thread t, Throwable exception) {
        if (Platform.isFxApplicationThread()) {
            Date errorDate = new Date();
            File logFile = new File(LOGS_DIRECTORY, String.format("error@%s.log", ERROR_LOG_DATE_FORMAT.format(errorDate)));
            if (logFile.exists()) {
                logFile.delete();
            }
            try {
                logFile.createNewFile();
                writeErrorLogFile(logFile, exception, errorDate);
            } catch (IOException e) {
                e.printStackTrace();
                AlertCreator.errorUser("An unknown error occurred. Funnily enough, another error occured while creating the error log."
                        + "\nInitial error: " + exception.getMessage()
                        + "\nLog-creation error: " + e.getMessage());
                return;
            }
            AlertCreator.errorUser("An unknown error occured. Detailed information can be found in " + logFile.getAbsolutePath());
            Platform.exit();

        } else {
            System.err.println("An unexpected error occurred in " + t);
        }
    }

    private static void writeErrorLogFile(File outputFile, Throwable exception, Date errorDate) throws FileNotFoundException {
        PrintStream ps = new PrintStream(outputFile);
        ps.println("┏━━━━━━━━━━━━━━━━━━━━━━━━━┓ ");
        ps.println("┃ EBook Manager Error Log ┃ ");
        ps.println("┗━━━━━━━━━━━━━━━━━━━━━━━━━┛ ");
        ps.println(String.format("E-Book manager version %s @ %s", VERSION ,ERROR_LOG_DATE_FORMAT.format(errorDate)));
        ps.println("Detailed exception stack trace below: \n");
        exception.printStackTrace(ps);
        ps.close();
    }

}
