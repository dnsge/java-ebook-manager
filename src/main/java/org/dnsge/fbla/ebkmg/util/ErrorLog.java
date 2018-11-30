package org.dnsge.fbla.ebkmg.util;

import org.dnsge.fbla.ebkmg.Main;
import org.dnsge.fbla.ebkmg.popup.AlertCreator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;

/**
 * Class with static methods to make error logs and show error popups
 *
 * @author Daniel Sage
 * @version 0.1
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class ErrorLog {

    /**
     * Creates a new error log file from a throwable
     *
     * @param logFile {@code File} object to store the log in
     * @param timestamp {@code Date} object representing the log timestamp
     * @param exception {@code Throwable} to be used to make a log file
     */
    public static void createErrorLog(File logFile, Date timestamp, Throwable exception) {
        if (logFile.exists()) {
            logFile.delete();
        }

        try {
            logFile.createNewFile();
            writeErrorLogFile(logFile, exception, timestamp);
        } catch (IOException e) {
            e.printStackTrace();
            AlertCreator.errorUser("An unknown error occurred. Funnily enough, another error occured while creating the error log."
                    + "\nInitial error: " + exception.getMessage()
                    + "\nLog-creation error: " + e.getMessage());
        }

    }

    /**
     * Creates an error log with the current time and makes a new log file
     *
     * @param exception {@code Throwable} to be used to make a log file
     * @return {@code File} object that was created
     * @see #createErrorLog(File, Date, Throwable)
     */
    public static File createErrorLog(Throwable exception) {
        Date timestamp = new Date();
        File logFile = new File(Main.LOGS_DIRECTORY, String.format("error@%s.log", Main.ERROR_LOG_DATE_FORMAT.format(timestamp)));
        createErrorLog(logFile, timestamp, exception);
        return logFile;
    }

    /**
     * Shows a generic popup that an error occured and where one can find the error log
     *
     * @param logLocation {@code File} object representing the log file
     * @param header String to have in the popup error message
     */
    public static void showLogCreationPopup(File logLocation, String header) {
        AlertCreator.errorUser(String.format("%s Detailed information can be found in %s", header, logLocation.getAbsolutePath()));
    }

    /**
     * Shows a generic popup that an 'unknown error' occured and where one can find the error log
     *
     * @param logLocation {@code File} object representing the log file
     * @see #showLogCreationPopup(File, String)
     */
    public static void showLogCreationPopup(File logLocation) {
        showLogCreationPopup(logLocation, "An unknown error occured.");
    }

    /**
     * Creates a logfile and shows an appropriate popup at the same time
     *
     * @param exception {@code Throwable} to be used to make the log file
     */
    public static void newErrorLogWithPopup(Throwable exception) {
        File logFile = createErrorLog(exception);
        showLogCreationPopup(logFile);
    }

    /**
     * Writes the data of a {@code Throwable} into a {@code File} with a specified timestamp
     *
     * @param outputFile Output {@code File}
     * @param exception Input {@code Throwable}
     * @param errorDate Timestamp as a {@code Date} object
     * @throws FileNotFoundException if the output {@code File} couldn't be found or couldn't be written to
     */
    private static void writeErrorLogFile(File outputFile, Throwable exception, Date errorDate) throws FileNotFoundException {
        PrintStream ps = new PrintStream(outputFile);
        ps.println("┏━━━━━━━━━━━━━━━━━━━━━━━━━┓ ");
        ps.println("┃ EBook Manager Error Log ┃ ");
        ps.println("┗━━━━━━━━━━━━━━━━━━━━━━━━━┛ ");
        ps.println(String.format("E-Book manager version %s @ %s", Main.VERSION, Main.ERROR_LOG_DATE_FORMAT.format(errorDate)));
        ps.println("Detailed exception stack trace below: \n");
        exception.printStackTrace(ps);
        ps.close();
    }

}
