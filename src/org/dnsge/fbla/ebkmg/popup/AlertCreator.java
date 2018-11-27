package org.dnsge.fbla.ebkmg.popup;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Class with {@code static} methods to notify the user with alerts
 *
 * @author Daniel Sage
 * @version 0.2
 */
public final class AlertCreator {

    /**
     * Notifies the user of an error
     *
     * @param message Body of the error
     */
    public static void errorUser(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setTitle("Error");
        alert.showAndWait();
    }

    /**
     * Notifies the user that an unknown error occurred with message:
     * {@code An unknown issue occurred while processing your request.}
     */
    public static void unknownError() {
        errorUser("An unknown issue occurred while processing your request.");
    }

    /**
     * Confirms that the user wants to do something
     *
     * @param prompt String prompt to show the user
     * @return If the user clicked Yes
     */
    public static boolean askYesOrNo(String prompt) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, prompt, ButtonType.NO, ButtonType.YES);
        alert.setTitle("Confirm");
        alert.showAndWait();
        return alert.getResult() == ButtonType.YES;
    }

    public static void infoUser(String prompt) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, prompt);
        alert.setTitle("Information");
        alert.showAndWait();
    }

}
