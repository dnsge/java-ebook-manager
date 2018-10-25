package org.dnsge.fbla.ebkmg;

import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import org.dnsge.fbla.ebkmg.models.Student;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

// todo: git repo

/**
 * Controller for the main JavaFX view
 *
 * @author Daniel Sage
 * @version 0.2
 */
public class MainPageController {
    // Menu bar stuff
    // todo: add more buttons/functionality to menubar
    @FXML private MenuBar  menuBar;
    @FXML private MenuItem connectToDatabase;
    // Table related stuff
    @FXML private TableView<Student>           mainTable;
    @FXML private TableColumn<Student, String> lastNameColumn;
    @FXML private TableColumn<Student, String> firstNameColumn;
    // Right side of the screen input nodes
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField studentIdField;
    @FXML private TextField ebookNameField;
    @FXML private TextField ebookCodeField;
    @FXML private Button updateDataButton;
    @FXML private Button cancelUpdateButton;
    @FXML private Button deleteRecordButton;
    // Bottom toolbar stuff
    @FXML private ToolBar buttonsToolbar;
    @FXML private Button newRecordButton;
    @FXML private Button generateReportButton; // Todo: this

    private Student selected;
    private SQLiteConnector sqLiteConnector = SQLiteConnector.getInstance();

    // todo: make a way to 'delete' an ebook record
    // todo: maybe consolidate ebook table into one table only? it would be easier probably

    /**
     * Called by JavaFX once all FXML fields/nodes have been created/registered
     */
    @FXML
    public void initialize() {
        registerMenuBarInteractions();
        registerTableDataInteractions();
        registerToolBarInteractions();
    }

