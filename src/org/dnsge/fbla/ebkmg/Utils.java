package org.dnsge.fbla.ebkmg;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;

/**
 * Util class with static methods for simpler code
 *
 * @author Daniel Sage
 * @version 0.1
 */
public class Utils {

    /**
     * Gets the Window/Stage of an ActionEvent
     *
     * @param event Action event to get Window source
     * @return Window of the event source
     */
    public static Window getWindowFromEvent(ActionEvent event) {
        return ((Node)event.getSource()).getScene().getWindow();
    }

    /**
     * Prompts the user to pick a file
     *
     * @param dialogTitle Title of the file picker dialog
     * @param initalDirPath Directory path as a {@code File} that the dialog opens to
     * @param ownerWindow  The owner window
     * @return File that is selected
     */
    public static File openFilePickerDialog(String dialogTitle, File initalDirPath, Window ownerWindow) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(dialogTitle);
        fileChooser.setInitialDirectory(initalDirPath);

        return fileChooser.showOpenDialog(ownerWindow);
    }

    /**
     * Prompts the user to pick a file
     *
     * @param dialogTitle Title of the file picker dialog
     * @param initialDirPath Directory path as a {@code String} that the dialog opens to
     * @param ownerWindow The owner window
     * @return File that is selected
     */
    public static File openFilePickerDialog(String dialogTitle, String initialDirPath, Window ownerWindow) {
        return openFilePickerDialog(dialogTitle, new File(initialDirPath), ownerWindow);
    }

}
