package org.dnsge.fbla.ebkmg.util;

import javafx.scene.Node;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;

/**
 * Util class with static methods for simpler code
 *
 * @author Daniel Sage
 * @version 0.2
 */
public final class Utils {

    /**
     * Gets the Window/Stage of a Node
     *
     * @param node Node to get Window source from
     * @return Window of the node
     */
    public static Window getWindowFromNode(Node node) {
        return node.getScene().getWindow();
    }

    /**
     * Prompts the user to pick a file
     *
     * @param dialogTitle Title of the file picker dialog
     * @param initialDirPath Directory path as a {@code File} that the dialog opens to
     * @param ownerWindow  The getOwner window
     * @return File that is selected
     */
    public static File openFilePickerDialog(String dialogTitle, File initialDirPath, Window ownerWindow) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(dialogTitle);
        fileChooser.setInitialDirectory(initialDirPath);

        return fileChooser.showOpenDialog(ownerWindow);
    }

    /**
     * Prompts the user to pick a file
     *
     * @param dialogTitle Title of the file picker dialog
     * @param initialDirPath Directory path as a {@code String} that the dialog opens to
     * @param ownerWindow The getOwner window
     * @return File that is selected
     */
    public static File openFilePickerDialog(String dialogTitle, String initialDirPath, Window ownerWindow) {
        return openFilePickerDialog(dialogTitle, new File(initialDirPath), ownerWindow);
    }

    /**
     * Prompts the user to save a file
     *
     * @param dialogTitle Title of the {@code FilePicker}
     * @param initialDirPath {@code File} object to initially open the picker to
     * @param ownerWindow {@code Window} object that this {@code FilePicker} belongs to
     * @param extensionFilter {@link javafx.stage.FileChooser.ExtensionFilter} object to filter by
     * @return Selected {@code File} to save to
     */
    public static File openSavePickerDialog(String dialogTitle, File initialDirPath, Window ownerWindow, FileChooser.ExtensionFilter extensionFilter) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(dialogTitle);
        fileChooser.setInitialDirectory(initialDirPath);
        if (extensionFilter != null) {
            fileChooser.getExtensionFilters().add(extensionFilter);
        }
        return fileChooser.showSaveDialog(ownerWindow);
    }

    /**
     * Prompts the user to save a file
     *
     * @param dialogTitle Title of the {@code FilePicker}
     * @param initialDirPath Path represented as a String to initially open the picker to
     * @param ownerWindow {@code Window} object that his {@code FilePicker} belongs to
     * @param extensionFilter {@link javafx.stage.FileChooser.ExtensionFilter} object to filter by
     * @return Selected {@code File} to save to
     */
    public static File openSavePickerDialog(String dialogTitle, String initialDirPath, Window ownerWindow, FileChooser.ExtensionFilter extensionFilter) {
        return openSavePickerDialog(dialogTitle, new File(initialDirPath), ownerWindow, extensionFilter);
    }

    /**
     * Prompts the user to select a directory
     *
     * @param dialogTitle Title of the {@code DirectoryPicker}
     * @param initialDirPath {@code File} object to initially open the picker to
     * @param ownerWindow Directory path as a {@code String} that the dialog opens to
     * @return Selected Directory as a {@code File}
     */
    public static File openDirectoryPicker(String dialogTitle, File initialDirPath, Window ownerWindow) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(dialogTitle);
        directoryChooser.setInitialDirectory(initialDirPath);
        return directoryChooser.showDialog(ownerWindow);
    }

    public static void openWebBrowser(URI uri) throws IOException {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(uri);
        }
    }

}