    /**
     * Registers event listeners for things related to the main table
     */
    private void registerTableDataInteractions() {
        // Set column cell value factories
        lastNameColumn.setCellValueFactory( param -> new SimpleStringProperty(param.getValue().getLastName()) );

        firstNameColumn.setCellValueFactory( param -> new SimpleStringProperty(param.getValue().getFirstName()) );

        // Create bindings for setting border color to gray on TextField content change
        for (TextField f : new TextField[]{firstNameField, lastNameField, ebookNameField, ebookCodeField}) {
            f.setOnKeyTyped(e -> f.setStyle("-fx-border-color: #5e5e5e;"));
        }

        mainTable.setOnMouseClicked((MouseEvent event) -> {
            ObservableList<Student> selectedStudentList = mainTable.getSelectionModel().getSelectedItems();
            if (selectedStudentList.size() > 0) {
                setDisableOnInteractions(false);
                selected = selectedStudentList.get(0);
                loadTextFieldsFromStudent(selected);
                resetFieldsStyle();
            }
        });

        updateDataButton.setOnAction(event -> {
            ObservableList<Student> selectedStudentList = mainTable.getSelectionModel().getSelectedItems();

            // Make sure we have actually selected a row
            if (selectedStudentList.size() > 0) {
                if (selected.equals(selectedStudentList.get(0))) { // Check if its the same object, should always be

                    // Create backup and save
                    Student.Memento preservedStudent = selected.saveToMemento();
                    saveTextFieldsToStudent(selected);

                    if (ebookNameField.getText().trim().isEmpty() ^ ebookCodeField.getText().trim().isEmpty()) {
                        // One field is filled in and the other is empty

                        if (ebookNameField.getText().trim().isEmpty())
                            ebookNameField.setStyle("-fx-border-color: red;");

                        if (ebookCodeField.getText().trim().isEmpty())
                            ebookCodeField.setStyle("-fx-border-color: red;");

                        selected.loadFromMemento(preservedStudent);
                        return;
                    }

                    try {
                        ConnectionSource connectionSource = sqLiteConnector.getConnectionSource();

                        // Use transactionManager to cancel changes if something goes wrong
                        TransactionManager.callInTransaction(connectionSource, (Callable<Void>) () -> {
                            sqLiteConnector.getStudentDao().update(selected);

                            return null;
                        });

                        resetFieldsStyle();

                    } catch (SQLException e) {
                        // If unique failed, then ebook code is already used
                        if (e.getCause().getMessage().contains("constraint failed")) {
                            // reload from backup
                            selected.loadFromMemento(preservedStudent);
                            loadTextFieldsFromStudent(selected);

                            resetFieldsStyle();
                            ebookCodeField.setStyle("-fx-border-color: red;");
                            // TODO: add error message popup/label
                        } else {
                            e.printStackTrace();
                        }
                    }
                }
            }
            mainTable.refresh();
        });

        cancelUpdateButton.setOnAction(event -> {
            finishChanges();
            // todo: prompt if there are unsaved changes
        });

        deleteRecordButton.setOnAction(event -> {
            // todo: confirm that user wants to delete it
            try {
                sqLiteConnector.getStudentDao().delete(selected);
                List<Student> allStudents = sqLiteConnector.getStudentDao().queryForAll();
                mainTable.setItems(FXCollections.observableArrayList(allStudents));
                mainTable.refresh();
                finishChanges();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Registers event listeners for things related to the menu bar
     */
    private void registerMenuBarInteractions() {
        // Bind 'Connect to Database' menu button
        connectToDatabase.setOnAction((ActionEvent event) -> {
            File databaseFile = Utils.openFilePickerDialog("Select Database", "L:/ebook_data/", menuBar.getScene().getWindow());

            try {
                // Connect database
                sqLiteConnector.connect(databaseFile.getAbsolutePath());
                ConnectionSource connectionSource = sqLiteConnector.getConnectionSource();

                // Create database tables if they don't exist
                TableUtils.createTableIfNotExists(connectionSource, Student.class);

                // Get all students
                List<Student> allStudents = sqLiteConnector.getStudentDao().queryForAll();

                // Generate list and set the items
                mainTable.setItems(FXCollections.observableArrayList(allStudents));

            } catch (SQLException | IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Registers event listeners for things related to the toolbar
     */
    private void registerToolBarInteractions() {
        // Set listener for when database connection state is changed
        // If connected, enable bottom toolbar, else disable it
        sqLiteConnector.getConnectedProperty().addListener((observable, oldValue, newValue) -> buttonsToolbar.setDisable(!newValue));

        newRecordButton.setOnAction(event -> {
            // todo: set up popup window to make new record

        });
    }

    /**
     * Sets whether the input nodes on the right half of the screen should be disabled
     *
     * @param disabled Whether the input methods should be disabled
     */
    private void setDisableOnInteractions(boolean disabled) {
        firstNameField.setDisable(disabled);
        lastNameField.setDisable(disabled);
        studentIdField.setDisable(disabled);
        ebookNameField.setDisable(disabled);
        ebookCodeField.setDisable(disabled);
        updateDataButton.setDisable(disabled);
        cancelUpdateButton.setDisable(disabled);
        deleteRecordButton.setDisable(disabled);
    }

    /**
     * Clears the text fields and resets their style
     */
    private void clearTextFields() {
        firstNameField.setText("");
        lastNameField.setText("");
        studentIdField.setText("");
        ebookNameField.setText("");
        ebookCodeField.setText("");

        resetFieldsStyle();
    }

    /**
     * Resets the style of the text fields
     */
    private void resetFieldsStyle() {
        firstNameField.setStyle("");
        lastNameField.setStyle("");
        studentIdField.setStyle("");
        ebookNameField.setStyle("");
        ebookCodeField.setStyle("");
    }

    /**
     * Deselects the row in the table and clears/disables the text fields
     */
    private void finishChanges() {
        clearTextFields();
        setDisableOnInteractions(true);
        mainTable.getSelectionModel().clearSelection();
    }

    /**
     * Fills in the text fields based off of a Student's information
     *
     * @param stu Student to load from
     */
    private void loadTextFieldsFromStudent(Student stu) {
        firstNameField.setText(stu.getFirstName());
        lastNameField.setText(stu.getLastName());
        studentIdField.setText(stu.getStudentId());
        if (stu.hasEbook()) {
            ebookNameField.setText(stu.getEbookName());
            ebookCodeField.setText(stu.getEbookCode());
        } else {
            ebookNameField.setText("");
            ebookCodeField.setText("");
        }
    }

    /**
     * Saves the information in the text fields to a Student
     *
     * @param stu Student to save to
     */
    private void saveTextFieldsToStudent(Student stu) {
        stu.setFirstName(firstNameField.getText());
        stu.setLastName(lastNameField.getText());
        stu.setStudentId(studentIdField.getText());
        stu.setEbookName(ebookNameField.getText());
        stu.setEbookCode(ebookCodeField.getText());

        stu.setHasEbook(!stu.getEbookCode().isEmpty());
    }
}
